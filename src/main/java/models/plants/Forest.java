package models.plants;

import models.Environment;

public class Forest extends Plant {

	public Forest(){
		super(Environment.FOREST_STRENGTH,Environment.FOREST_MOISTURE);
	}
	
	public Forest(int rootStrength, int moisture){
		
		super(rootStrength,moisture);
	}
}
