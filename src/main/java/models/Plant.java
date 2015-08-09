package models;

public abstract class Plant {
	//put functions of plants
	
	private int rootstrength;
	private double maxSaturation;
	private int moistureRequired;
	private int index;
	private int moisture;
	/*
	public static Grass GRASS = new Grass();
	public static Thicket THICKET = new Thicket();
	public static Forest FOREST = new Forest();
	public static Jungle JUNGLE = new Jungle();
	
	public static Plant TYPES[]={JUNGLE,FOREST,THICKET,GRASS};*/
	
	public Plant(int rootstrength, double maxSaturation, int moistureRequired, int index){
		this.rootstrength = rootstrength;
		this.maxSaturation = maxSaturation;
		this.moistureRequired = moistureRequired;
		this.index = index;
		this.moisture = moistureRequired;
	}
	
	public Plant(int rootstrength, double maxSaturation, int moistureRequired){
		this.rootstrength = rootstrength;
		this.maxSaturation = maxSaturation;
		this.moistureRequired = moistureRequired;
		this.index = 0;
	}

	public int getRootstrength() {
		return rootstrength;
	}

	public void setRootstrength(int rootstrength) {
		this.rootstrength = rootstrength;
	}

	public double getMaxSaturation() {
		return maxSaturation;
	}

	public void setMaxSaturation(int maxSaturation) {
		this.maxSaturation = maxSaturation;
	}

	public int getMoistureRequired() {
		return moistureRequired;
	}
	
	public void setMoistureRequired(int moistureRequired) {
		this.moistureRequired = moistureRequired;
	}
	
	public int getMoisture() {
		return moisture;
	}

	public void setMoisture(int moisture) {
		this.moisture = moisture;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
