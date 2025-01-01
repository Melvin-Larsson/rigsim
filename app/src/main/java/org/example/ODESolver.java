package org.example;

public interface ODESolver<X, DX> {
    public X step(X state, ODE<X, DX> ode, float time);
}
