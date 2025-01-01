package org.example;

public class MidPointSolver<X, DX> implements ODESolver<X, DX>{
    @Override
    public X step(X state, ODE<X, DX> ode, float time) {
        X midpoint = ode.add(state, ode.scale(ode.f(state), time / 2));
        DX dx = ode.f(midpoint);
        return ode.add(state, ode.scale(dx, time));
    }
}
