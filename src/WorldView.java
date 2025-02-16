import BasicAI.AITuple;
import main.Cell;

import java.util.ArrayList;
import java.util.Scanner;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// ============================================================================
// Programmer   : Joseph Vybihal
// Creation Date: January 2024
// Purpose      : Displays the GUI simulation from the model
//
// Input  : Configuration file & World model
// Uses   : ConfigFileReader, WorldModel, Frame, Canvas
// Outputs: Simulator's GUI view
//
// Other Developer Modifications (name - change)
// ---------------------------------------------
// TBD

public class WorldView {
    private Frame f;
    private MyCanvas c;
    private Canvas c2;

    private WorldModel wm;

    private Color[] pheromoneColors = {
            Color.RED, // TO_FOOD
            Color.GREEN, // TO_HOME
            Color.BLUE, // HOME
            Color.YELLOW, // FOOD
            Color.ORANGE, // DANGER
            Color.PINK // HELP_ME
    };

    private int worldWidth, worldHeight;
    private boolean displayModel;
    private boolean simulationRunning = false; // Flag to indicate if simulation is running
    public Thread simulationThread; // Thread used to control the simulation

    private int iteration;

    // ==========================================================================
    // ======================== CONSTRUCTOR =====================================
    // ==========================================================================
    /**
     * Constructor to initialize the view. This sets up the frame, canvas, buttons,
     * and initializes the world model based on the configuration.
     *
     * @param config The configuration object containing simulation settings
     */
    public WorldView(ConfigFileReader config) {
        this.worldHeight = config.maxHeight;
        this.worldWidth = config.maxWidth;
        this.displayModel = config.displayModel;
        this.iteration = 0;

        // Initialize Frame and Canvas
        f = new Frame("Prometheus Simulator");
        f.setLayout(new BorderLayout()); // Set BorderLayout for the frame
        // Set the canvas size to leave space for buttons
        int canvasHeight = this.worldHeight - 100; // Adjusted height for canvas, leaving space for buttons
        // Initialize Canvases
        c = new MyCanvas(this.worldWidth / 2 - 10, canvasHeight, Color.GRAY, 0);
        c2 = new MyCanvas(this.worldWidth / 2 - 10, canvasHeight, Color.LIGHT_GRAY, 1);

        wm = new WorldModel(config, Color.GRAY);

        // Button Panel
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.lightGray);
        Button startButton = new Button("Start");
        startButton.addActionListener(e -> {
            System.out.println("Start button clicked!");
            startSimulation(config); // Start the simulation
        });

        // Create Stop Button
        Button stopButton = new Button("Stop");
        stopButton.addActionListener(e -> stopSimulation()); // Stop the simulation

        // Create Close Button
        Button closeButton = new Button("Close");
        closeButton.addActionListener(e -> f.dispose()); // Close the window when clicked

        // Add buttons to the button panel
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(closeButton);

        f.add(c, BorderLayout.CENTER);
        f.add(c2, BorderLayout.EAST);
        f.add(buttonPanel, BorderLayout.SOUTH); // Add button panel at the bottom

        // Set the frame size
        f.setSize(this.worldWidth, this.worldHeight); // Set the size of the frame based on input size
        f.setVisible(true); // Make the frame visible

    }

    // ==========================================================================
    // ======================== START SIMULATION ================================
    // ==========================================================================
    /**
     * Starts the simulation thread if it isn't already running. This thread handles
     * the main simulation loop and updates the world.
     *
     * @param config The configuration object used for simulation parameters
     */
    public void startSimulation(ConfigFileReader config) {
        System.out.println("startSimulation");
        if (!simulationRunning) {
            simulationRunning = true;
            simulationThread = new Thread(() -> {
                // Simulation loop, iterates based on the main logic
                boolean done = false;
                try {
                    while (!done) {
                        // Perform each iteration's operation
                        if (config.displayIteration)
                            System.out.print("..." + (iteration + 1));
                        // Perform simulation step
                        if (config.delay > 0)
                            Thread.sleep(config.delay);
                        System.out.print("Before update" + iteration);
                        update(iteration);
                        System.out.print("After update" + iteration);
                        // Update stop condition
                        if (config.maxIterations == 0) {
                            done = true; // Handle infinite simulation case
                        } else if (iteration < config.maxIterations) {
                            iteration++;
                        } else {
                            done = true;
                        }
                    }

                    if (config.displayIteration)
                        System.out.println("...Done.");
                } catch (Exception e) {
                    System.out.println("Exception in simulation loop.");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                simulationRunning = false; // End of simulation
            });
            simulationThread.start(); // Start the simulation thread
            System.out.println("Simulation is already running.");
        }
    }

    // ==========================================================================
    // ======================== STOP SIMULATION ================================
    // ==========================================================================
    /**
     * Stops the simulation if it is running. This interrupts the simulation thread.
     */
    public void stopSimulation() {
        if (simulationRunning) {
            simulationThread.interrupt(); // Interrupt the simulation thread
            simulationRunning = false; // Set stop flag
            System.out.println("Simulation stopped.");
        } else {
            System.out.println("Simulation is not running.");
        }
    }

    // ==========================================================================
    // ==================== MAIN OBJECT SIMULATION LOOP =========================
    // ==========================================================================
    /**
     * Handles the main simulation loop. It iterates through the world model,
     * updates
     * the objects, and refreshes the display.
     *
     * @param index The current iteration index in the simulation loop
     * @throws Exception If any error occurs during simulation updates
     */

    public void update(int index) throws Exception {
        System.out.println("Updating iteration: " + index);
        ArrayList<AITuple> tuples;
        ArrayList<AITuple> inputFacts = new ArrayList<AITuple>();
        String token, argument;
        Scanner in;
        int result;
        Color[][] picture;
        int[] depth;
        // For all the autonimous objects in the world, make them move.

        for (WorldObject o : wm.objects) {

            //
            // Handling Robot objects
            //

            if (o.shape.equals("robot=")) {

                // Step 1: Get avatars to think

                // *** Step 1.1: Get sensor data for specific entities
                // sometimesRandom does not need sensor input (skip this step)
                // prometheus gets sensor input on its own (skip this step)
                // es needs sensor input

                if (o.robot.aiType.equals("es")) {
                    try {
                        result = wm.ultrasound(o);
                        inputFacts.add(new AITuple("sensor", "ULTRASOUND", result));
                    } catch (WorldModel.InsufficientBatteryException e) {
                        System.out.println(e.getMessage());
                        inputFacts.add(
                                new AITuple("battery_status", "ULTRASOUND_LOW_BATTERY", o.robot.currentBatteryLevel));
                        // TODO Handle the situation, e.g., send the robot to recharge
                    }

                    try {
                        result = wm.infrared(o);
                        inputFacts.add(new AITuple("sensor", "INFRARED", result));
                    } catch (WorldModel.InsufficientBatteryException e) {
                        System.out.println(e.getMessage());
                        inputFacts.add(
                                new AITuple("battery_status", "INFRARED_LOW_BATTERY", o.robot.currentBatteryLevel));
                    }

                    try {
                        result = wm.touch(o);
                        inputFacts.add(new AITuple("sensor", "TOUCH", result));
                    } catch (WorldModel.InsufficientBatteryException e) {
                        System.out.println(e.getMessage());
                        inputFacts.add(new AITuple("battery_status", "TOUCH_LOW_BATTERY", o.robot.currentBatteryLevel));
                    }

                    try {
                        picture = wm.picture(o, 5, 5);
                        inputFacts.add(new AITuple("sensor", "PICTURE", picture));
                    } catch (WorldModel.InsufficientBatteryException e) {
                        System.out.println(e.getMessage());
                        inputFacts.add(new AITuple("battery_status", "TOUCH_LOW_BATTERY", o.robot.currentBatteryLevel));
                    }

                    try {
                        depth = wm.depthScan(o, 5, 5);
                        inputFacts.add(new AITuple("sensor", "DEPTHSCAN", depth));
                    } catch (WorldModel.InsufficientBatteryException e) {
                        System.out.println(e.getMessage());
                        inputFacts.add(new AITuple("battery_status", "TOUCH_LOW_BATTERY", o.robot.currentBatteryLevel));
                    }

                }

                // *** Step 1.2: Consider next step based on sensor data
                tuples = o.robot.ai.think(0, inputFacts);
                System.out.println("AI decision: " + tuples);

                // Step 2: Change robot state in the world based on their thinking

                for (AITuple tuple : tuples) {

                    if (tuple.type == 'x') {
                        in = new Scanner(tuple.tag);
                        token = in.next();
                        if (in.hasNext()) {
                            argument = in.next();
                            tuple.iValue = Integer.parseInt(argument.substring(1, argument.length() - 1));
                        } else
                            tuple.iValue = 10;
                        tuple.tag = token;
                    }

                    switch (tuple.tag) {
                        case "#FORWARD" -> {
                            try {
                                wm.forward(o, tuple.iValue);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println(e.getMessage());
                                // TODO: Handle the situation, e.g., send the robot to recharge
                            }
                        }
                        case "#REVERSE" -> {
                            try {
                                wm.reverse(o, tuple.iValue);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("Reverse failed: " + e.getMessage());
                                // TODO: Handle the situation, possibly halt further movement or adjust strategy
                            }
                        }
                        case "#TURN" -> {
                            try {
                                wm.turn(o, tuple.iValue);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("Turn failed: " + e.getMessage());
                                // TODO: Decide if the robot should try a different maneuver or report status
                            }
                        }
                        case "#INFRARED" -> {
                            try {
                                wm.infrared(o);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("Infrared scanning failed: " + e.getMessage());
                                // TODO: Handle low battery - possibly deactivate sensor or reduce usage
                            }
                        }
                        case "#ULTRASOUND" -> {
                            try {
                                wm.ultrasound(o);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("Ultrasound scanning failed: " + e.getMessage());
                                // TODO: Adjust sensor settings or prioritize other tasks
                            }
                        }
                        case "#TOUCH" -> {
                            try {
                                wm.touch(o);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("Touch operation failed: " + e.getMessage());
                            }
                        }
                        case "#PICTURE" -> {
                            try {
                                wm.picture(o, 5, 5);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("PICTURE operation failed: " + e.getMessage());
                            }
                        }
                        case "DepthScan" -> {
                            try {
                                wm.depthScan(o, 5, 5);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("DepthScan operation failed: " + e.getMessage());
                            }
                        }
                        default -> System.out.println("No such cmd define!");
                    }
                }
            } else if (o.shape.equals("ant=")) {
                inputFacts.clear();
                Cell[] cells = wm.touchAndSmell(o);
                inputFacts.add(new AITuple("sensor", "TOUCHANDSMELL", cells));
                inputFacts.add(new AITuple("Energy", o.ant.getCurrentEnergy(), o.ant.carryFood));
                tuples = o.ant.ai.think(0, inputFacts);
                System.out.println("AI decision: " + tuples);

                // Step 2: Change robot state in the world based on their thinking

                for (AITuple tuple : tuples) {

                    if (tuple.type == 'x') {
                        in = new Scanner(tuple.tag);
                        token = in.next();
                        if (in.hasNext()) {
                            argument = in.next();
                            tuple.iValue = Integer.parseInt(argument.substring(1, argument.length() - 1));
                        } else
                            tuple.iValue = 10;
                        tuple.tag = token;
                    }

                    switch (tuple.tag) {
                        case "#FORWARD" -> {
                            try {
                                wm.forward(o, tuple.iValue);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println(e.getMessage());
                            }
                            break;
                        }
                        case "#REVERSE" -> {
                            try {
                                wm.reverse(o, tuple.iValue);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("Reverse failed: " + e.getMessage());
                                // TODO: Handle the situation, possibly halt further movement or adjust strategy
                            }
                            break;
                        }
                        case "#TURN" -> {
                            try {
                                wm.turn(o, tuple.iValue);
                            } catch (WorldModel.InsufficientBatteryException e) {
                                System.out.println("Turn failed: " + e.getMessage());
                                // TODO: Decide if the robot should try a different maneuver or report status
                            }
                            break;
                        }
                        case "#LEAVEPHEROMONE" -> {
                            wm.leavePheromone(o, tuple.PheromoneType, tuple.iValue);
                            break;
                        }
                        case "#EAT" -> {
                            wm.antEat(o);
                            break;
                        }
                        case "#PICKUPFOOD" -> {
                            wm.antPickupFood(o);
                            break;
                        }
                        case "#DROPFOOD" -> {
                            wm.antDropFood(o);
                            break;
                        }
                        default -> System.out.println("No such cmd define!");
                    }
                }
            }

        }

        c.repaint();
        c2.repaint();

        if (this.displayModel)
            wm.displayModel();
    }

    // ==============================================================================
    // ============================ UPDATING THE SCREEN
    // =============================
    // ==============================================================================

    // The actual canvas that houses the display window
    // -- Initializing the canvas
    // -- The paint() function that draws the objects to the window
    class MyCanvas extends Canvas {
        Color aColor;
        int type; // 0 for default, 1 for pheromone display

        public MyCanvas(int width, int height, Color bgColor, int type) {
            this.aColor = bgColor;
            this.type = type;
            setBackground(this.aColor);
            setSize(width, height);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (type == 1) {
                drawPheromones(g);
            } else {
                drawWorld(g);
            }
        }

        private void drawPheromones(Graphics g) {

            // Loop through each cell in the model grid
            for (int i = 0; i < wm.modelWidth; i++) {
                for (int j = 0; j < wm.modelHeight; j++) {
                    if (iteration % 20 == 0) {
                        wm.model[i][j].removePheromone();
                    }
                    int[] pheromones = wm.model[i][j].getPheromone(); // Retrieve the array of pheromone intensities for
                                                                      // the current cell

                    int maxPheromoneIndex = -1; // Initialize the index of the pheromone with the highest intensity
                    int maxPheromoneValue = 0; // Initialize the maximum pheromone intensity found in the cell

                    // Find the pheromone with the highest intensity in the cell
                    for (int k = 0; k < pheromones.length; k++) {

                        if (pheromones[k] > maxPheromoneValue) {
                            maxPheromoneValue = pheromones[k]; // Update the maximum pheromone value
                            maxPheromoneIndex = k; // Update the index of the maximum pheromone
                        }
                    }

                    // If a pheromone with intensity greater than zero is found
                    if (maxPheromoneIndex != -1) {
                        // Calculate color intensity based on pheromone strength
                        int intensity = Math.min(255, maxPheromoneValue * 10); // Example scaling factor, adjust as
                                                                               // necessary
                        Color baseColor = pheromoneColors[maxPheromoneIndex]; // Get the base color for the pheromone
                                                                              // type

                        // Adjust the color based on intensity
                        Color intenseColor = new Color(
                                Math.min(255, baseColor.getRed() * intensity / 255),
                                Math.min(255, baseColor.getGreen() * intensity / 255),
                                Math.min(255, baseColor.getBlue() * intensity / 255),
                                Math.min(255, intensity) // Ensure alpha does not exceed 255
                        );

                        g.setColor(intenseColor); // Set the color for drawing
                        g.fillRect(i, j, 1, 1); // Draw a 1x1 pixel rectangle at the (i, j) location
                    }
                }
            }
        }

        public void drawWorld(Graphics g) {
            System.out.println("paint");
            setBackground(this.aColor);
            for (WorldObject wo : wm.objects) {
                if (wo.shape.equals("circle")) {
                    g.setColor(Color.red);
                    g.fillOval(wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);
                    wm.fillOval(Color.red, wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);

                } else if (wo.shape.equals("food=")) {
                    g.setColor(Color.green);
                    g.fillRect(wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);
                    wm.fillRect(Color.green, wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);

                } else if (wo.shape.equals("triangle")) {
                    g.setColor(Color.black);
                    g.fillPolygon(new int[] { wo.xCoord, wo.xCoord + wo.xEnd, wo.xCoord },
                            new int[] { wo.yCoord, wo.yCoord, wo.yCoord + wo.yEnd }, 3);
                    wm.fillPolygon(Color.black, new int[] { wo.xCoord, wo.xCoord + wo.xEnd, wo.xCoord },
                            new int[] { wo.yCoord, wo.yCoord, wo.yCoord + wo.yEnd }, 3);
                }

                else if (wo.shape.equals("home=")) {
                    g.setColor(Color.red);
                    g.fillRect(wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);
                    wm.fillRect(Color.red, wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);

                } else if (wo.shape.equals("rect")) {
                    g.setColor(Color.black);
                    g.fillRect(wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);
                    wm.fillRect(Color.black, wo.xCoord, wo.yCoord, wo.xEnd, wo.yEnd);
                } else if (wo.shape.equals("charging-station=")) {
                    // Charging station - drawn larger than robot

                    // Set the color for the charging station
                    g.setColor(Color.yellow); // You can choose any color you like for the charging station

                    // Draw the charging station as a rectangle, making it larger than the robot
                    int width = 30; // Width of the charging station
                    int height = 20; // Height of the charging station

                    // Position the charging station slightly larger than the robot, at (wo.xCoord,
                    // wo.yCoord)
                    g.fillRect(wo.xCoord, wo.yCoord, width, height);

                    // Draw a simple indicator (like a charging symbol) on top of the charging
                    // station
                    g.setColor(Color.black);
                    g.fillRect(wo.xCoord + 10, wo.yCoord + 5, 10, 10); // Drawing a small square to represent the
                                                                       // charging point

                } else if (wo.shape.equals("robot=")) {
                    // Body shape & color
                    g.setColor(Color.blue);
                    g.drawRect(wo.xCoord, wo.yCoord, 10, 10);
                    wm.fillRect(Color.blue, wo.xCoord, wo.yCoord, 10, 10);
                    // Battery
                    int batteryWidth = 20;
                    int batteryHeight = 5;
                    int batteryX = wo.xCoord - batteryWidth / 2 + 5; // Adjust to appear above the robot
                    int batteryY = wo.yCoord - 10; // Adjust the position relative to the robot

                    // Draw the battery outline
                    g.setColor(Color.black);
                    g.drawRect(batteryX, batteryY, batteryWidth, batteryHeight);

                    // Fill based on current battery level
                    int fillWidth = (int) (batteryWidth * (wo.robot.currentBatteryLevel /
                            wo.robot.batteryCapacity));
                    g.setColor(Color.green);
                    g.fillRect(batteryX + 1, batteryY + 1, fillWidth, batteryHeight - 1);

                    // Front indicator
                    if (wo.direction.equals("north")) {
                        g.fillOval(wo.xCoord + 4, wo.yCoord, 3, 3);
                    } else if (wo.direction.equals("south")) {
                        g.fillOval(wo.xCoord + 4, wo.yCoord + 10, 3, 3);
                    } else if (wo.direction.equals("east")) {
                        g.fillOval(wo.xCoord + 8, wo.yCoord + 4, 3, 3);
                    } else if (wo.direction.equals("west")) {
                        g.fillOval(wo.xCoord, wo.yCoord + 4, 3, 3);
                    }

                    // Sensing emitter pulse
                    if (wo.robot != null) {
                        if (!wo.robot.testAndSetSensorDisplayFlag()) {
                            switch (wo.robot.displayType) {
                                case "infrared":
                                    g.setColor(Color.pink);
                                    g.drawLine(wo.robot.startX, wo.robot.startY, wo.robot.endX, wo.robot.endY);
                                    break;
                                case "ultrasound":
                                    g.setColor(Color.green);
                                    g.drawLine(wo.robot.startX, wo.robot.startY, wo.robot.endX, wo.robot.endY);
                                    break;
                                case "touch":
                                    g.setColor(Color.black);
                                    g.drawLine(wo.robot.startX, wo.robot.startY, wo.robot.endX, wo.robot.endY);
                                    break;
                                case "picture":
                                    g.setColor(Color.blue);
                                    g.drawLine(wo.robot.startX, wo.robot.startY, wo.robot.endX, wo.robot.endY);
                                    break;
                                case "depthscan":
                                    g.setColor(Color.yellow);
                                    g.drawLine(wo.robot.startX, wo.robot.startY, wo.robot.endX, wo.robot.endY);
                                    break;
                                default:
                                    System.out.println("No action for: " + wo.robot.displayType);
                                    break;
                            }
                        }
                    }

                } else if (wo.shape.equals("ant=")) {
                    if (wo.ant.getCurrentEnergy() <= 0) {
                        continue;
                    }
                    // Body shape & color
                    g.setColor(Color.ORANGE);
                    g.drawRect(wo.xCoord, wo.yCoord, 1, 1);
                    wm.fillRect(Color.ORANGE, wo.xCoord, wo.yCoord, 1, 1);
                    // Battery
                    int batteryWidth = 20;
                    int batteryHeight = 5;
                    int batteryX = wo.xCoord - batteryWidth / 2 + 5; // Adjust to appear above the robot
                    int batteryY = wo.yCoord - 10; // Adjust the position relative to the robot

                    // Draw the battery outline
                    g.setColor(Color.magenta);
                    g.drawRect(batteryX, batteryY, batteryWidth, batteryHeight);
                    // System.out.println("Battery: " + wo.ant.getCurrentEnergy() + " / " +
                    // wo.ant.getEnergyCapacity());
                    // Fill based on current battery level
                    int fillWidth = (int) (batteryWidth
                            * ((double) wo.ant.getCurrentEnergy() / wo.ant.getEnergyCapacity()));
                    g.setColor(Color.orange);
                    g.fillRect(batteryX + 1, batteryY + 1, fillWidth, batteryHeight - 1);

                    // Front indicator
                    if (wo.direction.equals("north")) {
                        g.fillOval(wo.xCoord + 4, wo.yCoord, 3, 3);
                    } else if (wo.direction.equals("south")) {
                        g.fillOval(wo.xCoord + 4, wo.yCoord + 10, 3, 3);
                    } else if (wo.direction.equals("east")) {
                        g.fillOval(wo.xCoord + 8, wo.yCoord + 4, 3, 3);
                    } else if (wo.direction.equals("west")) {
                        g.fillOval(wo.xCoord, wo.yCoord + 4, 3, 3);
                    }

                    // Sensing emitter pulse
                    if (wo.ant != null) {
                        if (!wo.ant.testAndSetSensorDisplayFlag()) {
                            switch (wo.ant.displayType) {
                                case "touchAndSmell":
                                    g.setColor(Color.pink);
                                    g.drawLine(wo.ant.startX, wo.ant.startY, wo.ant.endX, wo.ant.endY);
                                    break;
                                case "leavePheromone":
                                    g.setColor(Color.green);
                                    g.drawLine(wo.ant.startX, wo.ant.startY, wo.ant.endX, wo.ant.endY);
                                    break;
                                default:
                                    // System.out.println("No action for: " + wo.ant.displayType);
                                    break;
                            }
                        }
                    }
                } else {
                    System.out.println("Error: " + wo.shape);
                }
            }
        }

    }

}