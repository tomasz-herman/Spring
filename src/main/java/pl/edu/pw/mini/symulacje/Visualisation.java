package pl.edu.pw.mini.symulacje;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class Visualisation {
    public static final int VISUALISATION_SCALE = 25;
    public static final int RADIUS = 40;

    private final DoubleProperty center = new SimpleDoubleProperty();
    private final Circle circle;

    public Visualisation(Pane pane) {
        circle = new Circle(RADIUS);
        circle.setFill(Color.CRIMSON);
        circle.setStrokeWidth(3);
        circle.setStroke(Color.BLACK);
        Line line = new Line();
        line.setFill(Color.SADDLEBROWN);
        line.setStrokeWidth(3);
        Rectangle rectangle = new Rectangle(1, 1, Color.SKYBLUE);
        rectangle.heightProperty().bind(pane.heightProperty());
        rectangle.widthProperty().bind(pane.widthProperty());
        pane.getChildren().addAll(rectangle, line, circle);
        line.startXProperty().bind(pane.widthProperty().divide(2));
        line.endXProperty().bind(pane.widthProperty().divide(2));
        circle.centerXProperty().bind(pane.widthProperty().divide(2));
        line.endYProperty().bind(circle.centerYProperty());
        center.bind(pane.heightProperty().divide(2));
        Rectangle clip = new Rectangle(1, 1, Color.SKYBLUE);
        clip.heightProperty().bind(pane.heightProperty());
        clip.widthProperty().bind(pane.widthProperty());
        pane.setClip(clip);
    }

    public void update(double x) {
        circle.setCenterY(center.getValue() + x * VISUALISATION_SCALE);
    }
}
