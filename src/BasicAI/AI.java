package BasicAI;

import java.util.ArrayList;

public class AI implements AIProcedure {
    private AIProcedure brain;

    public AI(AIProcedure aBrain) {
        this.brain = aBrain;
    }

    public ArrayList<AITuple> think(int depth, ArrayList<AITuple> inputData) throws Exception {
        ArrayList<AITuple> result;

        result = brain.think(depth, inputData);

        return result;
    }
}
