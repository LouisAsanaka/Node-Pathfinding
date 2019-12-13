package pathfinding.model;

import javafx.animation.SequentialTransition;

import javafx.util.Duration;
import pathfinding.animation.AnimationUtil;
import pathfinding.ui.GraphConnection;
import pathfinding.ui.GraphNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class Graph {

    public static final class SearchMethods {

        public static final String UCS = "Uniform Cost Search";
        public static final String GREEDY = "Greedy Search";
        public static final String A_STAR = "A* Search";

        private SearchMethods() { }
    }

    public static final class SearchResult {
        public final double cost;
        public final List<GraphNode> path;

        public SearchResult(double cost, List<GraphNode> path) {
            this.cost = cost;
            this.path = path;
        }
    }

    private static final Duration PAUSE_DURATION = Duration.millis(800);

    // Implement graph using an adjacency list
    private final HashMap<GraphNode, ArrayList<Edge>> adjList;

    public Graph() {
        adjList = new HashMap<>();
    }

    public GraphNode addVertex(GraphNode v) {
        adjList.putIfAbsent(v, new ArrayList<>());
        return v;
    }

    public void removeVertex(GraphNode v) {
        List<Edge> edges = adjList.remove(v);
        for (Edge e : edges) {
            adjList.get(e.getEnding()).remove(new Edge(v, e.getWeightProperty()));
        }
    }

    public void connectVertices(GraphNode v1, GraphNode v2, GraphConnection connection) {
        addVertex(v1);
        addVertex(v2);
        adjList.get(v1).add(new Edge(v2, connection));
        adjList.get(v2).add(new Edge(v1, connection));
    }

    public Set<GraphNode> getVertices() {
        return adjList.keySet();
    }

    public ArrayList<Edge> getEdges(GraphNode v) {
        return adjList.getOrDefault(v, new ArrayList<>());
    }

    public Edge getEdge(GraphNode from, GraphNode to) {
        if (from == null || to == null) {
            return null;
        }
        var edges = getEdges(from);
        for (Edge edge : edges) {
            if (edge.getEnding().equals(to)) {
                return edge;
            }
        }
        return null;
    }

    public SearchResult search(GraphNode source, GraphNode goalNode,
                               String method) {
        if (source == null || goalNode == null) {
            throw new IllegalArgumentException(
                "Invalid arguments for UCS. Source: " + source + " | Goal: " + goalNode);
        }
        SequentialTransition animation = new SequentialTransition();

        HashMap<GraphNode, Double> currentPathDist = new HashMap<>();
        HashMap<GraphNode, Double> heuristic = new HashMap<>();
        HashMap<GraphNode, GraphNode> parent = new HashMap<>();

        // Change comparator depending on search method
        Comparator<GraphNode> comparator;
        switch (method) {
            case SearchMethods.UCS:
                // Only use g(x) - path cost
                comparator = Comparator.comparingDouble(
                    currentPathDist::get
                );
                break;
            case SearchMethods.GREEDY:
                // Only use h(x) - heuristic
                comparator = Comparator.comparingDouble(
                    heuristic::get
                );
                break;
            case SearchMethods.A_STAR:
                // Use both g(x) & h(x)
                comparator = Comparator.comparingDouble(
                    (o) -> currentPathDist.get(o) + heuristic.get(o)
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid search method " + method);
        }
        PriorityQueue<GraphNode> queue = new PriorityQueue<>(comparator);

        // Add initial node to the queue
        currentPathDist.put(source, 0.0);
        queue.add(source);

        Set<GraphNode> explored = new HashSet<>();
        GraphNode goal = null;
        while (!queue.isEmpty()) {
            GraphNode current = queue.poll();
            animation.getChildren().addAll(
                current.makeCurrentTransition(),
                AnimationUtil.pause(PAUSE_DURATION)
            );
            // The goal has been reached! (Lowest priority in the queue)
            if (current.equals(goalNode)) {
                // If the goal does not have a proper cost, the goal is
                // unreachable
                if (currentPathDist.getOrDefault(current, Double.POSITIVE_INFINITY)
                    != Double.POSITIVE_INFINITY) {
                    goal = current;
                    animation.getChildren().addAll(
                        current.makeGoalTransition(),
                        AnimationUtil.pause(PAUSE_DURATION)
                    );
                }
                break;
            }
            // Add the current node to the explored list
            explored.add(current);
            // Loop through all the edges
            for (Edge edge : getEdges(current)) {
                animation.getChildren().addAll(
                    edge.getConnection().makeHighlightTransition(),
                    AnimationUtil.pause(PAUSE_DURATION)
                );
                GraphNode neighbor = edge.getEnding();
                // Only expand unexplored nodes
                if (!explored.contains(neighbor)) {
                    animation.getChildren().addAll(
                        neighbor.makeFringeTransition(),
                        AnimationUtil.pause(PAUSE_DURATION)
                    );
                    // Current cost + edge cost
                    double newDist = currentPathDist.getOrDefault(current,
                        Double.POSITIVE_INFINITY) + edge.getWeight();
                    // Previous lowest cost through the node
                    double currentDist = currentPathDist.getOrDefault(neighbor,
                        Double.POSITIVE_INFINITY);
                    // If a shorter distance is found...
                    if (newDist < currentDist) {
                        // Update the new shorter distance
                        currentPathDist.put(neighbor, newDist);
                        heuristic.put(neighbor, neighbor.distance(goalNode));
                        parent.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }
        // Reconstruct path
        List<GraphNode> pathNodes = new ArrayList<>();
        List<Edge> pathEdges = new ArrayList<>();
        double cost = currentPathDist.get(goalNode);
        GraphNode prev = null;
        while (goal != null) {
            pathNodes.add(0, goal);
            Edge edge = getEdge(goal, prev);
            if (edge != null) {
                pathEdges.add(edge);
            }
            prev = goal;
            goal = parent.get(goal);
        }
        animation.getChildren().add(
            AnimationUtil.path(pathNodes, pathEdges)
        );
        // Finally play the animations that were queued up
        animation.play();

        System.out.println("Cost: " + cost);
        System.out.println("Path: " + pathNodes);
        return new SearchResult(cost, pathNodes);
    }

    public void print() {
        for (var entry : adjList.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            System.out.println(entry.getValue());
        }
    }
}
