package pathfinding.model;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import pathfinding.ui.GraphConnection;
import pathfinding.ui.GraphNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GraphCanvas {

    private final Pane canvas;
    private Graph graph;

    public GraphCanvas(Pane pane) {
        canvas = pane;
        graph = new Graph();
    }

    /**
     * Returns the graph.
     *
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Adds the node to the graph.
     *
     * @param node node to add
     */
    public void addNode(GraphNode node) {
        canvas.getChildren().add(node);
        graph.addVertex(node);
    }

    /**
     * Removes the specified node from the canvas.
     *
     * @param node node to remove
     */
    public void deleteNode(GraphNode node) {
        graph.removeVertex(node);
        canvas.getChildren().remove(node);
        node.markDeleted();
    }

    /**
     * Adds a connection between the two graph nodes.
     *
     * @param node1 first node
     * @param node2 second node
     * @param weight weight of the connection
     */
    public void addConnection(GraphNode node1, GraphNode node2, DoubleProperty weight) {
        // Create a connection between the centers of the nodes
        GraphConnection connection = new GraphConnection(node1, node2, weight);
        canvas.getChildren().add(connection);
        graph.connectVertices(node1, node2, connection);

        // Bring the two nodes to be in front of the line
        node1.toFront();
        node2.toFront();
    }

    /**
     * Returns whether two nodes are connected or not
     *
     * @param node1 first node
     * @param node2 second node
     * @return connected or not
     */
    public boolean areConnected(GraphNode node1, GraphNode node2) {
        List<Edge> edges = graph.getEdges(node1);
        for (Edge edge : edges) {
            if (edge.getEnding().equals(node2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all the nodes & connections on the graph
     */
    public void clear() {
        canvas.getChildren().clear();
        graph = new Graph();
    }

    /**
     * Resets the colors of all nodes & connections
     */
    public void resetColors() {
        for (Node node : canvas.getChildren()) {
            if (node instanceof GraphNode) {
                ((GraphNode) node).resetColor();
            } else if (node instanceof GraphConnection) {
                ((GraphConnection) node).resetColor();
            }
        }
    }

    /**
     * Saves the graph to a file
     * @param file file to save to
     * @throws IOException error saving the file
     */
    public void saveToFile(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        HashMap<GraphNode, Integer> indexMapping = new HashMap<>();
        HashMap<Integer, StringBuilder> lineMapping = new HashMap<>();
        int vertexCount = 0;
        ObservableList<Node> nodes = canvas.getChildrenUnmodifiable();
        for (Node child : nodes) {
            if (child.getClass() == GraphNode.class) {
                GraphNode node = (GraphNode) child;
                indexMapping.put(node, vertexCount);
                ++vertexCount;
                writer.write(String.valueOf(node.getLayoutX()));
                writer.write(',');
                writer.write(String.valueOf(node.getLayoutY()));
                writer.write(',');
                writer.write(String.valueOf(node.getData()));
                writer.write('|');
            }
        }
        writer.newLine();
        for (Node child : nodes) {
            if (child.getClass() == GraphConnection.class) {
                GraphConnection connection = (GraphConnection) child;
                int node1 = indexMapping.get(connection.getNode1());
                int node2 = indexMapping.get(connection.getNode2());
                lineMapping.putIfAbsent(node1, new StringBuilder());
                StringBuilder b1 = lineMapping.get(node1);
                b1.append(connection.getWeight());
                b1.append(',');
                b1.append(node2);
                b1.append('|');
                lineMapping.putIfAbsent(node2, new StringBuilder());
                StringBuilder b2 = lineMapping.get(node2);
                b2.append(connection.getWeight());
                b2.append(',');
                b2.append(node1);
                b2.append('|');
            }
        }
        for (int i = 0; i < vertexCount; ++i) {
            writer.append(lineMapping.get(i).toString());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }
}
