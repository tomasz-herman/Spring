package pl.edu.pw.mini.symulacje.functional;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Utils {
    @NotNull public static Function<Double, Double> function(@NotNull String exp, @NotNull String param) {
        Expression expression = new ExpressionBuilder(exp)
                .variable(param)
                .build()
                .setVariable(param, 0);
        ValidationResult validation = expression.validate();
        if(validation.isValid()) {
            return t -> expression.setVariable(param, t).evaluate();
        } else {
            System.err.println(validation.getErrors());
            return t -> 0.0;
        }
    }
}
