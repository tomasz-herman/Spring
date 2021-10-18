package pl.edu.pw.mini.symulacje;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import pl.edu.pw.mini.symulacje.functional.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static pl.edu.pw.mini.symulacje.functional.Utils.function;

public class Simulation implements EventHandler<ActionEvent> {
    private final double x0 = -6;
    private final double v0 = 0;
    private final double dt = 0.0167;
    private final double m = 1;
    private final double k = 0.1;
    private final double c = 5;
    private final Function<Double, Double> w = function("sin(t)", "t");

    private final BiFunction<Double, Double, Double> f = (x, t) -> c * (w.apply(t) - x);
    private final Function<Double, Double> g = v -> -k * v;
    private final Function<Double, Double> h = function("0", "t");
    private final TriFunction<Double, Double, Double, Double> F = (x, v, t) -> f.apply(x, t) + g.apply(v) + h.apply(t);

    private double t = 0.0;
    private double x = x0;
    private double v = v0;
    private double a = F.apply(x, v, t) / m;

    @Override
    public void handle(ActionEvent event) {
        t += dt;
        x += v * dt;
        v += a * dt;
        a = F.apply(x, v, t) / m;
        System.out.printf("x = %.2f, v = %.2f, a = %.2f, w = %.2f%n", x, v, a, w.apply(t));
    }
}
