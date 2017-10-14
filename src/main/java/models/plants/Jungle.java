package models.plants;

import models.Environment;

public class Jungle extends Plant {

	public Jungle(){
		super(Environment.JUNGLE_STRENGTH,Environment.JUNGLE_MOISTURE);
	}
	
	public Jungle(int rootStrength, int moisture){
		
		super(rootStrength,moisture);
	}
}
