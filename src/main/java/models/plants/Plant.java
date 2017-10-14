package models.plants;

import java.util.Random;

import models.Environment;
import models.TheRandom;

public abstract class Plant {
	//put functions of plants
	
	private int rootstrength;
	private int moistureRequired;
	private int index;
	private int moisture;
	private int rot;
	
	public Plant(int rootstrength, int moistureRequired, int index){
		this.rootstrength = rootstrength;
		this.moistureRequired = moistureRequired;
		this.index = index;
		this.moisture = moistureRequired;
		this.rot = 0;
	}
	
	public Plant(int rootstrength, int moistureRequired){
		this.rootstrength = rootstrength;
		this.moistureRequired = moistureRequired;
		this.index = 0;
		this.rot = 0;
	}
	
	public int getRot(){
	   return rot;
	}
	
	public void rot(){
	   rot--;
	}

	public int getRootstrength() {
		return rootstrength;
	}

	public void setRootstrength(int rootstrength) {
		
		if (rootstrength < 0) this.rootstrength = 0;
		else this.rootstrength = rootstrength;
	}

	public int getMoistureRequired() {
		return moistureRequired;
	}
	
	public void setMoistureRequired(int moistureRequired) {
		if (moistureRequired < 0) this.moistureRequired = 0;
		else this.moistureRequired = moistureRequired;
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

	public void geneticDrift() {
		
		Random rand = TheRandom.getInstance().get();
		
		if (rand.nextFloat() < Environment.CHANCE_OF_MUTATION){
		
			switch(rand.nextInt(4)){
			
			case 0:
			
				setRootstrength(getRootstrength() - 1);
				break;
				
			case 1:
				
				setRootstrength(getRootstrength() + 1);
				break;
	
			
			case 2:
				
				setMoistureRequired(getMoistureRequired() - 1);
				break;
				
			case 3:
				
				setMoistureRequired(getMoistureRequired() + 1);
				break;
			}
		}
	}
}
