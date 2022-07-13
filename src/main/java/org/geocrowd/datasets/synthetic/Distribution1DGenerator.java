package org.geocrowd.datasets.synthetic;

import java.util.Vector;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.geocrowd.Distribution1DEnum;
import org.geocrowd.common.utils.Utils;
import org.geocrowd.dtype.Range;

public class Distribution1DGenerator {

	public static Vector<Double> generate1DZipfValues(int n, double min, double max,
			boolean isInteger) {
		Vector<Double> values = new Vector<Double>();

		int count = 0;
		while (true) {
			if (count++ == n)
				break;

			int rand = (int) UniformGenerator.randomValue(new Range(0, max
					- min), true);
			double val = ((max - min) * Utils.zipf_pmf(n, rand, 1));
			if (isInteger) {
				double tmp = (int) val;
				values.add(tmp);
			} else
				values.add(val);
		}

		return values;
	}

	public static Vector<Double> generate1DDataset(int count, double min, double max,
			Distribution1DEnum dist, boolean isInteger) {
		Vector<Double> values = null;
		switch (dist) {
		case UNIFORM_1D: // uniform one-dimensional dataset
			values = UniformGenerator
					.randomSequence(count, min, max, isInteger);
			break;
		case ZIFFIAN_1D: // zipf distribution
			values = generate1DZipfValues(count, min, max, isInteger);
			break;
		case GAUSSIAN_1D:
			values = generate1DGaussianValues(count, min, max, isInteger);
			break;
		}
//		writeIntegersToFile(values, filePath);
		// writeIntegersToFileWithKey(values, filePath + ".data");
		
		return values;
	}
	
	/**
	 * A twisted version of Gaussian distribution, in which when the value is
	 * out of a range, we regenerate the value
	 * 
	 * @param count
	 * @param min
	 * @param max
	 * @param isInteger
	 * @return
	 */
	public static Vector<Double> generate1DGaussianValues(int count, double min,
			double max, boolean isInteger) {
        return generate1DGaussianValues(count, min, max, (max - min) / 4, isInteger);
	}

    public static Vector<Double> generate1DGaussianValues(int count, double min,
                                                          double max, double sd, boolean isInteger) {
        Vector<Double> values = new Vector<Double>();

        int n = 0;
        while (true) {
            NormalDistribution nd = new NormalDistribution((max - min) / 2, sd);
            double val = nd.sample();
            if (val > max || val < min)
                continue;
            if (isInteger) {
                double tmp = (int) val;
                values.add(tmp);
            } else
                values.add(val);

            if (n++ == count)
                break;
        }

        return values;
    }
}
