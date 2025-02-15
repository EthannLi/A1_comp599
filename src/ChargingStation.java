public class ChargingStation {
    private int xCoord, yCoord;
    private int chargingRate;
    private int energy;

    public ChargingStation(int chargingRate, int energy) {
        this.chargingRate = chargingRate; // The rate at which robots charge
        this.energy = energy; // The amount of energy the station has
    }

    // You can add methods like:
    // - chargeRobot() to increase the robot's battery
    // - getCoordinates() to return its position
    public void chargeRobot(Robot robot) {
        if (robot != null) {
            robot.currentBatteryLevel += getChargingRate();
        }
    }

    public int getEnergy() {
        return energy;
    }

    public int getChargingRate() {
        return chargingRate;
    }

    public void setChargingRate(int rate) {
        this.chargingRate = rate;
    }

    public int getX() {
        return xCoord;
    }

    public int getY() {
        return yCoord;
    }

    public void setCoordinates(int x, int y) {
        this.xCoord = x;
        this.yCoord = y;
    }
}
