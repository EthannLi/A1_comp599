package BasicAI;

import java.util.ArrayList;

public interface AIProcedure {
    ArrayList<AITuple> think(int depth, ArrayList<AITuple> inputData) throws Exception;
}
