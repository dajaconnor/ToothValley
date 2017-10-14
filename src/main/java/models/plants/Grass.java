package models.plants;

import models.Environment;

public class Grass extends Plant {
	
	public Grass(){
		super(Environment.GRASS_STRENGTH,Environment.GRASS_MOISTURE);
	}
	
	public Grass(int rootStrength, int moisture){
		
		super(rootStrength,moisture);
	}
}
