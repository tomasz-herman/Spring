package pl.edu.pw.mini.symulacje;

import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class Visualisation {
    private final Circle circle;

    public Visualisation(Pane pane) {
        circle = new Circle(40);
        circle.setFill(Color.CRIMSON);
        circle.setStrokeWidth(3);
        circle.setStroke(Color.BLACK);
        Line line = new Line();
        line.setFill(Color.SADDLEBROWN);
        line.setStrokeWidth(3);
        Rectangle rectangle = new Rectangle(1, 1, Color.SKYBLUE);
        rectangle.heightProperty().bind(pane.heightProperty());
        rectangle.widthProperty().bind(pane.widthProperty());
        rectangle.xProperty().bind(pane.widthProperty().divide(-2));
        rectangle.yProperty().bind(pane.heightProperty().divide(-2));
        Group group = new Group(rectangle, line, circle);
        pane.getChildren().add(group);
        line.startYProperty().bind(pane.heightProperty().divide(-2));
        line.endYProperty().bind(circle.centerYProperty());
        group.translateXProperty().bind(pane.widthProperty().divide(2));
        group.translateYProperty().bind(pane.heightProperty().divide(2));
    }

    public void update(double x) {
        circle.setCenterY(x * 25);
    }
}
