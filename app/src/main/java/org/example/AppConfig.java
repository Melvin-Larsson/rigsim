package org.example;

import java.io.File;
import java.io.Serializable;

public class AppConfig implements Serializable {
    public final EOdeSolver odeSolver;
    public final float gravity;
    public final float bounce;
    public final float viscousDrag;
    public final File currFile;

    public AppConfig(EOdeSolver odeSolver, float gravity, float bounce, float viscousDrag, File currFile) {
        this.odeSolver = odeSolver;
        this.gravity = gravity;
        this.bounce = bounce;
        this.viscousDrag = viscousDrag;
        this.currFile = currFile;
    }

}
