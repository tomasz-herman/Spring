package pl.edu.pw.mini.symulacje;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static javafx.animation.Animation.Status.PAUSED;
import static javafx.animation.Animation.Status.STOPPED;
import static javafx.scene.control.Alert.AlertType.ERROR;

public class Controller {
    private static final int MAX_LINE_CHART_DATA = 1_000;
    private static final int MAX_SCATTER_CHART_DATA = 1_000_000;
    private static final String FLOAT_FORMAT = "%.2f";
    private static final String CANNOT_START_SIMULATION = "Couldn't start simulation";
    private static final String RESUME_BTN_TEXT = "Resume";
    private static final String PAUSE_BTN_TEXT = "Pause";

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

        startButton.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        x0Value.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        v0Value.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        cValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        dtValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        kValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        mValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        wFun.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        hFun.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        stopButton.disableProperty().bind(timeline.statusProperty().isEqualTo(STOPPED));
        pauseButton.disableProperty().bind(timeline.statusProperty().isEqualTo(STOPPED));

        timeline.statusProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == PAUSED) pauseButton.setText(RESUME_BTN_TEXT);
            else pauseButton.setText(PAUSE_BTN_TEXT);
        });
    }

    @FXML private void onStart(ActionEvent event) {
        event.consume();
        try {
            final double x0 = parseTextField(x0Value, null);
            final double v0 = parseTextField(v0Value, null);
            final double dt = parseTextField(dtValue, v -> v > 0);
            final double m = parseTextField(mValue, v -> v > 0);
            final double k = parseTextField(kValue, v -> v >= 0);
            final double c = parseTextField(cValue, v -> v >= 0);

            Visualisation visualisation = new Visualisation(visualisationPane);

            Simulation springSimulation = new Simulation(
                    visualisation::update, this::update,
                    x0, v0, dt, m, k, c,
                    wFun.getText(), hFun.getText());

            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(dt), springSimulation));
            timeline.play();
        } catch (IllegalArgumentException e) {
            Alert errorAlert = new Alert(ERROR);
            errorAlert.setHeaderText(CANNOT_START_SIMULATION);
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }

    @FXML private void onPause(ActionEvent event) {
        event.consume();
        if (timeline.getStatus() == PAUSED) {
            timeline.play();
        } else if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.pause();
        }
    }

    @FXML private void onStop(ActionEvent event) {
        event.consume();
        timeline.stop();
        xSeries.getData().clear();
        xtSeries.getData().clear();
        xttSeries.getData().clear();
        kinematicsChart.getData().clear();
        kinematicsChart.getData().addAll(List.of(xSeries, xtSeries, xttSeries));
        fSeries.getData().clear();
        gSeries.getData().clear();
        hSeries.getData().clear();
        forcesChart.getData().clear();
        forcesChart.getData().addAll(List.of(fSeries, gSeries, hSeries));
        trajectorySeries.getData().clear();
        visualisationPane.getChildren().clear();
    }

    private void update(double x, double v, double a, double t, double w, double f, double g, double h) {
        updateLineChart(x, v, a, t, xSeries, xtSeries, xttSeries);
        updateLineChart(f, g, h, t, fSeries, gSeries, hSeries);

        trajectorySeries.getData().add(new XYChart.Data<>(x, v));
        if(fSeries.getData().size() > MAX_SCATTER_CHART_DATA) trajectorySeries.getData().remove(0);

        tValue.setText(String.format(FLOAT_FORMAT, t));
        xValue.setText(String.format(FLOAT_FORMAT, x));
        xtValue.setText(String.format(FLOAT_FORMAT, v));
        xttValue.setText(String.format(FLOAT_FORMAT, a));
        fValue.setText(String.format(FLOAT_FORMAT, f));
        gValue.setText(String.format(FLOAT_FORMAT, g));
        hValue.setText(String.format(FLOAT_FORMAT, h));
        wValue.setText(String.format(FLOAT_FORMAT, w));
    }

    private void updateLineChart(double a, double b, double c, double t, XYChart.Series<Number, Number> aSeries, XYChart.Series<Number, Number> bSeries, XYChart.Series<Number, Number> cSeries) {
        aSeries.getData().add(new XYChart.Data<>(t, a));
        if(aSeries.getData().size() > MAX_LINE_CHART_DATA) aSeries.getData().remove(0);
        bSeries.getData().add(new XYChart.Data<>(t, b));
        if(bSeries.getData().size() > MAX_LINE_CHART_DATA) bSeries.getData().remove(0);
        cSeries.getData().add(new XYChart.Data<>(t, c));
        if(cSeries.getData().size() > MAX_LINE_CHART_DATA) cSeries.getData().remove(0);
    }

    private double parseTextField(TextInputControl input, Function<Double, Boolean> validation) throws IllegalArgumentException {
        if (input == null || input.getText() == null) throw new IllegalArgumentException("Empty input");
        try {
            double number = Double.parseDouble(input.getText());
            if(validation == null || validation.apply(number)) {
                return number;
            } else {
                throw new IllegalArgumentException("Invalid input");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
