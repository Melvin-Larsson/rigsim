package org.example;

public class EulerSolver<X, DX> implements  ODESolver<X, DX>{
    @Override
    public X step(X state, ODE<X, DX> ode, float time) {
        DX dx = ode.f(state);
        return ode.add(state, ode.scale(dx, time));
    }
}
