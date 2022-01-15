package pl.edu.pw.mini.symulacje;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.Random;
import java.util.function.Consumer;

import static java.lang.Math.*;

public class Simulation implements EventHandler<ActionEvent> {
    private final Random rand = new Random();
    private final Consumer<Visualisation.State> updateVisualisation;
    private final Consumer<State> updateControls;

    private Parameters params;

    private State state;

    public Simulation(Consumer<Visualisation.State> updateVisualisation, Consumer<State> updateControls, Parameters params) {
        this.params = params;
        state = new State(0, 0, calculatePosition(0, params.R, params.L), 0, 0);
        for (int i = 0; i < 2; i++) handle(null);

        this.updateVisualisation = updateVisualisation;
        this.updateControls = updateControls;

        updateVisualisation.accept(new Visualisation.State(0, params.R, params.L));
        updateControls.accept(state);
    }

    public Parameters getParams() {
        return params;
    }

    public void setParams(Parameters params) {
        this.params = params;
    }

    private static double calculatePosition(double a, double R, double L) {
        final double cos = cos(a);
        final double sin = sin(a);
        return R * cos + sqrt(L * L - R * R * sin * sin);
    }

    @Override
    public void handle(ActionEvent event) {
        final double dt = Controller.DT;
        final double e = params.e * rand.nextGaussian();
        final double L = params.L + e;
        final double R = params.R;
        final double a = state.a + params.w * dt;
        final double t = state.t + dt;
        final double x = calculatePosition(a, R, L);
        final double xt = (x - state.x) / dt;
        final double xtt = (xt - state.xt) / dt;

        state = new State(t, a, x, xt, xtt);

        if(updateVisualisation != null) updateVisualisation.accept(new Visualisation.State(a, R, L));
        if(updateControls != null) updateControls.accept(state);
    }

    public record State (double t, double a, double x, double xt, double xtt) {}
    public record Parameters (double R, double L, double w, double e) {}
}
