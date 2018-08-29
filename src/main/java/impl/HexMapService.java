package impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphics.OpenGLWindow;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.TectonicPlate;
import models.TheRandom;
import models.plants.Forest;
import models.plants.Grass;
import models.plants.Jungle;
import models.plants.Plant;
import models.plants.Thicket;


@Component
public class HexMapService {
	
	@Autowired
	TectonicPlateService plateService;
	
	@Autowired
   HexService hexService;
	
	public void createMap() {

		OpenGLWindow openGL = OpenGLWindow.getInstance();

		//Creates all the hexes on the map
		generateAllHexes();

		//Prints all the hexes on the map
		openGL.printMap();
	}

	public void generateAllHexes(){
	   
	   int width = Environment.MAP_GRID[0];
	   int height = Environment.MAP_GRID[1];
		
		//Get perlin object
		PerlinNoise perlin = new PerlinNoise();
		
		//Set the size of the perlin pixel grid
		Pair noiseSize = OpenGLWindow.getInstance().getBasePrintCoords(width, height * 2 - 1, false).toPair();
		noiseSize.setX(noiseSize.getX() - (int)Environment.HEX_HEIGHT / 2);
		
		//Get perlin pixel grids for all hex attributes
		int[][] elevations = getPerlinPixels(perlin, noiseSize);
		int[][] densityMap = getPerlinPixels(perlin, noiseSize);
		
		
		//int[][] moistureMap = getPerlinPixels(perlin, noiseSize);
		//int[][] moistureInAirMap = new int[moistureMap.length][moistureMap[0].length];//getPerlinPixels(perlin, noiseSize);
		//int[][] plantMap = getPerlinPlants(perlin, noiseSize);
		
		HexMap map = HexMap.getInstance();
		
		map.setHexes(new Hashtable<Pair, Hex>(width * height));
		
		for (int x = 0; x < width; x++) {

			for (int y = 0; y < height; y++) {

				int Y = y + x / 2;
				
				TheRandom rand = TheRandom.getInstance();
				int plant = rand.get().nextInt(7) - 2;
				
				Pair pairCoordinates = Pair.wrap(x, y + x/5);
				Pair pair = OpenGLWindow.getInstance().getBasePrintCoords(pairCoordinates.getX(), pairCoordinates.getY(), false).toPair();

				int elevation = elevations[pair.getX()][pair.getY()] * 2 - Environment.MAX_ELEVATION / 2;
				
				Hex hex = makeHex(x, Y, elevation, (densityMap[pair.getX()][pair.getY()]) / 4, 
				      0, Environment.AVE_WATER, plant);
				map.addHex(hex);
			}
		}
		map.setPlates(plateService.generateTectonicPlates(noiseSize));
	}

	


	/**
	 * This populates the hex with its elevation, density, moisture, humidity, a
	 * plant, and it's coordinates.  It also adds the hex to all it's appropriate sets
	 * 
	 * @param x
	 * @param y
	 * @param elevations
	 * @param densityMap
	 * @param moistureMap
	 * @param moistureInAirMap
	 * @param plantMap
	 * @return Hex
	 */
	public Hex makeHex(int x, int y, int elevations, int densityMap,
			int moistureMap, int moistureInAirMap, int plant) {

		//Inverting density
		int density = 64 - densityMap;
		
		Pair hexID = new Pair(x, y);

		if (plant > 4) {
			plant = 4;
		}
		if (plant < 1) {
			plant = 1;
		}

		Plant[] newPlant = new Plant[Environment.NUM_PLANTS_PER_HEX];
		
		switch (plant) {

		case 1:
			newPlant[0] = new Grass();
			break;

		case 2:
			newPlant[0] = new Thicket();
			break;

		case 3:
			newPlant[0] = new Forest();
			break;

		case 4:
			newPlant[0] = new Jungle();
			break;

		default:
			break;

		}
		
		Hex hex = new Hex(hexID, setToRange(elevations, Environment.MAX_ELEVATION), setToRange(density, Environment.MAX_DENSITY), moistureMap, moistureInAirMap, newPlant);	

		return hex;
	}

	/**
	 * Makes perlin pixel map
	 * 
	 * @param perlin
	 * @param noiseSize
	 * @return int[][]
	 */
	private int[][] getPerlinPixels(PerlinNoise perlin, Pair noiseSize) {

		int[][] slopeNoise = perlin.twoDNoise(0.07F, 160, noiseSize);
		int[][] noise = perlin.twoDNoise(0.12F, 300, noiseSize);
		int[][] smallnoise = perlin.twoDNoise(0.02F, -160, noiseSize);

		return perlin.combineNoise(smallnoise, perlin.combineNoise(noise, slopeNoise));
	}

	/**
	 * Makes perlin pixel map
	 * 
	 * @param perlin
	 * @param noiseSize
	 * @return int[][]
	 */
	public int[][] getPerlinPlants(PerlinNoise perlin, Pair noiseSize) {

		int[][] slopeNoise = perlin.twoDNoise(0.003F, 3, noiseSize);
		int[][] noise = perlin.twoDNoise(0.002F, 2, noiseSize);
		
		return perlin.combineNoise(noise, slopeNoise);
	}

	
	/**
	 * Grabs a random hex from the map
	 * @return Hex
	 */
	public Hex pickRandomHex(){
		
		HexMap map = HexMap.getInstance();
		TheRandom rand = TheRandom.getInstance();
		
		List<Pair> keys = new ArrayList<Pair>(map.getHexes().keySet());
		Pair randomPair = keys.get(rand.get().nextInt(keys.size()));
		
		return map.getHex(randomPair);
		
	}
	
	public TectonicPlate pickRandomPlate(){
		
		HexMap map = HexMap.getInstance();
		TheRandom rand = TheRandom.getInstance();
		
		return map.getPlates().get(rand.get().nextInt(map.getPlates().size()));
	}

	public Hex pickRandomWhiteHex() {
		
		HexMap map = HexMap.getInstance();
		TheRandom rand = TheRandom.getInstance();
		List<Pair> keys  = new ArrayList<Pair>(map.getHexes().keySet());
		Pair randomKey = keys.get(rand.get().nextInt(keys.size()) );
		
		
		return map.getHex(randomKey);
	}
	
	/**
	 * Returns where all water is in an array: (totalwater, onGround, inPLants, inAir)
	 */
	public int[] allWater(){
		
		HexMap map = HexMap.getInstance();
		
		List<Pair> allHexes = new ArrayList<Pair>( map.getHexes().keySet() );
		int[] water = new int[4];
		
		for (Pair hexID : allHexes){
			
			if (hexID != null){
				
				water[0] += map.getHex(hexID).getTotalWater();
				water[1] += map.getHex(hexID).getMoisture();
				water[2] += map.getHex(hexID).getPlantMoisture();
				water[3] += map.getHex(hexID).getMoistureInAir();
				
			}
		}
		
		return water;
	}
	
	public int setToRange(int set, int range){
		
		if (set > range){
			
			set = range;
		}
		
		if (set < 0){
			
			set = 0;
		}
		
		return set;
	}

	public void normalizeElevation(List<Pair> allHexes, int totalElevation) {
		
		int currentAverage = totalElevation / allHexes.size();
		int changeRequired = Environment.AVE_ELEVATION - currentAverage;
		
		for (Pair pair : allHexes){
			
			HexMap.getInstance().getHex(pair).alterElevation(changeRequired);
		}
	}
}
