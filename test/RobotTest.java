package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RobotTest {
    private WorldModel wm;
    private WorldObject wo;
    private Robot robot;

    @BeforeEach
    void setUp() {
        // Assume these initialization methods exist
        wm = new WorldModel(new ConfigFileReader(), Color.WHITE); // Initialize a world model
        wo = new WorldObject(); // Create a world object
        robot = new Robot("es", "path/to/knowledgebase"); // Initialize a robot

        // Set the robot's starting position and orientation
        robot.startX = 10;
        robot.startY = 10;
        robot.endX = 10;
        robot.endY = 10;
        robot.displayType = "robot";
    }

    @Test
    void testDepthArrayFacingNorth() {
        wo.direction = "north";
        int viewWidth = 5;
        int viewDepth = 10;

        // Assume there is an obstacle 5 steps to the north
        wm.model[10][5] = Color.BLACK; // Set the obstacle

        int[] depths = robot.depthArray(wm, wo, viewWidth, viewDepth);
        assertEquals(5, depths[0], "Depth should be 5 for the first obstacle north");
    }

    @Test
    void testDepthArrayNoObstacle() {
        wo.direction = "south";
        int viewWidth = 5;
        int viewDepth = 10;

        // Scenario with no obstacles
        int[] depths = robot.depthArray(wm, wo, viewWidth, viewDepth);
        assertTrue(depths[0] == -1, "Should return -1 when no obstacle is detected");
    }

    @Test
    void testInfraredSensor() {
        wo.direction = "east"; // Set the direction east
        wm.model[20][10] = Color.BLACK; // Simulate an obstacle at (20,10)
        int result = robot.infraredSensor(wm, wo);
        assertEquals(10, result, "Infrared sensor should detect an obstacle 10 units away");
    }

    @Test
    void testUltrasoundSensor() {
        wo.direction = "west"; // Set the direction west
        wm.model[0][10] = Color.BLACK; // Simulate an obstacle directly to the west
        int result = robot.ultrasoundSensor(wm, wo);
        assertEquals(10, result, "Ultrasound sensor should detect an obstacle 10 units away");
    }

    @Test
    void testTouchSensor() {
        wo.direction = "north";
        robot.startY = 5;
        // Simulate that robot is right next to an obstacle
        wm.model[10][4] = Color.BLACK; // Obstacle is immediately north
        int result = robot.touchSensor(wm, wo);
        assertEquals(1, result, "Touch sensor should detect contact with an obstacle");
    }

    @Test
    void testBatteryDepletion() {
        robot.drainBattery(50.0);
        assertEquals(50.0, robot.currentEnergy, "Battery level should be depleted by 50 units");
    }

    @Test
    void testBatteryDepletionToZero() {
        robot.drainBattery(150.0);
        assertEquals(0, robot.currentEnergy, "Battery level should not go below zero");
    }

    @Test
    void testCaptureImage() {
        int viewWidth = 5;
        int viewDepth = 5;
        Color[][] image = robot.captureImage(wm, wo, viewWidth, viewDepth);
        assertNotNull(image, "Capture image should return a non-null array");
        assertEquals(viewDepth, image.length, "Image depth should match the specified depth");
        assertEquals(viewWidth, image[0].length, "Image width should match the specified width");
    }
}
