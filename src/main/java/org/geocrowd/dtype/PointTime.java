package org.geocrowd.dtype;



public class PointTime extends Point implements Comparable<PointTime> {

	int timestamp = 0;
	long userid;
	
	public PointTime(long userid,int timestamp, double x, double y) {
		super(x, y);
		this.userid = userid;
		this.timestamp = timestamp;
		// TODO Auto-generated constructor stub
	}


	

	public int getTimestamp() {
		return timestamp;
	}




	public long getUserid() {
		return userid;
	}




	@Override
	public int compareTo(PointTime o) {
		if (this.timestamp > o.timestamp)
			return 1;
		else if (this.timestamp < o.timestamp)
			return -1;
		else 
			return 0;
	}
}
