package org.geocrowd.datasets.synthetic;

import org.geocrowd.TaskDurationEnum;
import org.geocrowd.dtype.Range;


/**
 * A function to return task duration
 * @author ubriela
 *
 */

public class TaskDurationGenerator {

    private int minTaskDuration = 1;
	private int maxTaskDuration = 10;

	public TaskDurationGenerator(int minTaskDuration, int maxTaskDuration) {
        this.minTaskDuration = minTaskDuration;
		this.maxTaskDuration = maxTaskDuration;
	}

	public int nextTaskDuration(TaskDurationEnum durationDistribution) {
		switch (durationDistribution) {
		case CONSTANT:
			return maxTaskDuration;
		case RANDOM:
			return (int) UniformGenerator.randomValue(
					new Range(minTaskDuration, maxTaskDuration), false);
		default:
			break;
		}
		return 0;
	}
}