package org.example;

public interface ODE<X, DX> {
    public DX f(X x);
    public X add(X x, DX dx);
    public DX scale(DX x, float factor);
}
