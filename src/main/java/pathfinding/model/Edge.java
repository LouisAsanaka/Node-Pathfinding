package pathfinding.model;

import javafx.beans.property.DoubleProperty;
import pathfinding.ui.GraphConnection;
import pathfinding.ui.GraphNode;

public class Edge {

    private final GraphNode ending;
    private final DoubleProperty weight;
    private final GraphConnection connection;

    public Edge(GraphNode ending, DoubleProperty weight) {
        this.ending = ending;
        this.weight = weight;
        this.connection = null;
    }

    public Edge(GraphNode ending, GraphConnection connection) {
        this.ending = ending;
        this.weight = connection.getWeightProperty();
        this.connection = connection;
    }

    public GraphNode getEnding() {
        return ending;
    }

    public double getWeight() {
        return weight.get();
    }

    public DoubleProperty getWeightProperty() {
        return weight;
    }

    public GraphConnection getConnection() {
        return connection;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != Edge.class) {
            return false;
        }
        Edge other = (Edge) obj;
        return this.getWeight() == other.getWeight() && this.ending.equals(other.ending);
    }

    @Override
    public String toString() {
        return "Edge[ending=" + ending + ", weight=" + weight + "]";
    }
}
