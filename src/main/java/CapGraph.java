/**
 *
 */

import java.util.HashMap;
import java.util.HashSet;

public class CapGraph implements MyGraph {

    private HashMap<Integer, HashSet<Integer>> adjVertices = new HashMap<>();

    @Override
    public void addVertex(int num) {
        adjVertices.putIfAbsent(num, new HashSet<>());
    }

    @Override
    public void addEdge(int from, int to) {
        adjVertices.get(from).add(to);
    }

    @Override
    public HashMap<Integer, HashSet<Integer>> exportGraph() {
        return adjVertices;
    }

    @Override
    public HashSet<Integer> getNeighbours(int node) {
        return adjVertices.get(node);
    }


}
