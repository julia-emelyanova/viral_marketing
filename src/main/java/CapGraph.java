/**
 *
 */

import java.util.*;

/**
 * @author Your name here.
 *
 * For the warm up assignment, you must implement your MyGraph in a class
 * named CapGraph.  Here is the stub file.
 *
 */
public class CapGraph implements MyGraph {

    private HashMap<Integer, HashSet<Integer>> adjVertices = new HashMap<>();

    @Override
    public void addVertex(int num) {
        adjVertices.putIfAbsent(num, new HashSet<>());
    }

    @Override
    public boolean hasVertex(int num) {
        return adjVertices.keySet().contains(num);
    }

    @Override
    public void removeVertex(int num) {
        adjVertices.remove(num);
    }

    @Override
    public void addEdge(int from, int to) {
        adjVertices.get(from).add(to);
    }

    @Override
    public void removeEdge(int from, int to) {
        adjVertices.get(from).remove(to);
    }

    @Override
    public MyGraph getEgonet(int center) {

        MyGraph egoNet = new CapGraph();

        if (!adjVertices.containsKey(center)) {
            return egoNet;
        }

        egoNet.addVertex(center);
        HashSet<Integer> centerNeighbors = adjVertices.get(center);
        for (Integer neighbor : centerNeighbors) {
            egoNet.addVertex(neighbor);
            egoNet.addEdge(center, neighbor);

            HashSet<Integer> neighborsOfANeighbor = adjVertices.get(neighbor);
            for (Integer centerNeighbor : centerNeighbors) {
                if (neighborsOfANeighbor.contains(centerNeighbor)) {
                    egoNet.addVertex(centerNeighbor);
                    egoNet.addEdge(neighbor, centerNeighbor);
                }
            }

            if (neighborsOfANeighbor.contains(center)) {
                egoNet.addEdge(neighbor, center);
            }
        }

        return egoNet;
    }

    //todo delete this
    public MyGraph getEgonets(Set<Integer> nodes) {
        MyGraph egoNet = new CapGraph();

        for (int center : nodes) {
            if (!adjVertices.containsKey(center)) {
                continue;
            }

            egoNet.addVertex(center);
            HashSet<Integer> centerNeighbors = adjVertices.get(center);
            for (Integer neighbor : centerNeighbors) {
                egoNet.addVertex(neighbor);
                egoNet.addEdge(center, neighbor);

                HashSet<Integer> neighborsOfANeighbor = adjVertices.get(neighbor);
                for (Integer centerNeighbor : centerNeighbors) {
                    if (neighborsOfANeighbor.contains(centerNeighbor)) {
                        egoNet.addVertex(centerNeighbor);
                        egoNet.addEdge(neighbor, centerNeighbor);
                    }
                }

                if (neighborsOfANeighbor.contains(center)) {
                    egoNet.addEdge(neighbor, center);
                }
            }


        }

        return egoNet;

    }

    @Override
    public List<MyGraph> getSCCs() {

        MyGraph g = new CapGraph();
        Stack<Integer> vertices = new Stack<>();

        for (Map.Entry<Integer, HashSet<Integer>> entry : adjVertices.entrySet()) {
            vertices.add(entry.getKey());
            g.addVertex(entry.getKey());
            for (Integer n : entry.getValue()) {
                g.addEdge(entry.getKey(), n);
            }
        }


        List<Stack<Integer>> result = dfs1(g, vertices);
        Stack<Integer> finished = new Stack<>();
        for (Stack<Integer> stack : result) {
            finished.addAll(stack);
        }

        g = new CapGraph();
        for (Map.Entry<Integer, HashSet<Integer>> entry : adjVertices.entrySet()) {
            g.addVertex(entry.getKey());
            for (Integer n : entry.getValue()) {
                //transpose
                g.addVertex(n);
                g.addEdge(n, entry.getKey());
            }
        }


        result = dfs1(g, finished);

        List<MyGraph> graphs = new ArrayList<>(result.size());
        for (Stack<Integer> stack : result) {
            MyGraph graph = new CapGraph();
            for (Integer i : stack) {
                graph.addVertex(i);
                for (Integer n : adjVertices.get(i)) {
                    if (stack.contains(n)) {
                        graph.addEdge(i, n);
                    }
                }
            }
            graphs.add(graph);
        }

        return graphs;
    }

    private Stack<Integer> dfs(MyGraph g, Stack<Integer> vertices) {
        Set<Integer> visited = new HashSet<>();
        Stack<Integer> finished = new Stack<>();

        while (!vertices.empty()) {
            Integer v = vertices.pop();
            if (!visited.contains(v)) {
                dfsVisit(g, v, visited, finished);
            }
        }
        return finished;
    }

    private List<Stack<Integer>> dfs1(MyGraph g, Stack<Integer> vertices) {
        Set<Integer> visited = new HashSet<>();
        List<Stack<Integer>> result = new ArrayList<>();


        while (!vertices.empty()) {
            Integer v = vertices.pop();
            if (!visited.contains(v)) {
                Stack<Integer> finishedLocal = new Stack<>();
                dfsVisit1(g, v, visited, finishedLocal);
                result.add(finishedLocal);
            }
        }

        return result;
    }


    private void dfsVisit(MyGraph g, Integer v, Set<Integer> visited, Stack<Integer> finished) {
        visited.add(v);
        for (Integer neighbor : g.exportGraph().get(v)) {
            if (!visited.contains(neighbor)) {
                dfsVisit(g, neighbor, visited, finished);
            }
        }
        finished.add(v);
    }

    private void dfsVisit1(MyGraph g, Integer v, Set<Integer> visited, Stack<Integer> finished) {
        visited.add(v);
        for (Integer neighbor : g.exportGraph().get(v)) {
            if (!visited.contains(neighbor)) {
                dfsVisit1(g, neighbor, visited, finished);
            }
        }
        finished.add(v);
    }


    @Override
    public HashMap<Integer, HashSet<Integer>> exportGraph() {
        return adjVertices;
    }

    @Override
    public int getNumberOfVertexes() {
        return adjVertices.size();
    }

    @Override
    public HashSet<Integer> getNeighbours(int node) {
        return adjVertices.get(node);
    }


}
