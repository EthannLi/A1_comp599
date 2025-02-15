import BasicAI.AIProcedure;
import BasicAI.AISometimesRandom;

import java.awt.*;

public class Ant {
    // Parameters for ModelView
    String displayType;
    int startX, startY, endX, endY;
    private boolean painted;

    // Parameters for sensor emitter
    int sEndX, sEndY, offsetX = 0, offsetY = 0;
    // Parameters for battery
    int energyCapacity;
    int currentEnergy;
    int EnergyDrainRate; // Battery consumption per action unit

    // Parameters for BasicAI.AI
    AIProcedure ai;
    public String aiType;

    // Food
    boolean carryFood;

    // Methods
    Ant(String aiType) throws Exception {
        this.displayType = "n/a";
        this.startX = 0;
        this.startY = 0;
        this.endX = 0;
        this.endY = 0;
        this.painted = false;
        this.aiType = aiType;

        this.energyCapacity = 10000; // Assuming battery capacity is a percentage
        this.currentEnergy = 10000; // Starts fully charged
        this.EnergyDrainRate = 1; // Adjust as necessary per unit of action

        if (aiType.equals("sometimesRandom")) {
            this.ai = new AISometimesRandom(0, "#FORWARD", 10);
        } else
            throw new Exception("Robot BasicAI.AI not defined, says: " + aiType);
    }

    public int getEnergyCapacity() {
        return this.energyCapacity;
    }

    public int getCurrentEnergy() {
        return this.currentEnergy;
    }

    public boolean testAndSetSensorDisplayFlag() {
        boolean result = this.painted;
        this.painted = !this.painted;
        return result;
    }

    public void leavePheromone(WorldModel wm, WorldObject wo, main.PheromoneType type, int amount) {
        wm.model[wo.xCoord][wo.yCoord].addPheromone(type, amount);
    }

    public int touchAndSmell(WorldModel wm, WorldObject wo) {
        int touched = 0; // 0 indicates FALSE (not touching)

        return touched;
    }

    public void drainBattery(double amount) {
        if (this.carryFood) {
            amount *= 2; // Carrying food consumes more energy
        }
        this.currentEnergy -= amount;
        if (this.currentEnergy < 0) {
            this.currentEnergy = 0; // Prevent battery level from going negative
            System.out.println("Battery depleted. Ant is dead.");
        }
    }

    public boolean isBatteryDepleted() {
        return this.currentEnergy <= 0;
    }

    public void pickupFood() {
        this.carryFood = true;
    }

    public void dropFood() {
        this.carryFood = false;
    }

    public void eatFood() {
        if (this.carryFood) {
            this.dropFood();
            this.currentEnergy = this.energyCapacity;
        }
    }

}
