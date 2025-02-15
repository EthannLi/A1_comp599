// ============================================================================
// Programmer   : Joseph Vybihal
// Creation Date: January 2024
// Purpose      : The cell of the ArrayList of objects
//
// Input  : Parameters that define an object
// Uses   : N/A
// Outputs: Object of type WorldObject
//
// Other Developer Modifications (name - change)
// ---------------------------------------------
// TBD

public class WorldObject {
    String shape;
    int xCoord, yCoord, xEnd, yEnd;
    String name, direction;

    Robot robot;

    Ant ant;

    ChargingStation chargingStation;

    ChargingStation food;

    public WorldObject(String shape, String ID, int x, int y) {
        this.shape = shape;
        this.name = ID;
        this.xCoord = x;
        this.yCoord = y;
        this.xEnd = -1;
        this.yEnd = -1;
        this.direction = "n/a";
        this.robot = null;
        this.chargingStation = null;
    }

    public WorldObject(String shape, String ID, int x, int y, int xEnd, int yEnd, String direction, String aiType,
            String knowledgeBaseFilename) throws Exception {
        this.shape = shape;
        this.name = ID;
        this.xCoord = x;
        this.yCoord = y;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.direction = direction;
        this.chargingStation = null;

        if (shape.equals("robot="))
            this.robot = new Robot(aiType, knowledgeBaseFilename);
        else
            this.robot = null;
    }

    public WorldObject(String shape, String ID, int x, int y, int xEnd, int yEnd, int chargingRate) {
        this.shape = shape;
        this.name = ID;
        this.xCoord = x;
        this.yCoord = y;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.direction = "n/a";
        this.robot = null;
        if (shape.equals("charging-station="))
            this.chargingStation = new ChargingStation(chargingRate, Integer.MAX_VALUE);
    }

    public WorldObject(String shape, String ID, int x, int y, int xEnd, int yEnd, String direction, String algorithm)
            throws Exception {
        this.shape = shape;
        this.name = ID;
        this.xCoord = x;
        this.yCoord = y;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.direction = direction;
        this.chargingStation = null;
        this.robot = null;

        if (shape.equals("ant="))
            this.ant = new Ant(algorithm);
        else
            this.ant = null;
    }

    // food
    public WorldObject(String shape, String ID, int x, int y, int xEnd, int yEnd) {
        this.shape = shape;
        this.name = ID;
        this.xCoord = x;
        this.yCoord = y;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.direction = "n/a";
        this.robot = null;
        this.chargingStation = null;
        if (shape.equals("food=")) {
            this.food = new ChargingStation(1, xEnd * yEnd);
        }

    }

}
