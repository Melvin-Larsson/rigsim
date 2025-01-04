package org.example;

import java.util.function.Supplier;

public enum EOdeSolver {
    Euler(EulerSolver::new, "Euler"),
    MidPoint(MidPointSolver::new, "Midpoint"),
    RungeKutta(RungeKuttaSolver::new, "RungeKutta");

    private final Supplier<ODESolver<?,?>> supplier;
    public final String displayName;

    EOdeSolver(Supplier<ODESolver<?, ?>> supplier, String displayName){
        this.supplier = supplier;
        this.displayName = displayName;
    }

    public ODESolver<?,?> getSolver(){
        return supplier.get();
    }

}
