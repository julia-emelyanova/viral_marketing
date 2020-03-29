import org.apache.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/*
Nodes switched: 304 640 898 182 390 743 204 876 557 543
Iterations: 7
Best set: 742 266 507 605 861 557 221 543 975 767
Best result: 28
-------------------
Nodes switched: 80 352 912 949 872 904 298 586 812 431
Iterations: 5
Best set: 904 296 873 73 298 586 890 812 622 431
Best result: 47
-------------------
 */

//https://link.springer.com/article/10.1007/s41109-018-0062-7
public class ViralMarketing {

    final static Logger logger = Logger.getLogger(ViralMarketing.class);

    MyGraph g;
    Graph graph;
    Set<Integer> nodesUsingA;
    Set<Integer> currentUsersSet;
    Set<Integer> initialUsers;

    float rewardA = 2f; //person should switch to a
    float rewardB = 1f;

    protected String styleSheetVisualizeCascading =
            "node {" +
//                    "	fill-color: black;" +
                    "    size: 3px;" +
                    "    fill-color: #777;" +
//                    "    text-mode: hidden;" +
                    "    z-index: 1;" +
                    "}" +
                    "edge {" +
                    "    shape: line;" +
                    "    fill-color: #aaa;" +
                    "    arrow-size: 3px, 2px;" +
                    "    z-index: 0;" +
                    "}";

    protected String styleSheetCommon =
            "node.explored {" +
                    "	fill-color: red;" +
                    "}" +
                    "node.marked {" +
                    "	fill-color: green;" +
                    "    z-index: 2;" +
                    "}" +
                    "node.changed {" +
                    "	fill-color: yellow;" +
                    "}";

    protected String styleSheetVisualizeApproximation =
            "node {" +
                    "    size: 9px;" +
                    "    fill-color: #777;" +
                    "    z-index: 1;" +
                    "}" +
                    "edge {" +
                    "    shape: line;" +
                    "    fill-color: #777;" +
                    "    arrow-size: 6px, 4px;" +
                    "    z-index: 0;" +
                    "}" +
                    "node.worse {" +
                    "	fill-color: red;" +
                    "}" +
                    "node.current {" +
                    "	fill-color: yellow;" +
                    "}" +
                    "node.better {" +
                    "	fill-color: green;" +
                    "}";

    public static void main(String[] args) {
        ViralMarketing viralMarketing = new ViralMarketing();

        if (args.length > 0 && args[0].equals("basic")) {
            viralMarketing.runBasicAlgorithm();
        } else {
            for (int i = 0; i < 100; i++) {
                viralMarketing.runApproximationAlgorithm();
            }
        }
    }

    protected void runBasicAlgorithm() {

        initGraph(styleSheetVisualizeCascading);

        g = readGraph("facebook_1000");

        for (Map.Entry<Integer, HashSet<Integer>> entry : g.exportGraph().entrySet()) {
            int node = entry.getKey();
            addNode(node);
            for (int n : entry.getValue()) {
                addNode(n);
                addEdge(node, n);
            }
        }

//        Set<Integer> initialUsers = switchRandomNodes(75);
        Set<Integer> initialUsers = getGoodSolutionForFacebook1000();
        for (int i : initialUsers) {
            logger.debug("switched " + i);
        }
        graph.display();
        sleep(60000);


        cascadeChanges(initialUsers, true);
        logger.debug("done");

    }

    protected MyGraph readGraph(String filename) {
        MyGraph g = new CapGraph();

        try {
            File myObj = new File(getFileFromResources(filename));
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] nodes = data.split(" ");
                g.addVertex(Integer.parseInt(nodes[0]));
                g.addVertex(Integer.parseInt(nodes[1]));
                g.addEdge(Integer.parseInt(nodes[0]), Integer.parseInt(nodes[1]));

            }
            myReader.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }

        return g;
    }

    private void addNode(int node, String className, boolean showLabel) {
        String nodeName = String.valueOf(node);
        Node n = graph.addNode(nodeName);
        if (className != null) {
            n.setAttribute("ui.class", className);
        }
        if (showLabel) {
            n.setAttribute("ui.label", nodeName);
        }
    }

    private void addNode(int node) {
        addNode(node, null, false);
    }

    private void addNode(int node, String className) {
        addNode(node, className, false);
    }

    private void addEdge(int from, int to) {
        String node1 = String.valueOf(from);
        String node2 = String.valueOf(to);
        graph.addEdge(node1 + "_" + node2, node1, node2, true);
    }

    private void addAttribute(int node, String attributeName, String attributeValue) {
        String nodeName = String.valueOf(node);
        graph.getNode(nodeName).setAttribute(attributeName, attributeValue);
    }

    private void removeAttribute(int node, String attributeName) {
        String nodeName = String.valueOf(node);
        graph.getNode(nodeName).removeAttribute(attributeName);
    }

    private void removeAttribute(String attributeName) {
        for (Node n : graph.getEachNode()) {
            n.removeAttribute(attributeName);
        }
    }

    private void removeAttribute(Iterable<Integer> nodes, String attributeName) {
        for (int node : nodes) {
            removeAttribute(node, attributeName);
        }
    }

    private void cascadeChanges(Set<Integer> nodesUsingA, boolean displayNodes) {

        int generationNum = 1;
        logger.debug("Nodes switched in " + generationNum + " generation: " + nodesUsingA.size());


        while (true) {

            if (displayNodes) {
                for (int user : nodesUsingA) {
                    logger.debug("Mark node " + user);
                    addAttribute(user, "ui.class", "marked");

                }
                sleep();
            }

            generationNum++;
            Set<Integer> nodesSwitchedInCurrentGen = new HashSet<>();

            for (Integer node : nodesUsingA) {
                HashSet<Integer> neighbors = g.getNeighbours(node);
                for (Integer neighbor : neighbors) {
                    if (!(nodesUsingA.contains(neighbor) || nodesSwitchedInCurrentGen.contains(neighbor))
                            && ifSwitch(neighbor, nodesUsingA, rewardA, rewardB)) {

                        if (displayNodes) {
                            addAttribute(neighbor, "ui.class", "changed");
                        }

                        nodesSwitchedInCurrentGen.add(neighbor);
                    }
                }
            }

            nodesUsingA.addAll(nodesSwitchedInCurrentGen);

            if (nodesSwitchedInCurrentGen.isEmpty()) {
                break;
            }

            if (displayNodes) {
                for (int node : nodesSwitchedInCurrentGen) {
                    for (int n : g.getNeighbours(node)) {
                        addNode(n);
                        addEdge(node, n);
                    }
                }
                logger.debug("Nodes switched in " + generationNum + " generation: " + nodesUsingA.size());
                sleep(500);
            }
        }
    }

    protected void runApproximationAlgorithm() {
        initGraph(styleSheetVisualizeApproximation);
        g = readGraph("facebook_1000");
        initialUsers = switchRandomNodes(10);
        currentUsersSet = new HashSet<>(initialUsers);
        graph.display();
        logger.debug("Nodes switched: " + setToString(currentUsersSet));

        Set<Integer> bestUserSet = new HashSet<>(initialUsers);
        int bestResult = 0;
        int currentResult;
        int iterationNumber = 0;
        boolean bestSetHasChanged;
        int prevUser;
        do {
            bestSetHasChanged = false;
            iterationNumber++;
            initialUsers = new HashSet<>(bestUserSet);
            currentUsersSet = new HashSet<>(initialUsers);

            displayCurrentUsersSet();

            for (int currentUser : initialUsers) {

                int bestUser = currentUser;

                LinkedList<Integer> usersForAnalysis = new LinkedList<>();
                usersForAnalysis.add(currentUser);

                for (int neighbor : g.getNeighbours(currentUser)) {
                    if (!currentUsersSet.contains(neighbor)) {
                        usersForAnalysis.add(neighbor);
                    }
                }

                prevUser = currentUser;
                for (int userForAnalysis : usersForAnalysis) {
                    currentUsersSet.remove(prevUser);
                    currentUsersSet.add(userForAnalysis);
                    prevUser = userForAnalysis;

                    nodesUsingA = new HashSet<>(currentUsersSet);

                    cascadeChanges(nodesUsingA, false);

                    currentResult = nodesUsingA.size();
                    addAttribute(userForAnalysis, "ui.label", "" + currentResult);

                    if (currentResult > bestResult) {
                        bestUserSet = new HashSet<>(currentUsersSet);
                        bestResult = currentResult;
                        bestSetHasChanged = true;
                        bestUser = userForAnalysis;
                        addAttribute(userForAnalysis, "ui.class", "better");
                    } else {
                        addAttribute(userForAnalysis, "ui.class", "worse");
                    }

                    sleep();

                }

                removeAttribute(usersForAnalysis, "ui.class");
                addAttribute(bestUser, "ui.class", "marked");
                sleep();

            }

            removeAttribute("ui.label");

        } while (bestSetHasChanged);

        logger.info("Iterations: " + iterationNumber);
        logger.info("Best set: " + setToString(bestUserSet));
        logger.info("Best result: " + bestResult);
    }

    private void initGraph(String styleSheetVisualizeApproximation) {
        graph = new SingleGraph("Viral Marketing");
        graph.addAttribute("ui.stylesheet", styleSheetCommon + styleSheetVisualizeApproximation);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        graph.setStrict(false);
        graph.setAutoCreate(true);
    }

    private String setToString(Set<Integer> users) {
        return Stream.of(users).map(String::valueOf).collect(Collectors.joining(" "));
    }

    private void displayCurrentUsersSet() {
        for (int user : currentUsersSet) {
            addNode(user, "current");
            for (Integer neighbor : g.getNeighbours(user)) {
                addNode(neighbor, null);
                addEdge(user, neighbor);
            }
            sleep(500);
        }

        sleep();

        for (Node n : graph.getEachNode()) {
            n.removeAttribute("ui.class");
        }

        sleep();
    }

    protected boolean ifSwitch(Integer node, Set<Integer> nodesUsingA, float rewardA, float rewardB) {

        HashSet<Integer> neighbors = new HashSet<>(g.getNeighbours(node));
        int neighborsSize = neighbors.size();
        neighbors.retainAll(nodesUsingA);
        int neighborsUsingASize = neighbors.size();
        if (neighborsUsingASize == 0) {
            return false;
        }

        float p = (float) neighborsUsingASize / neighborsSize;
        float reward = rewardB / (rewardA + rewardB);

        return p > reward;
    }

    protected Set<Integer> switchRandomNodes(int nodesNum) {
        Set<Integer> vertices = g.exportGraph().keySet();
        Set<Integer> nodesToSwitch = new HashSet<>(nodesNum);

        Random rand = new Random();
        Set<Integer> nodeIdsToSwitch = new HashSet<>(nodesNum);
        for (int i = 0; i < nodesNum; i++) {
            while (true) {
                int nodeToSwitch = rand.nextInt(vertices.size());
                if (!nodeIdsToSwitch.contains(nodeToSwitch)) {
                    nodeIdsToSwitch.add(nodeToSwitch);
                    break;
                }
            }
        }

        int i = 0;
        for (Integer vertice : vertices) {
            if (nodeIdsToSwitch.contains(i)) {
                nodesToSwitch.add(vertice);
            }
            i++;
        }

        return nodesToSwitch;
    }

    private String getFileFromResources(String fileName) {

        ClassLoader classLoader = GraphStreamExample.class.getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return resource.getFile();
        }

    }

    protected void sleep() {
        sleep(500);
    }

    protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    protected Set<Integer> getGoodSolutionForFacebook1000() {
        Set<Integer> initialUsers = new HashSet<>();

        initialUsers.add(133);
        initialUsers.add(261);
        initialUsers.add(775);
        initialUsers.add(144);
        initialUsers.add(400);
        initialUsers.add(912);
        initialUsers.add(17);
        initialUsers.add(529);
        initialUsers.add(785);
        initialUsers.add(21);
        initialUsers.add(407);
        initialUsers.add(24);
        initialUsers.add(409);
        initialUsers.add(665);
        initialUsers.add(28);
        initialUsers.add(671);
        initialUsers.add(36);
        initialUsers.add(552);
        initialUsers.add(41);
        initialUsers.add(169);
        initialUsers.add(297);
        initialUsers.add(171);
        initialUsers.add(939);
        initialUsers.add(172);
        initialUsers.add(557);
        initialUsers.add(813);
        initialUsers.add(558);
        initialUsers.add(431);
        initialUsers.add(687);
        initialUsers.add(436);
        initialUsers.add(53);
        initialUsers.add(437);
        initialUsers.add(950);
        initialUsers.add(312);
        initialUsers.add(319);
        initialUsers.add(64);
        initialUsers.add(322);
        initialUsers.add(69);
        initialUsers.add(455);
        initialUsers.add(967);
        initialUsers.add(328);
        initialUsers.add(712);
        initialUsers.add(201);
        initialUsers.add(329);
        initialUsers.add(330);
        initialUsers.add(78);
        initialUsers.add(463);
        initialUsers.add(591);
        initialUsers.add(719);
        initialUsers.add(465);
        initialUsers.add(466);
        initialUsers.add(211);
        initialUsers.add(595);
        initialUsers.add(725);
        initialUsers.add(476);
        initialUsers.add(477);
        initialUsers.add(606);
        initialUsers.add(354);
        initialUsers.add(227);
        initialUsers.add(102);
        initialUsers.add(358);
        initialUsers.add(742);
        initialUsers.add(998);
        initialUsers.add(106);
        initialUsers.add(237);
        initialUsers.add(754);
        initialUsers.add(372);
        initialUsers.add(758);
        initialUsers.add(250);
        initialUsers.add(378);
        initialUsers.add(762);
        initialUsers.add(123);
        initialUsers.add(637);
        initialUsers.add(510);
        initialUsers.add(639);

        return initialUsers;
    }
}
