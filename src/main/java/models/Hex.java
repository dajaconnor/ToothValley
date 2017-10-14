package models;

import java.awt.Color;

import enums.DisplayType;
import enums.TectonicEdgeDirection;
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
	private int incomingMoistureInAir;
	private int elevation;
	private Plant[] vegetation = new Plant[Environment.NUM_PLANTS_PER_HEX];
	private Color color = DIRT;
	private int fire = 0;
	private Integer tectonicState;


	public static int MAX_PLANT_STRENGTH = 16;
	public static Color WATER = new Color(68, 247, 235);
	public static Color DIRT = new Color(108, 91, 46);
	public static Color GRASS = new Color(211, 221, 77);
	public static Color THICKET = new Color(124, 226, 117);
	public static Color MARSH = new Color(77, 193, 129);
	public static Color FOREST = new Color(21, 181, 51);
	public static Color JUNGLE = new Color(6, 102, 23);
	public static Color FIRE = new Color(255, 0, 0);
	public static Color STONE = new Color(173, 193, 173);
	public static Color SNOW = new Color(230, 230, 255);

	public Plant[] getVegetation() {
		return vegetation;
	}

	public Plant getRandomPlant(){
		return vegetation[TheRandom.getInstance().get().nextInt(vegetation.length)];
	}

	public int getDensity() {
		return density;
	}

	public int getSoil(){
		return Environment.MAX_DENSITY - density;
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

	public int alterDensity(int change) {
		
		return setDensity(getDensity() + change);
	}

	public int getMoistureInAir() {
		return moistureInAir;
	}

	public int alterMoistureInAir(int change){

		if (this.moistureInAir + change >= 0){

			this.moistureInAir += change;
			return Math.abs(change);
		} else {

			int altered = this.moistureInAir;
			this.moistureInAir = 0;
			return altered;
		}
	}
	
	public int getIncomingMoistureInAir() {
		return incomingMoistureInAir;
	}

	public int alterIncomingMoistureInAir(int incoming){
		
		incomingMoistureInAir += incoming;
		return incoming;
	}
	
	public void resolveMoistureInAir(){
		
		moistureInAir += incomingMoistureInAir;
		incomingMoistureInAir = 0;
	}
	
	public boolean rain(){

		int excessHumidity = elevation + moistureInAir - Environment.RAIN_THRESHHOLD;
		
		if (excessHumidity <= 0) return false;
		
		int amountToRain = (excessHumidity * Environment.PERCENT_MOISTURE_EXCESS_TO_DROP) / 100;

		boolean flood = amountToRain > Environment.MAX_RAINFALL_PER_TICK;
		if (flood) amountToRain = Environment.MAX_RAINFALL_PER_TICK;
		
		alterMoisture(alterMoistureInAir(-amountToRain));
		
		return flood;
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

		int standingWater = getStandingWater();

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

	public int getMoisture() {
		return this.moisture;
	}

	public int alterMoisture(int change) {

		int changed = 0;

		// change is negative
		if (change < 0) {

			if (getMoisture() + change < 0) {

				changed = getMoisture();
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
	public boolean setElevation(int elevation) {

		boolean evaluateThis = false;

		if (elevation > this.elevation){

			evaluateThis= true;
		}

		this.elevation = elevation;

		return evaluateThis;
	}
	
	public void alterElevation(int changeRequired) {
		
		this.elevation += changeRequired;
	}

	public double getHumidity() {
		return ((double) this.moistureInAir * ((double) this.elevation / Environment.MAX_ELEVATION))
				/ Environment.RAIN_THRESHHOLD;
	}

	/**
	 * range = 0-63
	 * 
	 * @return
	 */
	public double getSaturation() {
		return ((double) getMoisture()) / (double) (Environment.MAX_DENSITY - this.density);
	}

	public int getStandingWater() {

		int standingWater = getMoisture() - (Environment.MAX_DENSITY - this.density);

		if (standingWater < 0) {
			standingWater = 0;
		}

		return standingWater;
	}
	
	public int getDisplayElevation(int snowLevel){
		
		if (elevation > snowLevel) return getStandingWater() / (Environment.WATER_PER_ELEVATION * 2) + elevation;
		return getCombinedElevation();
	}

	public int getCombinedElevation() {
		
		return getStandingWater() / Environment.WATER_PER_ELEVATION + elevation;
	}

	public int getPrintElevation() {

		return getStandingWater() + elevation;
	}

	public int getSoilStability() {
		int stability = density / 2;
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
	public boolean addPlant(Plant plant) {

		boolean success = false;

		// If the new plant can tolerate the saturation range
		if (plant != null && getSoil() >= plant.getRootstrength() && plant.getMoistureRequired() <= getMoisture()){

			Plant newPlant = null;
			int index = 0;

			if (plant instanceof Grass){

				newPlant = new Grass(plant.getRootstrength(), plant.getMoistureRequired());
				index = 0;
			}

			if (plant instanceof Thicket){

				newPlant = new Thicket(plant.getRootstrength(), plant.getMoistureRequired());
				index = 1;
			}

			if (plant instanceof Forest){

				newPlant = new Forest(plant.getRootstrength(), plant.getMoistureRequired());
				index = 2;
			}

			if (plant instanceof Jungle){

				newPlant = new Jungle(plant.getRootstrength(), plant.getMoistureRequired());
				index = 3;
			}

			if (newPlant != null){
				
				newPlant.geneticDrift();

				if (addPlantAtIndex(newPlant, index)) {

					success = true;
				}
			}
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

		plant.getClass().toString();

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
			if (this.vegetation[index].getRootstrength() >= Environment.ROOT_STRENGTH_DEATH_GAIN) this.density--;
			this.vegetation[index] = null;

			if (!hasPlant()) {

				HexMap.getInstance().removeGreenHex(hexID);
			}
		}
	}

	public void killAllPlants(){

		for (int i = 0; i < vegetation.length; i++){

			deletePlant(i);
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
	public int hexFireResistence() {

		return (int) (this.getTotalWater() / Environment.FLAMABILITY);
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
	public int getTotalWater() {

		return getPlantMoisture() + getMoisture() + getMoistureInAir() + getIncomingMoistureInAir();
	}

	/**
	 * Gets the strength of a fire on a hex
	 * 
	 * @param hex
	 * @return
	 */
	public int getFireStrength() {

		Plant[] plants = this.getVegetation();
		int fireStrength = 0;

		for (Plant plant : plants) {
			if (plant != null) {
				fireStrength += plant.getMoisture();
			}
		}

		return fireStrength + getMoisture();
	}

	/**
	 * Returns the color this hex should be
	 */
	public Color getColor(DisplayType displayType, int snowLevel) {

		switch (displayType) {

		case ELEVATION:

			return getElevationColor();

		case MOISTURE:

			return getMoistureColor();

		case HUMIDITY:

			return getHumidityColor();

		case DENSITY:

			return getDensityColor();

		case TECTONICS:

			return getTectonicColor();

		default:

			return getNormalColor(displayType, snowLevel);

		}
	}

	private Color getFireColor(){
		int green = TheRandom.getInstance().get().nextInt(255);
		return new Color(255, green, 0);
	}

	private Color getNormalColor(DisplayType displayType, int snowLevel) {
		Plant plant = getHighestVegetation();

		if (fire > 0) {

			color = getFireColor();
		}

		else if (plant == null) {

			color = DIRT;

			// Make it whiter
			if (density > Environment.MAX_FULL_DIRT){

				int densityForColor = density - Environment.MAX_FULL_DIRT;

				color = getColorMerge(DIRT, STONE, densityForColor, Environment.MIN_FULL_STONE);

			}	
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

		int standingWater = getStandingWater();

		if (standingWater > 0){

			// only snow if there's standing water and no plant, 
			// or the snow is above the rootstrength
			if (elevation > snowLevel){
				if (plant == null || standingWater > plant.getRootstrength()){
					color = SNOW;
				}
			} else {

				Color waterColor = WATER;

				Color moistureColor = getMoistureColor();
				int intColor = Math.abs(moistureColor.getBlue() - 255);
				waterColor = new Color(0,intColor / 2,moistureColor.getBlue());


				if (standingWater >= Environment.WATER_BUFFER){

					color = waterColor;
				} else{

					color = getColorMerge(color, waterColor, standingWater, Environment.WATER_BUFFER);
				}
			}
		}

		return color;
	}

	private Color getElevationColor() {
		
		int color = elevation / 2;
		
		if (color > 255) {

			return Color.WHITE;
		}
		if (color < 0) {

			return Color.BLACK;
		}

		return new Color(color, color, color);
	}

	private Color getHumidityColor() {
		if (moistureInAir * 3 > 255) {

			return new Color(85, 85, 255);
		} 
		else if (moistureInAir < 0){

			return new Color(0, 0, 0);
		}
		else {

			int colorForAir = moistureInAir;

			if (colorForAir > 64){
				colorForAir = 64;
			}

			return new Color(colorForAir / 2, colorForAir / 2,
					colorForAir * 3);
		}
	}

	private Color getDensityColor() {
		if (density * 4 > 255) {

			return Color.WHITE;
		}
		if (density < 0) {

			return Color.BLACK;
		} else {

			return new Color(density * 4, density * 4,
					density * 4);
		}
	}

	private Color getTectonicColor() {
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
	}

	private Color getMoistureColor() {
		int moisture = getMoisture();

		if (moisture > 255 || moisture > 255) {

			return new Color(0, 0, 255);
		}
		if (getMoisture() < 0) {

			return new Color(0, 0, 0);
		} else {
			return new Color(0, 0, moisture);
		}
	}

	private Color getColorMerge(Color colorLow, Color colorHigh, int determiningValue, int cutoff){

		int blue = getColorMergeOneColor(colorLow.getBlue(), colorHigh.getBlue(), determiningValue, cutoff);
		int green = getColorMergeOneColor(colorLow.getGreen(), colorHigh.getGreen(), determiningValue, cutoff);
		int red = getColorMergeOneColor(colorLow.getRed(), colorHigh.getRed(), determiningValue, cutoff);

		return new Color(red, green, blue);
	}

	private int getColorMergeOneColor(int colorLow, int colorHigh, int determiningValue, int cutoff){

		if (determiningValue <= 0) return colorLow;
		if (determiningValue >= cutoff) return colorHigh;

		return ((colorLow * (cutoff - determiningValue)) + (colorHigh * determiningValue)) / cutoff;
	}

	/**
	 * 
	 * @param whether
	 *            current color is water (or marsh)
	 * @return true is color should be water (or marsh)
	 */
	public boolean isWater(boolean water) {

		int standingWater = getStandingWater();

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

	public void setTectonicState(TectonicEdgeDirection edgeDirection) {

		switch(edgeDirection){

		case UP:

			tectonicState = 1;
			break;

		case DOWN:

			tectonicState = -1;
			break;

		case STABLE:

			tectonicState = 0;
			break;
		}
	}
}
