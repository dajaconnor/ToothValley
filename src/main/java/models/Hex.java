package models;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import enums.Direction;
import enums.DisplayType;
import models.plants.Forest;
import models.plants.Grass;
import models.plants.Jungle;
import models.plants.Plant;
import models.plants.Thicket;

public class Hex {

	private Pair hexID;
	private int density;
	private int moisture;
	private int moistureInAir;
	private int elevation;
	private Plant[] vegetation = new Plant[3];
	private Color color = DIRT;
	private int fire = 0;
	private Map<Direction, Integer> wind;
	private Integer tectonicState;


	public static int MAX_PLANT_STRENGTH = 16;
	public static Color WATER = new Color(68, 247, 235);
	public static Color DIRT = new Color(108, 91, 46);
	public static Color GRASS = new Color(251, 251, 77);
	public static Color THICKET = new Color(124, 226, 117);
	public static Color MARSH = new Color(77, 193, 129);
	public static Color FOREST = new Color(21, 181, 51);
	public static Color JUNGLE = new Color(6, 102, 23);
	public static Color FIRE = new Color(255, 0, 0);
	public static Color STONE = new Color(200, 220, 180);

	public Plant[] getVegetation() {
		return vegetation;
	}
	
	public Plant getRandomPlant(){
	   return vegetation[TheRandom.getInstance().get().nextInt(3)];
	}

	public void setVegetation(Plant[] vegetation) {
		this.vegetation = vegetation;
	}

	public int getDensity() {
		return density;
	}
	
	public int getSoil(){
		return Environment.MAX_DENSITY + 1 - density;
	}

	public int setDensity(int density) {

		if (density < 0) {
			this.density = 0;

			return density;
		}

		int leftover = 0;
		
		if (density > Environment.MAX_DENSITY){
		   this.density = Environment.MAX_DENSITY;
		   leftover = density - Environment.MAX_DENSITY;
		}else{
			this.density = density;
		}

		return leftover;
	}

	public int getMoistureInAir() {
		return moistureInAir;
	}

	public int getPressure() {

		return moistureInAir - elevation / 16 + 16;
	}

	public Map<Direction, Integer> getWind() {
		return wind;
	}

	public void setWind(Map<Direction, Integer> wind) {
		this.wind = wind;
	}
	
	public void clearWind(){
		
		this.wind = new HashMap<Direction, Integer>();
	}
	
	public void addWind(Direction direction, int strength){
		
		this.wind.put(direction, strength);
	}
	
	public void setMoistureInAir(int moistureInAir) {
		this.moistureInAir = moistureInAir;
	}
	
	public int alterMoistureInAir(int moistureInAir){
		
		if (this.moistureInAir + moistureInAir >= 0){
		
			this.moistureInAir += moistureInAir;
			return Math.abs(moistureInAir);
		} else {
			
			int altered = this.moistureInAir;
			this.moistureInAir = 0;
			return altered;
		}
	}

	public Hex() {

	}

	public Hex(Pair hexID, int elevation, int density, int moisture, int air,
			Plant[] plants) {

		this.hexID = hexID;
		this.elevation = elevation;
		this.density = density;
		this.moisture = moisture;
		this.moistureInAir = air;
		this.vegetation = plants;

		int standingWater = getStandingWater(0);

		if (standingWater > 0) {

			HexMap.getInstance().addSaturatedHex(hexID);

			if (standingWater > getPlantMoisture()) {
				this.color = WATER;
			}
		}
	}

	public Pair getHexID() {
		return this.hexID;
	}

	public void setHexID(Pair hexID) {
		this.hexID = hexID;
	}

	public int getMoisture(int standingBodyWater) {
		return this.moisture + standingBodyWater;
	}

	public int alterMoisture(int change, boolean isInBody) {

	   if (isInBody){
	      return 0;
	   }
	   
		int changed = 0;

		// change is negative
		if (change < 0) {

			if (getMoisture(0) + change < 0) {

				changed = getMoisture(0);
				moisture = 0;
			} else {

				changed = Math.abs(change);
				moisture += change;
			}
		} 
		
		// Change is positive
		else {
			
			Plant[] plants = this.getVegetation();
			
			for (Plant plant : plants) {

				if (plant != null &&
					change > changed &&
					plant.getMoistureRequired() > plant.getMoisture()) {
					
					int needs = plant.getMoistureRequired() - plant.getMoisture();
					
					// Need more then there is
					if (needs >= (change - changed)){

						// just get what's left
						plant.setMoisture(plant.getMoisture() + change - changed);
						changed = change;
					}
					
					// Don't need as much as there is
					else{
						
						plant.setMoisture(plant.getMoisture() + needs);
						
						// still some left over
						changed += needs;
					}
				}
			}
			
			// Ground gets whatever the plants leave behind
			moisture += change - changed;
		}

		return changed;
	}

	public int getElevation() {
		return this.elevation;
	}

	// returns true if it needs to be evaluated for a body
	public boolean setElevation(int elevation, boolean isInBody) {
	   
	   boolean evaluateThis = false;

	   if (isInBody && elevation > this.elevation){
	      
	      evaluateThis= true;
	   }
	   
		this.elevation = elevation;
		
		return evaluateThis;
	}

	public double getHumidity() {
		return ((double) this.moistureInAir * ((double) this.elevation / Environment.MAX_ELEVATION))
				/ Environment.AIR_DENSITY;
	}

	/**
	 * range = 0-63
	 * 
	 * @return
	 */
	public double getSaturation(int standingBodyWater) {
		return ((double) getMoisture(standingBodyWater)) / (double) (Environment.MAX_DENSITY + 1 - this.density);
	}

	public int getStandingWater(int standingBodyWater) {
	   
		int standingWater = getMoisture(standingBodyWater) - (Environment.MAX_DENSITY + 1 - this.density);

		if (standingWater < 0) {
			standingWater = 0;
		}

		return standingWater;
	}

	public int getCombinedElevation(int standingBodyWater) {
		return getStandingWater(standingBodyWater) / Environment.WATER_PER_ELEVATION + elevation;
	}
	
	public int getPrintElevation(int standingBodyWater) {
		
		return getStandingWater(standingBodyWater) + elevation;
	}

	public int getSoilStability() {
		int stability = density;
		for (Plant plant : vegetation) {
			if (plant != null) {
				stability += plant.getRootstrength() * 3;
			}
		}
		return stability;
	}

	/**
	 * This will attempt to add a plant. Will not add if not enough moisture,
	 * wrong saturation, or all current plants are stronger. This replaces the
	 * plant (if any) at allowed index.
	 * 
	 * @param plant
	 */
	public boolean addPlant(Plant plant, int standingBodyWater) {

		boolean success = false;
		TheRandom rand = TheRandom.getInstance();
		
		// If the new plant can tolerate the saturation range
		if (plant != null && getSoil() >= plant.getRootstrength() - 1 && plant.getMoistureRequired() - 1 <= getMoisture(standingBodyWater)){
			
			Plant newPlant = null;
			
			if (plant instanceof Grass){
				
				newPlant = new Grass(plant.getRootstrength(), plant.getMoistureRequired());
			}
			
			if (plant instanceof Thicket){
				
				newPlant = new Thicket(plant.getRootstrength(), plant.getMoistureRequired());
			}
			
			if (plant instanceof Forest){
				
				newPlant = new Forest(plant.getRootstrength(), plant.getMoistureRequired());
			}
			
			if (plant instanceof Jungle){
				
				newPlant = new Jungle(plant.getRootstrength(), plant.getMoistureRequired());
			}
			
			if (newPlant != null){
			
				// Evolve for rockier soil
				if (getSoil() == plant.getRootstrength() - 1){
					
					newPlant.setRootstrength(plant.getRootstrength() - 1);
				}
				
				// Evolve for better strength
				else if(getSoil() >= plant.getRootstrength() + Environment.EVOLUTION_DESIRE && rand.get().nextFloat() < Environment.EVOLUTION_RATE){
					
					newPlant.setRootstrength(plant.getRootstrength() + 1);
				}
				
				// Evolve for drier environment
				if (plant.getMoistureRequired() - 1 == getMoisture(standingBodyWater) && getMoisture(standingBodyWater) > 1) {
					
					newPlant.setMoistureRequired(getMoisture(standingBodyWater) - 1);
				}
				
				// Evolve for better hierarchy
				else if(plant.getMoistureRequired() + Environment.EVOLUTION_DESIRE <= getMoisture(standingBodyWater) && rand.get().nextFloat() < Environment.EVOLUTION_RATE){
					
					newPlant.setMoistureRequired(plant.getMoistureRequired() + 1);
				}
	
				// For all plant spots
				for (int index = 0; index < 3; index++) {
	
					if (addPlantAtIndex(newPlant, index)) {
	
						success = true;
						break;
					}
				}
			}
		}
		
		if (!success){
			
			if (plant instanceof Jungle){
				
				addPlant(new Forest(), standingBodyWater);
			}
		
			else if (plant instanceof Forest){
				
				addPlant(new Thicket(), standingBodyWater);
			}
			
			else if (plant instanceof Thicket){
				
				addPlant(new Grass(), standingBodyWater);
			}
		}

		return success;
	}

	/**
	 * Grows the strongest plant possible given the 'maxPlantStrength'
	 * @param maxPlantStrength
	 * @return boolean (success)
	 */
	public boolean addPlant(int maxPlantStrength, int standingBodyWater) {

		boolean success = false;
		Plant lowestPlant = getLowestVegetation();
		int lowStrength = 10000;
		
		if (lowestPlant == null){
			
			lowStrength = 0;
		}
		else{
			
			lowStrength = lowestPlant.getMoistureRequired();
		}

		// It's conceivable for something to grow
		if (lowStrength < getMoisture(standingBodyWater) || maxPlantStrength > lowStrength){
			
			Plant plant = new Jungle();
			
			do{
				
				if (success){
					
					break;
				}
				
				if (maxPlantStrength >= plant.getMoistureRequired()
						&& getMoisture(standingBodyWater) >= plant.getMoistureRequired()
						&& getSaturation(standingBodyWater) <= plant.getMaxSaturation()) {
					
					// For all plant spots
					for (int index = 0; index < 3; index++) {
	
						if (addPlantAtIndex(plant, index)) {
	
							success = true;
							break;
						}
					}
				}
				
				plant = getNextLowestPlant(plant);
				
			
			}
			while(plant != null);
		}

		return success;
	}

	/**
	 * Creates the plant at the given index, deletes any plant it may be
	 * replacing, and consumes the appropriate moisture
	 * 
	 * DOES NOT DO ANY VALIDATION!! Should only be consumed by addPlant
	 * 
	 * @param plant
	 * @param index
	 * @return true if successful
	 */
	private boolean addPlantAtIndex(Plant plant, int index) {

		boolean success = false;

		// If the spot is empty,fill it
		if (this.vegetation[index] == null) {

			success = true;
		}

		// If the spot is occupied by lesser plant, kill it and fill the spot
		else if (this.vegetation[index].getMoistureRequired() < plant
				.getMoistureRequired()) {

			deletePlant(index);
			success = true;
		}

		if (success) {

			this.moisture -= plant.getMoistureRequired();
			plant.setMoisture(plant.getMoistureRequired());
			plant.setIndex(index);
			this.vegetation[index] = plant;
			HexMap.getInstance().addGreenHex(this.hexID);
		}

		return success;
	}

	/**
	 * Deletes the plant at the given index and releases it's moisture into the
	 * air
	 * 
	 * @param index
	 */
	public void deletePlant(int index) {

		if (this.vegetation[index] != null) {
			this.moistureInAir += this.vegetation[index].getMoisture();
			this.vegetation[index] = null;

			if (!hasPlant()) {

				HexMap.getInstance().removeGreenHex(hexID);
			}
		}

	}

	/**
	 * Returns true if the hex has any plant at all
	 */
	public boolean hasPlant() {

		boolean has = false;

		for (Plant plant : vegetation) {

			if (plant != null) {

				has = true;
				break;
			}
		}

		return has;
	}

	/**
	 * Finds and returns the plant which displaces the most moisture
	 * 
	 * @return
	 */
	public Plant getHighestVegetation() {

		int highest = 0;
		Plant plant = null;

		for (Plant veggie : this.vegetation) {

			if (veggie != null && veggie.getMoistureRequired() > highest) {

				highest = veggie.getMoistureRequired();
				plant = veggie;
			}
		}

		return plant;
	}
	
	/**
	 * Finds and returns the plant which displaces the least moisture
	 * 
	 * @return
	 */
	public Plant getLowestVegetation() {

		int lowest = 10000;
		Plant plant = null;

		for (Plant veggie : this.vegetation) {

			if (veggie != null && veggie.getMoistureRequired() < lowest) {

				lowest = veggie.getMoistureRequired();
				plant = veggie;
			}
			else{
				
				plant = null;
				break;
			}
		}

		return plant;
	}

	/**
	 * Gets the fire resistance of a hex
	 * 
	 * @param hex
	 * @return
	 */
	public int hexFireResistence(int standingBodyWater) {

		return (int) (this.getTotalWater(standingBodyWater) / Environment.FLAMABILITY);
	}

	/**
	 * Gets all moisture in plants
	 */
	public int getPlantMoisture() {

		int moistureInPlant = 0;
		Plant[] plants = this.getVegetation();

		for (Plant plant : plants) {
			if (plant != null) {

				moistureInPlant += plant.getMoisture();

			}
		}

		return moistureInPlant;
	}

	/**
	 * Gets the total water on the hex
	 */
	public int getTotalWater(int standingBodyWater) {

		return getPlantMoisture() + getMoisture(standingBodyWater) + getMoistureInAir();
	}

	/**
	 * Gets the strength of a fire on a hex
	 * 
	 * @param hex
	 * @return
	 */
	public int getFireStrength(int standingBodyWater) {

		Plant[] plants = this.getVegetation();
		int fireStrength = 0;

		for (Plant plant : plants) {
			if (plant != null) {
				fireStrength += plant.getMoisture();
			}
		}

		return fireStrength + getMoisture(standingBodyWater);
	}

	/**
	 * Returns the color this hex should be
	 */
	public Color getColor(int standingBodyWater, DisplayType displayType) {

		switch (displayType) {

		case ELEVATION:

			if (elevation > 255) {

				return Color.WHITE;
			}
			if (elevation < 0) {

				return Color.BLACK;
			}

			return new Color(elevation, elevation, elevation);

		case MOISTURE:
		   
		   int moisture = getMoisture(standingBodyWater);
			
			if (moisture * 4 > 255 || moisture > 255) {

				return new Color(0, 0, 255);
			}
			if (getMoisture(standingBodyWater) < 0) {

				return new Color(0, 0, 0);
			} else {
				return new Color(0, 0, moisture * 4);
			}

		case HUMIDITY:
			
/*			if (HexMap.getInstance().getClouds().containsKey(hexID)){
				
				return new Color(HexMap.getInstance().getClouds().get(hexID),0,0);
			}*/

			if (moistureInAir * 3 > 255) {

				return new Color(85, 85, 255);
			} 
			else if (moistureInAir < 0){
				
				return new Color(85, 85, 0);
			}
			else {

				int colorForAir = moistureInAir;
				
				if (colorForAir > 64){
					colorForAir = 64;
				}
				
				return new Color(colorForAir / 2, colorForAir / 2,
						colorForAir * 3);
			}

		case DENSITY:

			if (density * 4 > 255) {

				return Color.BLACK;
			}
			if (density < 0) {

				return Color.WHITE;
			} else {

				return new Color(255 - density * 4, 255 - density * 4,
						255 - density * 4);
			}
			
		case TECTONICS:
			
			if (tectonicState == null){
				return Color.WHITE;
			} else {
			
			switch (tectonicState){
				
				case -2:
					return Color.RED;
				case -1:
					return new Color(127,0,0);
				case 0:
					return Color.GRAY;
				case 1:
					return new Color(0,0,127);
				case 2:
					return Color.BLUE;
				}
			}
			return Color.WHITE;

		default:

			Plant plant = getHighestVegetation();

			// Check if the color should be water
			if (isWater(color == WATER || color == MARSH, standingBodyWater)) {

				if (plant != null && plant instanceof Jungle) {

					color = MARSH;
				} else {

					color = WATER;
				}
			}

			else if (fire > 0) {

				color = FIRE;
			}

			else if (plant == null) {

				color = DIRT;
				
				int red = color.getRed();
				int green = color.getGreen();
				int blue = color.getBlue();
				
				float redAdjust = (float) (255 - red) / 32f;
/*				float greenAdjust = (float) (255 - green) / 32f;
				float blueAdjust = (float) (255 - blue) / 32f;*/
				
				// Make it whiter
				if (density > 32){
					
					red += redAdjust * (density - 32);
					green += redAdjust * (density - 32);
					blue += redAdjust * (density - 32);
					
					red = setToRange(blue, 255);
					green = setToRange(green, 255);
					blue = setToRange(blue, 255);
					
					color = new Color(red, green, blue);
				}	
				
				/*
				if (density > (Environment.MAX_DENSITY - 10)) {

					color = STONE;
				} else {
					color = DIRT;
				}*/
			}

			else {

				int redAdjust = 0;
				int greenAdjust = 0;

				if (plant instanceof Grass) {

					color = GRASS;
					
					redAdjust =  (plant.getRootstrength() - Environment.GRASS_STRENGTH) * Environment.COLOR_CHANGE_CONSTANT;
					greenAdjust = (plant.getRootstrength() - Environment.GRASS_STRENGTH + plant.getMoistureRequired() - Environment.GRASS_MOISTURE) * Environment.COLOR_CHANGE_CONSTANT;
				}

				if (plant instanceof Thicket) {

					color = THICKET;
					
					redAdjust =  (plant.getRootstrength() - Environment.THICKET_STRENGTH) * Environment.COLOR_CHANGE_CONSTANT;
					greenAdjust = (plant.getRootstrength() - Environment.THICKET_STRENGTH + plant.getMoistureRequired() - Environment.THICKET_MOISTURE) * Environment.COLOR_CHANGE_CONSTANT;
				}

				if (plant instanceof Forest) {

					color = FOREST;
					
					redAdjust =  (plant.getRootstrength() - Environment.FOREST_STRENGTH) * Environment.COLOR_CHANGE_CONSTANT;
					greenAdjust = (plant.getRootstrength() - Environment.FOREST_STRENGTH + plant.getMoistureRequired() - Environment.FOREST_MOISTURE) * Environment.COLOR_CHANGE_CONSTANT;
				}

				if (plant instanceof Jungle) {

					color = JUNGLE;
					
					redAdjust =  (plant.getRootstrength() - Environment.JUNGLE_STRENGTH)/2;
					greenAdjust = (plant.getRootstrength() - Environment.JUNGLE_STRENGTH)/2 + (plant.getMoistureRequired() - Environment.JUNGLE_MOISTURE)/2;
				}
				
				color = new Color(setToRange(color.getRed() + redAdjust,255), setToRange(color.getGreen() + greenAdjust,255), setToRange(color.getBlue(),255));
			}

			return color;

		}
	}

	/**
	 * 
	 * @param whether
	 *            current color is water (or marsh)
	 * @return true is color should be water (or marsh)
	 */
	public boolean isWater(boolean water, int standingBodyWater) {

		int standingWater = getStandingWater(standingBodyWater);

		if (water) {

			// If plants finally take over
			if (getPlantMoisture() > standingWater + Environment.WATER_BUFFER
					|| standingWater == 0) {

				water = false;
			}
		}

		else {

			if (standingWater > getPlantMoisture() + Environment.WATER_BUFFER) {

				water = true;

			}
		}

		return water;
	}

	public int getFire() {
		return fire;
	}

	public void setFire(int fire) {
		this.fire = fire;
	}
	
	public int setToRange(int var, int max){
		
		int returnInt = var;
		
		if (var < 0){
			
			returnInt = 0;
		}
		if (var > max){
			
			returnInt = max;
		}
		
		return returnInt;
	}
	
	public Plant getNextLowestPlant(Plant plant){
		
		Plant returnPlant = null;
		
		if (plant instanceof Jungle){
			
			returnPlant = new Forest();
		}
		if (plant instanceof Forest){
			
			returnPlant = new Thicket();
		}
		if (plant instanceof Thicket){
			
			returnPlant = new Grass();
		}
		
		return returnPlant;
	}
	
	public Pair[] getAdj(){

		Pair[] pairs = new Pair[6];
		
		pairs[0] = hexID.N();
		pairs[1] = hexID.NE();
		pairs[2] = hexID.SE();
		pairs[3] = hexID.S();
		pairs[4] = hexID.SW();
		pairs[5] = hexID.NW();
		
		return pairs;
	}
	
	public Integer getTectonicState() {
		return tectonicState;
	}

	public void setTectonicState(Integer tectonicState) {
		this.tectonicState = tectonicState;
	}
}
