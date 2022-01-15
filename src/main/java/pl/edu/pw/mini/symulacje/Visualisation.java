package pl.edu.pw.mini.symulacje;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import static java.lang.Math.*;

public class Visualisation {
    public final DoubleProperty VISUALISATION_SCALE = new SimpleDoubleProperty(25);
    private final Circle Circle = new Circle();
    private final Line UpperArm = new Line();
    private final Line Forearm = new Line();
    private final Rectangle Hand = new Rectangle();
    private final DoubleProperty xCenter = new SimpleDoubleProperty();
    private final DoubleProperty yCenter = new SimpleDoubleProperty();

    public Visualisation(Pane pane) {
        xCenter.bind(pane.heightProperty().divide(2));
        yCenter.bind(pane.heightProperty().divide(2));

        Circle.setStrokeWidth(1);
        Circle.setStroke(Color.BLACK);
        Circle.setFill(Color.TRANSPARENT);
        Circle.centerXProperty().bind(xCenter.divide(2));
        Circle.centerYProperty().bind(yCenter);

        UpperArm.startXProperty().bind(xCenter.divide(2));
        UpperArm.startYProperty().bind(yCenter);

        Forearm.endYProperty().bind(yCenter);

        Hand.setWidth(40);
        Hand.setHeight(20);
        Hand.setFill(Color.TRANSPARENT);
        Hand.setStrokeWidth(1);
        Hand.setStroke(Color.BLACK);
        Hand.yProperty().bind(yCenter.subtract(Hand.heightProperty().divide(2)));

        Line wLine = new Line();
        wLine.setFill(Color.RED);
        wLine.getStrokeDashArray().addAll(25d, 20d, 5d, 20d);
        wLine.endXProperty().bind(pane.widthProperty());
        wLine.startYProperty().bind(yCenter);
        wLine.endYProperty().bind(yCenter);

        Rectangle rectangle = new Rectangle(1, 1, Color.SKYBLUE);
        rectangle.heightProperty().bind(pane.heightProperty());
        rectangle.widthProperty().bind(pane.widthProperty());
        pane.getChildren().addAll(Circle, UpperArm, Forearm, Hand, wLine);

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
    }

    public void update(State state) {
        Circle.setRadius(state.R);
        UpperArm.setEndX(UpperArm.getStartX() + state.x1());
        UpperArm.setEndY(UpperArm.getStartY() + state.y1());
        Forearm.setStartX(UpperArm.getEndX());
        Forearm.setStartY(UpperArm.getEndY());
        Forearm.setEndX(UpperArm.getStartX() + state.x2());
        Hand.setX(Forearm.getEndX());
    }

    public record State (double a, double R, double L) {
        public double x1() {
            return R * cos(a);
        }

        public double y1() {
            return R * sin(a);
        }

        public double x2() {
            return x1() + sqrt(L * L - pow(R * sin(a), 2));
        }
    }
}
