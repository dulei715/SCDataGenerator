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
package org.geocrowd.common.crowd;

import org.geocrowd.datasets.params.GeocrowdConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericWorker.
 * 
 * @author ubriela
 */
public class GenericWorker {

	/** The user id. */
	public String id;
	
    /**
     * The entry time.
     */
    private int onlineTime;

	/** The lat. */
	protected double lat;

	/** The lng. */
	protected double lng;

	/** The maximum number of tasks the worker can perform per time instance. */
	protected int capacity;

	/**
	 * The number indicate how active the worker is between 0 and 1. Active
	 * workers are likely to travel and to perform tasks.
	 */
	private double activeness;

    private double reliability;

    private WorkingRegion mbr;


	/**
	 * Instantiates a new generic worker.
	 */
	public GenericWorker() {
		super();
	}

	public GenericWorker(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	/**
	 * Instantiates a new generic worker.
	 * 
	 * @param workerId
	 *            the user id
	 * @param lat
	 *            the lat
	 * @param lng
	 *            the lng
	 * @param capacity
	 *            the max task no
	 */
	public GenericWorker(String workerId, double lat, double lng, int capacity) {
		super();
		this.id = workerId;
		this.lat = lat;
		this.lng = lng;
		this.capacity = capacity;
	}

	/**
	 * Gets the latitude.
	 * 
	 * @return the latitude
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * Gets the longitude.
	 * 
	 * @return the longitude
	 */
	public double getLng() {
		return lng;
	}

	public void setId(String workerId) {
		this.id = workerId;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Gets the max task no.
	 * 
	 * @return the max task no
	 */
	public int getCapacity() {
		// return 1;
		return capacity;
	}

	/**
	 * Gets the user id.
	 * 
	 * @return the user id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Inc max task no.
	 */
	public void incCapacity() {
		capacity++;
	}

	public double getActiveness() {
		return activeness;
	}

	public void setActiveness(double activeness) {
		this.activeness = activeness;
	}
	

	public int getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(int onlineTime) {
        this.onlineTime = onlineTime;
    }

    public WorkingRegion getMbr() {
        return mbr;
    }



    public void setMbr(WorkingRegion mbr) {
        this.mbr = mbr;
    }

    /**
     * Sets the max lat.
     *
     * @param l
     *            the new max lat
     */
    public void setMaxLat(double l) {
        mbr.setMaxLat(l);
    }

    /**
     * Sets the max lng.
     *
     * @param l
     *            the new max lng
     */
    public void setMaxLng(double l) {
        mbr.setMaxLng(l);
    }

    /**
     * Sets the min lat.
     *
     * @param l
     *            the new min lat
     */
    public void setMinLat(double l) {
        mbr.setMinLat(l);
    }

    /**
     * Sets the min lng.
     *
     * @param l
     *            the new min lng
     */
    public void setMinLng(double l) {
        mbr.setMinLng(l);
    }


    @Override
	public String toString() {
        String split = GeocrowdConstants.delimiter_dataset;
		return getId() + split + getLat() + split + getLng() + split + getCapacity() + split + getActiveness()
                + split + "[" + getMbr().getMinLat() + split + getMbr().getMinLng() + split + getMbr().getMaxLat()
                + split + getMbr().getMaxLng() + "]" + getReliability();
	}

    public double getReliability() {
        return reliability;
    }

    public void setReliability(double reliability) {
        this.reliability = reliability;
    }
}
