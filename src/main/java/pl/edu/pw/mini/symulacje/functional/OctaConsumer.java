package pl.edu.pw.mini.symulacje.functional;

@FunctionalInterface
public interface OctaConsumer<T> {
    void accept(T a, T b, T c, T d, T e, T f, T g, T h);
}
