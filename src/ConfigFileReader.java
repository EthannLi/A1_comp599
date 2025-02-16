import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

// ============================================================================
// Programmer   : Joseph Vybihal
// Creation Date: January 2024
// Purpose      : To extract the configuration data to build the simulation
//
// Input  : Configuration file
// Uses   : WorldModel & WorldObject
// Outputs: Configuration parameters including world objects
//
// Other Developer Modifications (name - change)
// ---------------------------------------------
// TBD

public class ConfigFileReader {
    // World Parameters
    int maxIterations, maxWidth, maxHeight, delay;
    boolean displayModel, displayIteration;

    // Objects that populate the model
    ArrayList<WorldObject> objects;

    // Constructor does all the work **********************************************
    public ConfigFileReader(String filename) throws Exception {
        String token, data1, data2, data3, data4, data5, data6, data7, data8;
        File ptr = new File(filename);
        objects = new ArrayList<>();

        // Open the configuration file
        System.out.println("Running from path: " + ptr.getAbsolutePath());
        Scanner fileReader = new Scanner(ptr);

        // Process file token-by-token: key object type ("tag="), "#" as comment
        while (fileReader.hasNext()) {
            token = fileReader.next();

            if (token.equals("maxIteration=")) {
                token = fileReader.next();
                this.maxIterations = Integer.parseInt(token);

            } else if (token.equals("delay=")) {
                token = fileReader.next();
                this.delay = Integer.parseInt(token);

            } else if (token.equals("maxWidth=")) {
                token = fileReader.next();
                this.maxWidth = Integer.parseInt(token);

            } else if (token.equals("maxHeight=")) {
                token = fileReader.next();
                this.maxHeight = Integer.parseInt(token);

            } else if (token.equals("displayModel=")) {
                token = fileReader.next();
                if (token.equals("no"))
                    this.displayModel = false;
                else
                    this.displayModel = true;

            } else if (token.equals("displayIteration=")) {
                token = fileReader.next();
                if (token.equals("no"))
                    this.displayIteration = false;
                else
                    this.displayIteration = true;

            } else if (token.equals("#")) {
                token = fileReader.nextLine();

            } else if (token.equals("ant=")) {
                data1 = fileReader.next();
                data2 = fileReader.next();
                data3 = fileReader.next();
                data4 = fileReader.next();
                data5 = fileReader.next();
                data6 = fileReader.next();
                data7 = fileReader.next();
                objects.add(new WorldObject(token, data1, Integer.parseInt(data2), Integer.parseInt(data3),
                        Integer.parseInt(data4), Integer.parseInt(data5), data6, data7));

            } else if (token.equals("charging-station=")) {
                data1 = fileReader.next();
                data2 = fileReader.next();
                data3 = fileReader.next();
                data4 = fileReader.next();
                data5 = fileReader.next();
                data6 = fileReader.next();
                data7 = fileReader.next();
                // objects.add(new WorldObject(token, data1, Integer.parseInt(data2),
                // Integer.parseInt(data3),
                // Integer.parseInt(data4), Integer.parseInt(data5), Integer.parseInt(data6),
                // Integer.parseInt(data7)));
            } else if (token.equals("food=") || token.equals("home=")) {
                System.out.println("food or home");
                data1 = fileReader.next();
                data2 = fileReader.next();
                data3 = fileReader.next();
                data4 = fileReader.next();
                data5 = fileReader.next();
                objects.add(new WorldObject(token, data1, Integer.parseInt(data2), Integer.parseInt(data3),
                        Integer.parseInt(data4), Integer.parseInt(data5)));
            } else if (token.equals("object=")) {
                data1 = fileReader.next();
                data2 = fileReader.next();
                data3 = fileReader.next();
                data4 = fileReader.next();
                data5 = fileReader.next();
                objects.add(new WorldObject(data1, "obstacle", Integer.parseInt(data2), Integer.parseInt(data3),
                        Integer.parseInt(data4), Integer.parseInt(data5), "n/a", "n/a", "n/a"));
            } else if (token.equals("mant=")) {
                int homeX = 0, homeY = 0, homeXEnd = 0, homeYEnd = 0;
                boolean homeFound = false;

                for (WorldObject object : objects) {
                    if (object.shape.equals("home=")) {
                        homeX = object.xCoord;
                        homeY = object.yCoord;
                        homeXEnd = homeX + object.xEnd;
                        homeYEnd = homeY + object.yEnd;
                        homeFound = true;
                        break;
                    }
                }

                if (!homeFound) {
                    System.out.println("Config file token error: " + token + " No home object found before mant");
                    System.exit(0);
                }

                data1 = fileReader.next();
                int numAnts = Integer.parseInt(data1);
                int totalHomeSpace = (homeXEnd - homeX) * (homeYEnd - homeY);

                if (numAnts > totalHomeSpace) {
                    System.out.println("Too many ants for the home location");
                    System.exit(0);
                }

                int currentX = homeX;
                int currentY = homeY;

                for (int i = 0; i < numAnts; i++) {
                    objects.add(
                            new WorldObject("ant=", "ant" + i, currentX, currentY, 1, 1, "north", "sometimesRandom"));
                    System.out.println("ant" + i + " " + currentX + " " + currentY);
                    // Move to the next position
                    currentX++;
                    if (currentX >= homeXEnd) { // If we reach the end of the row, move to the next row
                        currentX = homeX;
                        currentY++;
                    }
                }
            } else {
                System.out.println("Config file token error: " + token);
                System.exit(0);
            }

        }

        fileReader.close();

        // Display simulation parameters
        System.out.println("CONFIGURATION PARAMETERS");
        System.out.println("maxIterations    : " + this.maxIterations);
        System.out.println("maxWidth         : " + this.maxWidth);
        System.out.println("maxHeight        : " + this.maxHeight);
        System.out.println("delay            : " + this.delay);
        System.out.println("displayModel     : " + this.displayModel);
        System.out.println("displayIteration : " + this.displayIteration);
    }
}
