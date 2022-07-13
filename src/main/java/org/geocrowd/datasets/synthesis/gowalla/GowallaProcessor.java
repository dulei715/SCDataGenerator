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
package org.geocrowd.datasets.synthesis.gowalla;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Date;
import java.util.logging.SimpleFormatter;

import org.geocrowd.DatasetEnum;
import org.geocrowd.TaskCategoryEnum;
import org.geocrowd.TaskType;
import org.geocrowd.WorkerType;
import org.geocrowd.common.crowd.ExpertWorker;
import org.geocrowd.common.crowd.GenericWorker;
import org.geocrowd.common.crowd.RegionWorker;
import org.geocrowd.common.crowd.WorkerFactory;
import org.geocrowd.common.crowd.WorkingRegion;
import org.geocrowd.common.entropy.Coord;
import org.geocrowd.common.entropy.EntropyUtility;
import org.geocrowd.common.entropy.Observation;
import org.geocrowd.common.utils.TaskUtility;
import org.geocrowd.common.utils.Utils;
import org.geocrowd.datasets.params.GeocrowdConstants;
import org.geocrowd.datasets.params.GowallaConstants;
import org.geocrowd.datasets.synthetic.GenericProcessor;
import org.geocrowd.datasets.synthetic.TaskCategoryGenerator;
import org.geocrowd.dtype.Point;
import org.geocrowd.dtype.PointTime;

import javax.xml.bind.DatatypeConverter;

/**
 * The Class PreProcess.
 * 
 * @author Leyla & Hien To
 */
public class GowallaProcessor extends GenericProcessor {

	/**
	 * Instantiates a new pre process.
	 */
	public GowallaProcessor(int instances, WorkerType workerType, TaskType taskType, TaskCategoryEnum taskCategory) {
		super(instances, DatasetEnum.GOWALLA, workerType, taskType, taskCategory);
	}

	/**
	 * Giving a set of points, compute the MBR covering all the points.
	 */
	@Override
	public void computeBoundary() {
		try {
			FileReader reader = new FileReader(
					GowallaConstants.gowallaFileName_CA);
			BufferedReader in = new BufferedReader(reader);
			int cnt = 0;
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split("\\s");
				Double lat = Double.parseDouble(parts[2]);
				Double lng = Double.parseDouble(parts[3]);

				if (lat < minLat)
					minLat = lat;
				if (lat > maxLat)
					maxLat = lat;
				if (lng < minLng)
					minLng = lng;
				if (lng > maxLng)
					maxLng = lng;
				cnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// dump boundary to file
		dumpBoundary();
	}

	/**
	 * Compute sync location density.
	 * 
	 * @return the hashtable
	 */
	@Override
	public Hashtable<Integer, Hashtable<Integer, Integer>> computeLocationDensity() {
		String workerFilePath = Utils.datasetToWorkerPath(DATA_SET);
		Hashtable<Integer, Hashtable<Integer, Integer>> densities = new Hashtable<Integer, Hashtable<Integer, Integer>>();

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < GeocrowdConstants.TIME_INSTANCE; i++) {
			try {
				FileReader file = new FileReader(workerFilePath + i + ".txt");
				BufferedReader in = new BufferedReader(file);

				// create whole path automatically if not exist
				String filename = 
						Utils.datasetToWorkerPointPath() + i + ".txt";
				Path pathToFile = Paths.get(filename);
				Files.createDirectories(pathToFile.getParent());
				
				FileWriter writer = new FileWriter(filename);
				BufferedWriter out = new BufferedWriter(writer);

				while (in.ready()) {
					String line = in.readLine();
					String[] parts = line.split(",");
					Double lat = Double.parseDouble(parts[1]);
					Double lng = Double.parseDouble(parts[2]);
					sb.append(lat + "\t" + lng + "\n");
					int row = latToRowIdx(lat);
					int col = lngToColIdx(lng);
					if (densities.containsKey(row)) {
						if (densities.get(row).containsKey(col))
							densities.get(row).put(col,
									densities.get(row).get(col) + 1);
						else
							densities.get(row).put(col, 1);
					} else {
						Hashtable<Integer, Integer> rows = new Hashtable<Integer, Integer>();
						rows.put(col, 1);
						densities.put(row, rows);
					}
				}

				out.write(sb.toString());
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return densities;
	}

	/**
	 * Compute sync location density.
	 * 
	 * @return the hashtable
	 */
	public Hashtable<Integer, Hashtable<Integer, Integer>> computeLocationDensityFromFile() {
		String filePath = GowallaConstants.gowallaFileName_CA_loc;

		Hashtable<Integer, Hashtable<Integer, Integer>> densities = new Hashtable<Integer, Hashtable<Integer, Integer>>();
		try {
			FileReader file = new FileReader(filePath);
			BufferedReader in = new BufferedReader(file);
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split(GeocrowdConstants.delimiter.toString());
				Double lat = Double.parseDouble(parts[0]);
				Double lng = Double.parseDouble(parts[1]);
				int row = latToRowIdx(lat);
				int col = lngToColIdx(lng);
				if (densities.containsKey(row)) {
					if (densities.get(row).containsKey(col))
						densities.get(row).put(col,
								densities.get(row).get(col) + 1);
					else
						densities.get(row).put(col, 1);
				} else {
					Hashtable<Integer, Integer> rows = new Hashtable<Integer, Integer>();
					rows.put(col, 1);
					densities.put(row, rows);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return densities;
	}

	/**
	 * Extract coordinate (only) from datafile.
	 * 
	 * @param filename
	 * @param sampleRate
	 */
	public void extractCoords(String filename, int sampleRate) {
		try {
			FileReader reader = new FileReader(filename);
			BufferedReader in = new BufferedReader(reader);
			StringBuffer sb = new StringBuffer();
			int cnt = 0;
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split("\\s");
				Double lat = Double.parseDouble(parts[2]);
				Double lng = Double.parseDouble(parts[3]);
				if (Math.random() < sampleRate / 100.0) {
					sb.append(lat + "\t" + lng + "\n");
					cnt++;
				}
			}

			Path pathToFile = Paths.get(filename + ".dat");
			Files.createDirectories(pathToFile.getParent());
			
			FileWriter writer = new FileWriter(filename + ".dat");
			BufferedWriter out = new BufferedWriter(writer);
			out.write(sb.toString());
			out.close();

			System.out.println("Number of checkins: " + cnt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extract MBR of the workers from datafile Export recent location of each
	 * user to a file.
	 * 
	 * @param filename
	 *            the filename
	 */
	public void extractMBRs(String filename) {
		try {
			FileReader reader = new FileReader(filename);
			BufferedReader in = new BufferedReader(reader);
			StringBuffer sb = new StringBuffer();
			HashMap<Integer, ArrayList<Point>> data = new HashMap<Integer, ArrayList<Point>>();
			ArrayList<Point> points = new ArrayList<Point>();
			Integer prev_id = -1;
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split("\\s");
				Integer id = Integer.parseInt(parts[0]);
				Double lat = Double.parseDouble(parts[2]);
				Double lng = Double.parseDouble(parts[3]);
				if (id.equals(prev_id)) { // add to current list
					points.add(new Point(lat, lng));
				} else {
					// create new list
					points = new ArrayList<Point>();
					points.add(new Point(lat, lng));

					// add current list to data
					data.put(prev_id, points);

					sb.append(lat + "\t" + lng + "\n");
				}

				prev_id = id;
			}
			data.put(prev_id, points);

			Path pathToFile = Paths.get(filename + ".dat");
			Files.createDirectories(pathToFile.getParent());
			
			FileWriter writer = new FileWriter(filename + ".dat");
			BufferedWriter out = new BufferedWriter(writer);
			out.write(sb.toString());
			out.close();
			sb.delete(0, sb.length());

			// iterate through HashMap keys Enumeration
			double sum = 0;
			int count = 0;
			double maxMBR = 0;
			Iterator<Integer> it = data.keySet().iterator();
			while (it.hasNext()) {
				Integer t = it.next();
				ArrayList<Point> pts = data.get(t);
				WorkingRegion mbr = new WorkingRegion(Utils.computeMBR(pts));
				double d = TaskUtility.diagonalLength(mbr);
				sum += d;
				count++;
				if (d > maxMBR)
					maxMBR = d;
				double mcd = Utils.MCD(pts.get(0), pts);
				sb.append(t.toString() + "\t" + mbr.getMinLat() + "\t"
						+ mbr.getMinLng() + "\t" + mbr.getMaxLat() + "\t"
						+ mbr.getMaxLng() + "\t" + d + "\t" + mcd + "\n");
			}

			writer = new FileWriter(filename + ".mbr.txt");
			out = new BufferedWriter(writer);
			out.write(sb.toString());
			out.close();

			System.out.println("Number of users: " + data.keySet().size());
			System.out.println("Average users' MBR size: " + sum / count);
			System.out.println("Max users' MBR size: " + maxMBR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * partition all_data_time into T instances based on timestamp
	 * 
	 * @param filename
	 * @param instance
	 *            : the number of time instance
	 */
	public void extractWorkersInstances(String filename, String outputPath,
			int instance) {
		try {
			FileReader reader = new FileReader(filename);
			BufferedReader in = new BufferedReader(reader);
			StringBuffer sb = new StringBuffer();
			HashMap<Integer, ArrayList<PointTime>> data = new HashMap<Integer, ArrayList<PointTime>>();
			ArrayList<PointTime> points = new ArrayList<PointTime>();
			PriorityQueue<PointTime> sortedData = new PriorityQueue<>();
			Integer prev_id = -1;
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split("\\s");
				Integer id = Integer.parseInt(parts[0]);
				String time = parts[1];
				String timeParts[] = time.split("-");
				int year = Integer.valueOf(timeParts[0]);
				int month = Integer.valueOf(timeParts[1]);
				int day = Integer.valueOf(timeParts[2].substring(0, 2));
				int hour = Integer.valueOf(timeParts[2].substring(3, 5));
				int timestamp = ((year - 2005) * 365 + month * 30 + day) * 24
						+ hour;

				Double lat = Double.parseDouble(parts[2]);
				Double lng = Double.parseDouble(parts[3]);

				/**
				 * Add point to queue
				 */
				PointTime pt = new PointTime(id, timestamp, lat, lng);
				sortedData.add(pt);

				if (id.equals(prev_id)) { // add to current list
					points.add(pt);
				} else {
					// create new list
					points = new ArrayList<PointTime>();
					points.add(pt);

					// add current list to data
					data.put(prev_id, points);

					sb.append(lat + "\t" + lng + "\n");
				}

				prev_id = id;
			}
			data.put(prev_id, points);

			Path pathToFile = Paths.get(filename + ".dat");
			Files.createDirectories(pathToFile.getParent());
			
			FileWriter writer = new FileWriter(filename + ".dat");
			BufferedWriter out = new BufferedWriter(writer);
			out.write(sb.toString());
			out.close();

			// iterate through HashMap keys Enumeration
			double sum = 0;
			int count = 0;
			double maxMBR = 0;
			HashMap<Integer, PointTime> userLocs = new HashMap<Integer, PointTime>();
			Iterator<Integer> it = data.keySet().iterator();
			while (it.hasNext()) {
				Integer t = it.next();
				ArrayList<PointTime> pts = data.get(t);
				ArrayList<Point> pts_t = new ArrayList<Point>(pts);
				WorkingRegion mbr = new WorkingRegion(Utils.computeMBR(pts_t));

				userLocs.put(
						t,
						new PointTime(t, 0,
								(mbr.getMaxLat() + mbr.getMinLat()) / 2.0, (mbr
										.getMaxLng() + mbr.getMinLng()) / 2.0));
			}

			/**
			 * Create data for each time instance
			 */
			int width = sortedData.size() / instance;
			ArrayList<PointTime> allDataArr = new ArrayList<PointTime>();
			while (!sortedData.isEmpty()) {
				allDataArr.add(sortedData.poll());
			}

			for (int t = 0; t < instance; t++) {
				HashMap<Integer, PointTime> currUserLocs = (HashMap<Integer, PointTime>) userLocs
						.clone();

				/**
				 * construct a dictionary of users that update their locations
				 */
				HashMap<Long, ArrayList<PointTime>> userUpdates = new HashMap<>();
				for (PointTime point : allDataArr.subList(t * width, t * width
						+ width)) {
					if (userUpdates.containsKey(point.getUserid()))
						userUpdates.get(point.getUserid()).add(point);
					else {
						ArrayList<PointTime> arr = new ArrayList<PointTime>();
						arr.add(point);
						userUpdates.put(point.getUserid(), arr);
					}
				}

				/**
				 * For each user
				 */
				int updateCount = 0; // number of users update their locations
				Iterator currIter = currUserLocs.keySet().iterator();
				while (currIter.hasNext()) {
					Integer key = (Integer) currIter.next();
					ArrayList<PointTime> new_locs = null;
					if (userUpdates.containsKey(key))
						new_locs = userUpdates.get(key);
					else
						continue;

					updateCount++;
					ArrayList<Point> pts_t = new ArrayList<Point>(new_locs);
					WorkingRegion mbr = new WorkingRegion(
							Utils.computeMBR(pts_t));

					PointTime p = new PointTime(key, 0,
							(mbr.getMaxLat() + mbr.getMinLat()) / 2.0,
							(mbr.getMaxLng() + mbr.getMinLng()) / 2.0);
					currUserLocs.put(key, p);
				}

				System.out.println("updates count " + updateCount);
				saveWorkerInstance(outputPath, t, currUserLocs);

			}

			System.out.println("Number of users: " + data.keySet().size());
			System.out.println("Average users' MBR size: " + sum / count);
			System.out.println("Max users' MBR size: " + maxMBR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void extractWorkersInstances2(String filename, String outputPath, int instance, double min_x, double min_y,
                                         double max_x, double max_y) {
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);
            PriorityQueue<PointTime> sortedData = new PriorityQueue<>();
            while (in.ready()) {
                String line = in.readLine();
                String[] parts = line.split("\\s");
                Integer id = Integer.parseInt(parts[0]);
                String time = parts[1];
                long millis = DatatypeConverter.parseDateTime(time).getTimeInMillis();
                String timeParts[] = time.split("-");
                int timestamp = (int) (millis / 1000);
                Double lat = Double.parseDouble(parts[2]);
                Double lng = Double.parseDouble(parts[3]);

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
                while (endTime - startTime < 3600*24) {
                    points.put(pt.getUserid(), pt);
                    mbr.extend(pt.getX(), pt.getY());
                    pt = sortedData.poll();
                    if (pt == null) {
                        endTime = Integer.MAX_VALUE;
                    } else {
                        endTime = pt.getTimestamp();
                    }
                }
                if (points.size() > 150) {
                    ArrayList<PointTime> temp = new ArrayList<>();
                    temp.addAll(points.values());
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

	private void saveWorkerInstance(String path, int t,
			HashMap<Integer, PointTime> userLocs) {
		StringBuffer sb = new StringBuffer();
		for (PointTime p : userLocs.values()) {
			// p.debug();
			// System.out.println(p.getY());
			// System.out.println(p.getX() + "\t" + p.getY());
			sb.append(p.getUserid() + "\t" + p.getX() + "\t" + p.getY() + "\n");
		}

		FileWriter writer;
		try {
			String filename = path + String.format("%04d", t) + ".txt";
			Path pathToFile = Paths.get(filename);
			Files.createDirectories(pathToFile.getParent());
			
			writer = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(sb.toString(), 0, sb.length() - 1);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Get subset of the gowalla dataset, within a rectangle.
	 * 
	 * @param filename
	 *            the filename
	 * @param min_x
	 *            the min_x
	 * @param min_y
	 *            the min_y
	 * @param max_x
	 *            the max_x
	 * @param max_y
	 *            the max_y
	 */
	public void filterInput(String filename, double min_x, double min_y,
			double max_x, double max_y) {
		System.out.println("Filtering location data...");
		try {
			FileReader reader = new FileReader(GowallaConstants.gowallaFileName);
			BufferedReader in = new BufferedReader(reader);
			
			Path pathToFile = Paths.get(filename);
			Files.createDirectories(pathToFile.getParent());
			
			FileWriter writer = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(writer);

			int cnt = 0;
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split(GeocrowdConstants.delimiter.toString());
				Integer userID = Integer.parseInt(parts[0]);
				Double lat = Double.parseDouble(parts[2]);
				Double lng = Double.parseDouble(parts[3]);
				Integer pointID = Integer.parseInt(parts[4]);
				if ((lat < min_x) || (lat > max_x)
						|| (lng < (min_y) || (lng > (max_y))))
					continue;

				out.write(line + "\n");

				cnt++;
			}
            System.out.println("filtered worker num = " + cnt);
			out.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Assuming Gowalla users are the workers, we assume all users who checked
	 * in during the day as available workers for that. The method returns a
	 * hashtable <day, workers>
	 * 
	 * This function is used as an input for saveWorkers()
	 * 
	 * @param datasetfile
	 *            the datasetfile
	 * @return the hashtable
	 */
	public Hashtable<Date, ArrayList<GenericWorker>> generateRealWorkers(
			String datasetfile) {
		Hashtable<Date, ArrayList<GenericWorker>> hashTable = new Hashtable();
		try {
			FileReader reader = new FileReader(
					GowallaConstants.gowallaFileName_CA);
			BufferedReader in = new BufferedReader(reader);
			int cnt = 0;
			TaskCategoryGenerator tcGen = new TaskCategoryGenerator(
					GeocrowdConstants.TASK_CATEGORY_NUMBER);
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split(GeocrowdConstants.delimiter.toString());
				String[] DateTimeStr = parts[1].split("T");
				Date date = Date.valueOf(DateTimeStr[0]);
				
				GenericWorker w = WorkerFactory.getWorker(workerType,
						Double.parseDouble(parts[2]),
						Double.parseDouble(parts[3]));
				w.setId(parts[0]);
				w.setCapacity(0);	// not used yet
				w.setActiveness(0);	// not used yet
				
				if (workerType == WorkerType.REGION
						|| workerType == WorkerType.EXPERT
						|| workerType == WorkerType.SENSING) {
					RegionWorker rw = (RegionWorker) w;
					WorkingRegion mbr = new WorkingRegion(w.getLat() - 2 / resolution, w.getLng()
							- 2 / resolution, w.getLat() + 2 / resolution, w.getLng() + 2
							/ resolution);
					// make sure the MBR is within the boundary
					if (mbr.getMinLat() < minLat)
						mbr.setMinLat(minLat);
					if (mbr.getMaxLat() > maxLat)
						mbr.setMaxLat(maxLat);
					if (mbr.getMinLng() < minLng)
						mbr.setMinLng(minLng);
					if (mbr.getMaxLng() > maxLng)
						mbr.setMaxLng(maxLng);
					rw.setMbr(mbr);
				}
				if (workerType == WorkerType.EXPERT) {
					ExpertWorker ew = (ExpertWorker) w;
					ew.addExpertise(tcGen.nextTaskCategory(taskCategoryType));
				}
			
				if (!hashTable.containsKey(date)) {
					ArrayList<GenericWorker> workers = new ArrayList<GenericWorker>();
					workers.add(w);
					hashTable.put(date, workers);
				} else {
					ArrayList<GenericWorker> workers = hashTable.get(date);
					boolean found = false; // check if the worker is already in
											// the worker list, if yes -->
											// update his maxTass and R
					for (GenericWorker o : workers) {
						RegionWorker rw = (RegionWorker) w;
						if (rw.getId().equals(w.getId())) {
							
							o.incCapacity(); // set maxTask as the number of
												// check-ins

							// set working region R of each worker as MBR of his
							// check-ins locations
							if (w.getLat() < rw.getMbr().getMinLat())
								rw.setMinLat(w.getLat());
							if (w.getLat() > rw.getMbr().getMaxLat())
								rw.setMaxLat(w.getLat());
							if (w.getLng() < rw.getMbr().getMinLng())
								rw.setMinLng(w.getLng());
							if (w.getLng() > rw.getMbr().getMaxLng())
								rw.setMaxLng(w.getLng());

							found = true;
							break;
						}
					}
					if (!found) {
						workers.add(w);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hashTable;
	}

	/**
	 * Used as an input for saveLocationEntropy.
	 * 
	 * @return which location belongs to which grid cell
	 */
	public Hashtable<Integer, Coord> locIdToCellIndices() {
		Hashtable hashTable = new Hashtable();
		try {
			FileReader reader = new FileReader(
					GowallaConstants.gowallaFileName_CA);
			BufferedReader in = new BufferedReader(reader);
			int cnt = 0;
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split("\\s");
				Integer userID = Integer.parseInt(parts[0]);
				Double lat = Double.parseDouble(parts[2]);
				Double lng = Double.parseDouble(parts[3]);
				Integer pointID = Integer.parseInt(parts[4]);
				int row = latToRowIdx(lat);
				int col = lngToColIdx(lng);
				Coord g = new Coord(row, col);
				if (!hashTable.containsKey(pointID)) {
					hashTable.put(pointID, g);
				}

				cnt++;
			}
			System.out.println("Hashtable<location, grid> size: "
					+ hashTable.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hashTable;
	}

	/**
	 * Read data from a file, e.g., gowalla file
	 * 
	 * @param datasetfile
	 *            the datasetfile
	 * @return a hashtable <location id, occurrences>
	 */
	public Hashtable<Integer, ArrayList<Observation>> readRealEntropyData(
			String datasetfile) {
		Hashtable hashTable = new Hashtable();
		try {
			FileReader reader = new FileReader(datasetfile);
			BufferedReader in = new BufferedReader(reader);
			int cnt = 0;
			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split("\\s");
				Integer userID = Integer.parseInt(parts[0]);
				Double lat = Double.parseDouble(parts[2]);
				Double lng = Double.parseDouble(parts[3]);
				Integer pointID = Integer.parseInt(parts[4]);
				if (!hashTable.containsKey(pointID)) {
					ArrayList<Observation> obs = new ArrayList<Observation>();
					Observation o = new Observation(userID);
					obs.add(o);
					hashTable.put(pointID, obs);
				} else {
					ArrayList<Observation> u = (ArrayList<Observation>) hashTable
							.get(pointID);
					boolean found = false;
					for (Observation o : u) {
						if (o.getUserId() == userID) {
							o.incObserveCount();
							found = true;
							break;
						}
					}
					if (!found) {
						Observation o = new Observation(userID);
						u.add(o);
					}
				}
				cnt++;
			}
			System.out
					.println("Hashtable <location, occurrences> size: "
							+ hashTable.size()
							+ " (the number of cells with checkins)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hashTable;
	}

	/**
	 * Save what location in what grid and its corresponding location entropy,
	 * the entropy information is get from a file which was generated before.
	 * 
	 * @param hashTable
	 *            the hash table
	 */
	public void saveLocationEntropy(Hashtable<Integer, Coord> hashTable) {
		try {
			FileReader reader = new FileReader(
					GowallaConstants.gowallaEntropyFileName);
			BufferedReader in = new BufferedReader(reader);

			String entropyPath = EntropyUtility.datasetToEntropyPath(DATA_SET);
			
			// create whole path automatically if not exist
			Path pathToFile = Paths.get(entropyPath);
			Files.createDirectories(pathToFile.getParent());
			
			FileWriter writer = new FileWriter(entropyPath);
			BufferedWriter out = new BufferedWriter(writer);

			while (in.ready()) {
				String line = in.readLine();
				String[] parts = line.split(",");
				int pointId = Integer.parseInt(parts[0]);
				double entropy = Double.parseDouble(parts[1]);
				Coord g = hashTable.get(pointId);
				out.write(g.getRowId() + "," + g.getColId() + "," + entropy
						+ "\n");
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save real workers.
	 * 
	 * Fix the number of time instances
	 * 
	 * @param hashTable
	 *            the hash table
	 */
	public void saveRealWorkers(
			Hashtable<Date, ArrayList<ExpertWorker>> hashTable) {
		try {
			// sort key and iterate based on key
			List<Date> dates = new ArrayList<Date>(hashTable.keySet());
			Collections.sort(dates);

			Integer instanceCnt = 0;
			Integer dayCnt = 0;
			int workerCount = 0;
			int daysPerInstance = dates.size()
					/ GeocrowdConstants.TIME_INSTANCE;
			System.out.println("days per one instance: " + daysPerInstance);
			BufferedWriter out = null;
			for (Date date : dates) {
				if (dayCnt == 0) {
					String filename = 
							GowallaConstants.gowallaWorkerFileNamePrefix
							+ instanceCnt.toString() + ".txt";
					Path pathToFile = Paths.get(filename);
					Files.createDirectories(pathToFile.getParent());
					
					FileWriter writer = new FileWriter(filename);
					out = new BufferedWriter(writer);
				} else if (dayCnt == daysPerInstance) {
					instanceCnt++;
					dayCnt = 0;
					System.out.println("worker count: " + workerCount);
					workerCount = 0;
					out.close();
					continue;
				}

				dayCnt++;

				ArrayList<ExpertWorker> workers = hashTable.get(date);
				for (ExpertWorker o : workers) {
					out.write(o + "\n");
					workerCount++;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save real workers.
	 * 
	 * Given max online worker per instance
	 * 
	 * @param hashTable
	 *            the hash table
	 */
	public void saveRealWorkersMax2(
			Hashtable<Date, ArrayList<ExpertWorker>> hashTable) {
		try {
			// sort key and iterate based on key
			List<Date> dates = new ArrayList<Date>(hashTable.keySet());
			Collections.sort(dates);

			Integer instanceCnt = 0;
			Integer workerCnt = 0;
			BufferedWriter out = null;
			int i = 0;
			for (Date date : dates) {
				i++;
				if (i < GowallaConstants.MIN_TIME)
					continue;

				if (workerCnt == 0) {
					String filename = 
							GowallaConstants.gowallaWorkerFileNamePrefix
							+ instanceCnt.toString() + ".txt";
					Path pathToFile = Paths.get(filename);
					Files.createDirectories(pathToFile.getParent());
					
					FileWriter writer = new FileWriter(filename);
					out = new BufferedWriter(writer);
				} else if (workerCnt > GeocrowdConstants.WORKER_NUMBER) {
					instanceCnt++;
					System.out.println("worker count " + i + " : " + workerCnt);
					workerCnt = 0;
					out.close();
					continue;
				}

				workerCnt++;

				ArrayList<ExpertWorker> workers = hashTable.get(date);
				for (ExpertWorker o : workers) {
					out.write(o + "\n");
					workerCnt++;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save real workers.
	 * 
	 * Given max online worker per instance
	 * 
	 * @param hashTable
	 *            the hash table
	 */
	public void saveRealWorkersMax(
			Hashtable<Date, ArrayList<GenericWorker>> hashTable) {
		try {
			// sort key and iterate based on key
			List<Date> dates = new ArrayList<Date>(hashTable.keySet());
			Collections.sort(dates);

			Integer instanceCnt = 0;

			BufferedWriter out = null;
			int i = 0;
			for (Date date : dates) {
				i++;
				if (i < GowallaConstants.MIN_TIME)
					continue;

				String filename = 
						GowallaConstants.gowallaWorkerFileNamePrefix
						+ instanceCnt.toString() + ".txt";
				Path pathToFile = Paths.get(filename);
				Files.createDirectories(pathToFile.getParent());
				
				FileWriter writer = new FileWriter(filename);
				out = new BufferedWriter(writer);
				Integer workerCnt = 0;

				ArrayList<GenericWorker> workers = hashTable.get(date);
				for (GenericWorker o : workers) {
					out.write(o + "\n");
					workerCnt++;
				}

				instanceCnt++;
				System.out.println("worker count " + instanceCnt + " \t "
						+ workerCnt);
				out.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
