package main;

import java.awt.Color;

public class Cell {

    private int[] pheromones;
    // [To food, To home, Home, Food, Danger,Help me]
    private boolean hasFood;
    private Color color;
    private Color defaultColor;
    private boolean isHome;

    public Cell() {
        this.pheromones = new int[main.PheromoneType.values().length];
        this.hasFood = false;
        this.color = Color.WHITE;
        this.defaultColor = Color.gray;
        this.isHome = false;
    }

    public void setHome() {
        this.isHome = true;
        this.color = Color.RED;
    }

    public boolean isHome() {
        return this.isHome;
    }

    public void setDefaultColor(Color color) {
        this.defaultColor = color;
    }

    public void addPheromone(main.PheromoneType type, int amount) {
        if (amount > 10) {
            amount = 10;
        }
        if (amount < 0) {
            amount = 0;
        }
        if (type != null) {
            this.pheromones[type.ordinal()] += amount;
        } else {
            System.out.println("Invalid pheromone type");
        }
    }

    public int[] getPheromone() {
        return this.pheromones;
    }

    public void removePheromone() {
        for (int i = 0; i < pheromones.length; i++) {
            if (pheromones[i] > 0) {
                pheromones[i]--;
            }
        }
    }

    public void setFood() {
        this.color = Color.GREEN;
        this.hasFood = true;
    }

    public void reduceFood() {
        this.hasFood = false;
        this.color = defaultColor;
    }

    public boolean hasFood() {
        return this.hasFood;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public void toString(int x, int y) {
        System.out.println("Cell at (" + x + ", " + y + ")");
        System.out.println("Pheromones: ");
        for (int i = 0; i < pheromones.length; i++) {
            if (pheromones[i] > 0) {
                System.out.println(main.PheromoneType.values()[i] + ": " + pheromones[i]);
            }
        }
        System.out.println("hasFood: " + hasFood);
        System.out.println("Color: " + color);
    }
}
