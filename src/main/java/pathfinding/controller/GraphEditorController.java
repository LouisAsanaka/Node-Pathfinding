package pathfinding.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import pathfinding.Constants;
import pathfinding.MainApplication;
import pathfinding.model.Graph;
import pathfinding.model.GraphCanvas;
import pathfinding.ui.GraphNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class GraphEditorController {

    private enum InteractionMode {
        CREATE, EDIT, DELETE, VIEW, SEARCH
    }
    private static final double EDITOR_INSET = 20.0;

    private InteractionMode mode = InteractionMode.VIEW;

    private Stage stage;
    private FileChooser fileChooser;
    private GraphCanvas graphCanvas;

    private GraphNode currentlySelected = null;
    private Line guidingLine = null;

    private File currentFile = null;

    @FXML
    private Pane graphCanvasPane;

    @FXML
    private Label interactionMode;

    @FXML
    private Label costLabel;

    @FXML
    private void initialize() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Graph files (* " + Constants.EXT + ")",
                "*" + Constants.EXT
            )
        );
        graphCanvas = new GraphCanvas(graphCanvasPane);
        graphCanvasPane.setFocusTraversable(true);

        guidingLine = new Line();
        guidingLine.setStrokeWidth(2);
        guidingLine.setStroke(Color.web("#7f7f7f"));
        guidingLine.setStrokeLineCap(StrokeLineCap.BUTT);
        guidingLine.getStrokeDashArray().setAll(10.0, 5.0);
        guidingLine.setMouseTransparent(true);
        guidingLine.setVisible(false);
        graphCanvasPane.getChildren().add(guidingLine);
    }

    @FXML
    private void newFile() {
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            currentFile = file;
            stage.setTitle(currentFile.getAbsolutePath() + " - " + MainApplication.WINDOW_TITLE);
            clearGraph();
        }
    }

    @FXML
    private void openFile() {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                loadFromFile(file);
            } catch (IOException|NumberFormatException e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText("Failed to load file.");
                a.showAndWait();
                return;
            }
            currentFile = file;
            stage.setTitle(currentFile.getAbsolutePath() + " - " + MainApplication.WINDOW_TITLE);
        }
    }

    private void loadFromFile(File file) throws IOException, NumberFormatException {
        clearGraph();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        HashMap<Integer, GraphNode> indexMapping = new HashMap<>();
        String[] vertices = reader.readLine().split("\\|");
        int vertexCount = 0;
        for (String vertexData : vertices) {
            if (vertexData.isEmpty()) {
                continue;
            }
            String[] data = vertexData.split(",");
            GraphNode node = makeNode(
                Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                data[2]
            );
            graphCanvas.addNode(node);
            indexMapping.put(vertexCount, node);
            ++vertexCount;
        }
        for (int i = 0; i < vertexCount; ++i) {
            String[] connections = reader.readLine().split("\\|");
            for (String connectionData : connections) {
                String[] data = connectionData.split(",");
                if (connectionData.isEmpty()) {
                    continue;
                }
                double weight = Double.parseDouble(data[0]);
                int endingIndex = Integer.parseInt(data[1]);
                GraphNode node1 = indexMapping.get(i);
                GraphNode node2 = indexMapping.get(endingIndex);
                if (!graphCanvas.areConnected(node1, node2)) {
                    graphCanvas.addConnection(node1, node2,
                        new SimpleDoubleProperty(weight));
                }
            }
        }
        reader.close();
    }

    @FXML
    private void saveFile() {
        if (currentFile == null) {
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                currentFile = file;
            }
        }
        try {
            graphCanvas.saveToFile(currentFile);
            stage.setTitle(currentFile.getAbsolutePath() + " - " + MainApplication.WINDOW_TITLE);
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Failed to save file.");
            a.showAndWait();
        }
    }

    @FXML
    private void clearGraph() {
        graphCanvas.clear();
        graphCanvasPane.getChildren().add(guidingLine);
        currentlySelected = null;
    }

    @FXML
    private void openHelp() {
        Alert helpDialog = new Alert(Alert.AlertType.INFORMATION);
        helpDialog.setHeaderText("Help");
        helpDialog.setContentText(
            "Switch between multiple modes by pressing the keys:\n" +
            "- V (View): View the graph\n" +
            "- C (Create): Create nodes & connections\n" +
            "- E (Edit): Edit nodes & connections\n" +
            "- D (Delete): Delete nodes\n" +
            "- S (Search): Search the graph by choosing a start & end node\n"
        );
        helpDialog.showAndWait();
    }

    @FXML
    private void changeMode(KeyEvent event) {
        KeyCode key = event.getCode();
        switch (key) {
            case C: // Create mode
                mode = InteractionMode.CREATE;
                interactionMode.setText("Create Mode");
                currentlySelected = null;
                graphCanvas.resetColors();
                costLabel.setVisible(false);
                break;
            case E: // Edit mode
                mode = InteractionMode.EDIT;
                interactionMode.setText("Edit Mode");
                currentlySelected = null;
                graphCanvas.resetColors();
                costLabel.setVisible(false);
                break;
            case D: // Delete mode
                mode = InteractionMode.DELETE;
                interactionMode.setText("Delete Mode");
                currentlySelected = null;
                graphCanvas.resetColors();
                costLabel.setVisible(false);
                break;
            case V: // View mode
                mode = InteractionMode.VIEW;
                interactionMode.setText("View Mode");
                currentlySelected = null;
                graphCanvas.resetColors();
                costLabel.setVisible(false);
                break;
            case S: // Search mode
                mode = InteractionMode.SEARCH;
                interactionMode.setText("Search Mode");
                currentlySelected = null;
                graphCanvas.resetColors();
                costLabel.setVisible(true);
                costLabel.setText("Cost: ");
                break;
            default:
                break;
        }
    }

    @FXML
    private void primaryButton(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        switch (mode) {
            case CREATE:
                if (currentlySelected != null) {
                    currentlySelected = null;
                    guidingLine.setVisible(false);
                    break;
                }
                TextInputDialog dialog = new TextInputDialog();
                dialog.setHeaderText("Name of the node: ");
                Optional<String> result = dialog.showAndWait();
                if (result.isEmpty()) {
                    break;
                }
                graphCanvas.addNode(
                    makeNode(
                        ((Pane) event.getSource()).sceneToLocal(event.getSceneX(), event.getSceneY()),
                        result.get()
                    )
                );
                break;
            case SEARCH:
                if (currentlySelected != null) {
                    currentlySelected = null;
                }
                break;
            default:
                break;
        }
    }

    @FXML
    private void mouseMove(MouseEvent event) {
        if (guidingLine.isVisible()) {
            Point2D point = guidingLine.getParent().sceneToLocal(
                event.getSceneX(), event.getSceneY()
            );
            guidingLine.setEndX(point.getX());
            guidingLine.setEndY(point.getY());
        }
    }

    private GraphNode makeNode(Point2D p, String s) {
        return makeNode(p.getX(), p.getY(), s);
    }

    private GraphNode makeNode(double x, double y, String s) {
        GraphNode node = new GraphNode(
            x, y, new SimpleStringProperty(s)
        );
        // Register events after the node is rendered
        Platform.runLater(
            () -> registerEvents(node)
        );
        return node;
    }

    private static class Delta {
        double x, y;
    }

    private void registerEvents(GraphNode node) {
        final Delta dragDelta = new Delta();
        node.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = node.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = node.getLayoutY() - mouseEvent.getSceneY();
            getScene().setCursor(Cursor.CLOSED_HAND);
            node.toFront();
        });
        node.setOnMouseDragged(mouseEvent -> {
            final Bounds bounds = node.getParent().getBoundsInLocal();
            final double boundsWidth = bounds.getWidth();
            final double boundsHeight = bounds.getHeight();

            double x = mouseEvent.getSceneX() + dragDelta.x;
            double y = mouseEvent.getSceneY() + dragDelta.y;
            if (x >= EDITOR_INSET && x < boundsWidth - node.getLayoutBounds().getWidth() - EDITOR_INSET) {
                node.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
            } else if (x < EDITOR_INSET) {
                node.setLayoutX(EDITOR_INSET);
            } else {
                node.setLayoutX(boundsWidth - node.getLayoutBounds().getWidth() - EDITOR_INSET);
            }
            if (y >= EDITOR_INSET && y < boundsHeight - node.getLayoutBounds().getHeight() - EDITOR_INSET) {
                node.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            } else if (y < EDITOR_INSET) {
                node.setLayoutY(EDITOR_INSET);
            } else {
                node.setLayoutY(boundsHeight - node.getLayoutBounds().getHeight() - EDITOR_INSET);
            }
        });
        node.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.isStillSincePress()) {
                switch (mode) {
                    case DELETE:
                        graphCanvas.deleteNode(node);
                        getScene().setCursor(Cursor.DEFAULT);
                        break;
                    case CREATE:
                        if (currentlySelected == null) {
                            currentlySelected = node;
                            Point2D point = guidingLine.getParent().sceneToLocal(
                                mouseEvent.getSceneX(), mouseEvent.getSceneY()
                            );
                            guidingLine.setStartX(node.getLayoutX() + node.getWidth() / 2);
                            guidingLine.setStartY(node.getLayoutY() + node.getHeight() / 2);
                            guidingLine.setEndX(point.getX());
                            guidingLine.setEndY(point.getY());
                            guidingLine.setVisible(true);
                        } else {
                            if (currentlySelected != node &&
                                !graphCanvas.areConnected(currentlySelected, node)) {
                                graphCanvas.addConnection(currentlySelected, node,
                                    new SimpleDoubleProperty(100));
                            }
                            currentlySelected = null;
                            guidingLine.setVisible(false);
                        }
                        break;
                    case EDIT:
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setHeaderText("New name: ");
                        Optional<String> result = dialog.showAndWait();
                        if (result.isEmpty()) {
                            return;
                        }
                        node.setData(result.get());
                        break;
                    case SEARCH:
                        if (currentlySelected == null) {
                            currentlySelected = node;
                        } else {
                            ChoiceDialog<String> search = new ChoiceDialog<>();
                            search.getItems().addAll(
                                Graph.SearchMethods.UCS,
                                Graph.SearchMethods.GREEDY,
                                Graph.SearchMethods.A_STAR
                            );
                            search.setHeaderText("Search Method:");
                            search.setSelectedItem(Graph.SearchMethods.UCS);
                            Optional<String> method = search.showAndWait();
                            method.ifPresentOrElse(name -> {
                                graphCanvas.resetColors();

                                Graph.SearchResult searchResult;
                                searchResult = graphCanvas.getGraph().search(
                                    currentlySelected, node, name);
                                costLabel.setText("Cost: " + searchResult.cost);
                                currentlySelected = null;
//                                if (searchResult != null) {
//                                    Alert resultDialog = new Alert(Alert.AlertType.INFORMATION);
//                                    resultDialog.setHeaderText("Search Result");
//                                    resultDialog.setContentText(
//                                        "Cost: " + searchResult.cost + "\n" +
//                                        "Path: " + searchResult.path
//                                    );
//                                    resultDialog.showAndWait();
//                                }
                            }, () -> currentlySelected = null);
                        }
                        break;
                    default:
                        break;
                }
            }
            mouseEvent.consume();
        });
        node.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }
        });
        node.setOnMouseReleased(
            mouseEvent -> getScene().setCursor(Cursor.HAND)
        );
        node.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private Scene getScene() {
        return stage.getScene();
    }
}
