package BasicAI;

import java.awt.*;
import java.util.Arrays;

public class AITuple {
    // A tuple is "Category : tag : value"
    public String category;
    public String tag;
    public char type;
    public String sValue;
    public int iValue;
    public float fValue;
    public double dValue;
    public boolean bValue;

    public Color[][] Picture;
    public int[] Depth;
    public main.PheromoneType PheromoneType;
    public main.Cell[] Cells;

    public AITuple(String category, String tag) {
        this.category = category;
        this.tag = tag;
        this.type = 'x'; // no value
        this.sValue = null;
        this.iValue = 0;
        this.fValue = 0.0f;
    }

    public AITuple(String category, String tag, String value) {
        this.category = category;
        this.tag = tag;
        this.type = 's'; // string value
        this.sValue = value;
        this.iValue = 0;
        this.fValue = 0.0f;
    }

    public AITuple(String category, String tag, int value) {
        this.category = category;
        this.tag = tag;
        this.type = 'i'; // integer value
        this.iValue = value;
        this.sValue = null;
        this.fValue = 0.0f;
    }

    public AITuple(String category, String tag, float value) {
        this.category = category;
        this.tag = tag;
        this.type = 'f'; // float value
        this.fValue = value;
        this.sValue = null;
        this.iValue = 0;
    }

    public AITuple(String category, String tag, double value) {
        this.category = category;
        this.tag = tag;
        this.type = 'd'; // double value
        this.dValue = value;
        this.sValue = null;
        this.iValue = 0;
    }

    public AITuple(String category, String tag, Color[][] picture) {
        this.category = category;
        this.tag = tag;
        this.type = 'c'; // color value
        this.dValue = 0.0;
        this.sValue = null;
        this.iValue = 0;
        this.Picture = picture;
    }

    public AITuple(String category, String tag, int[] depth) {
        this.category = category;
        this.tag = tag;
        this.type = 'p'; //
        this.dValue = 0.0;
        this.sValue = null;
        this.iValue = 0;
        this.Depth = depth;
    }

    public AITuple(String category, String tag, main.Cell[] cells) {
        this.category = category;
        this.tag = tag;
        this.type = 't'; // touch
        this.dValue = 0.0;
        this.sValue = null;
        this.iValue = 0;
        this.Cells = cells;
    }

    public AITuple(String category, String tag, main.PheromoneType type, int value) {
        this.category = category;
        this.tag = tag;
        this.type = 'l'; // leave
        this.dValue = 0.0;
        this.sValue = null;
        this.iValue = value;
        this.PheromoneType = type;
    }

    public AITuple(String tag, int energy, boolean carryFood) {
        this.category = "Info";
        this.tag = tag;
        this.type = 'l'; // leave
        this.dValue = 0.0;
        this.sValue = null;
        this.iValue = energy;
        this.bValue = carryFood;
    }


    @Override
    public String toString() {
        return switch (type) {
            case 's' -> category + " : " + tag + " : " + sValue;
            case 'i' -> category + " : " + tag + " : " + iValue;
            case 'f' -> category + " : " + tag + " : " + fValue;
            case 'd' -> category + " : " + tag + " : " + dValue;
            case 'c' -> category + " : " + tag + " : " + Arrays.deepToString(Picture);
            case 'p' -> category + " : " + tag + " : " + Arrays.toString(Depth);
            case 't' -> category + " : " + tag + " : " + Arrays.toString(Cells);
            case 'l' -> category + " : " + tag + " : " + PheromoneType + " : " + iValue;
            case 'x' -> category + " : " + tag;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
