package org.example;

import org.ejml.simple.SimpleMatrix;

public interface ODE<X, DX> {
    public DX f(X x);
    public X add(X x, DX dx);
    public DX scale(DX x, float factor);
}
