package impl;

import graphics.OpenGLWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.Environment;
import models.Forest;
import models.Grass;
import models.Hex;
import models.HexMap;
import models.Jungle;
import models.Pair;
import models.Plant;
import models.TectonicPlate;
import models.Thicket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HexMapService {
	
	@Autowired
	TectonicPlateService plateService;
	
	public void createMap() {

		HexMap map = HexMap.getInstance();
		//Window window = Window.getInstance();
		OpenGLWindow openGL = OpenGLWindow.getInstance();
		

		int height = OpenGLWindow.Y / (int) openGL.height;
		int width = OpenGLWindow.X / (int) (openGL.sideWidth + openGL.bodyWidth);
		
		if (width % 2 == 1){
			
			width ++;
		}
		
		int[] hexSize = new int[2];
		hexSize[0] = width;
		hexSize[1] = height;

		map.setSize(hexSize);

		//Creates all the hexes on the map
		generateAllHexes(width, height);

		//Prints all the hexes on the map
		openGL.printMap();
	}

	public void generateAllHexes(int width, int height){
		
		//Get perlin object
		PerlinNoise perlin = new PerlinNoise();
		
		//Set the size of the perlin pixel grid
		int[] noiseSize = new int[2];
		noiseSize[0] = OpenGLWindow.X;
		noiseSize[1] = OpenGLWindow.Y;
		
		//Get perlin pixel grids for all hex attributes
		int[][] elevations = getPerlinPixels(perlin, noiseSize);
		int[][] densityMap = getPerlinPixels(perlin, noiseSize);
		
		
		//int[][] moistureMap = getPerlinPixels(perlin, noiseSize);
		//int[][] moistureInAirMap = new int[moistureMap.length][moistureMap[0].length];//getPerlinPixels(perlin, noiseSize);
		//int[][] plantMap = getPerlinPlants(perlin, noiseSize);
		
		HexMap map = HexMap.getInstance();
		
		for (int x = 0; x < width; x++) {

			for (int y = 0; y < height; y++) {

				int Y = y + x / 2;
				
				Random rand = new Random();
				int plant = rand.nextInt(7) - 2;
				
				Pair pair = OpenGLWindow.getInstance().getBasePrintCoords(x, Y).toPair();
				
				if (pair.getX() >= elevations.length){
					 pair.setX(pair.getX() - elevations.length);
				}
				if (pair.getY() >= elevations[pair.getX()].length){
					pair.setY(pair.getY() - elevations[pair.getX()].length);
				}
				if (pair.getY() < 0){
					pair.setY(pair.getY() + elevations[pair.getX()].length);
				}
				if (pair.getX() < 0){
					pair.setX(pair.getX() + elevations.length);
				}
				
				Hex hex = makeHex(x, Y, elevations[pair.getX()][pair.getY()], densityMap[pair.getX()][pair.getY()]/4, Environment.AVE_WATER / 2, Environment.AVE_WATER / 2, plant);
				map.addHex(hex);
				map.updateHexDisplay(hex);
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

		Plant[] newPlant = new Plant[3];
		
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
		
		Hex hex = new Hex(hexID, setToRange(elevations, 255), setToRange(density, 63), moistureMap, moistureInAirMap, newPlant);	

		return hex;
	}

	/**
	 * Makes perlin pixel map
	 * 
	 * @param perlin
	 * @param noiseSize
	 * @return int[][]
	 */
	public int[][] getPerlinPixels(PerlinNoise perlin, int[] noiseSize) {

		int[][] slopeNoise = perlin.twoDNoise(0.07F, 160, noiseSize);
		int[][] noise = perlin.twoDNoise(0.12F, 200, noiseSize);
		int[][] smallnoise = perlin.twoDNoise(0.02F, -60, noiseSize);

		return perlin.combineNoise(smallnoise, perlin.combineNoise(noise, slopeNoise));
	}

	/**
	 * Makes perlin pixel map
	 * 
	 * @param perlin
	 * @param noiseSize
	 * @return int[][]
	 */
	public int[][] getPerlinPlants(PerlinNoise perlin, int[] noiseSize) {

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
		Random rand = new Random();
		
		List<Pair> keys = new ArrayList<Pair>(map.getHexes().keySet());
		Pair randomPair = keys.get(rand.nextInt(keys.size()));
		
		return map.getHex(randomPair);
		
	}
	
	public TectonicPlate pickRandomPlate(){
		
		HexMap map = HexMap.getInstance();
		Random rand = new Random();
		
		return map.getPlates().get(rand.nextInt(map.getPlates().size()));
	}

	public Hex pickRandomWhiteHex() {
		
		HexMap map = HexMap.getInstance();
		Random rand = new Random();
		List<Pair> keys  = new ArrayList<Pair>(map.getHexes().keySet());
		Pair randomKey = keys.get(rand.nextInt(keys.size()) );
		
		
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
}
