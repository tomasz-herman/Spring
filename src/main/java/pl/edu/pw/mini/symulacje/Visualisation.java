package pl.edu.pw.mini.symulacje;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class Visualisation {
    public final DoubleProperty VISUALISATION_SCALE = new SimpleDoubleProperty(25);

    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty w = new SimpleDoubleProperty();

    public Visualisation(Pane pane) {
        Circle circle = new Circle();
        circle.radiusProperty().bind(VISUALISATION_SCALE);
        circle.setFill(Color.CRIMSON);
        circle.setStrokeWidth(3);
        circle.setStroke(Color.BLACK);
        Line line = new Line();
        line.setFill(Color.SADDLEBROWN);
        line.setStrokeWidth(3);
        Line wLine = new Line();
        wLine.setFill(Color.RED);
        wLine.getStrokeDashArray().addAll(25d, 20d, 5d, 20d);
        wLine.endXProperty().bind(pane.widthProperty());
        Rectangle rectangle = new Rectangle(1, 1, Color.SKYBLUE);
        rectangle.heightProperty().bind(pane.heightProperty());
        rectangle.widthProperty().bind(pane.widthProperty());
        pane.getChildren().addAll(rectangle, wLine, line, circle);
        line.startXProperty().bind(pane.widthProperty().divide(2));
        line.endXProperty().bind(pane.widthProperty().divide(2));
        circle.centerXProperty().bind(pane.widthProperty().divide(2));
        line.endYProperty().bind(circle.centerYProperty());
        DoubleProperty center = new SimpleDoubleProperty();
        center.bind(pane.heightProperty().divide(2));
        Rectangle clip = new Rectangle(1, 1, Color.SKYBLUE);
        clip.heightProperty().bind(pane.heightProperty());
        clip.widthProperty().bind(pane.widthProperty());
        pane.setClip(clip);
        pane.setOnScroll(event -> {
            double scale = VISUALISATION_SCALE.get();
            double scroll = event.getDeltaY() / event.getMultiplierY();
            double multiplier = 1 + scroll / 10;
            VISUALISATION_SCALE.set(scale * multiplier);
        });

        circle.centerYProperty().bind(x.multiply(VISUALISATION_SCALE).add(center));
        wLine.startYProperty().bind(w.multiply(VISUALISATION_SCALE).add(center));
        wLine.endYProperty().bind(w.multiply(VISUALISATION_SCALE).add(center));
    }

    public void update(double x, double w) {
        this.x.set(x);
        this.w.set(w);
    }
}
