package org.example;

import java.util.ArrayList;
import java.util.List;

//Of order 4, don't know how to do other orders
public class RungeKuttaSolver<X, DX> implements ODESolver<X, DX>{
    @Override
    public X step(X state, ODE<X, DX> ode, float time) {
        List<DX> k = new ArrayList<>();

        k.add(ode.scale(ode.f(state), time));

        for(int i = 1; i < 4; i++){
            k.add(ode.scale(ode.f(ode.add(state, ode.scale(k.get(i-1), 0.5f))), time));
        }

        float[] factors = {1/6.0f, 1/3.0f, 1/3.0f, 1/6.0f};
        X res = state;
        for(int i = 0; i < 4; i++){
            res = ode.add(res, ode.scale(k.get(i), factors[i]));
        }

        return res;
    }
}
