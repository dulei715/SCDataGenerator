package org.geocrowd.datasets.synthetic;

import java.util.ArrayList;

import org.geocrowd.Distribution2DEnum;
import org.geocrowd.ArrivalRateEnum;
import org.geocrowd.datasets.params.GeocrowdConstants;
import org.geocrowd.dtype.Range;
import org.geocrowd.dtype.Rectangle;

public class TimeInstancesGenerator {

	public static int gaussianCluster = 1;

	protected int instances = 0;
	protected Distribution2DEnum workerDist;
	protected Distribution2DEnum taskDist;
	protected ArrivalRateEnum workerCycle;
	protected ArrivalRateEnum taskCycle;
	protected int wMean = 0;
	protected int tMean = 0;

	protected String workerPath = "";
	protected String taskPath = "";

	private Rectangle boundary = null;

	/**
	 * 
	 * @param instances
	 *            : the number of time instances
	 * @param wc
	 *            : worker's arrival rate
	 * @param tc
	 *            : task's arrival rate
	 * @param wMean
	 *            : mean of worker count
	 * @param tMean
	 *            : mean of task count
	 * @param boundary
	 *            : domain
	 * @param wd
	 *            : worker distribution
	 * @param td
	 *            : task distribution
	 * @param workerPath
	 *            : output directory of tasks
	 * @param taskPath
	 *            : output directory of workers
	 */
	public TimeInstancesGenerator(int instances, ArrivalRateEnum wc,
			ArrivalRateEnum tc, int wMean, int tMean, Rectangle boundary,
			Distribution2DEnum wd, Distribution2DEnum td, String workerPath,
			String taskPath) {
		super();
		this.instances = instances;
		this.workerCycle = wc;
		this.taskCycle = tc;
		this.wMean = wMean;
		this.tMean = tMean;
		this.boundary = boundary;
		this.workerDist = wd;
		this.taskDist = td;
		this.workerPath = workerPath;
		this.taskPath = taskPath;

		generateData();
	}

	/**
	 * 
	 * @param instances
	 *            : the number of time instances
	 * @param workerCycle
	 *            : worker's arrival rate
	 * @param taskCycle
	 *            : task's arrival rate
	 * @param wMean
	 *            : mean of worker count
	 * @param tMean
	 *            : mean of task count
	 * @param workerPath
	 *            : output directory of tasks
	 * @param taskPath
	 *            : output directory of workers
	 */
	public TimeInstancesGenerator(int instances, ArrivalRateEnum workerCycle,
			ArrivalRateEnum taskCycle, int wMean, int tMean, String workerPath,
			String taskPath) {
		super();
		this.instances = instances;
		this.workerCycle = workerCycle;
		this.taskCycle = taskCycle;
		this.wMean = wMean;
		this.tMean = tMean;
		this.workerPath = workerPath;
		this.taskPath = taskPath;
	}

	/**
	 * 
	 * @return list of worker counts for time instances
	 */
	protected ArrayList<Integer> computeWorkerCounts() {
		ArrayList<Integer> workerCounts = ArrivalRateGenerator.generateCounts(
				instances, wMean, workerCycle);

		for (int i : workerCounts)
			System.out.print(i + "\t");
		System.out.println();
		return workerCounts;
	}

	/**
	 * 
	 * @return list of task counts for time instances
	 */
	protected ArrayList<Integer> computeTaskCounts() {
		ArrayList<Integer> taskCounts = ArrivalRateGenerator.generateCounts(
				instances, tMean, taskCycle);
		for (int i : taskCounts)
			System.out.print(i + "\t");
		System.out.println();
		return taskCounts;
	}

	/**
	 * Similar to Random Walk Model (unpredictable movement of particles in
	 * physics)
	 */
	protected void generateData() {

		ArrayList<Integer> workerCounts = computeWorkerCounts();
		ArrayList<Integer> taskCounts = computeTaskCounts();

		ArrayList<Long> seeds = new ArrayList<>();
		// compute seed for Gaussian cluster
		Distribution2DGenerator.gaussianCluster = gaussianCluster;
		for (int j = 0; j < GeocrowdConstants.MAX_MIXTURE_CLUSTER_NUMBER; j++)
			seeds.add((long) UniformGenerator.randomValue(
					new Range(0, 1000000), true));
		Distribution2DGenerator.seeds = seeds;

		for (int i = 0; i < instances; i++) {
			// update time instance
			Distribution2DGenerator.time = i;

			// worker varying distributions
			Distribution2DGenerator wdg = new Distribution2DGenerator(
					workerPath + "workers" + i + ".txt");
			wdg.distributionIndicator = 0;

//			wdg.varianceX = boundary.getHighPoint().getX() - boundary.getLowPoint().getX();
//			wdg.varianceY = boundary.getHighPoint().getY() - boundary.getLowPoint().getY();

			wdg.varianceX = GeocrowdConstants.WORKER_LOCATION_VARIANCE;
			wdg.varianceY = GeocrowdConstants.WORKER_LOCATION_VARIANCE;
			wdg.defaultGaussianCenter.setX(GeocrowdConstants.WORKER_LOCATION_MEAN);
			wdg.defaultGaussianCenter.setY(GeocrowdConstants.WORKER_LOCATION_MEAN);
			Distribution2DGenerator.gaussianCluster = 1;
			if (workerDist == Distribution2DEnum.MIXTURE_GAUSSIAN_UNIFORM_MULTICENTROID){
				Distribution2DGenerator.gaussianCluster = GeocrowdConstants.WORKER_CLUSTER_NUMBER;
			}

			switch (workerDist){
				case UNIFORM_2D:
					System.out.println("Worker dist: Uniform");
					break;
				case GAUSSIAN_2D:
					System.out.println("Worker dist: GAUS Centers:" + Distribution2DGenerator.gaussianCluster + "  Mean:"+wdg.defaultGaussianCenter.getX()+" Variance:"+wdg.varianceY);
					break;
				case MIXTURE_GAUSSIAN_UNIFORM_MULTICENTROID:
					System.out.println("Worker dist: SKEWED Centers:" + Distribution2DGenerator.gaussianCluster + "  Mean:"+wdg.defaultGaussianCenter.getX()+" Variance:"+wdg.varianceY);
					break;
			}


			wdg.generate2DDataset(workerCounts.get(i), boundary, workerDist);

			// task default distributions
			Distribution2DGenerator tdg = new Distribution2DGenerator(taskPath
					+ "tasks" + i + ".txt");
			tdg.distributionIndicator = 1;
			tdg.varianceX = boundary.getHighPoint().getX()/20;
			tdg.varianceY = boundary.getHighPoint().getY()/20;
			tdg.defaultGaussianCenter.setY(0.5);
			tdg.defaultGaussianCenter.setX(0.5);
			Distribution2DGenerator.gaussianCluster = 1;
			if (taskDist == Distribution2DEnum.MIXTURE_GAUSSIAN_UNIFORM_MULTICENTROID){
				Distribution2DGenerator.gaussianCluster = 3;
			}

			switch (taskDist){
				case UNIFORM_2D:
					System.out.println("Task dist: Uniform");
					break;
				case GAUSSIAN_2D:
					System.out.println("Task dist: GAUS Centers:" + Distribution2DGenerator.gaussianCluster + "  Mean:"+tdg.defaultGaussianCenter.getX()+" Variance:"+tdg.varianceY);
					break;
				case MIXTURE_GAUSSIAN_UNIFORM_MULTICENTROID:
					System.out.println("Task dist: SKEWED Centers:" + Distribution2DGenerator.gaussianCluster + "  Mean:"+tdg.defaultGaussianCenter.getX()+" Variance:"+tdg.varianceY);
					break;
			}
			tdg.generate2DDataset(taskCounts.get(i), boundary, taskDist);
		}
	}
}
