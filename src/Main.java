import java.awt.*;
import java.util.Scanner;
import java.lang.Thread;

// ===========================================================================
// Programmer   : Joseph Vybihal
// Creation Date: January 2024
// Purpose      : Main simulation loop and world configuration file processing
//
// Input : World configuration filename
// Uses  : ConfigurationFileReader & WorldView (GUI simulation)
// Output: Console user input and error messages
//
// Other Developer Modifications (name - change)
// ---------------------------------------------
// TBD

public class Main {
    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
        String simConfigFilename;
        int i;
        boolean done = false;

        // Welcome & get configuration filename
        System.out.println("Prometheus BasicAI.AI Simulator vr 0.1");
        System.out.println("Simulation configuration filename: ");
        simConfigFilename = userInput.nextLine();

        try {
            // Extract configuration file
            ConfigFileReader config = new ConfigFileReader(simConfigFilename);
            WorldView wv = new WorldView(config);
            wv.simulationThread.join();
//            // Main simulation loop
//            i = 0 ;
//            while(!done) {
//                // Display run-time information
//                if (config.displayIteration) System.out.print("..." + (i + 1));
//
//                // Perform simulation step
//                if (config.delay > 0) Thread.sleep(config.delay);
//                wv.update(i);
//
//                // Update stop condition
//                if (config.maxIterations == 0) {
//                    // TODO: Future - Finish infinite simulation case
//                    done = true;
//                } else if (i < config.maxIterations) {
//                    i++;
//                } else {
//                    done = true;
//                }
//            }
//
//            if (config.displayIteration) System.out.println("...Done.");

        } catch(Exception e) {
            System.out.println("Exception in simulation loop.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        // Ending the simulation & housekeeping
        // System.out.println("Simulation Ended.");
    }
}