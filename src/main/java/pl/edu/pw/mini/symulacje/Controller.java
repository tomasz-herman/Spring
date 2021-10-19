package pl.edu.pw.mini.symulacje;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;

public class Controller {

    @FXML private ScatterChart<Number, Number> trajectoryChart;
    @FXML private LineChart<Number, Number> kinematicsChart;
    @FXML private LineChart<Number, Number> forcesChart;

    @FXML private Label fValue;
    @FXML private Label gValue;
    @FXML private Label hValue;
    @FXML private Label tValue;
    @FXML private Label wValue;
    @FXML private Label xValue;
    @FXML private Label xtValue;
    @FXML private Label xttValue;
    @FXML private TextField x0Value;
    @FXML private TextField v0Value;
    @FXML private TextField cValue;
    @FXML private TextField dtValue;
    @FXML private TextField kValue;
    @FXML private TextField mValue;
    @FXML private TextArea wFunValue;
    @FXML private TextArea hFunValue;
    @FXML private Pane visualisationPane;

    private XYChart.Series<Number, Number> xSeries;
    private XYChart.Series<Number, Number> xtSeries;
    private XYChart.Series<Number, Number> xttSeries;

    private XYChart.Series<Number, Number> fSeries;
    private XYChart.Series<Number, Number> gSeries;
    private XYChart.Series<Number, Number> hSeries;

    private XYChart.Series<Number, Number> trajectorySeries;

    @FXML private void initialize() {
        xSeries = new XYChart.Series<>("x", FXCollections.observableList(new LinkedList<>()));
        xtSeries = new XYChart.Series<>("xt", FXCollections.observableList(new LinkedList<>()));
        xttSeries = new XYChart.Series<>("xtt", FXCollections.observableList(new LinkedList<>()));
        kinematicsChart.getData().addAll(List.of(xSeries, xtSeries, xttSeries));

        fSeries = new XYChart.Series<>("f", FXCollections.observableList(new LinkedList<>()));
        gSeries = new XYChart.Series<>("g", FXCollections.observableList(new LinkedList<>()));
        hSeries = new XYChart.Series<>("h", FXCollections.observableList(new LinkedList<>()));
        forcesChart.getData().addAll(List.of(fSeries, gSeries, hSeries));

        trajectorySeries = new XYChart.Series<>();
        trajectoryChart.getData().add(trajectorySeries);
    }

    @FXML private void onStart(ActionEvent event) {
        final double frameDuration = 16;

        Timeline timeline = new Timeline();

        Visualisation visualisation = new Visualisation(visualisationPane);

        Simulation springSimulation = new Simulation(visualisation::update, this::update, 6, 0, 0.0167, 1, 0.1, 5, "0", "0");

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(frameDuration), springSimulation));
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.play();
    }

    @FXML private void onStop(ActionEvent event) {

    }

    private void update(double x, double v, double a, double t, double w, double f, double g, double h) {
        xSeries.getData().add(new XYChart.Data<>(t, x));
        if(xSeries.getData().size() > 1000) xSeries.getData().remove(0);
        xtSeries.getData().add(new XYChart.Data<>(t, v));
        if(xtSeries.getData().size() > 1000) xtSeries.getData().remove(0);
        xttSeries.getData().add(new XYChart.Data<>(t, a));
        if(xttSeries.getData().size() > 1000) xttSeries.getData().remove(0);

        fSeries.getData().add(new XYChart.Data<>(t, f));
        if(fSeries.getData().size() > 1000) fSeries.getData().remove(0);
        gSeries.getData().add(new XYChart.Data<>(t, g));
        if(gSeries.getData().size() > 1000) gSeries.getData().remove(0);
        hSeries.getData().add(new XYChart.Data<>(t, h));
        if(hSeries.getData().size() > 1000) hSeries.getData().remove(0);

        trajectorySeries.getData().add(new XYChart.Data<>(x, v));
    }
}
