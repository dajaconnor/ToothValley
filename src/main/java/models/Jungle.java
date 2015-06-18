package models;

public class Jungle extends Plant {

	public Jungle(){
		super(Environment.JUNGLE_STRENGTH,Environment.JUNGLE_SATURATION,Environment.JUNGLE_MOISTURE);
	}
	
	public Jungle(int rootStrength, int moisture){
		
		super(rootStrength,Environment.JUNGLE_SATURATION,moisture);
	}
}
