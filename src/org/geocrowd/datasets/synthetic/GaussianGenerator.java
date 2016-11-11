package org.geocrowd.datasets.synthetic;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.geocrowd.datasets.params.GeocrowdConstants;

/**
 * Created by jianxun on 16/4/16.
 */
public class GaussianGenerator {
    /** maps [-1, 1] of Gaussian distribution to [min, max] */
    public static double Generate(double min, double max, double sd) {
        NormalDistribution nd = new NormalDistribution(0, sd);
        double result = nd.sample();
        while (result < -1 || result > 1) {
            result = nd.sample();
        }
        result = (result + 1) / 2 * (max - min) + min;
        return result;
    }

    public static int GenerateInt(int min, int max, double sd) {
        NormalDistribution nd = new NormalDistribution(0, sd);
        double result = nd.sample();
        while (result < -1 || result > 1) {
            result = nd.sample();
        }
        result = (result + 1) / 2 * (max - min + 1) + min;
        return (int) Math.floor(result);
    }
}
