import java.awt.*;
import java.util.ArrayList;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.Random;

import main.Cell;
import main.PheromoneType;

// ============================================================================
// Programmer   : Joseph Vybihal
// Creation Date: January 2024
// Purpose      : Manages the objects and the terrain model
//
// Input  : Configuration object
// Uses   : ConfigFileReader
// Outputs: API for Sensors & Actuators
//
// Other Developer Modifications (name - change)
// ---------------------------------------------
// TBD

public class WorldModel {
    // World objects
    ArrayList<WorldObject> objects;

    // World pixels
    Cell[][] model;
    Color background;
    int modelWidth, modelHeight;

    // View window parameters
    static int titleBarWidth = 30;

    // Noise parameters
    private double infraredNoiseStdDev = 0.05; // 5% of the distance as standard deviation
    private double ultrasoundNoiseStdDev = 0.10; // 10% of the distance
    private double touchNoiseStdDev = 0.02; // 2% of the distance

    // Exceptions
    public static class InsufficientBatteryException extends Exception {
        public InsufficientBatteryException(String message) {
            super(message);
        }
    }

    //
    // Initialize the models ***********************************************
    //
    public WorldModel(ConfigFileReader config, Color aColor) {
        // Model of the objects in the world
        objects = new ArrayList<>();
        objects = config.objects;
        this.modelWidth = config.maxWidth / 2;
        this.modelHeight = config.maxHeight;
        // Model of the pixels in the canvas
        model = new Cell[modelWidth][modelHeight];

        for (int i = 0; i < modelWidth; i++) {
            for (int j = 0; j < modelHeight; j++) {
                model[i][j] = new Cell();
                model[i][j].setDefaultColor(aColor);
            }
        }

        setModelBackground(aColor);

        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).name.equals("obstacle")) {
                if (objects.get(i).shape.equals("rect=")) {
                    for (int j = 0; j < objects.get(i).xEnd; j++) {
                        for (int k = 0; k < objects.get(i).yEnd; k++) {
                            model[objects.get(i).xCoord + j][objects.get(i).yCoord + k].setColor(Color.black);
                        }
                    }
                } else if (objects.get(i).shape.equals("triangle=")) {
                    // Define the coordinates of the three vertices of the triangle
                    int x1 = objects.get(i).xCoord;
                    int y1 = objects.get(i).yCoord;
                    int x2 = objects.get(i).xCoord + objects.get(i).xEnd;
                    int y2 = objects.get(i).yCoord;
                    int x3 = objects.get(i).xCoord;
                    int y3 = objects.get(i).yCoord + objects.get(i).yEnd;

                    // Calculate the bounding box of the triangle
                    int minX = Math.min(x1, Math.min(x2, x3));
                    int maxX = Math.max(x1, Math.max(x2, x3));
                    int minY = Math.min(y1, Math.min(y2, y3));
                    int maxY = Math.max(y1, Math.max(y2, y3));

                    // Iterate through each point in the bounding box
                    for (int x = minX; x <= maxX; x++) {
                        for (int y = minY; y <= maxY; y++) {
                            // Check if the point (x, y) is inside the triangle
                            if (pointInTriangle(x, y, x1, y1, x2, y2, x3, y3)) {
                                // Set the color of the model pixel to black if it is inside the triangle
                                model[x][y].setColor(Color.black);
                            }
                        }
                    }
                }
            }
            if (objects.get(i).shape.equals("food=")) {
                for (int j = 0; j < objects.get(i).xEnd; j++) {
                    for (int k = 0; k < objects.get(i).yEnd; k++) {
                        model[objects.get(i).xCoord + j][objects.get(i).yCoord + k].setFood();
                    }
                }
            } else if (objects.get(i).shape.equals("home=")) {
                for (int j = 0; j < objects.get(i).xEnd; j++) {
                    for (int k = 0; k < objects.get(i).yEnd; k++) {
                        model[objects.get(i).xCoord + j][objects.get(i).yCoord + k].setHome();
                    }
                }
            }
        }
    }

    // Function to determine if a point is inside a triangle
    private boolean pointInTriangle(int px, int py, int x1, int y1, int x2, int y2, int x3, int y3) {
        int d1, d2, d3;
        boolean has_neg, has_pos;

        // Calculate the 'sign' of the point with respect to the triangle's three edges
        d1 = sign(px, py, x1, y1, x2, y2);
        d2 = sign(px, py, x2, y2, x3, y3);
        d3 = sign(px, py, x3, y3, x1, y1);

        // Check if the point is on all the same side of all edges
        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        // The point is in the triangle if it is on the same side of all edges
        return !(has_neg && has_pos);
    }

    // Helper function to calculate the sign of a point with respect to an edge of
    // the triangle
    private int sign(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) {
        // Cross product to determine if the point is above or below the edge
        return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y);
    }

    //
    // Pixel model update methods *****************************************
    //
    public void displayModel() {
        int i, j;
        char out = 0;
        for (j = 0; j < this.modelHeight; j++) {
            for (i = 0; i < this.modelWidth; i++) {
                if (this.model[i][j].getColor().equals(Color.gray))
                    out = '.';
                else if (this.model[i][j].getColor().equals(Color.red))
                    out = 'R';
                else if (this.model[i][j].getColor().equals(Color.green))
                    out = 'G';
                else if (this.model[i][j].getColor().equals(Color.blue))
                    out = 'B';
                else if (this.model[i][j].getColor().equals(Color.yellow))
                    out = 'Y';
                else if (this.model[i][j].getColor().equals(Color.CYAN))
                    out = 'H';
                else if (this.model[i][j].getColor().equals(Color.WHITE))
                    out = ' ';
                else if (this.model[i][j].getColor().equals(Color.BLACK))
                    out = 'X';
                else if (this.model[i][j].getColor().equals(Color.orange))
                    out = 'O';
                else if (this.model[i][j].getColor().equals(Color.pink))
                    out = 'P';
                else if (this.model[i][j].getColor().equals(Color.magenta))
                    out = 'M';
                else if (this.model[i][j].getColor().equals(Color.lightGray))
                    out = 'L';
                else if (this.model[i][j].getColor().equals(Color.darkGray))
                    out = 'D';
                else
                    out = '?';
                System.out.print(out);
            }
            System.out.println();
        }
        System.out.println("\n");
    }

    public void setModelBackground(Color aColor) {
        int i, j;

        this.background = aColor;

        for (i = 0; i < modelWidth; i++) {
            for (j = 0; j < modelHeight; j++) {
                model[i][j].setColor(this.background);
            }
        }
    }

    private boolean isColorEqual(Color aColor, Color bColor) {
        if (aColor.getRed() == bColor.getRed() &&
                aColor.getBlue() == bColor.getBlue() &&
                aColor.getGreen() == bColor.getGreen())
            return true;

        return false;
    }

    public void fillOval(Color aColor, int x, int y, int width, int height) {
        double PI = 3.1415926535;
        double angle, r = (double) width; // this is a circle implementation!!;
        int x1 = 0, y1 = 0;

        // circle
        for (angle = 0; angle < 360; angle += 0.1) {
            x1 = (int) (r * cos(angle * PI / 180));
            y1 = (int) (r * sin(angle * PI / 180));
            model[x + x1][y + y1].setColor(aColor);
        }

        // fill
        for (y1 = y; y1 < y + height; y1++) {
            for (x1 = x; !isColorEqual(model[x1][y1].getColor(), aColor); x1++)
                model[x1][y1].setColor(aColor);
            for (x1 = x - 1; !isColorEqual(model[x1][y1].getColor(), aColor); x1--)
                model[x1][y1].setColor(aColor);
        }
        for (y1 = y - 1; y1 > y - height; y1--) {
            for (x1 = x; !isColorEqual(model[x1][y1].getColor(), aColor); x1++)
                model[x1][y1].setColor(aColor);
            for (x1 = x - 1; !isColorEqual(model[x1][y1].getColor(), aColor); x1--)
                model[x1][y1].setColor(aColor);
        }
    }

    public void fillRect(Color aColor, int xCoord, int yCoord, int width, int height) {
        int i, j;
        for (j = yCoord; j < yCoord + height; j++) {
            for (i = xCoord; i < xCoord + width; i++) {
                this.model[i][j].setColor(aColor);
            }
        }
    }

    public void fillPolygon(Color aColor, int[] xPoints, int[] yPoints, int nPoints) {
        int i, j;
        for (i = 0; i < nPoints; i++) {
            for (j = 0; j < nPoints; j++) {
                this.model[xPoints[i]][yPoints[j]].setColor(aColor);
            }
        }
    }

    //
    // Actuator methods ****************************************************
    //

    public void forward(WorldObject o, int distance) throws Exception {
        if (distance < 0)
            distance = 0;
        // Calculate the energy needed for the move
        int energyCost = distance * o.ant.EnergyDrainRate;

        // Check if the robot has enough battery to complete the action
        if (o.ant.getCurrentEnergy() < energyCost) {
            System.out.println("Not enough battery to complete the move.");
            throw new InsufficientBatteryException("FORWARD: current battery: " + o.ant.currentEnergy
                    + " is less than requirement " + energyCost);
        }
        int i;
        int xDiff = 0, yDiff = 0;
        int xNext, yNext;
        // int xBoundBox, yBoundBox;

        if (o.direction.equals("north")) {
            xDiff = 0;
            yDiff = -1;
            // xBoundBox = 0;
            // yBoundBox = -1;
        } else if (o.direction.equals("south")) {
            xDiff = 0;
            yDiff = 1;
            // xBoundBox = 0;
            // yBoundBox = o.yEnd + 1;
        } else if (o.direction.equals("east")) {
            xDiff = 1;
            yDiff = 0;
            // xBoundBox = o.xEnd + 1;
            // yBoundBox = 0;
        } else if (o.direction.equals("west")) {
            xDiff = -1;
            yDiff = 0;
            // xBoundBox = -1;
            // yBoundBox = 0;
        } else
            throw new Exception("FORWARD: direction wrong - " + o.direction);

        for (i = 0; i < distance; i++) {
            xNext = o.xCoord + xDiff;
            yNext = o.yCoord + yDiff;

            if (xNext < 0 || xNext > this.modelWidth)
                break;
            if (yNext < WorldModel.titleBarWidth || yNext > this.modelHeight)
                break;

            // if (isColorEqual(this.model[xNext + xBoundBox][yNext + yBoundBox].getColor(),
            // this.background)
            // || isColorEqual(this.model[xNext + xBoundBox][yNext + yBoundBox].getColor(),
            // Color.green)
            // || isColorEqual(this.model[xNext + xBoundBox][yNext + yBoundBox].getColor(),
            // Color.red)) {
            if (isColorEqual(this.model[xNext][yNext].getColor(), this.background)
                    || isColorEqual(this.model[xNext][yNext].getColor(), Color.green)
                    || isColorEqual(this.model[xNext][yNext].getColor(), Color.red)) {
                o.xCoord = xNext;
                o.yCoord = yNext;
            }
            if (!isColorEqual(this.model[xNext][yNext].getColor(), Color.BLACK)) {
                o.xCoord = xNext;
                o.yCoord = yNext;
            } else{
                break;
            }
        }
        o.ant.drainBattery(energyCost);
    }

    public void reverse(WorldObject o, int distance) throws Exception {
        if (distance < 0)
            distance = 0;
        // Calculate the energy needed for the move
        int energyCost = distance * o.ant.EnergyDrainRate;

        // Check if the robot has enough battery to complete the action
        if (o.ant.currentEnergy < energyCost) {
            System.out.println("Not enough battery to complete the move.");
            throw new InsufficientBatteryException("REVERSE: current battery: " + o.ant.currentEnergy
                    + " is less than requirement " + energyCost);
        }
        int i;
        int xDiff = 0, yDiff = 0;
        int xNext, yNext;
        // int xBoundBox, yBoundBox;

        if (o.direction.equals("north")) {
            xDiff = 0;
            yDiff = 1;
            // xBoundBox = 0;
            // yBoundBox = o.yEnd + 1;
        } else if (o.direction.equals("south")) {
            xDiff = 0;
            yDiff = -1;
            // xBoundBox = 0;
            // yBoundBox = -1;
        } else if (o.direction.equals("east")) {
            xDiff = -1;
            yDiff = 0;
            // xBoundBox = -1;
            // yBoundBox = 0;
        } else if (o.direction.equals("west")) {
            xDiff = 1;
            yDiff = 0;
            // xBoundBox = o.xEnd + 1;
            // yBoundBox = 0;
        } else
            throw new Exception("REVERSE: direction wrong - " + o.direction);

        for (i = 0; i < distance; i++) {
            xNext = o.xCoord + xDiff;
            yNext = o.yCoord + yDiff;

            if (xNext < 0 || xNext > this.modelWidth)
                break;
            if (yNext < WorldModel.titleBarWidth || yNext > this.modelHeight)
                break;

            if (isColorEqual(this.model[xNext][yNext].getColor(), this.background)
                    || isColorEqual(this.model[xNext][yNext].getColor(), Color.green)
                    || isColorEqual(this.model[xNext][yNext].getColor(), Color.red)) {
                o.xCoord = xNext;
                o.yCoord = yNext;
            }
        }
        o.ant.drainBattery(energyCost);
    }

    // Turn()
    // Assumes degrees is measured clockwise from the direction of motion.
    // In other words, degree 0 is not turning, degree 90 is turning right, degree
    // 270
    // is turning left, degree 180 is facing backwards.
    //
    // For this version of the program, turns of 90 degrees are only supported. In
    // other words: 0, 90, 180, 270, and 360 (which is the same as 0).
    public void turn(WorldObject o, int degrees) throws Exception {
        int absoluteDegree = 0; // measured from true North
        int normalizedDegrees = ((degrees % 360) + 360) % 360;

        if (normalizedDegrees % 90 != 0) {
            throw new Exception("TURN: degrees not a multiple of 90 - " + degrees);
        }
        // Calculate the energy needed for the move
        int costAngle = normalizedDegrees;
        if (costAngle > 180) {
            costAngle = 360 - costAngle;
        }
        int energyCost = (costAngle / 90) * o.ant.EnergyDrainRate;

        // Check if the robot has enough battery to complete the action
        if (o.ant.currentEnergy < energyCost) {
            System.out.println("Not enough battery to complete the move.");
            throw new InsufficientBatteryException("TURN: current battery: " + o.ant.currentEnergy
                    + " is less than requirement " + energyCost);
        }
        // Compute the new position from absolute north

        if (o.direction.equals("north")) {
            absoluteDegree = degrees % 360;
        } else if (o.direction.equals("south")) {
            absoluteDegree = (180 + degrees) % 360;
        } else if (o.direction.equals("east")) {
            absoluteDegree = (90 + degrees) % 360;
        } else if (o.direction.equals("west")) {
            absoluteDegree = (270 + degrees) % 360;
        } else
            throw new Exception("TURN: direction owrng - " + o.direction);

        // Adjust the robot's relative position from absolute position

        switch (absoluteDegree) {
            case 0:
                o.direction = "north";
                break;
            case 90:
                o.direction = "east";
                break;
            case 180:
                o.direction = "south";
                break;
            case 270:
                o.direction = "west";
                break;
            default:
                throw new Exception("TURN: absoluteDegrees not a multiple of 90 - " + absoluteDegree);
        }
        o.ant.drainBattery(energyCost);
    }

    //
    // Sensor Methods *****************************************************
    //

    // Ant sensors

    public Cell[] touchAndSmell(WorldObject o) throws Exception {
        int energyCost = o.ant.EnergyDrainRate * 1; // Assume touch consumes less energy
        Cell[] cells = new Cell[3];
        if (o.ant.currentEnergy < energyCost) {
            System.out.println("Not enough battery to perform touch scanning.");
            throw new InsufficientBatteryException("touchAnt: current battery: " + o.ant.currentEnergy
                    + " is less than requirement " + energyCost);
        }
        if (o.direction.equals("north")) {
            cells[0] = model[o.xCoord - 1][o.yCoord - 1];
            cells[1] = model[o.xCoord][o.yCoord - 1];
            cells[2] = model[o.xCoord + 1][o.yCoord - 1];
        } else if (o.direction.equals("south")) {
            cells[1] = model[o.xCoord][o.yCoord + 1];
            cells[0] = model[o.xCoord - 1][o.yCoord + 1];
            cells[2] = model[o.xCoord + 1][o.yCoord + 1];
        } else if (o.direction.equals("east")) {
            cells[1] = model[o.xCoord + 1][o.yCoord];
            cells[0] = model[o.xCoord + 1][o.yCoord - 1];
            cells[2] = model[o.xCoord + 1][o.yCoord + 1];
        } else if (o.direction.equals("west")) {
            cells[1] = model[o.xCoord - 1][o.yCoord];
            cells[0] = model[o.xCoord - 1][o.yCoord + 1];
            cells[2] = model[o.xCoord - 1][o.yCoord - 1];
        } else {
            throw new Exception("touchAnt: direction wrong - " + o.direction);
        }
        o.ant.drainBattery(energyCost);
        return cells;
    }

    public void antEat(WorldObject o) {
        if (model[o.xCoord][o.yCoord].hasFood()) {
            model[o.xCoord][o.yCoord].reduceFood();
            o.ant.currentEnergy = o.ant.energyCapacity;
        }
    }

    public void antPickupFood(WorldObject o) throws Exception {
        int energyCost = o.ant.EnergyDrainRate * 2;
        if (o.ant.currentEnergy < energyCost) {
            System.out.println("Not enough battery to perform touch scanning.");
            throw new InsufficientBatteryException("touchAnt: current battery: " + o.ant.currentEnergy
                    + " is less than requirement " + energyCost);
        }
        if (model[o.xCoord][o.yCoord].hasFood()) {
            model[o.xCoord][o.yCoord].reduceFood();
            o.ant.carryFood = true;
        }
        o.ant.drainBattery(energyCost);
    }

    public void antDropFood(WorldObject o) {
        if (o.ant.carryFood) {
            if (!model[o.xCoord][o.yCoord].hasFood()) {
                model[o.xCoord][o.yCoord].setFood();
                o.ant.carryFood = false;
            } else {
                System.out.println("Cannot drop food here. Cell already has food.");
            }
        }
    }

    public void leavePheromone(WorldObject o, main.PheromoneType type, int amount) {
        // Directly add pheromone at the current position
        model[o.xCoord][o.yCoord].addPheromone(type, amount);

        // Spread the pheromone only for HELP_ME or DANGER types
        if (type == main.PheromoneType.HELP_ME || type == main.PheromoneType.DANGER ||
                type == main.PheromoneType.FOOD || type == main.PheromoneType.HOME ||
                type == PheromoneType.TO_FOOD || type == PheromoneType.TO_HOME) {
            // Define the maximum radius of diffusion
            int radius = 3;

            // Iterate over a grid centered at (x, y) with the radius of 3
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    // Calculate the target cell coordinates
                    int nx = o.xCoord + dx;
                    int ny = o.yCoord + dy;

                    // Ensure the target coordinates are within the grid boundaries
                    if (nx >= 0 && nx < this.modelWidth && ny >= 0 && ny < this.modelHeight) {
                        // Calculate the distance from the center to the current point to determine
                        // pheromone decay
                        int distance = Math.max(Math.abs(dx), Math.abs(dy));
                        int decayedAmount = Math.max(0, amount - distance);

                        // Diffuse the pheromone if the decayed amount is positive
                        if (decayedAmount > 0 && !model[nx][ny].getColor().equals(Color.BLACK)) {

                            model[nx][ny].addPheromone(type, decayedAmount);
//                            System.out.println("Spreading pheromone at (" + nx + ", " + ny + "):" + decayedAmount
//                                    + "now:" + model[nx][ny].getPheromone()[type.ordinal()]);
                        }
                    }
                }
            }
        }
    }

    // TODO: Future - Add depth-array
    // Note: infrared and ultrasound are similar except for parameters & noise
    // coefficients

    public int infrared(WorldObject o) throws Exception {
        double energyCost = o.robot.batteryDrainRate * 3; // Assume infrared consumes more energy
        if (o.robot.currentBatteryLevel < energyCost) {
            System.out.println("Not enough battery to perform infrared scanning.");
            throw new InsufficientBatteryException("infrared: current battery: " + o.robot.currentBatteryLevel
                    + " is less than requirement " + energyCost);
        }
        int distance = o.robot.infraredSensor(this, o);
        Random rand = new Random();
        double noise = rand.nextGaussian() * infraredNoiseStdDev * distance;
        distance += noise;

        o.robot.drainBattery(energyCost);
        return (int) Math.round(distance);
    }

    public int ultrasound(WorldObject o) throws Exception {
        double energyCost = o.robot.batteryDrainRate * 2; // Assume infrared consumes more energy
        if (o.robot.currentBatteryLevel < energyCost) {
            System.out.println("Not enough battery to perform ultrasound scanning.");
            throw new InsufficientBatteryException("ultrasound: current battery: " + o.robot.currentBatteryLevel
                    + " is less than requirement " + energyCost);
        }
        int distance = o.robot.ultrasoundSensor(this, o);
        Random rand = new Random();
        double noise = rand.nextGaussian() * ultrasoundNoiseStdDev * distance;
        distance += noise;

        o.robot.drainBattery(energyCost);
        return (int) Math.round(distance);
    }

    public int touch(WorldObject o) throws Exception {
        double energyCost = o.robot.batteryDrainRate * 1; // Assume infrared consumes more energy
        if (o.robot.currentBatteryLevel < energyCost) {
            System.out.println("Not enough battery to perform touch  scanning.");
            throw new InsufficientBatteryException("touch: current battery: " + o.robot.currentBatteryLevel
                    + " is less than requirement " + energyCost);
        }

        int distance = o.robot.touchSensor(this, o);
        Random rand = new Random();
        double noise = rand.nextGaussian() * touchNoiseStdDev * distance;
        distance += noise;

        o.robot.drainBattery(energyCost);
        return (int) Math.round(distance);
    }

    public Color[][] picture(WorldObject o, int viewWidth, int viewDepth) throws Exception {
        // Dynamic energy cost based on the size of the area being "scanned"
        double baseCost = 5; // Base cost for an arbitrary unit area
        double energyCost = baseCost * (viewWidth * viewDepth) / (100); // Adjust the divisor based on expected scaling

        if (o.robot.currentBatteryLevel < energyCost) {
            System.out.println("Not enough battery to perform picture scanning.");
            throw new InsufficientBatteryException("picture: current battery: " + o.robot.currentBatteryLevel
                    + " is less than requirement " + energyCost);
        }
        Color[][] picture = o.robot.captureImage(this, o, viewWidth, viewDepth);
        o.robot.drainBattery(energyCost);
        return picture;
    }

    public int[] depthScan(WorldObject o, int viewWidth, int viewDepth) throws Exception {
        double energyCost = o.robot.batteryDrainRate * viewDepth; // Assume depth scan consumes energy proportional to
                                                                  // depth
        if (o.robot.currentBatteryLevel < energyCost) {
            System.out.println("Not enough battery to perform depth scanning.");
            throw new InsufficientBatteryException("depthScan: current battery: " + o.robot.currentBatteryLevel
                    + " is less than requirement " + energyCost);
        }
        int[] depths = o.robot.depthArray(this, o, viewWidth, viewDepth);
        o.robot.drainBattery(energyCost);
        return depths;
    }

    public int calculateObstacleDistance(int startX, int startY, int directionFactorX, int directionFactorY) {
        int distance = 0;
        while (true) {
            int checkX = startX + directionFactorX * distance;
            int checkY = startY + directionFactorY * distance;

            // Check if the coordinates are out of the model's bounds
            if (checkX < 0 || checkX >= modelWidth || checkY < 0 || checkY >= modelHeight) {
                return -1; // Return -1 if out of bounds, indicating no obstacle or beyond measurement
                           // range
            }

            // Check if the point is an obstacle
            if (!model[checkX][checkY].equals(background)) {
                return distance; // Return the distance from the starting point to the obstacle
            }

            distance++;
        }
    }

}
