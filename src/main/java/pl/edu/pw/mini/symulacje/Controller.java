package pl.edu.pw.mini.symulacje;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.function.Function;

import static java.lang.Math.*;
import static javafx.animation.Animation.Status.PAUSED;
import static javafx.animation.Animation.Status.STOPPED;
import static javafx.scene.control.Alert.AlertType.ERROR;

public class Controller {
    public static final double DT = 1.0 / 60.0;

    private static final int MAX_LINE_CHART_DATA = (int)(60 / DT);
    private static final int LINE_CHART_RANGE = 60;
    private static final int MAX_SCATTER_CHART_DATA = 2_500;
    private static final String FLOAT_FORMAT = "%.2f";
    private static final String CANNOT_START_SIMULATION = "Couldn't start simulation";
    private static final String RESUME_BTN_TEXT = "Resume";
    private static final String PAUSE_BTN_TEXT = "Pause";

    @FXML private ScatterChart<Number, Number> trajectoryChart;
    @FXML private LineChart<Number, Number> positionChart;
    @FXML private LineChart<Number, Number> velocityChart;
    @FXML private LineChart<Number, Number> accelerationChart;


    @FXML private Label tValue;
    @FXML private Label xValue;
    @FXML private Label xtValue;
    @FXML private Label xttValue;
    @FXML private TextField wValue;
    @FXML private TextField RValue;
    @FXML private TextField LValue;
    @FXML private TextField eValue;
    @FXML private Pane visualisationPane;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;
    @FXML private AnchorPane trajectoryPane;

    private XYChart.Series<Number, Number> xSeries;
    private XYChart.Series<Number, Number> xtSeries;
    private XYChart.Series<Number, Number> xttSeries;

    private XYChart.Series<Number, Number> trajectorySeries;

    private final Timeline timeline = new Timeline();

    private double trajectoryChartScale = 0.0;
    private double positionChartScale = 0.0;
    private double velocityChartScale = 0.0;
    private double accelerationChartScale = 0.0;

    @FXML private void initialize() {
        timeline.setCycleCount(Timeline.INDEFINITE);

        xSeries = new XYChart.Series<>("x", FXCollections.observableList(new LinkedList<>()));
        xtSeries = new XYChart.Series<>("xt", FXCollections.observableList(new LinkedList<>()));
        xttSeries = new XYChart.Series<>("xtt", FXCollections.observableList(new LinkedList<>()));
        positionChart.getData().add(xSeries);
        velocityChart.getData().add(xtSeries);
        accelerationChart.getData().add(xttSeries);

        trajectorySeries = new XYChart.Series<>(FXCollections.observableList(new LinkedList<>()));
        trajectoryChart.getData().add(trajectorySeries);

        startButton.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        wValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        RValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        LValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        eValue.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
        stopButton.disableProperty().bind(timeline.statusProperty().isEqualTo(STOPPED));
        pauseButton.disableProperty().bind(timeline.statusProperty().isEqualTo(STOPPED));

        timeline.statusProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == PAUSED) pauseButton.setText(RESUME_BTN_TEXT);
            else pauseButton.setText(PAUSE_BTN_TEXT);
        });

        trajectoryPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            double paneWidth = newValue.doubleValue();
            double paneHeight = trajectoryPane.getHeight();
            resizeTrajectoryChart(paneWidth, paneHeight);
        });

        trajectoryPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            double paneWidth = trajectoryPane.getWidth();
            double paneHeight = newValue.doubleValue();
            resizeTrajectoryChart(paneWidth, paneHeight);
        });
    }

    private void resizeTrajectoryChart(double paneWidth, double paneHeight) {
        double chartSize = Math.min(paneWidth, paneHeight);
        if(paneWidth > paneHeight) {
            double margin = (paneWidth - chartSize) / 2;
            AnchorPane.setRightAnchor(trajectoryChart, margin);
            AnchorPane.setLeftAnchor(trajectoryChart, margin);
            AnchorPane.setBottomAnchor(trajectoryChart, 0.0);
            AnchorPane.setTopAnchor(trajectoryChart, 0.0);
        } else {
            double margin = (paneHeight - chartSize) / 2;
            AnchorPane.setBottomAnchor(trajectoryChart, margin);
            AnchorPane.setTopAnchor(trajectoryChart, margin);
            AnchorPane.setRightAnchor(trajectoryChart, 0.0);
            AnchorPane.setLeftAnchor(trajectoryChart, 0.0);
        }
    }

    @FXML private void onStart(ActionEvent event) {
        event.consume();
        try {
            final double w = parseTextField(wValue, v -> v > 0);
            final double R = parseTextField(RValue, v -> v > 0);
            final double L = parseTextField(LValue, v -> v > 0);
            final double e = parseTextField(eValue, v -> v >= 0);
            Simulation.Parameters params = new Simulation.Parameters(R, L, w, e);

            trajectoryChartScale = 0.0;
            positionChartScale = 0.0;
            velocityChartScale = 0.0;
            accelerationChartScale = 0.0;

            Visualisation visualisation = new Visualisation(visualisationPane);

            Simulation springSimulation = new Simulation(visualisation::update, this::update, params);

            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(DT), springSimulation));
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
        positionChart.getData().clear();
        positionChart.getData().add(xSeries);
        velocityChart.getData().clear();
        velocityChart.getData().add(xtSeries);
        accelerationChart.getData().clear();
        accelerationChart.getData().add(xttSeries);
        trajectorySeries.getData().clear();
        visualisationPane.getChildren().clear();
    }

    private void update(Simulation.State state) {
        updateLineChart(state.x(), state.xt(), state.xtt(), state.t(), xSeries, xtSeries, xttSeries);

        positionChartScale = updateChartScale(state.x(), state.xt(), state.xtt(), state.t(), positionChartScale, positionChart);
        velocityChartScale = updateChartScale(state.x(), state.xt(), state.xtt(), state.t(), velocityChartScale, velocityChart);
        accelerationChartScale = updateChartScale(state.x(), state.xt(), state.xtt(), state.t(), accelerationChartScale, accelerationChart);

        trajectorySeries.getData().add(new XYChart.Data<>(state.x(), state.xt()));
        trajectoryChartScale = updateChartScale(state.x(), state.xt(), trajectoryChartScale, trajectoryChart);
        if(trajectorySeries.getData().size() > MAX_SCATTER_CHART_DATA) {
            int index = 1;
            var iterator = trajectorySeries.getData().iterator();
            while(iterator.hasNext()) {
                iterator.next();
                if(index++ % 2 == 0) {
                    iterator.remove();
                }
            }
        }

        tValue.setText(String.format(FLOAT_FORMAT, state.t()));
        xValue.setText(String.format(FLOAT_FORMAT, state.x()));
        xtValue.setText(String.format(FLOAT_FORMAT, state.xt()));
        xttValue.setText(String.format(FLOAT_FORMAT, state.xtt()));
    }

    private double updateChartScale(double a, double b, double c, double t, double scale, LineChart<Number, Number> chart) {
        scale = max(max(abs(a), abs(b)), max(abs(c), scale));
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        yAxis.setUpperBound(scale);
        yAxis.setLowerBound(-scale);
        double tStart = max(ceil((t - LINE_CHART_RANGE) / 10) * 10, 0);
        double tEnd = tStart + LINE_CHART_RANGE;
        xAxis.setUpperBound(tEnd);
        xAxis.setLowerBound(tStart);
        return scale;
    }

    private double updateChartScale(double a, double b, double scale, ScatterChart<Number, Number> chart) {
        scale = max(scale, max(abs(a), abs(b)));
        ((NumberAxis)chart.getXAxis()).setUpperBound(scale);
        ((NumberAxis)chart.getXAxis()).setLowerBound(-scale);
        ((NumberAxis)chart.getYAxis()).setUpperBound(scale);
        ((NumberAxis)chart.getYAxis()).setLowerBound(-scale);
        return scale;
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
