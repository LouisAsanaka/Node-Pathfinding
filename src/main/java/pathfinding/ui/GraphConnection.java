package pathfinding.ui;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Optional;

public class GraphConnection extends Region {

    private class DeleteListener implements ChangeListener<Boolean> {
        @Override
        public void changed(ObservableValue<? extends Boolean> observableValue,
                            Boolean old, Boolean current) {
            if (current) {
                Pane parent = (Pane) getParent();
                parent.getChildren().remove(GraphConnection.this);
                GraphConnection.this.node1.deleted.removeListener(this);
                GraphConnection.this.node2.deleted.removeListener(this);
            }
        }
    }

    public static final Color TEXT_FILL = Color.web("#bcbcbc");
    public static final Color LINE_COLOR = Color.web("#7f7f7f");

    private final Line line;
    private final Label weightLabel;
    private final GraphNode node1, node2;
    private final DoubleProperty weight;

    public GraphConnection(GraphNode node1, GraphNode node2, DoubleProperty weight) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;

        DeleteListener listener = new DeleteListener();
        node1.deleted.addListener(listener);
        node2.deleted.addListener(listener);

        line = new Line();
        line.startXProperty().bind(
            node1.layoutXProperty().add(node1.widthProperty().divide(2)));
        line.startYProperty().bind(
            node1.layoutYProperty().add(node1.heightProperty().divide(2)));
        line.endXProperty().bind(
            node2.layoutXProperty().add(node2.widthProperty().divide(2)));
        line.endYProperty().bind(
            node2.layoutYProperty().add(node2.heightProperty().divide(2)));
        line.setStrokeWidth(2);
        resetColor();
        line.setStrokeLineCap(StrokeLineCap.BUTT);
        line.getStrokeDashArray().setAll(10.0, 5.0);
        line.setMouseTransparent(true);

        weightLabel = new Label();
        weightLabel.textProperty().bind(weight.asString());
        weightLabel.setFont(Font.font("Segoe UI", 18));
        weightLabel.setTextFill(TEXT_FILL);
        weightLabel.layoutXProperty().bind(
            line.startXProperty().add(line.endXProperty()).divide(2).subtract(
                weightLabel.widthProperty().divide(2))
        );
        weightLabel.layoutYProperty().bind(
            line.startYProperty().add(line.endYProperty()).divide(2).subtract(
                weightLabel.heightProperty().divide(2))
        );
        weightLabel.setOnMouseClicked(mouseEvent -> {
            mouseEvent.consume();

            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("New weight: ");
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }
            try {
                double newWeight = Double.parseDouble(result.get());
                this.weight.set(newWeight);
            } catch (NumberFormatException e) {
                System.out.println("Unknown format.");
            }
        });
        // Fix the issue of the region blocking other bounds
        setPickOnBounds(false);
        getChildren().addAll(line, weightLabel);
    }

    public GraphNode getNode1() {
        return node1;
    }

    public GraphNode getNode2() {
        return node2;
    }

    public double getWeight() {
        return weight.get();
    }

    public DoubleProperty getWeightProperty() {
        return weight;
    }

    public void resetColor() {
        setLineColor(LINE_COLOR);
    }

    public void setLineColor(Color color) {
        line.setStroke(color);
    }

    public Transition makeHighlightTransition() {
        return makeColorTransition(Color.RED);
    }

    public Transition makePathTransition() {
        return makeColorTransition(Color.ORANGE);
    }

    private Transition makeColorTransition(Color target) {
        return new Transition() {
            {
                setCycleDuration(Duration.millis(600));
                setInterpolator(Interpolator.EASE_OUT);
            }

            @Override
            protected void interpolate(double frac) {
                setLineColor(
                    LINE_COLOR.interpolate(target, frac)
                );
            }
        };
    }
}
