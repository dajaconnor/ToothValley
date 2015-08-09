package models;

public class Thicket extends Plant {
	
	public Thicket(){
		super(Environment.THICKET_STRENGTH,Environment.THICKET_SATURATION,Environment.THICKET_MOISTURE);
	}
	
	public Thicket(int rootStrength, int moisture){
		
		super(rootStrength,Environment.THICKET_SATURATION,moisture);
	}
}
