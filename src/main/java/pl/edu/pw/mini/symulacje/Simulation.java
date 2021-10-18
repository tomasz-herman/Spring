package pl.edu.pw.mini.symulacje;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import pl.edu.pw.mini.symulacje.functional.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static pl.edu.pw.mini.symulacje.functional.Utils.function;

public class Simulation implements EventHandler<ActionEvent> {
    private final Consumer<Double> update;

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

    public Simulation(Consumer<Double> update, double x0, double v0, double dt, double m, double k, double c, String w, String h) {
        this.update = update;
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
        this.x = x0;
        this.v = v0;
        this.a = F.apply(x, v, t) / m;
        update.accept(x);
    }

    @Override
    public void handle(ActionEvent event) {
        t += dt;
        x += v * dt;
        v += a * dt;
        a = F.apply(x, v, t) / m;
        update.accept(x);
    }
}
