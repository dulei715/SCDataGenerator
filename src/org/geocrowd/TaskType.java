package org.geocrowd;

public enum TaskType {
	GENERIC,// have lat, lon, start time, expiry time
	REWARD,// have reward
	SENSING,// have region
	EXPERT// have category(expertise)
}