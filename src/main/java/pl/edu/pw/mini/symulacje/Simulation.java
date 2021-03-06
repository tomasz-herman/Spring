package pl.edu.pw.mini.symulacje;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import pl.edu.pw.mini.symulacje.functional.OctaConsumer;
import pl.edu.pw.mini.symulacje.functional.TriFunction;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static pl.edu.pw.mini.symulacje.functional.Utils.function;

public class Simulation implements EventHandler<ActionEvent> {
    public static final double UPDATE_TIME = 0.01667;

    private final BiConsumer<Double, Double> updateVisualisation;
    private final OctaConsumer<Double> updateControls;

    private final double dt;
    private final double m;
    private final double k;
    private final double c;
    private final Function<Double, Double> w;

    private final BiFunction<Double, Double, Double> f;
    private final Function<Double, Double> g;
    private final Function<Double, Double> h;
    private final TriFunction<Double, Double, Double, Double> F;

    private double t;
    private double x;
    private double v;
    private double a;
    private double u;

    public Simulation(BiConsumer<Double, Double> updateVisualisation, OctaConsumer<Double> updateControls, double x0, double v0, double dt, double m, double k, double c, String w, String h) {
        this.updateVisualisation = updateVisualisation;
        this.updateControls = updateControls;
        this.dt = dt;
        this.m = m;
        this.k = k;
        this.c = c;
        this.w = function(w, "t");

        this.f = (x, t) -> this.c * (this.w.apply(t) - x);
        this.g = v -> -this.k * v;
        this.h = function(h, "t");
        this.F = (x, v, t) -> this.f.apply(x, t) + this.g.apply(v) + this.h.apply(t);

        this.t = 0.0;
        this.u = 0.0;
        this.x = x0;
        this.v = v0;
        this.a = F.apply(x, v, t) / m;
        updateVisualisation.accept(x, this.w.apply(t));
        updateControls.accept(x, v, a, t, this.w.apply(t), f.apply(x, t), g.apply(t), this.h.apply(t));
    }

    @Override
    public void handle(ActionEvent event) {
        a = F.apply(x, v, t) / m;
        v += a * dt;
        x += v * dt;
        t += dt;

        u += dt;
        if(u > UPDATE_TIME) {
            u -= UPDATE_TIME;
            updateVisualisation.accept(x, w.apply(t));
            updateControls.accept(x, v, a, t, w.apply(t), f.apply(x, t), g.apply(v), h.apply(t));
        }
    }
}
