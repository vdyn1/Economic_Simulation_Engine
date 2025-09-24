package model;

import Bind.Bind;

public class Model2 {
    @Bind
    private int LL;
    @Bind
    private double[] activeClients;
    @Bind
    private double[] membershipsSold;
    @Bind
    private double[] revenue;
    @Bind
    private double[] expenses;
    @Bind
    private double[] totalClients;
    @Bind
    private double[] profit;
    @Bind
    private double[] personalTrainingSessions;
    @Bind
    private double[] groupClasses;
    @Bind
    private double[] newClients;


    public Model2() {
    }

    public void run() {
        profit = new double[LL];
        for (int i = 0; i < LL; i++) {
            profit[i] = revenue[i] - expenses[i];
        }
    }
}
