package models;

import java.util.Comparator;

public class WaterElevationPair extends Pair implements Comparator<WaterElevationPair>, Comparable<WaterElevationPair> {

	public WaterElevationPair(int x, int y) {
		super(x, y);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(WaterElevationPair o) {

		int diff = this.x - o.x;
		
		if (diff == 0) {
			
			diff = this.y - o.y;
		}
		
		return diff;
	}

	@Override
	public int compare(WaterElevationPair o1, WaterElevationPair o2) {

		return o1.compareTo(o2);
	}
	
	@Override
	public WaterElevationPair clone() {

		return new WaterElevationPair(x, y);
	}
	
	public int fullWaterHeight() {
		
		return x * Environment.WATER_PER_ELEVATION + y;
	}
	
	public void addPair(WaterElevationPair o) {
		x += o.x;
		y += o.y;
	}
}
