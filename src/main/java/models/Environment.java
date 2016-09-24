package models;

public class Environment {
	
	//Maximum values
	public static final int MAX_ELEVATION = 255;
	public static final int MAX_DENSITY = 63;
	public static final int MAX_SLOPE = 5;
	
	// The smaller this is, the steeper stuff gets
	public static final int SLOPE_CONSTANT = 1;
	
	//Defines the percent of the map tht attempts to grow
	public static final double GROW_RATE = 0.8;
	
	//The likelyhood that something will burn on a tick per region
	public static final double BURN_RATE = 0.0001;
	
	//The speed at which flames die
	public static final int BURN_DOWN_RATE = 18;
	
	//The ability for fire to spread
	public static final double FLAMABILITY = 1.6;
	
	//The power with which lightning strikes
	public static final int LIGHTNING_STRENGTH = 48;
	
	//Defines the power of standing water to destroy plants (bigger the deadlier)
	public static final float ROT_RATE = 0.005F;
	
	//Defines the resilience of plants to the sun
	public static final int DRY_PLANT = 200;
	
	//Multiplier for the ability of a flood to drown plants.  The bigger, the stronger.
	public static final float FLOOD_STRENGTH = 2;
	
	//Defines the change in water level required to alter hex color
	public static final int WATER_BUFFER = 0;
	
	//Defines the average amount of water for each hex
	public static final int AVE_WATER = 700;
	
	//Defines the speed at which wind changes (lower is faster), must be at least 3
	public static final int WIND_CHANGE = 250;
	
	//Defines the strength with which wind blows (minimum disparity between air moisture for wind)
	public static final int WIND_POWER = 50;
	
	//Defines the humidity at which rain starts by itself
	public static final int RAIN_INDEX = 64;
	
	//Defines the point of condensation (when moistureInAir + elevation/4 == AIR_DENSITY, it rains
	public static final double AIR_DENSITY = 30;
	
	// Determines the amount of moistureInAir required to constitute a 'cloud'
	public static final int CLOUD = 48;

	// Defines the number of times a hex can topple in a single run
	public static final int TOPPLE_DEPTH = 100;
	
	//Defines the rate at which unneeded evolution occurs
	public static final float EVOLUTION_RATE = 1F;//0.9F;
	
	//Defines the conditions when unneeded evolution is possible
	public static final int EVOLUTION_DESIRE = 1;
	
	// Defines the number of hexes per edge hex
	public static final int PLATE_EDGE_FREQUENCY = 800;
	
	// Number of tectonic movements per tick
	public static final int TECTONIC_ACTIVITY = 100;
	
	// Defines the likelihood of a straight edge on a tectonic plate
	public static final int PERCENT_STRAIGHT_PLATE_EDGE = 95;
	
	//The next set is all default variables for vegetation
	public static final double GRASS_SATURATION = 1;
	public static final int GRASS_MOISTURE = 2;
	public static final int GRASS_STRENGTH = 1;
	public static final double THICKET_SATURATION = 1;
	public static final int THICKET_MOISTURE = 5;
	public static final int THICKET_STRENGTH = 1;
	public static final double FOREST_SATURATION = 1.4;
	public static final int FOREST_MOISTURE = 24;
	public static final int FOREST_STRENGTH = 12;
	public static final double JUNGLE_SATURATION = 2;
	public static final int JUNGLE_MOISTURE = 16;
	public static final int JUNGLE_STRENGTH = 24;
	
	public static final int WATER_CHANGE_PER_KEY_PRESS = 1;
	public static final int COLOR_CHANGE_CONSTANT = 1/2; // 0 is no color change with evolution
	public static final int TECTONIC_FIRST_STRETCH = 5;
	public static final int TECTONIC_MIN_STRAIGHT = 3;
	public static final int WATER_PER_ELEVATION = 4;
	public static final int MAP_HEIGHT = 1100; // should correlate with screen size in pixels
	public static final int MAP_WIDTH = 1900;
	public static final double ZOOM = 6;

}
