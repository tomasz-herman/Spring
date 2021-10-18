package pl.edu.pw.mini.symulacje;

import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class Visualisation {
    private final Scene scene;
    private final Circle circle;

    public Visualisation() {
        circle = new Circle(40);
        circle.setFill(Color.CRIMSON);
        circle.setStrokeWidth(3);
        circle.setStroke(Color.BLACK);
        Line line = new Line();
        line.setFill(Color.SADDLEBROWN);
        line.setStrokeWidth(3);
        Group group = new Group();
        group.getChildren().addAll(line, circle);
        scene = new Scene(group, 800, 800, Color.SKYBLUE);
        line.startYProperty().bind(scene.heightProperty().divide(-2));
        line.endYProperty().bind(circle.centerYProperty());
        group.translateXProperty().bind(scene.widthProperty().divide(2));
        group.translateYProperty().bind(scene.heightProperty().divide(2));
    }

    public Scene getScene() {
        return scene;
    }

    public void update(double x) {
        circle.setCenterY(x * 25);
    }
}
