package pl.edu.pw.mini.symulacje;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainPanel extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new Pane();

        final double frameDuration = 16;

        Timeline timeline = new Timeline();

        Simulation springSimulation = new Simulation();

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(frameDuration), springSimulation));
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.play();

        primaryStage.show();
    }
}
