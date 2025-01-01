package org.example;

import org.ejml.simple.SimpleMatrix;

public interface Constraint {
    float value(SimpleMatrix state);
    SimpleMatrix derivative(SimpleMatrix state);
    SimpleMatrix sndDerivative(SimpleMatrix state);
}
