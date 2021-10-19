package pl.edu.pw.mini.symulacje;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

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
    @FXML private TextArea wFun;
    @FXML private TextArea hFun;
    @FXML private Pane visualisationPane;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    private XYChart.Series<Number, Number> xSeries;
    private XYChart.Series<Number, Number> xtSeries;
    private XYChart.Series<Number, Number> xttSeries;

    private XYChart.Series<Number, Number> fSeries;
    private XYChart.Series<Number, Number> gSeries;
    private XYChart.Series<Number, Number> hSeries;

    private XYChart.Series<Number, Number> trajectorySeries;

    private final Timeline timeline = new Timeline();

    @FXML private void initialize() {
        timeline.setCycleCount(Timeline.INDEFINITE);

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

        startButton.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        x0Value.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        v0Value.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        cValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        dtValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        kValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        mValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        wFun.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        hFun.disableProperty().bind(timeline.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        stopButton.disableProperty().bind(timeline.statusProperty().isEqualTo(Animation.Status.STOPPED));
        pauseButton.disableProperty().bind(timeline.statusProperty().isEqualTo(Animation.Status.STOPPED));

        timeline.statusProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == Animation.Status.PAUSED) pauseButton.setText("Resume");
            else pauseButton.setText("Pause");
        });
    }

    @FXML private void onStart(ActionEvent event) {
        final double frameDuration = parseTextField(dtValue, v -> v > 0);

        Visualisation visualisation = new Visualisation(visualisationPane);

        Simulation springSimulation = new Simulation(
                visualisation::update, this::update,
                parseTextField(x0Value, null),
                parseTextField(v0Value, null),
                parseTextField(dtValue, v -> v > 0),
                parseTextField(mValue, v -> v > 0),
                parseTextField(kValue, v -> v >= 0),
                parseTextField(cValue, v -> v >= 0),
                wFun.getText(), hFun.getText());

        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(frameDuration), springSimulation));
        timeline.play();
    }

    @FXML private void onPause(ActionEvent event) {
        if (timeline.getStatus() == Animation.Status.PAUSED) {
            timeline.play();
        } else if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.pause();
        }
    }

    @FXML private void onStop(ActionEvent event) {
        timeline.stop();
        xSeries.getData().clear();
        xtSeries.getData().clear();
        xttSeries.getData().clear();
        fSeries.getData().clear();
        gSeries.getData().clear();
        hSeries.getData().clear();
        trajectorySeries.getData().clear();
        visualisationPane.getChildren().clear();
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
        if(fSeries.getData().size() > 1000000) trajectorySeries.getData().remove(0);

        tValue.setText(String.format("%.2f", t));
        xValue.setText(String.format("%.2f", x));
        xtValue.setText(String.format("%.2f", v));
        xttValue.setText(String.format("%.2f", a));
        fValue.setText(String.format("%.2f", f));
        gValue.setText(String.format("%.2f", g));
        hValue.setText(String.format("%.2f", h));
        wValue.setText(String.format("%.2f", w));
    }

    private double parseTextField(TextInputControl input, Function<Double, Boolean> validation) throws IllegalArgumentException {
        if (input == null || input.getText() == null) throw new IllegalArgumentException("Bad value");
        try {
            double number = Double.parseDouble(input.getText());
            if(validation == null || validation.apply(number)) {
                return number;
            } else {
                throw new IllegalArgumentException("Bad input");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
