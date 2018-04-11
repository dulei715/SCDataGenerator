package org.geocrowd.datasets.synthesis.foursquare;

import org.geocrowd.common.crowd.GenericTask;
import org.geocrowd.common.crowd.WorkingRegion;
import org.geocrowd.datasets.synthetic.GenericProcessor;
import org.geocrowd.datasets.synthetic.UniformGenerator;
import org.geocrowd.dtype.Point;
import org.geocrowd.dtype.PointTime;
import org.geocrowd.dtype.Range;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by jianxun on 16/5/15.
 */
public class FoursquareProcessor {

    public static void extractTaskInstances(String filename, String outputPath, int instance, int maxPerInstance,
                                            double min_x, double min_y, double max_x, double max_y) {
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);
            PriorityQueue<FoursquareCheckins> sortedData = new PriorityQueue<>();
            while (in.ready()) {
                String line = in.readLine();
                FoursquareCheckins checkin = FoursquareCheckins.parse(line);
                sortedData.add(checkin);
            }
            System.out.println("total task num = " + sortedData.size());
            // sample tasks into instances
            // calculate mbr at the same time
            WorkingRegion mbr = new WorkingRegion(min_x, min_y, max_x, max_y);
            ArrayList<ArrayList<Point>> instances = new ArrayList<>();
            FoursquareCheckins pt = sortedData.poll();
            int result_num = 0;
            HashMap<String, PointTime> points = new HashMap<>();
            while (sortedData.size() > 0 && instances.size() < instance) {
                points.clear();
                long startTime = pt.utcTime;
                long endTime = startTime;
                while (endTime - startTime < 3600 * 24) {
                    points.put(pt.venueId, new PointTime(pt.venueId.hashCode(), (int)pt.utcTime, pt.latitude, pt.longitude));
                    mbr.extend(pt.latitude, pt.longitude);
                    pt = sortedData.poll();
                    if (pt == null) {
                        endTime = Integer.MAX_VALUE;
                    } else {
                        endTime = pt.utcTime;
                    }
                }
                if (points.size() > 50) {
                    ArrayList<Point> temp = new ArrayList<>();
                    temp.addAll(points.values());
                    if (points.size() > maxPerInstance) {
                        ArrayList<Point> picked = new ArrayList<>();
                        HashSet<Double> sample = UniformGenerator.randomDistinctValues(maxPerInstance, new Range(0, points.size() - 1), true);
                        for (Double idx : sample) {
                            int idxInt = idx.intValue();
                            if (idx >= 0 && idx < points.size()) {
                                picked.add(temp.get(idxInt));
                            }
                        }
                        temp = picked;
                    }
                    result_num += temp.size();
                    instances.add(temp);
                }
            }
            System.out.println("result task num = " + result_num);
            System.out.println("task instances = " + instances.size());
            // for each point, generate a worker and save to file
            // scale mbr to [0-1, 0-1]
            Path pathToFile = Paths.get(outputPath);
            Files.createDirectories(pathToFile.getParent());
            for (int i = 0; i < instance; i++) {
                GenericProcessor.timeCounter = i;
                FileWriter writer = new FileWriter(outputPath + i + ".txt");
                BufferedWriter out = new BufferedWriter(writer);
                StringBuilder sb = new StringBuilder();
                ArrayList<Point> p = instances.get(i);
                for (Point ptm : p) {
                    ptm.setX((ptm.getX() - mbr.getMinLat()) / (mbr.getMaxLat() - mbr.getMinLat()));
                    ptm.setY((ptm.getY() - mbr.getMinLng()) / (mbr.getMaxLng() - mbr.getMinLng()));
                    GenericTask t = GenericProcessor.generateGenericTask(ptm.getX(), ptm.getY());
                    sb.append(t).append("\n");
                }
                out.write(sb.toString());
                out.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
