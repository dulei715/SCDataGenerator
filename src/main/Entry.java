package main;

import org.geocrowd.*;
import org.geocrowd.datasets.params.GeocrowdConstants;
import org.geocrowd.datasets.synthesis.didi.DiDiProcessor;
import org.geocrowd.datasets.synthetic.GenericProcessor;
import org.geocrowd.datasets.synthetic.TimeInstancesGenerator;
import org.geocrowd.dtype.Rectangle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jianxun on 16/4/2.
 */
public class Entry {
    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "US", "UNI"));
        Map<String, String> argMap = parseParams(args);
        int instances = getIntParam(argMap, "instance", 30);
        int workerNumPerIns = getIntParam(argMap, "worker_num_per_instance", 200);
        int taskNumPerIns = getIntParam(argMap, "task_num_per_instance", 200);
        int uniqueWorkerCount = getIntParam(argMap, "unique_worker_count", 10000);
        GeocrowdConstants.MIN_TASK_DURATION = getIntParam(argMap, "min_task_duration", 5);
        GeocrowdConstants.MAX_TASK_DURATION = getIntParam(argMap, "max_task_duration", 10);
        GeocrowdConstants.MIN_TASK_REQUIREMENT = getIntParam(argMap, "min_task_requirement", 5);
        GeocrowdConstants.MAX_TASK_REQUIREMENT = getIntParam(argMap, "max_task_requirement", 7);
        GeocrowdConstants.MIN_TASK_CONFIDENCE = getDoubleParam(argMap, "min_task_confidence", 0.85);
        GeocrowdConstants.MAX_TASK_CONFIDENCE = getDoubleParam(argMap, "max_task_confidence", 0.9);
        GeocrowdConstants.MIN_WORKER_CAPACITY = getIntParam(argMap, "min_worker_capacity", 5);
        GeocrowdConstants.MAX_WORKER_CAPACITY = getIntParam(argMap, "max_worker_capacity", 7);
        GeocrowdConstants.MIN_WORKER_RELIABILITY = getDoubleParam(argMap, "min_worker_reliability", 0.75);
        GeocrowdConstants.MAX_WORKER_RELIABILITY = getDoubleParam(argMap, "max_worker_reliability", 0.8);
        GeocrowdConstants.MIN_WORKING_SIDE_LENGTH = getDoubleParam(argMap, "min_working_side_length", 0.15);
        GeocrowdConstants.MAX_WORKING_SIDE_LENGTH = getDoubleParam(argMap, "max_working_side_length", 0.2);
        GeocrowdConstants.BATCH_INTERVAL_TIME = getDoubleParam(argMap, "batch_interval_time", 120);
        GeocrowdConstants.WORKER_LOCATION_MEAN = getDoubleParam(argMap, "worker_location_mean", 0.5);
        GeocrowdConstants.WORKER_LOCATION_VARIANCE = getDoubleParam(argMap, "worker_location_variance", 0.2);
        GeocrowdConstants.WORKER_CLUSTER_NUMBER = getIntParam(argMap, "worker_cluster_number", 3);

        System.out.println(argMap.toString());

        if (argMap.containsKey("real")) {
//            GowallaProcessor prep = new GowallaProcessor(instances, WorkerType.GENERIC, TaskType.GENERIC,
//                    TaskCategoryEnum.RANDOM);
            // read from dataset/real/gowalla/gowalla_totalCheckins.txt
            // output into file "dataset/real/gowalla/gowalla_filtered"
            // pittsburgh
            // prep.filterInput("dataset/real/gowalla/gowalla_filtered", 40.401465, -80.053916, 40.491322, -79.849905);
            // los angeles
            // prep.filterInput("dataset/real/gowalla/gowalla_filtered", 33.692965, -118.661469, 34.353218, -118.161934);

            // read from "dataset/real/gowalla/gowalla_filtered"
            // output into "dataset/real/gowalla/worker/gowalla_workersxxxx.txt"
//            prep.extractWorkersInstances2("dataset/real/gowalla/gowalla_filtered",
//                    "dataset/real/worker/workers", instances, 33.692965, -118.661469, 34.353218, -118.161934);
//            FoursquareProcessor.extractTaskInstances("dataset/real/foursquare/foursquare_filtered.txt",
//                    "dataset/real/task/tasks", instances, 300, 33.692965, -118.661469, 34.353218, -118.161934);
//            DiDiProcessor.extractWorkersInstances("dataset/real/didi/drivers.csv",
//                    "dataset/real/worker/workers", instances,
//                    39.92583, 116.17851, 39.92583, 116.17851);
//            DiDiProcessor.extractTaskInstances("dataset/real/didi/orders.csv",
//                    "dataset/real/task/tasks", instances, 300,
//                    39.87024, 116.51174, 39.87024, 116.51174);

            DiDiProcessor.extractWorkersInstances("dataset/real/didi/drivers.csv",
                    "dataset/real/worker/workers", (int)(GeocrowdConstants.TOTAL_REAL_DATA_TIME_LENGTH /GeocrowdConstants.BATCH_INTERVAL_TIME),
                    39.7558, 116.1996, 40.0229, 116.5457, GeocrowdConstants.BATCH_INTERVAL_TIME);
            DiDiProcessor.extractTaskInstances("dataset/real/didi/orders.csv",
                    "dataset/real/task/tasks", (int)(GeocrowdConstants.TOTAL_REAL_DATA_TIME_LENGTH /GeocrowdConstants.BATCH_INTERVAL_TIME),
                    39.7558, 116.1996, 40.0229, 116.5457, GeocrowdConstants.BATCH_INTERVAL_TIME);

        }




        Distribution2DEnum wd = Distribution2DEnum.UNIFORM_2D;
        Distribution2DEnum td = Distribution2DEnum.UNIFORM_2D;
        if (argMap.containsKey("unif")) {
            td = Distribution2DEnum.UNIFORM_2D;

        } else if (argMap.containsKey("gaus")) {
            td = Distribution2DEnum.GAUSSIAN_2D;

        } else if (argMap.containsKey("skew")) {
            td = Distribution2DEnum.MIXTURE_GAUSSIAN_UNIFORM_MULTICENTROID;
        } else if (argMap.containsKey("zipf")) {
            td = Distribution2DEnum.ZIPFIAN_2D;
        }

        if (GeocrowdConstants.WORKER_CLUSTER_NUMBER == 0){
            wd = Distribution2DEnum.GAUSSIAN_2D;
        } else if (GeocrowdConstants.WORKER_CLUSTER_NUMBER >= 1){
            wd = Distribution2DEnum.MIXTURE_GAUSSIAN_UNIFORM_MULTICENTROID;
        }


        TimeInstancesGenerator ti = new TimeInstancesGenerator(instances,
                ArrivalRateEnum.CONSTANT, ArrivalRateEnum.CONSTANT, workerNumPerIns, taskNumPerIns,
                new Rectangle(0, 0, 1, 1), wd,
                td, "./res/dataset/worker/",
                "./res/dataset/task/");


        if (argMap.containsKey("general") && !argMap.containsKey("real")) {
            GenericProcessor gp = new GenericProcessor(instances, uniqueWorkerCount, DatasetEnum.UNIFORM, WorkerIDEnum.UNIFORM,
                    WorkerType.GENERIC, WorkingRegionEnum.RANDOM, WorkerCapacityEnum.RANDOM, TaskType.GENERIC,
                    TaskCategoryEnum.RANDOM, TaskRadiusEnum.RANDOM, TaskRewardEnum.RANDOM, TaskDurationEnum.RANDOM);
        }
    }

    public static int getIntParam(Map<String, String> argMap, String name, int def) {
        String value = argMap.get(name);
        int result = def;
        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static double getDoubleParam(Map<String, String> argMap, String name, double def) {
        String value = argMap.get(name);
        double result = def;
        if (value != null) {
            try {
                result = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String getStringParam(Map<String, String> argMap, String name, String def) {
        String value = argMap.get(name);
        if (value != null) {
            return value;
        }
        return def;
    }

    public static Map<String, String> parseParams(String[] args) {
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String[] parts = args[i].split("=");
            if (parts.length > 1) {
                argMap.put(parts[0], parts[1]);
            } else {
                argMap.put(parts[0], "");
            }
        }
        return argMap;
    }
}
