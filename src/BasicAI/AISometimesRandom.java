package BasicAI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import main.PheromoneType;

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
        int step_size = 10;
        if(boundary[1] == 1 && boundary[0] == 1) {
            System.out.println("Boundary detected");
//            if(facing == 2 && state == State.SEARCHING) {
//                result.add(new AITuple("actuator", "#TURN", 180));
//                updateFacing(180);
//            }else if(facing == 0 && state == State.RETURNING){
//                result.add(new AITuple("actuator", "#TURN", 180));
//                updateFacing(180);
//            }else{
            System.out.println("North boundary");
            int turnDegree = random.nextBoolean() ? 90 : 270;
            result.add(new AITuple("actuator", "#TURN", 180));
            updateFacing(180);
            result.add(new AITuple("actuator", "#FORWARD", step_size));
            result.add(new AITuple("actuator", "#TURN", turnDegree));
            updateFacing(turnDegree);
            result.add(new AITuple("actuator", "#FORWARD", step_size));


            // GET AWAY FROM THE BOUNDARY
                /*
                first turn 180 then walk forward to get away from the boundary then turn 90 or 270
                 */
//            }
            return result;
        }
        // New: print pheromone mapping from first sensor tuple (if exists)
        for (AITuple tuple : inputData) {
            if ("sensor".equals(tuple.category) && tuple.Cells != null && tuple.Cells.length > 0) {
                System.out.println("Pheromone Mapping: " + getAllPheromones(tuple.Cells));
                break;
            }
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
        double pFollow = 0.2;
        int maxPheroCell = -1;
        int maxPheroVal = 0;
        // Select desired pheromone types based on ant state.
        main.PheromoneType desired1, desired2;
        if(state == State.SEARCHING) {
            desired1 = main.PheromoneType.FOOD;
            desired2 = main.PheromoneType.TO_FOOD;
        } else {
            desired1 = main.PheromoneType.HOME;
            desired2 = main.PheromoneType.TO_HOME;
        }
        // Search sensor tuples for the strongest desired pheromone.
        for (AITuple tuple: inputData) {
            if(tuple.category.equals("sensor") && tuple.Cells != null) {
                Map<Integer, Map<main.PheromoneType, Integer>> allPheromones = getAllPheromones(tuple.Cells);
                for (Map.Entry<Integer, Map<main.PheromoneType, Integer>> entry : allPheromones.entrySet()) {
                    int cellIndex = entry.getKey();
                    Map<main.PheromoneType, Integer> cellPheros = entry.getValue();
                    int value1 = cellPheros.getOrDefault(desired1, 0);
                    int value2 = cellPheros.getOrDefault(desired2, 0);
                    int cellMax = Math.max(value1, value2);
                    if (cellMax > maxPheroVal) {
                        maxPheroVal = cellMax;
                        maxPheroCell = cellIndex;
                    }
                }
            }
        }
        if(counter > 1000) {
            System.out.println("Counter reset");
            double chance = random.nextDouble();
            if(maxPheroCell != -1 && chance > pFollow) {
                // Follow pheromone gradient using the strongest desired pheromone.
                System.out.println("---------------------------------------Following pheromone---------------------------------------");
                if (maxPheroCell == 1) { // forward cell
                    result.add(new AITuple("actuator", "#FORWARD", step_size));
                } else {
                    // Turn left if cell index 0; right if cell index 2 (adjust as needed for side cells).
                    int turnDegree = (maxPheroCell == 0) ? 270 : 90;
                    result.add(new AITuple("actuator", "#TURN", turnDegree));
                    updateFacing(turnDegree);
                    result.add(new AITuple("actuator", "#FORWARD", step_size));
                }
            } else {
                result.addAll(randomMovement());
                if (state == State.SEARCHING) {
                    result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.TO_FOOD, 10));
                    result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.TO_HOME, 10));

                } else {
                    result.add(new AITuple("sensor", "#LEAVEPHEROMONE", main.PheromoneType.TO_HOME, 10));
                }
            }
        } else {
            counter++;
            result.addAll(randomMovement());
            if (state == State.SEARCHING) {
                result.add(new AITuple("sensor", "#LEAVEPHEROMONE", PheromoneType.TO_FOOD, 10));
                result.add(new AITuple("sensor", "#LEAVEPHEROMONE", PheromoneType.TO_HOME, 10));
            } else {
                result.add(new AITuple("sensor", "#LEAVEPHEROMONE", PheromoneType.TO_HOME, 10));
            }
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
                        System.out.println("Counter: " + i);
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

    // New randomMovement function with north (SEARCHING) and south (RETURNING) heuristic
    private ArrayList<AITuple> randomMovement() {
        ArrayList<AITuple> result = new ArrayList<>();
        int step_size = 10;
        if(random.nextDouble() < 0.2) {
            if (state == State.SEARCHING) {
                // Compute minimal turn to north (facing == 0)
                int diff = (0 - facing);
                if (diff < 0) diff += 4;
                if (diff != 0) {
                    int turnDegree = diff * 90;
                    result.add(new AITuple("actuator", "#TURN", turnDegree));
                    updateFacing(turnDegree);
                }
            } else if (state == State.RETURNING) {
                // Compute minimal turn to south (facing == 2)
                int diff = (2 - facing);
                if (diff < 0) diff += 4;
                if (diff != 0) {
                    int turnDegree = diff * 90;
                    result.add(new AITuple("actuator", "#TURN", turnDegree));
                    updateFacing(turnDegree);
                }
            }
        }else{
            result.add(new AITuple("actuator", "#FORWARD", step_size));

        }
        return result;
    }

    // New function to get all pheromone types and their values for each cell (indexed by cell position)
    private Map<Integer, Map<main.PheromoneType, Integer>> getAllPheromones(main.Cell[] cells) {
        Map<Integer, Map<main.PheromoneType, Integer>> result = new HashMap<>();

        for (int i = 0; i < cells.length; i++) {
            int[] pheros = cells[i].getPheromone();
            Map<main.PheromoneType, Integer> cellPheromones = new HashMap<>();
            for (int j = 0; j < pheros.length; j++) {
                if (pheros[j] > 0) {
                    cellPheromones.put(main.PheromoneType.values()[j], pheros[j]);
                }
            }
            result.put(i, cellPheromones);
        }
        return result;
    }

    // Supports only: 0: North, 1: East, 2: South, 3: West (0°, 90°, 180°, 270°)
    private void updateFacing(int turnDegree) {
        if(turnDegree < 0)
            turnDegree = 360 + turnDegree;
        int turns = (turnDegree / 90) % 4;
        facing = (facing + turns) % 4;
    }


}