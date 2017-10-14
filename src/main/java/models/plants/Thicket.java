package models.plants;

import models.Environment;

public class Thicket extends Plant {
	
	public Thicket(){
		super(Environment.THICKET_STRENGTH,Environment.THICKET_MOISTURE);
	}
	
	public Thicket(int rootStrength, int moisture){
		
		super(rootStrength,moisture);
	}
}
