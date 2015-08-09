package impl;

import graphics.OpenGLWindow;
import graphics.Window;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;


public class PerlinNoise extends JPanel {

    public void drawHexPerlin() {

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
    			
    			Window.getInstance().drawHex(x, y+x/2, elevations);
    			
    			if (y == height - 1 && x % 2 == 1){
    				
    				Window.getInstance().drawHex(x, y+1+x/2, elevations);
    			}
    		}
    	}
		
	}

	public void drawOneDPerlin(){
    	
    	Window window = Window.getInstance();
    	
    	List<Integer> noise = oneDNoise();
    	
    	for (int x = 0; x < noise.size(); x++){
    		
    		window.drawPixel(x, noise.get(x)+50);
    	}
    }
    
    
    public void drawTwoDPerlin(){
    	
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
	public List<Integer> oneDNoise(){
		
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
	public double interpolate(int y0, int y1, int y2, int y3, double x){
		
		int P = (y3 - y2) - (y0 - y1);
		int Q = (y0 - y1) - P;
		int R = (y2 - y0);
		int S = y1;
		
		return (P*x*x*x + Q*x*x + R*x + S);
	}
	
	/**
	 * Creates a vertical guideline of 1D Perlin noise
	 * @param wavePoints
	 * @param waveLength
	 * @param amp
	 * @param digits
	 * @return
	 */
	public int[] makeSpline(int[] wavePoints, int waveLength, int amp, int digits){
		
		int numWaves = (digits / waveLength) - 1;
		int[] spline = new int[digits];
		
		//For every Y pixel
		for (int i = 0; i < digits; i++){
			
			int wave = i / waveLength;
			double xVar = (double) (i % waveLength) / (double) waveLength;
			
			//If remaining space is less than two full wavelengthsnumWaves
			if (wave >= numWaves){
				
				if (wave > numWaves){
					wave = numWaves;
				}
				
				xVar = (double) (i - numWaves * waveLength) / (double) (digits - numWaves * waveLength);
				
			}
			
			//Wrap vertically
			int[] interPoints = getInterPoints(wavePoints, wave);
			
			//Get height
			int z = (int) interpolate(interPoints[0], interPoints[1], interPoints[2], interPoints[3], xVar);
			
			//In case I iterate over the same pixel twice... (?)
			if (spline[i] != 0){
				spline[i] = (spline[i] + z)/2;
			}
			else{
				spline[i] = z;
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
	public int[][] twoDNoise(float frequency, int amp, int[] size){
		
		int numWaves = (int) (1.0 / frequency);
		int waveLength = size[0] / numWaves;
		int[][] ZArray = new int[size[0]][size[1]];
		int[][] wavePoints = new int[numWaves][size[1]/waveLength];
		
		//Make the grid wavePoints
		for (int i = 0; i < wavePoints.length; i++){
			wavePoints[i] = makeNoise(wavePoints[i].length, amp);
		}
		
		//splines are the vertical ridges from which to interpolate all 2D space
		int[][] splines = new int[wavePoints.length][size[1]];
		
		//Window window = Window.getInstance();
		
		//Create vertical splines
		for (int i = 0; i < wavePoints.length; i++){
			
			splines[i] = makeSpline(wavePoints[i], waveLength, amp, size[1]);
			
			for (int n = 0; n < splines[i].length; n++){
				
				if (splines[i][n] < 0){
					splines[i][n] = 0;
				}
				if (splines[i][n] > 255){
					splines[i][n] = 255;
				}
				
				//window.drawPixel(i * waveLength, n, splines[i][n]);
			}
		}
		
		//Create full landscape / Horizontal splines
		//For every value of y
		for (int yIndex = 0; yIndex < size[1]; yIndex++){
			
			//For every value of x
			for (int xIndex = 0; xIndex < size[0]; xIndex++){
				
				int wave = xIndex/waveLength;
				
				double xVar = (((float) xIndex - (wave * (float) waveLength))/waveLength);
				
				//If remaining space is less than two full wavelengths
				if (wave >= numWaves - 1){
					
					
					xVar = (double) (xIndex - wave * waveLength) / (double) ((size[0] % waveLength) + waveLength);
					
					
					if (wave > numWaves - 1){
						wave = numWaves;
					}
				}
				
				//Wraps horizontally
				int[] interPoints = getInterPoints(splines, wave, yIndex);
				
				ZArray[xIndex][yIndex] = (int) interpolate(interPoints[0], interPoints[1], interPoints[2], interPoints[3], xVar);
				
				//Print out the results
				int shade = ZArray[xIndex][yIndex];
				
				if (shade < 0){
					shade = 0;
				}
				if (shade > 255){
					shade = 255;
				}
				
				//window.drawPixel(xIndex, yIndex, shade);
			}
		}
		
		return ZArray;
	}
		
	//For horizontal splines
	public int[] getInterPoints(int[][] splines, int wave, int yIndex){
		
		int[] interPoints = new int[4];
		
		
		for (int n = 0; n < 4; n++){
			
			int wrap = 0;
			boolean set = false;
			
			while (!set){
			
				wrap ++;
					
				if (wave + n >= splines.length * wrap){
					
					if (wave + n < splines.length * (wrap + 1)){
						
						interPoints[n] = splines[wave + n - splines.length * wrap][yIndex];
						set = true;
						
					}
				}
					
				else{
					
					interPoints[n] = splines[wave + n][yIndex];
					set = true;
				}
			}
		}
		
		return interPoints;
	}
	
	//For vertical splines
	public int[] getInterPoints(int[] splines, int wave){
		
		int[] interPoints = new int[4];
		
		for (int n = 0; n < 4; n++){
			
			int wrap = 0;
			boolean set = false;
			
			while (!set){
			
				wrap ++;
				
				if (wave + n >= splines.length * wrap){
					
					if (wave + n < splines.length * (wrap + 1)){
					
						interPoints[n] = splines[wave + n - splines.length * wrap];
						set = true;
					}
				}
				else{
					
					interPoints[n] = splines[wave + n];
					set = true;
				}
			}
		}
		
		return interPoints;
	}
	
	/**
	 * 
	 * @param frequency - The percentage of width in each wave
	 * @param amp - The max height of each wave
	 * @param Ylist
	 * @return
	 */
	public List<Integer> perlin(float frequency, int amp, List<Integer> Ylist){
		
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
	
	
	public int[] makeNoise(int numWaves, int amp){
		
		int[] noise = new int[numWaves];
		
		for (int i = 0; i < numWaves; i++){
			noise[i] = (amp - (int) (amp * OpenGLWindow.rand.nextFloat()));
			
		}
		
		return noise;
	}
	
	/**
	 * Returns a list of random points between 0 and amp
	 * @param numWaves
	 * @param amp
	 * @return
	 */
	public List<Integer> getWavePoints(int numWaves, int amp){
		
		List<Integer> wavePoints = new ArrayList<Integer>();
		
		for (int i = 0; i < numWaves; i++){
			wavePoints.add(amp - (int) (amp * Window.rand.nextFloat()));
			
		}
		
		return wavePoints;
	}
	
	
	public List<Integer> joinLists(List<Integer> List1, List<Integer> List2){
		
		for (int i = 0; i < List2.size(); i++){
			
			List1.add(List2.get(i));
		}
		
		return List1;
	}
	
	public int[][] combineNoise(int[][] noise1, int[][] noise2){
		
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
