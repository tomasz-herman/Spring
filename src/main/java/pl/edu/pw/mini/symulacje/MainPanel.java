package pl.edu.pw.mini.symulacje;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainPanel extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final double frameDuration = 16;

        Timeline timeline = new Timeline();

        Visualisation visualisation = new Visualisation();
        primaryStage.setScene(visualisation.getScene());

        Simulation springSimulation = new Simulation(visualisation::update, 10, 0, 0.0167, 1, 0.1, 5, "0", "0");

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(frameDuration), springSimulation));
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.play();

        primaryStage.show();
    }
}
