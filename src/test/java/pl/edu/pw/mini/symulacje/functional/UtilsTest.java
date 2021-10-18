package pl.edu.pw.mini.symulacje.functional;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.pw.mini.symulacje.functional.Utils.function;

class UtilsTest {
    @Test
    public void testFunctionResult() {
        Function<Double, Double> f = function("sin(t) + 2", "t");
        double param = Math.PI / 6;
        double result = f.apply(param);
        assertThat(result).isCloseTo(Math.sin(param) + 2, Offset.offset(1e-7));
    }
}