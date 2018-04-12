/*******************************************************************************
 * @ Year 2013
 * This is the source code of the following papers. 
 * 
 * 1) Geocrowd: A Server-Assigned Crowdsourcing Framework. Hien To, Leyla Kazemi, Cyrus Shahabi.
 * 
 * 
 * Please contact the author Hien To, ubriela@gmail.com if you have any question.
 *
 * Contributors:
 * Hien To - initial implementation
 *******************************************************************************/
package org.geocrowd.datasets.synthesis.didi;

import org.geocrowd.common.crowd.*;
import org.geocrowd.datasets.synthetic.GenericProcessor;
import org.geocrowd.datasets.synthetic.UniformGenerator;
import org.geocrowd.dtype.Point;
import org.geocrowd.dtype.PointTime;
import org.geocrowd.dtype.Range;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author Jian Xun
 */
public class DiDiProcessor extends GenericProcessor {
    public static void extractWorkersInstances(String filename, String outputPath, int instance, double min_x, double min_y,
                                         double max_x, double max_y) {
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);
            PriorityQueue<PointTime> sortedData = new PriorityQueue<>();
            while (in.ready()) {
                String line = in.readLine();
                String[] parts = line.split(",");
                if (parts[2].equals("0")) {
                    continue;
                }
                Long id = Long.parseLong(parts[0]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                long millis = sdf.parse(parts[1]).getTime();
                int timestamp = (int) (millis / 1000);
                Double lng = Double.parseDouble(parts[3]);
                Double lat = Double.parseDouble(parts[4]);

                /**
                 * Add point to queue
                 */
                PointTime pt = new PointTime(id, timestamp, lat, lng);
                sortedData.add(pt);
            }
            // partition workers into instances, each instance lasts 60 minutes
            // calculate mbr at the same time
            WorkingRegion mbr = new WorkingRegion(min_x, min_y, max_x, max_y);
            ArrayList<ArrayList<PointTime>> instances = new ArrayList<>();
            PointTime pt = sortedData.poll();
            int result_num = 0;
            HashMap<Long, PointTime> points = new HashMap<>();
            while (sortedData.size() > 0 && instances.size() < instance) {
                points.clear();
                int startTime = pt.getTimestamp();
                int endTime = startTime;
                while (endTime - startTime < 120) {
                    points.put(pt.getUserid(), pt);
                    mbr.extend(pt.getX(), pt.getY());
                    pt = sortedData.poll();
                    if (pt == null) {
                        break;
                    }
                    endTime = pt.getTimestamp();
                }
                if (points.size() > 150) {
                    ArrayList<PointTime> temp = new ArrayList<>(points.values());
                    instances.add(temp);
                    result_num += points.size();
                }
            }
            System.out.println("result worker num = " + result_num);
            System.out.println("worker instances = " + instances.size());
            // for each point, generate a worker and save to file
            // scale mbr to [0-1, 0-1]
            Path pathToFile = Paths.get(outputPath);
            Files.createDirectories(pathToFile.getParent());
            for (int i = 0; i < instances.size(); i++) {
                FileWriter writer = new FileWriter(outputPath + i + ".txt");
                BufferedWriter out = new BufferedWriter(writer);
                StringBuilder sb = new StringBuilder();
                ArrayList<PointTime> p = instances.get(i);
                for (PointTime ptm : p) {
                    ptm.setX((ptm.getX() - mbr.getMinLat()) / (mbr.getMaxLat() - mbr.getMinLat()));
                    ptm.setY((ptm.getY() - mbr.getMinLng()) / (mbr.getMaxLng() - mbr.getMinLng()));
                    GenericWorker w = generateGenericWorker(ptm.getX(), ptm.getY(), ptm.getUserid());
                    sb.append(w).append("\n");
                }
                out.write(sb.toString());
                out.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void extractTaskInstances(String filename, String outputPath, int instance, int maxPerInstance,
                                            double min_x, double min_y, double max_x, double max_y) {
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);
            PriorityQueue<DiDiCheckin> sortedData = new PriorityQueue<>();
            while (in.ready()) {
                String line = in.readLine();
                String[] parts = line.split(",");
                long id = Long.parseLong(parts[0]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                long millis = sdf.parse(parts[1]).getTime();
                int timestamp = (int) (millis / 1000);
                double lnt = Double.parseDouble(parts[2]);
                double lat = Double.parseDouble(parts[3]);
                sortedData.add(new DiDiCheckin(id, timestamp, lnt, lat));
            }
            System.out.println("total task num = " + sortedData.size());
            // sample tasks into instances
            // calculate mbr at the same time
            WorkingRegion mbr = new WorkingRegion(min_x, min_y, max_x, max_y);
            ArrayList<ArrayList<Point>> instances = new ArrayList<>();
            DiDiCheckin pt = sortedData.poll();
            int result_num = 0;
            HashMap<Long, PointTime> points = new HashMap<>();
            while (sortedData.size() > 0 && instances.size() < instance) {
                points.clear();
                long startTime = pt.timestamp;
                long endTime = startTime;
                while (endTime - startTime < 120) {
                    points.put(pt.orderId, new PointTime(pt.orderId, pt.timestamp, pt.latitude, pt.longitude));
                    mbr.extend(pt.latitude, pt.longitude);
                    pt = sortedData.poll();
                    if (pt == null) {
                        break;
                    }
                    endTime = pt.timestamp;
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
