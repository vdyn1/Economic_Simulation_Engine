package model;

import Bind.Bind;

public class Model1 {
    @Bind
    private int LL;
    @Bind
    private double[] twKI;
    @Bind
    private double[] twKS;
    @Bind
    private double[] twEKS;
    @Bind
    private double[] twIMP;
    @Bind
    private double[] twINW;
    @Bind
    private double[] KI;
    @Bind
    private double[] KS;
    @Bind
    private double[] INW;
    @Bind
    private double[] EKS;
    @Bind
    private double[] IMP;
    @Bind
    private double[] GDP;
    @Bind
    private double[] a;
    @Bind
    private double[] b;
    @Bind
    private double[] test;


    public Model1() {
    }


    public void run() {
        GDP = new double[LL];
        GDP[0] = KI[0] + KS[0] + INW[0] + EKS[0] - IMP[0];

        for (int t = 1; t < LL; t++) {
            KI[t] = twKI[t] * KI[t - 1];
            KS[t] = twKS[t] * KS[t - 1];
            INW[t] = twINW[t] * INW[t - 1];
            EKS[t] = twEKS[t] * EKS[t - 1];
            IMP[t] = twIMP[t] * IMP[t - 1];
            GDP[t] = KI[t] + KS[t] + INW[t] + EKS[t] - IMP[t];
        }
    }
}
