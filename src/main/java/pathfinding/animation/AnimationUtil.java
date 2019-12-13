package pathfinding.animation;

import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import pathfinding.model.Edge;
import pathfinding.ui.GraphNode;

import java.util.List;

public class AnimationUtil {

    public static PauseTransition pause(Duration pauseTime) {
        return new PauseTransition(pauseTime);
    }

    public static ParallelTransition path(List<GraphNode> nodes, List<Edge> edges) {
        ParallelTransition transition = new ParallelTransition();
        for (GraphNode node : nodes) {
            transition.getChildren().add(node.makePathTransition());
        }
        for (Edge edge : edges) {
            transition.getChildren().add(edge.getConnection().makePathTransition());
        }
        return transition;
    }
}
