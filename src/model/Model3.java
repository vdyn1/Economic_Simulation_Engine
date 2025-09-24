package model;

import Bind.Bind;

public class Model3 {
    @Bind
    private int LL;
    @Bind
    private double[] CL;
    @Bind
    private double[] IN;
    @Bind
    private double[] TP;
    @Bind
    private double[] AVGC;

    public Model3() {
    }

    public void run() {
        AVGC = new double[LL];
        for (int i = 0; i < LL; i++) {
            AVGC[i] = IN[i] / CL[i];
        }
    }
}
