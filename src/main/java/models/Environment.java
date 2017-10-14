package models;

public class Environment {

   // Maximum values
   public static final int MAX_ELEVATION = 512;
   public static final int MAX_DENSITY = 64;

   // Defines the average amount of water for each hex
   public static final int AVE_WATER = 1400;
   public static final int AVE_ELEVATION = 128;
   
   public static final int NUM_PLANTS_PER_HEX = 4;

   // Defines the percent of the plants that attempt to grow
   public static final double GROW_RATE = 0.1;

   // The percent of the map that quick grow will fill
   public static final double QUICK_GROW_LIMIT = 0.03;
   
   // Number of ticks to quick grow after the sim starts
   public static final int QUICK_GROW_LENGTH = 100;

   // The percent of the map that quick grow will fill
   public static final double QUICK_GROW_RATE = 1;

   // Number of ticks between force grows on slow growth
   public static final int FORCE_GROW_INTERVAL = 1;
   
   // The likelyhood that something will burn on a tick per region
   public static final double BURN_RATE = 0.0001;

   // The speed at which flames die
   public static final int BURN_DOWN_RATE = 18;

   // The ability for fire to spread, the bigger the more flammable
   public static final double FLAMABILITY = 1.15;

   // The power with which lightning strikes
   public static final int LIGHTNING_STRENGTH = 252;

   // Defines the power of standing water to destroy plants (bigger the
   // deadlier)
   public static final float ROT_RATE = 0.005F;

   // Defines the resilience of plants to the sun
   public static final int DRY_PLANT = 200;

   // Multiplier for the ability of a flood to drown plants. The bigger, the
   // stronger.
   public static final float FLOOD_STRENGTH = 0.1f;

   // Defines the change in water level required to alter hex color
   public static final int WATER_BUFFER = 0;

   // Defines the point of condensation (when moistureInAir + elevation >
   // RAIN_THRESHHOLD, it rains
   public static final int RAIN_THRESHHOLD = 10;
   
   // The percent of moisture above the computed rain threshold to rain
   public static final int PERCENT_MOISTURE_EXCESS_TO_DROP = 20;
   public static final int MAX_RAINFALL_PER_TICK = 1000;
   public static final int FLOOD_WATER_CONTINUE_SIZE = 10;
   public static final int EVAPORATE_PERCENT = 100;
   public static boolean REALISTIC_WATER_FLOW = false;
   public static final int WATER_PER_ELEVATION = 64;

   // Determines the amount of moistureInAir required to constitute a 'cloud'
   public static final int CLOUD = 64;
   public static final int MAX_CLOUD_SIZE = 50;
   
   // The bigger, the more you see the coriolis
   public static final double CORIOLIS_RELIANCE = 0.9;

   // Plant resistance to evaporation
   public static final int EVAPORATION_RESISTANCE = 8;

   // Defines the rate at which unneeded evolution occurs
   public static final float EVOLUTION_RATE = 1F;// 0.9F;

   // Defines the conditions when unneeded evolution is possible
   public static final int EVOLUTION_DESIRE = 1;

   // Defines the number of hexes per edge hex
   public static final int PLATE_EDGE_FREQUENCY = 800;

   // Number of tectonic movements per tick
   public static final int TECTONIC_ACTIVITY = 10;
   public static final int TECTONIC_AMPLITUDE = 4;

   // Defines the number of times a hex can topple in a single run
   public static final int TOPPLE_DEPTH = 3;

   // Defines the likelihood of a straight edge on a tectonic plate
   public static final int PERCENT_STRAIGHT_PLATE_EDGE = 95;

   // The next set is all default variables for vegetation
   public static final double GRASS_SATURATION = 1;
   public static final int GRASS_MOISTURE = 2;
   public static final int GRASS_STRENGTH = 1;
   public static final double THICKET_SATURATION = 1;
   public static final int THICKET_MOISTURE = 5;
   public static final int THICKET_STRENGTH = 3;
   public static final double FOREST_SATURATION = 1.4;
   public static final int FOREST_MOISTURE = 24;
   public static final int FOREST_STRENGTH = 12;
   public static final double JUNGLE_SATURATION = 2;
   public static final int JUNGLE_MOISTURE = 16;
   public static final int JUNGLE_STRENGTH = 24;
   public static final int ROOT_STRENGTH_DEATH_GAIN = 150;

   public static final int PERCENT_CHANCE_OF_SOIL_LOOSENING = 1;
   
   public static final int WATER_CHANGE_PER_KEY_PRESS = 1;
   public static final int COLOR_CHANGE_CONSTANT = 1 / 2; // 0 is no color
                                                          // change with
                                                          // evolution
   public static final int TECTONIC_FIRST_STRETCH = 5;
   public static final int TECTONIC_MIN_STRAIGHT = 3;
   
   public static final int DRAW_LINE_TOLERANCE = 2;
   public static final int MAP_HEIGHT = 1500; // should correlate with screen
                                              // size in pixels
   public static final int MAP_WIDTH = 1900;
   public static final double ZOOM = 8;

   public static final int FAST_PAN = 6;
   public static final int SLOW_PAN = 1;
   public static final int MOVE_MULTIPLIER = 5;

   public static final int CHANCE_OF_TECTONIC_PLATE_CHANGE = 10000;
   public static final int HOW_SLOW_WATER_MOVES = 2; // 2 is faster, slower as number goes up
   public static final int SNOW_MELT = 1; // multiplies HOW_SLOW_WATER_MOVES
   public static final int YEAR_IN_TICKS = 128; // used for snow level change sine frequecy
   public static final int SNOW_LEVEL_AMPLITUDE = 64;
   
   public static final int MAX_FULL_DIRT = 32;
   public static final int MIN_FULL_STONE = 130;
   public static final int NORMALIZE_EVEL_FREQ = 100;
   public static final float CHANCE_OF_MUTATION = 0.7f;
   public static final int CATACLISMIC_AVALANCHE = 15;
   
   // don't move stuff around underwater if it super deep
   public static final int STANDING_WATER_EROSION_CUTOFF = WATER_PER_ELEVATION * 10;
   public static final boolean QUICK_FLOW = true;

   public static final int AVE_TICKS_BETWEEN_TECTONIC_VERTICAL_MOVE = 5;
   public static final int NUM_PLANT_TYPES = 4;



   // These are set when the map is generated, and never altered.
   public static int[] MAP_GRID;
   public static int[] TRUE_CENTER;
   public static double HEX_HEIGHT = ((double) ZOOM * Math.pow(3D, 0.5D));
   public static double HEX_SIDE_WIDTH = ZOOM / 2;
   public static double HEX_BODY_WIDTH = ZOOM;
   public static double HEX_RADIUS = ZOOM;
}
