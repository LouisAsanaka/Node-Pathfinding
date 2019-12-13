package pathfinding;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import pathfinding.controller.GraphEditorController;

import java.io.IOException;

public class MainApplication extends Application {

    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 600;
    public static final String WINDOW_TITLE = "Map Pathfinding";

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/scene.fxml")
        );
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load FXML file! Aborting...");
            System.exit(1);
            return;
        }
        GraphEditorController controller = loader.getController();
        controller.setStage(primaryStage);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Untitled - " + WINDOW_TITLE);
        primaryStage.getIcons().add(new Image("/images/icon.png"));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
