package pathfinding.ui;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class GraphNode extends Region {

    private static final Color RECT_FILL = Color.web("#383838");
    private static final Color TEXT_FILL = Color.web("#bcbcbc");

    private final StringProperty data;
    private final Label label;

    public final BooleanProperty deleted = new SimpleBooleanProperty(false);

    public GraphNode(Point2D point, StringProperty s) {
        this(point.getX(), point.getY(), s);
    }

    public GraphNode(double x, double y, StringProperty s) {
        super();
        setLayoutX(x);
        setLayoutY(y);
        data = s;
        label = new Label();
        label.textProperty().bind(data);
        resetColor();
        label.setStyle(
            "-fx-padding: 10px;"
        );
        label.setFont(Font.font("Segoe UI", 20));
        getChildren().add(label);
    }

    public void markDeleted() {
        deleted.set(true);
    }

    public String getData() {
        return data.get();
    }

    public void setData(String s) {
        data.set(s);
    }

    public void resetColor() {
        setColor(RECT_FILL, TEXT_FILL);
    }

    public void setColor(Color background, Color text) {
        label.setBackground(new Background(
            new BackgroundFill(
                background, new CornerRadii(12), new Insets(0, 1, 0, 1)
            )
        ));
        label.setTextFill(text);
    }

    private Transition makeColorTransition(Color background, Color text) {
        return new Transition() {
            {
                setCycleDuration(Duration.millis(600));
                setInterpolator(Interpolator.EASE_OUT);
            }

            @Override
            protected void interpolate(double frac) {
                setColor(
                    RECT_FILL.interpolate(background, frac),
                    TEXT_FILL.interpolate(text, frac)
                );
            }
        };
    }

    // Helper methods
    public Transition makeCurrentTransition() {
        return makeColorTransition(Color.LIMEGREEN, Color.BLACK);
    }

    public Transition makeFringeTransition() {
        return makeColorTransition(Color.RED, Color.WHITE);
    }

    public Transition makeGoalTransition() {
        return makeColorTransition(Color.GREEN, Color.WHITE);
    }

    public Transition makePathTransition() {
        return makeColorTransition(Color.ORANGE, Color.WHITE);
    }

    public double distance(GraphNode node) {
        return Math.sqrt(Math.pow(this.getLayoutX() - node.getLayoutX(), 2) +
            Math.pow(this.getLayoutY() - node.getLayoutY(), 2));
    }

    @Override
    public String toString() {
        return getData();
    }
}
