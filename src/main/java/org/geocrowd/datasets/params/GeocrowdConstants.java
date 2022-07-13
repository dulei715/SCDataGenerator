/*******************************************************************************
 * @ Year 2013
 * This is the source code of the following papers.
 * 
 * 
 * Please contact the author Hien To, ubriela@gmail.com if you have any question.
 *
 * Contributors:
 * Hien To - initial implementation
 *******************************************************************************/
package org.geocrowd.datasets.params;

// TODO: Auto-generated Javadoc
/**
 * The Class Constants.
 */
public class GeocrowdConstants {

	public static Character delimiter = '\t';
	public static String delimiter_dataset = ";";

	/** The number of time instances. */
	public static int TIME_INSTANCE = 7;
	
	/** The number of tasks. */
	public static int TASK_NUMBER = 1000;

	/**
	 * The number of workers
	 */
	public static int WORKER_NUMBER = 0;

	/** the length of task availability */
    public static int MIN_TASK_DURATION = 0;
	public static int MAX_TASK_DURATION = 10;
	
	/** enable random k */
	public static boolean RANDOM_REQUIREMENT = false;

	/** required number of responses */
    public static int MIN_TASK_REQUIREMENT = 1;
	public static int MAX_TASK_REQUIREMENT = 50;

    /** required confidence of responses */
    public static double MIN_TASK_CONFIDENCE = 0.5;
    public static double MAX_TASK_CONFIDENCE = 0.9;

	// shared parameters
	/** number of task categories/expertise. */
	public static int TASK_CATEGORY_NUMBER = 10;

	/** number of tasks that a worker want to get. */
    public static int MIN_WORKER_CAPACITY = 1;
	public static int MAX_WORKER_CAPACITY = 20;

    /** range of reliabilities of a worker */
    public static double MIN_WORKER_RELIABILITY = 0.5;
    public static double MAX_WORKER_RELIABILITY = 0.9;

	/** maximum range of an mbr is 5% of the entire x or y dimension. */
    public static double MIN_WORKING_REGION = 0.01;
	public static double MAX_WORKING_REGION = 0.05;

    /** range of working side length */
    public static double MIN_WORKING_SIDE_LENGTH = 0.01;
    public static double MAX_WORKING_SIDE_LENGTH = 0.2;

	/** The small resolution. */
	public static int SMALL_GRID_RESOLUTION = 1;
	
	
	/** The maximum price per task. */
	public static double MAX_TASK_REWARD = 5.0;

	// synthetic dataset
	/** The skewed resolution. */
	public static int SKEWED_GRID_RESOLUTION = 500;

	/** The uni resolution. */
	public static int UNIFORM_GRID_RESOLUTION = 100;

    public static double MIXTURE_GAUSSIAN_PORTION = 0.9;
	
	
	public static final int SCALE_GRID_RESOLUTION = 500;

	/** The worker file path. */
	public static String WORKER_FILE_PATH = "./res/dataset/worker/workers";

	/** The task file path. */
	public static String TASK_FILE_PATH = "./res/dataset/task/tasks";
	
	/** The worker file path. */
	public static String WORKER_SCALE_FILE_PATH = "./dataset/scale/worker_dist.txt";

	/** The task file path. */
	public static String TASK_SCALE_FILE_PATH = "./dataset/scale/task_dist.txt";

	/** The interval time between two batches **/
	public static double BATCH_INTERVAL_TIME = 120;

	/** The total time length of the real data set **/
	public static double TOTAL_REAL_DATA_TIME_LENGTH = 3600;

	public static double WORKER_LOCATION_MEAN = 0.5;

	public static double WORKER_LOCATION_VARIANCE = 0.05;

	public static int WORKER_CLUSTER_NUMBER = 3;

	public static int MAX_MIXTURE_CLUSTER_NUMBER = 10;

	public static double WORKER_SPEED = 0.25;
}