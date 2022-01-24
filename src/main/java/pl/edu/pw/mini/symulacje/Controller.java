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
    private Simulation simulation;

    private ChartRange trajectoryRange = new ChartRange();
    private ChartRange positionRange = new ChartRange();
    private ChartRange velocityRange = new ChartRange();
    private ChartRange accelerationRange = new ChartRange();

    @FXML private void initialize() {
        timeline.setCycleCount(Timeline.INDEFINITE);

        xSeries = new XYChart.Series<>(FXCollections.observableList(new LinkedList<>()));
        xtSeries = new XYChart.Series<>(FXCollections.observableList(new LinkedList<>()));
        xttSeries = new XYChart.Series<>(FXCollections.observableList(new LinkedList<>()));
        positionChart.getData().add(xSeries);
        velocityChart.getData().add(xtSeries);
        accelerationChart.getData().add(xttSeries);

        trajectorySeries = new XYChart.Series<>(FXCollections.observableList(new LinkedList<>()));
        trajectoryChart.getData().add(trajectorySeries);

        wValue.setOnAction(this::onChangeParams);
        LValue.setOnAction(this::onChangeParams);
        RValue.setOnAction(this::onChangeParams);
        eValue.setOnAction(this::onChangeParams);

        startButton.disableProperty().bind(timeline.statusProperty().isNotEqualTo(STOPPED));
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
            Simulation.Parameters params = parseParams();

            trajectoryRange = new ChartRange();
            positionRange = new ChartRange();
            velocityRange = new ChartRange();
            accelerationRange = new ChartRange();

            Visualisation visualisation = new Visualisation(visualisationPane);

            simulation = new Simulation(visualisation::update, this::update, params);

            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(DT), simulation));
            timeline.play();
        } catch (IllegalArgumentException e) {
            Alert errorAlert = new Alert(ERROR);
            errorAlert.setHeaderText(CANNOT_START_SIMULATION);
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void onChangeParams(ActionEvent event) {
        if (simulation != null) {
            Simulation.Parameters params = parseParams();
            simulation.setParams(params);
        }
    }

    private Simulation.Parameters parseParams() {
        final double w = parseTextField(wValue, v -> v > 0);
        final double R = parseTextField(RValue, v -> v > 0);
        final double L = parseTextField(LValue, v -> v > 0);
        final double e = parseTextField(eValue, v -> v >= 0 && R <= L + v && R <= L - v);
        return new Simulation.Parameters(R, L, w, e);
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

        updateTimeChartRange(state.x(), state.t(), positionRange, positionChart);
        updateTimeChartRange(state.xt(), state.t(), velocityRange, velocityChart);
        updateTimeChartRange(state.xtt(), state.t(), accelerationRange, accelerationChart);

        trajectorySeries.getData().add(new XYChart.Data<>(state.x(), state.xt()));
        updateChartScale(state.x(), state.xt(), trajectoryRange, trajectoryChart);
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

    private void updateTimeChartRange(double val, double t, ChartRange range, LineChart<Number, Number> chart) {
        range.maxY = max(val, range.maxY);
        range.minY = min(val, range.minY);
        range.minX = max(ceil((t - LINE_CHART_RANGE) / 10) * 10, 0);
        range.maxX = range.minX + LINE_CHART_RANGE;
        if(range.autosize) {
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();
            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            yAxis.setUpperBound(range.maxY);
            yAxis.setLowerBound(range.minY);
            xAxis.setUpperBound(range.maxX);
            xAxis.setLowerBound(range.minX);
        }
    }

    private void updateChartScale(double a, double b, ChartRange range, ScatterChart<Number, Number> chart) {
        range.maxX = max(a, range.maxX);
        range.minX = min(a, range.minX);
        range.maxY = max(b, range.maxY);
        range.minY = min(b, range.minY);
        if(range.autosize) {
            double diff = range.maxX - range.minX - range.maxY + range.minY;
            double maxX = range.maxX, minX = range.minX, maxY = range.maxY, minY = range.minY;
            if(diff > 1e-6) {
                maxY = range.maxY + diff * 0.5;
                minY = range.minY - diff * 0.5;
            } else if(diff < -1e-6) {
                maxX = range.maxX - diff * 0.5;
                minX = range.minX + diff * 0.5;
            }
            ((NumberAxis)chart.getXAxis()).setUpperBound(maxX);
            ((NumberAxis)chart.getXAxis()).setLowerBound(minX);
            ((NumberAxis)chart.getYAxis()).setUpperBound(maxY);
            ((NumberAxis)chart.getYAxis()).setLowerBound(minY);
        }
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

    private static class ChartRange {
        public boolean autosize = true;
        public double minX = Double.POSITIVE_INFINITY;
        public double maxX = Double.NEGATIVE_INFINITY;
        public double minY = Double.POSITIVE_INFINITY;
        public double maxY = Double.NEGATIVE_INFINITY;
    }
}
