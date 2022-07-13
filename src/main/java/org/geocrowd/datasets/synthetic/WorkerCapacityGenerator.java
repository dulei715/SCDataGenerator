package org.geocrowd.datasets.synthetic;

import org.geocrowd.WorkerCapacityEnum;
import org.geocrowd.dtype.Range;

/**
 * Worker capacity is generated based on several criteria. For example, the
 * capacity of the worker is large if he/she is active.
 * 
 * @author ubriela
 *
 */
public class WorkerCapacityGenerator {
    private int minCapacity = 1;
	private int maxCapacity = 10;
	private double activeness = 0;

	public WorkerCapacityGenerator(int minCapacity, int maxCapacity) {
        this.minCapacity = minCapacity;
		this.maxCapacity = maxCapacity;
	}

	public void setActiveness(double activeness) {
		this.activeness = activeness;
	}

	public int nextWorkerCapacity(WorkerCapacityEnum capacityType) {
		switch (capacityType) {
		case CONSTANT:
			return maxCapacity;
		case RANDOM:
			// randomly between [minCapacity, maxCapacity)
			return (int) UniformGenerator.randomValue(
					new Range(minCapacity, maxCapacity), true);
		case ACTIVENESS_BASED:
			// The higher worker activeness, the higher capacity.
			return (int) activeness * maxCapacity;
		}
		return 0;
	}
}
