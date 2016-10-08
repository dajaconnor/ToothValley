package impl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import models.Environment;
import models.Pair;
import models.TheRandom;

@SuppressWarnings("serial")
public class PerlinNoise extends JPanel {

		
	/**
	 * This returns all y values for perlin noise
	 * @return
	 */
	public List<Integer> oneDNoise(){
		
		int amp = (Environment.MAP_HEIGHT * 3/4);
		List<Integer> noise = perlin(0.1F, amp, new ArrayList<Integer>());
		
		
		amp = (Environment.MAP_HEIGHT * 1/4);
		noise = perlin(0.05F, amp, noise);
		
		amp = (Environment.MAP_HEIGHT * 1/8);
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
	public int[][] twoDNoise(float frequency, int amp, Pair size){
		
		int numXWaves = (int) (1.0 / frequency);
		int waveXLength = size.getX() / numXWaves;
		int numYWaves = size.getY()/waveXLength;
		int waveYLength = size.getY() / numYWaves;
		int[][] ZArray = new int[size.getX()][size.getY()];
		int[][] wavePoints = new int[numXWaves][numYWaves];
		
		//Make the grid wavePoints
		for (int i = 0; i < wavePoints.length; i++){
			wavePoints[i] = makeNoise(wavePoints[i].length, amp);
		}
		
		//splines are the vertical ridges from which to interpolate all 2D space
		int[][] splines = new int[wavePoints.length][size.getY()];
		
		//Window window = Window.getInstance();
		
		//Create vertical splines
		for (int i = 0; i < wavePoints.length; i++){
			
			splines[i] = makeSpline(wavePoints[i], waveYLength, amp, size.getY());
			
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
		for (int yIndex = 0; yIndex < size.getY(); yIndex++){
			
			//For every value of x
			for (int xIndex = 0; xIndex < size.getX(); xIndex++){
				
				int wave = xIndex/waveXLength;
				
				double xVar = (((float) xIndex - (wave * (float) waveXLength))/waveXLength);
				
				//If remaining space is less than two full wavelengths
				if (wave >= numXWaves - 1){
					
					
					xVar = (double) (xIndex - wave * waveXLength) / (double) ((size.getX() % waveXLength) + waveXLength);
					
					
					if (wave > numXWaves - 1){
						wave = numXWaves;
					}
				}
				
				//Wraps horizontally
				int[] interPoints = getInterPoints(splines, wave, yIndex);
				
				ZArray[xIndex][yIndex] = (int) interpolate(interPoints[0], interPoints[1], interPoints[2], interPoints[3], xVar);
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
		int waveLength = (int) (frequency * Environment.MAP_WIDTH);
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
			noise[i] = (amp - (int) (amp * TheRandom.getInstance().get().nextFloat()));
			
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
			wavePoints.add(amp - (int) (amp * TheRandom.getInstance().get().nextFloat()));
			
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
