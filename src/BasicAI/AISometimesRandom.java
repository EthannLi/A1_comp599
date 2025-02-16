package BasicAI;

import main.PheromoneType;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class AISometimesRandom implements AIProcedure {
    private int modulo;
    private int counter;
    private String baseCommand;
    private int baseValue;
    Random random;
    // 0: North, 1: East, 2: South, 3: West
    int facing = 0;

    private boolean pickedFood;

    // New swarm state
    private enum State { SEARCHING, RETURNING }
    private State state; // SEARCHING: seeking food, RETURNING: carrying food to nest

    public AISometimesRandom(int modulo, String baseCommand, int baseValue) {
        Date date = new Date();
        long seed = date.getTime();

        this.modulo = modulo;
        this.baseCommand = baseCommand;
        this.baseValue = baseValue;
        this.counter = 0;
        this.random = new Random(seed);
        this.facing = 0;
        this.pickedFood = false;
        // Initialize default state
        this.state = State.SEARCHING;
    }

    public ArrayList<AITuple> think(int depth, ArrayList<AITuple> inputData) throws Exception {
        ArrayList<AITuple> result = new ArrayList<>();
        int[] boundary = boundaryCheck(inputData);
        int step_size = 15;
        if(boundary[1] == 1 && boundary[0] == 1) {
            System.out.println("Boundary detected");
            int turnDegree = random.nextBoolean() ? 90 : 270;
            result.add(new AITuple("actuator", "#TURN", turnDegree));
            updateFacing(turnDegree);
            return result;
        }
        // Check for food and nest using sensor color signatures:
        int[] foodData = foodCheck(inputData); // returns {cellIndex, flag} if food detected
        int[] nestData = nestCheck(inputData); // returns {cellIndex, flag} if nest detected

        // State transitions:
        if(state == State.SEARCHING && foodData[1] == 1) {
            // Sensed food; pick up and switch to RETURNING.
            System.out.println("Food pickup");
            result.add(new AITuple("actuator", "#PICKUP", 1));
            result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.TO_HOME, 10));
            result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.FOOD, 10));
            state = State.RETURNING;
            return result;
        } else if(state == State.RETURNING && nestData[1] == 1) {
            // Sensed nest; drop food and switch to SEARCHING.
            System.out.println("Nest reached; dropping food");
            result.add(new AITuple("actuator", "#DROP", 1));
            result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.TO_HOME, 10));
            state = State.SEARCHING;
            return result;
        }
        // Default probabilistic actions:
        double pFollow = 0.6;
        int maxPheroCell = -1;
        int maxPheroVal = 0;
        int maxPheroFoodIndex = -1;
        int maxPheroFoodVal = 0;
        // Search for strongest pheromone (assume TO_FOOD for SEARCHING, TO_HOME for RETURNING)
        // Also search for FOOD since the food also has a pheromone
        for (AITuple tuple: inputData) {
            if(tuple.category.equals("sensor")) {
                for (int i = 0; i < tuple.Cells.length; i++) {
                    int[] pheros = tuple.Cells[i].getPheromone();
                    if(pheros[main.PheromoneType.FOOD.ordinal()] > maxPheroFoodVal) {
                        maxPheroFoodVal = pheros[main.PheromoneType.FOOD.ordinal()];
                        maxPheroFoodIndex = i;
                    }
                    int index = (state == State.SEARCHING) ? main.PheromoneType.TO_FOOD.ordinal() : main.PheromoneType.TO_HOME.ordinal();
                    if(pheros[index] > maxPheroVal) {
                        maxPheroVal = pheros[index];
                        maxPheroCell = i;
                    }
                }
            }
        }
        if(counter > 100) {
            System.out.println("Counter reset");
            double chance = random.nextDouble();
            if((maxPheroFoodIndex != -1 || maxPheroCell != -1) && chance > pFollow) {
                if (maxPheroCell != -1 && random.nextDouble() < pFollow ) {
                    // Follow pheromone gradient:
                    System.out.println("---------------------------------------Following pheromone---------------------------------------");
                    if (maxPheroCell == 1) { // forward cell
                        result.add(new AITuple("actuator", "#FORWARD", step_size));
                    } else {
                        // Turn left if cell index 0; right if index 2.
                        int turnDegree = (maxPheroCell == 0) ? 270 : 90;
                        result.add(new AITuple("actuator", "#TURN", turnDegree));
                        updateFacing(turnDegree);
                        result.add(new AITuple("actuator", "#FORWARD", step_size));
                    }
                } else if(maxPheroFoodIndex != -1 && state == State.SEARCHING) {
                    // Follow pheromone gradient to food:
                    System.out.println("---------------------------------------Following pheromone to food---------------------------------------");
                    if(maxPheroFoodIndex == 1) { // forward cell
                        result.add(new AITuple("actuator", "#FORWARD", step_size));
                    }else{
                        // Turn left if cell index 0; right if index 2.
                        int turnDegree = (maxPheroFoodIndex == 0) ? 270 : 90;
                        result.add(new AITuple("actuator", "#TURN", turnDegree));
                        updateFacing(turnDegree);
                        result.add(new AITuple("actuator", "#FORWARD", step_size));
                    }

                }
            } else {
                // Randomized action for swarm behavior:
                double randVal = random.nextDouble();
                if (randVal < 0.2) {
                    if(state ==State.SEARCHING && facing != 0) {
                        // Calculate minimal positive turn from current facing to north.
                        int turnDegree = (4 - facing) * 90; // e.g. if facing 1 (east), turn 270 to reach north.
                        result.add(new AITuple("actuator", "#TURN", turnDegree));
                        updateFacing(turnDegree);
                    }else if(state == State.RETURNING && facing != 2) {
                        // Calculate minimal positive turn from current facing to south.
                        int turnDegree = (2 - facing) * 90; // e.g. if facing 1 (east), turn 180 to reach south.
                        result.add(new AITuple("actuator", "#TURN", turnDegree));
                        updateFacing(turnDegree);
                    }
                } else {
                    // Move forward without turning
                    result.add(new AITuple("actuator", "#FORWARD", step_size));
                }
                // Leave pheromone based on state:
                if (state == State.SEARCHING) {
                    result.add(new AITuple("sensor", "#LEAVEPHEROMONE", PheromoneType.TO_FOOD, 10));
                }
                else {
                    result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.TO_HOME, 10));
                }
            }
        }else{
            counter++;
            // Randomized action for swarm behavior:
            double randVal = random.nextDouble();

            if (randVal < 0.3) {
                // Random turn only
                if(random.nextDouble() < 0.80){
                    if(state ==State.SEARCHING && facing != 0) {
                        // Calculate minimal positive turn from current facing to north.
                        int turnDegree = (4 - facing) * 90; // e.g. if facing 1 (east), turn 270 to reach north.
                        result.add(new AITuple("actuator", "#TURN", turnDegree));
                        updateFacing(turnDegree);
                    }else if(state == State.RETURNING && facing != 2) {
                        // Calculate minimal positive turn from current facing to south.
                        int turnDegree = (2 - facing) * 90; // e.g. if facing 1 (east), turn 180 to reach south.
                        result.add(new AITuple("actuator", "#TURN", turnDegree));
                        updateFacing(turnDegree);
                    }
                }else {
                    int turnDegree = random.nextBoolean() ? 90 : 270;
                    result.add(new AITuple("actuator", "#TURN", turnDegree));
                    updateFacing(turnDegree);
                }
            } else if (randVal < 0.7) {
                // Move forward without turning
                result.add(new AITuple("actuator", "#FORWARD", step_size));
            }
            // Leave pheromone based on state:
            if (state == State.SEARCHING)
                result.add(new AITuple("sensor", "#LEAVEPHEROMONE", PheromoneType.TO_FOOD, 10));
            else
                result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.TO_HOME, 10));
        }
        return result;
    }

    private int[] boundaryCheck(ArrayList<AITuple> inputData) {
        for (AITuple tuple: inputData) {
            if (tuple.category.equals("sensor")) {
                for (int i = 0; i < tuple.Cells.length; i++) {
                    if (tuple.Cells[i].getColor().getRed() == 0 &&
                            tuple.Cells[i].getColor().getGreen() == 0 &&
                            tuple.Cells[i].getColor().getBlue() == 0) {

                        if(i == 1) return new int[]{i, 1};
                    }
                }
            }
        }
        return new int[]{-1, 0};
    }

    public int[] foodCheck(ArrayList<AITuple> inputData) {
        for (AITuple tuple: inputData) {
            if (tuple.category.equals("sensor")) {
                for (int i = 0; i < tuple.Cells.length; i++) {
                    if (tuple.Cells[i].getColor().getRed() == 0 &&
                            tuple.Cells[i].getColor().getGreen() == 255 &&
                            tuple.Cells[i].getColor().getBlue() == 0) {
                        return new int[]{i, 1};
                    }
                }
            }
        }
        return new int[]{-1, 0};
    }


    public int[] nestCheck(ArrayList<AITuple> inputData) {
        for (AITuple tuple: inputData) {
            if (tuple.category.equals("sensor")) {
                for (int i = 0; i < tuple.Cells.length; i++) {
                    if (tuple.Cells[i].getColor().getRed() == 255 &&
                            tuple.Cells[i].getColor().getGreen() == 0 &&
                            tuple.Cells[i].getColor().getBlue() == 0) {
                        return new int[]{i, 1};
                    }
                }
            }
        }
        return new int[]{-1, 0};
    }


    // Helper method to update the facing of the ant based on turn degrees
    // Supports only: 0: North, 1: East, 2: South, 3: West (0°, 90°, 180°, 270°)
    private void updateFacing(int turnDegree) {
        if(turnDegree < 0)
            turnDegree = 360 + turnDegree;
        int turns = (turnDegree / 90) % 4;
        facing = (facing + turns) % 4;
    }

}