package org.geocrowd;

public enum WorkerType {
	GENERIC,// have id, lat, lon, capacity, activeness
	REGION,// have work region
	SENSING,// nothing
	EXPERT,// have expertise(an array of integer)
	TRUST// have score
}