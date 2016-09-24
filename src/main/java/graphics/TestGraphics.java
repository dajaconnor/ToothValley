package graphics;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;



public class TestGraphics extends JPanel {

    private static void drawHexPerlin() {

    	Window window = Window.getInstance();
    	
    	int height = ((Window.Y - Window.yBuffer * 2) / window.height) -1;
    	int width = ((Window.X - Window.xBuffer * 2 - window.sideWidth) / (window.sideWidth + window.bodyWidth));
    	
    	int[] size = new int[2];
    	size[0] = Window.X;
    	size[1] = Window.Y;
    	
    	int[][] slopeNoise = twoDNoise(0.1F, 180, size);
    	int[][] noise = twoDNoise(0.022F, 100, size);
    	int[][] elevations = combineNoise(noise, slopeNoise);

    
    	for (int x = 0; x < width; x++){
    		
    		for (int y = 0; y < height; y++){
    			
    			window.drawHex(x, y+x/2, elevations);
    			
    			if (y == height - 1 && x % 2 == 1){
    				
    				window.drawHex(x, y+1+x/2, elevations);
    			}
    		}
    	}
		
	}

	public static void drawOneDPerlin(){
    	
    	Window window = Window.getInstance();
    	
    	List<Integer> noise = oneDNoise();
    	
    	for (int x = 0; x < noise.size(); x++){
    		
    		window.drawPixel(x, noise.get(x)+50);
    	}
    }
    
    
    public static void drawTwoDPerlin(){
    	
    	Window window = Window.getInstance();
    	
    	int[] size = new int[2];
    	size[0] = Window.X;
    	size[1] = Window.Y;
    	
    	//must equal 256
    	int[][] slopeNoise = twoDNoise(0.1F, 180, size);
    	int[][] noise = twoDNoise(0.022F, 100, size);
    	//int[][] smallNoise = twoDNoise(0.003F, 120);
    	
    	int[][] combinedNoise = combineNoise(noise, slopeNoise);
    	//combinedNoise = combineNoise(combinedNoise, slopeNoise);
    			
    	for (int x = 0; x < noise.length; x++){
    		
    		for (int y = 0; y < noise[x].length; y++){
    			
    			int volume = combinedNoise[x][y] - 12;
    			
    			if (volume > 255){
    				volume = 255;
    			}
    			
    			if (volume < 0){
    				volume = 0;
    			}
    			
    			window.drawPixel(x, y, volume);
    		}
    	}
    }
	
	/**
	 * This returns all y values for perlin noise
	 * @return
	 */
	public static List<Integer> oneDNoise(){
		
		int amp = (Window.Y * 3/4);
		List<Integer> noise = perlin(0.1F, amp, new ArrayList<Integer>());
		
		
		amp = (Window.Y * 1/4);
		noise = perlin(0.05F, amp, noise);
		
		amp = (Window.Y * 1/8);
		noise = perlin(0.01F, amp, noise);
		
		
		return noise;
	}
	
	
	/**
	 * At given point x, y1 is the y coordinate preceeding x, y2 the coordinate after.  
	 * y0 and y3 are the outer y coordinates that affect the curve
	 * 
	 * @param y0
	 * @param y1
	 * @param y2
	 * @param y3
	 * @param x
	 * @return
	 */
	public static double interpolate(int y0, int y1, int y2, int y3, double x){
		
		int P = (y3 - y2) - (y0 - y1);
		int Q = (y0 - y1) - P;
		int R = (y2 - y0);
		int S = y1;
		
		return (P*x*x*x + Q*x*x + R*x + S);
	}
	
	
	public static int[] makeSpline(int[] wavePoints, int waveLength, int amp){
		
		int numWaves = wavePoints.length;
		int[] spline = new int[(numWaves - 1) * waveLength];
		
		for (int wave = 0; wave < numWaves - 3; wave++){
			
			for (int x = wave * waveLength; x < (wave * waveLength + waveLength); x++){
				
				double xVar = (((float) x - (wave * (float) waveLength))/waveLength);
				
				int y = (int) interpolate(wavePoints[wave], wavePoints[wave+1], wavePoints[wave+2], wavePoints[wave+3], xVar);
				
				if (spline[x] != 0){
					spline[x] = spline[x] + y - amp/2;
				}
				else{
					spline[x] = y;
				}
			}
		}
		
		return spline;
	}
	
	/**
	 * Creates a 2D map of elevation using frequency and amplitude
	 * @param frequency
	 * @param amp
	 * @return int[][]
	 */
	public static int[][] twoDNoise(float frequency, int amp, int[] size){
		
		int numWaves = (int) (1.0 / frequency);
		int waveLength = (int) (frequency * size[0]);
		
		int[][] ZArray = new int[size[0]][size[1]];
		int[][] wavePoints = new int[numWaves + 4][(size[1]/waveLength) + 5];
		
		//Make the grid wavePoints
		for (int i = 0; i < numWaves+4; i++){
			wavePoints[i] = makeNoise(wavePoints[i].length, amp);
		}
		
		//splines are the vertical ridges from which to interpolate all 2D space
		int[][] splines = new int[numWaves+4][size[1]];
		
		//Create vertical guide splines
		splines[0] = makeSpline(wavePoints[0], waveLength, amp);
		splines[numWaves+3] = makeSpline(wavePoints[wavePoints.length - 1], waveLength, amp);
		
		//Create vertical splines
		for (int i = 0; i < wavePoints.length; i++){
			
			splines[i] = makeSpline(wavePoints[i], waveLength, amp);
		}
		
		//Create full landscape
		//For every value of y
		for (int yIndex = 0; yIndex < size[1]; yIndex++){
			
			//For every value of x
			for (int xIndex = 0; xIndex < size[0]; xIndex++){
				
				int wave = xIndex/waveLength;
				
				if (wave + 3 < splines.length){
				
					double xVar = (((float) xIndex - (wave * (float) waveLength))/waveLength);
				
					ZArray[xIndex][yIndex] = (int) interpolate(splines[wave][yIndex], splines[wave+1][yIndex], splines[wave+2][yIndex], splines[wave+3][yIndex], xVar);
				}
			}
		}
		
		return ZArray;
	}
		

	/**
	 * 
	 * @param frequency - The percentage of width in each wave
	 * @param amp - The max height of each wave
	 * @param Ylist
	 * @return
	 */
	public static List<Integer> perlin(float frequency, int amp, List<Integer> Ylist){
		
		int numWaves = (int) (1.0 / frequency);
		int waveLength = (int) (frequency * Window.X);
		List<Integer> wavePoints = getWavePoints(numWaves+4, amp);
		
		for (int wave = 0; wave < numWaves; wave++){
			
			for (int x = wave * waveLength; x < (wave * waveLength + waveLength); x++){
				
				double xVar = (((float) x - (wave * (float) waveLength))/waveLength);
				
				int y = (int) interpolate(wavePoints.get(wave), wavePoints.get(wave+1), wavePoints.get(wave+2), wavePoints.get(wave+3), xVar);
				
				if (Ylist.size() > x){
					Ylist.set(x, Ylist.get(x) + y - amp/2);
				}
				else{
					Ylist.add(y);
				}
			}
		}
		
		return Ylist;
	}
	
	
	public static int[] makeNoise(int numWaves, int amp){
		
		int[] noise = new int[numWaves];
		
		for (int i = 0; i < numWaves; i++){
			noise[i] = (amp - (int) (amp * Window.rand.nextFloat()));
			
		}
		
		return noise;
	}
	
	/**
	 * Returns a list of random points between 0 and amp
	 * @param numWaves
	 * @param amp
	 * @return
	 */
	public static List<Integer> getWavePoints(int numWaves, int amp){
		
		List<Integer> wavePoints = new ArrayList<Integer>();
		
		for (int i = 0; i < numWaves; i++){
			wavePoints.add(amp - (int) (amp * Window.rand.nextFloat()));
			
		}
		
		return wavePoints;
	}
	
	
	public static List<Integer> joinLists(List<Integer> List1, List<Integer> List2){
		
		for (int i = 0; i < List2.size(); i++){
			
			List1.add(List2.get(i));
		}
		
		return List1;
	}
	
	public static int[][] combineNoise(int[][] noise1, int[][] noise2){
		
		int[][] combinedNoise = new int[noise1.length][];
		
		for (int x = 0; x < noise1.length; x++){
			
			combinedNoise[x] = new int[noise1[0].length];
			
			for (int y = 0; y < noise1[0].length; y++){
				
				combinedNoise[x][y] = noise1[x][y] + noise2[x][y];
			}
		}
		
		return combinedNoise;
	}
}
