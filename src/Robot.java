import BasicAI.AIProcedure;
import BasicAI.AISometimesRandom;

import java.awt.*;

// Class Robot is a member of Class WorldObject
// Therefore, Robot contains those special parameters specific to Robot
// Parameters that Robots have in common to "regular" objects are stored in WorldObject
public class Robot {
    // Parameters for ModelView
    String displayType;
    int startX, startY, endX, endY;
    private boolean painted;

    // Parameters for sensor emitter
    int sEndX, sEndY, offsetX = 0, offsetY = 0;
    // Parameters for battery
    double batteryCapacity;
    double currentBatteryLevel;
    double batteryDrainRate; // Battery consumption per action unit

    // Parameters for BasicAI.AI
    AIProcedure ai;
    private String knowledgeBaseFilename;
    public String aiType;

    // Methods

    Robot(String aiType, String knowledgeBaseFilename) throws Exception {
        this.displayType = "n/a";
        this.startX = 0;
        this.startY = 0;
        this.endX = 0;
        this.endY = 0;
        this.painted = false;
        this.aiType = aiType;

        this.batteryCapacity = 100.0; // Assuming battery capacity is a percentage
        this.currentBatteryLevel = 100.0; // Starts fully charged
        this.batteryDrainRate = 0.1; // Adjust as necessary per unit of action

        if (aiType.equals("sometimesRandom")) {
            this.ai = new AISometimesRandom(4, "#FORWARD", 10);
        } else
            throw new Exception("Robot BasicAI.AI not defined, says: " + aiType);
    }

    private void setSensorDisplay(String type, int x1, int y1, int x2, int y2) {
        this.displayType = type;
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
        this.painted = false;
    }

    public boolean testAndSetSensorDisplayFlag() {
        boolean result = this.painted;
        this.painted = !this.painted;
        return result;
    }

    private int emitterProcedure(WorldModel wm, WorldObject wo, int distanceLimit) {
        int distance = -1; // negative numbers reflect measurement error
        Color background;

        sEndX = wo.xCoord;
        sEndY = wo.yCoord;
        offsetX = 0;
        offsetY = 0;

        // Determine offsets
        if (wo.direction.equals("north")) {
            offsetX = 0;
            offsetY = -1;
        } else if (wo.direction.equals("south")) {
            offsetX = 0;
            offsetY = 11; // 10 + 1
        } else if (wo.direction.equals("east")) {
            offsetX = 11; // 10 + 1
            offsetY = 0;
        } else if (wo.direction.equals("west")) {
            offsetX = -1;
            offsetY = 0;
        }

        // Cast the sensor beam

        do {
            distance++;

            background = wm.model[sEndX + offsetX][sEndY + offsetY].getColor();

            if (wo.direction.equals("north"))
                sEndY--;
            else if (wo.direction.equals("south"))
                sEndY++;
            else if (wo.direction.equals("east"))
                sEndX++;
            else if (wo.direction.equals("west"))
                sEndX--;

        } while (background.equals(wm.background) && distance < distanceLimit);

        return distance;
    }

    public int infraredSensor(WorldModel wm, WorldObject wo) {
        int distance = -1; // negative numbers reflect measurement error
        int distanceLimit = 100;

        distance = emitterProcedure(wm, wo, distanceLimit);

        setSensorDisplay("infrared", wo.xCoord + offsetX, wo.yCoord + offsetY, sEndX, sEndY);

        return distance;
    }

    public int ultrasoundSensor(WorldModel wm, WorldObject wo) {
        int distance = -1; // negative numbers reflect measurement error
        int distanceLimit = 50;

        distance = emitterProcedure(wm, wo, distanceLimit);

        // TODO: Future - Incorrectly displaying sensor beam

        setSensorDisplay("ultrasound", wo.xCoord + offsetX, wo.yCoord + offsetY, sEndX, sEndY);

        return distance;
    }

    public int touchSensor(WorldModel wm, WorldObject wo) {
        int touched = 0; // 0 indicates FALSE (not touching)
        int distance = -1; // -1 indicates error
        int distanceLimit = 5;
        // TODO: Bug: "distacceLimit" needs to be 5 since the simulator does not permit
        // the robot to get closer to a wall.

        distance = emitterProcedure(wm, wo, distanceLimit);

        setSensorDisplay("touch", wo.xCoord + offsetX, wo.yCoord + offsetY, sEndX, sEndY);

        if (distance < distanceLimit)
            touched = 1; // TRUE (it touched something)

        return touched;
    }

    public void drainBattery(double amount) {
        this.currentBatteryLevel -= amount;
        if (this.currentBatteryLevel < 0) {
            this.currentBatteryLevel = 0; // Prevent battery level from going negative
            System.out.println("Battery depleted. Robot is inactive until recharged.");
        }
    }


    public boolean isBatteryDepleted() {
        return this.currentBatteryLevel <= 0;
    }

    // Simulate capturing an image within the robot's field of view
    public Color[][] captureImage(WorldModel wm, WorldObject wo, int viewWidth, int viewDepth) {
        Color[][] view = new Color[viewDepth][viewWidth]; // Create a 2D array to represent the image
        int directionFactorX = 0, directionFactorY = 0;

        // Calculate direction factors based on current facing direction
        switch (wo.direction) {
            case "north":
                directionFactorY = -1;
                break;
            case "south":
                directionFactorY = 1;
                break;
            case "east":
                directionFactorX = 1;
                break;
            case "west":
                directionFactorX = -1;
                break;
        }

        // Populate the view array with data from the world model
        for (int d = 0; d < viewDepth; d++) {
            for (int w = -viewWidth / 2; w <= viewWidth / 2; w++) {
                int checkX = wo.xCoord + directionFactorX * d;
                int checkY = wo.yCoord + directionFactorY * d + w;
                if (checkX >= 0 && checkX < wm.modelWidth && checkY >= 0 && checkY < wm.modelHeight) {
                    view[d][w + viewWidth / 2] = wm.model[checkX][checkY].getColor(); // Simulate "seeing" what's at that position
                } else {
                    view[d][w + viewWidth / 2] = Color.gray;
                    // TODO gray for out-of-bounds
                }
            }
        }
        return view;
    }

    public int[] depthArray(WorldModel wm, WorldObject wo, int viewWidth, int viewDepth) {
        int[] depths = new int[viewDepth]; // Create an array to represent the depth data
        int directionFactorX = 0, directionFactorY = 0;

        // Calculate direction factors based on current facing direction
        switch (wo.direction) {
            case "north":
                directionFactorY = -1;
                break;
            case "south":
                directionFactorY = 1;
                break;
            case "east":
                directionFactorX = 1;
                break;
            case "west":
                directionFactorX = -1;
                break;
        }

        // Populate the depth array with data from the world model
        for (int d = 0; d < viewDepth; d++) {
            int checkX = this.startX + directionFactorX * d;
            int checkY = this.startY + directionFactorY * d;
            if (checkX >= 0 && checkX < wm.modelWidth && checkY >= 0 && checkY < wm.modelHeight) {
                depths[d] = wm.calculateObstacleDistance(checkX, checkY, directionFactorX, directionFactorY);
            } else {
                depths[d] = -1; // Use -1 to indicate out-of-bounds or no obstacle detected
            }
        }
        return depths;
    }

}
