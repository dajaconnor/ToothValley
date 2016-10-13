package graphics;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3d;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import enums.DisplayType;
import impl.HexService;
import models.DPair;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;

public class OpenGLWindow {

	HexService hexService = new HexService();

	public static int xBuffer = 5;
	public static int yBuffer = 5;

	public static Color water = new Color(68, 247, 235);
	public static Color dirt = new Color(108, 91, 46);
	public static Color grass = new Color(251, 251, 77);
	public static Color thicket = new Color(124, 186, 117);
	public static Color marsh = new Color(77, 193, 129);
	public static Color forest = new Color(21, 181, 51);
	public static Color jungle = new Color(6, 102, 23);

	public double panx = 0;
	public double pany = 0;
	private boolean running = true;
	private boolean paused = false;
	private boolean autoSpin = false;
	private int waterChange = 1; // 1 stays the same.
	private Pair offset = new Pair(0,0);
	

	private DisplayType displayType = DisplayType.NORMAL;
	

	// To make singleton
	private static final OpenGLWindow INSTANCE = new OpenGLWindow(false);

	public static OpenGLWindow getInstance() {
		return INSTANCE;
	}

	// ****

	private OpenGLWindow(boolean fullscreen) {

		if (INSTANCE != null) {
			throw new IllegalStateException("Already instantiated");
		}

		try {
			init(fullscreen);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			Sys.alert(GAME_TITLE, "An error occured and the game will exit.");
		}
	}

	/** Game title */
	public static final String GAME_TITLE = "Environmental Sim";

	/**
	 * Initialise the game
	 * 
	 * @throws Exception
	 *             if init fails
	 */
	private static void init(boolean fullscreen) throws Exception {

		Display.setTitle(GAME_TITLE);
		Display.setFullscreen(fullscreen);

		// Enable vsync if we can (due to how OpenGL works, it cannot be
		// guarenteed to always work)
		Display.setVSyncEnabled(true);

		// Create default display of 640x480
		Display.setDisplayMode(new DisplayMode(Environment.MAP_WIDTH, Environment.MAP_HEIGHT));
		Display.create();

		// Init openGL stuff?
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		// povy in radians, aspect ratio X/Y, zNear, zFar
		gluPerspective(45f, (float) (Environment.MAP_WIDTH / Environment.MAP_HEIGHT), 1000f , 5000f);
		
		//I don't know why, but this puts the map on the screen...
		//glTranslatef(-500, -500, -2000);
		glTranslatef(-Environment.MAP_WIDTH/2, -300, -2000);

		glRotatef(-50f,1f,0f,0f);

		glMatrixMode(GL_MODELVIEW);
		
		glEnable(GL_DEPTH_TEST);
		
		// Setup Keyboard
		Keyboard.enableRepeatEvents(false);

	}

	public void drawTriangle(DPair vertice1, DPair vertice2, DPair vertice3,
			Pair colorElev, int elev2, int elev3) {

		// Begin drawing
		glBegin(GL11.GL_TRIANGLES);

		// Set triangle color
		int red = HexMap.intToRed(colorElev.getX());
		int green = HexMap.intToGreen(colorElev.getX());
		int blue = HexMap.intToBlue(colorElev.getX());

		glColor3f((float) red / 256, (float) green / 256,
				(float) blue / 256);

		// Elevations should be: colorElev.getY(), elev2, elev3
		// Middle
		glVertex3d(vertice1.getX(), vertice1.getY(),
				colorElev.getY());

		// Edges
		glVertex3d(vertice2.getX(), vertice2.getY(), elev2);
		glVertex3d(vertice3.getX(), vertice3.getY(), elev3);

		glEnd();
	}
	
	public void drawLine(DPair vertice1, DPair vertice2, int elev1, int elev2) {

		// Begin drawing
		glBegin(GL11.GL_LINES);

		glLineWidth(1);
		glColor3f(0.5f, 0.5f, 0.5f);

		glVertex3d(vertice1.getX(), vertice1.getY(), elev1);

		glVertex3d(vertice2.getX(), vertice2.getY(), elev2);
		
		glEnd();
	}

	public void printMap() {

	   HexMap map = HexMap.getInstance();
	   

		Map<Pair,Pair> displayMap = map.getDisplayMap();
		
		if(isAutoSpin()){

			glRotatef(1f,0f,0f,1f);
		}
		
		// Clear the screen.
		GL11.glDepthMask(true);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		printAllPairs(displayMap, offset);

		// Check inputs
		keyInput();

		// Flip display
		Display.update();

	}

   private void printAllPairs(Map<Pair, Pair> displayMap, Pair localOffset) {
      for (Pair hexId : displayMap.keySet()) {

			printHex(hexId, displayMap, localOffset);
		}
   }

	public void keyInput() {
		
		while (Keyboard.next()) {
				
			if (Keyboard.getEventKeyState()){

				switch (Keyboard.getEventKey()) {
	
					case Keyboard.KEY_F1:
		
						displayType = DisplayType.NORMAL;
						
						break;
		
					case Keyboard.KEY_F2:
		
						displayType = DisplayType.HUMIDITY;
						
						break;
		
					case Keyboard.KEY_F3:
		
						displayType = DisplayType.MOISTURE;
						
						break;
		
					case Keyboard.KEY_F4:
		
						displayType = DisplayType.ELEVATION;
						
						break;
		
					case Keyboard.KEY_F5:
		
						displayType = DisplayType.DENSITY;
						
						break;
						
					case Keyboard.KEY_F6:
						
						displayType = DisplayType.TECTONICS;
						
						break;
						
					case Keyboard.KEY_SPACE:
						
						if (isPaused()){
							
							setPaused(false);
						}
						
						else{
							
							setPaused(true);
						}
						
						break;
						
					case Keyboard.KEY_R:
						
						if (isAutoSpin()){
							
							setAutoSpin(false);
						} else{
							
							setAutoSpin(true);
						}
						
						break;
						
					case Keyboard.KEY_X:
						
						alterWaterChangeBy(Environment.WATER_CHANGE_PER_KEY_PRESS);
						
						break;

					case Keyboard.KEY_Z:
						
						alterWaterChangeBy(-Environment.WATER_CHANGE_PER_KEY_PRESS);
						
						break;	
					
					default:
	
				}
			}
		}
		
		if (!isAutoSpin()){
			
			if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)){
				
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
					
				   if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
				      
				      glTranslatef(0, 5*Environment.FAST_PAN, 0);
				   }else{
				   
				      glTranslatef(0, 5*Environment.SLOW_PAN, 0);
				   }
					
				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
					
					shiftUp(Environment.FAST_PAN);
				}else{
				
				   shiftUp(Environment.SLOW_PAN);
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
				
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
					
				   if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
                  
                  glTranslatef(0, -5*Environment.FAST_PAN, 0);
               }else{
               
                  glTranslatef(0, -5*Environment.SLOW_PAN, 0);
               }
					
				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
					
					shiftDown(Environment.FAST_PAN);
				}else{
				
				   shiftDown(Environment.SLOW_PAN);
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
				
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
					
				   if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
                  
                  glTranslatef(-5*Environment.FAST_PAN, 0, 0);
               }else{
               
                  glTranslatef(-5*Environment.SLOW_PAN, 0, 0);
               }
					
				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
					
					shiftLeft(Environment.FAST_PAN);
				}else{
				
					shiftLeft(Environment.SLOW_PAN);
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
				
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
					
					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
                  
                  glTranslatef(5*Environment.FAST_PAN, 0, 0);
               }else{
               
                  glTranslatef(5*Environment.SLOW_PAN, 0, 0);
               }
					
				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
					
					shiftRight(Environment.FAST_PAN);
				}else{
					
				   shiftRight(Environment.SLOW_PAN);
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_Q)){
				
				glRotatef(1f,0f,0f,1f);
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_E)){
				
				glRotatef(1f,0f,0f,-1f);
			}
		}
	}

	private void shiftUp(int amount){
	   
	   offset.setY(offset.getY() + amount);
	}
	
	private void shiftDown(int amount){
      
	   offset.setY(offset.getY() - amount);
   }

	private void shiftLeft(int amount){
   
	   offset.setX(offset.getX() - amount);
	}

	private void shiftRight(int amount){
   
	   offset.setX(offset.getX() + amount);
	}
	
	private void printHex(Pair northId, Map<Pair, Pair> map, Pair localOffset) {

		int water = HexMap.colorToInt(Hex.WATER);

		Pair northColorElev = map.get(northId);

		Pair southId = northId.S();
		Pair southColorElev = map.get(southId);

		Pair eastId = northId.SE();
		Pair eastColorElev = map.get(eastId);

		// Outer elevation guides
		int NE_Elev = map.get(eastId.N()).getY();
		int SE_Elev = map.get(eastId.S()).getY();
		int W_Elev = map.get(southId.NW()).getY();

		DPair north = getBasePrintCoords(northId, localOffset);
		DPair south = getBasePrintCoords(southId, localOffset);
		DPair east = getBasePrintCoords(eastId, localOffset);

		//DPair epicenter = realSE(north);

		// North hex
		// NW
		int outerElev = (W_Elev + northColorElev.getY() + southColorElev.getY()) / 3;
		int centerElev = (eastColorElev.getY() + northColorElev.getY() + southColorElev
				.getY()) / 3;
		
		
		drawTriangle(north, realSW(north), realSE(north), northColorElev,
				outerElev, centerElev);

		
		// N
		outerElev = (NE_Elev + northColorElev.getY() + eastColorElev.getY()) / 3;
		drawTriangle(north, realE(north), realSE(north), northColorElev,
				outerElev, centerElev);
		
		if (!(northColorElev.getX() == water && eastColorElev.getX() == water)){
			//Border
			drawLine(realE(north),realSE(north), outerElev, centerElev);
		}
		
		// East hex
		// NE
		drawTriangle(east, realNW(east), realW(east), eastColorElev, outerElev,
				centerElev);
	 
		
		// SE
		outerElev = (SE_Elev + southColorElev.getY() + eastColorElev.getY()) / 3;
		drawTriangle(east, realSW(east), realW(east), eastColorElev, outerElev,
				centerElev);

		if (!(southColorElev.getX() == water && eastColorElev.getX() == water)){
			//Border
			drawLine(realSW(east),realW(east), outerElev, centerElev);
		}
		
		// South hex
		// S
		drawTriangle(south, realE(south), realNE(south), southColorElev,
				outerElev, centerElev);

		// SW
		outerElev = (W_Elev + southColorElev.getY() + northColorElev.getY()) / 3;
		drawTriangle(south, realNW(south), realNE(south), southColorElev,
				outerElev, centerElev);
		
		if (!(southColorElev.getX() == water && northColorElev.getX() == water)){
			//Border
			drawLine(realNW(south),realNE(south), outerElev, centerElev);
		}
	}

	/**
	 * Get's coordinates that exist in perlin map
	 * 
	 * @param x
	 * @param y
	 * @param localOffset 
	 * @return
	 */
	public DPair getBasePrintCoords(double x, double y) {
	   
		double xCoord = x * (Environment.HEX_BODY_WIDTH + Environment.HEX_SIDE_WIDTH);
		double yCoord = y * Environment.HEX_HEIGHT + Environment.HEX_HEIGHT / 2 - x * Environment.HEX_HEIGHT / 2;

		return new DPair(xCoord, yCoord);
	}

	public DPair getBasePrintCoords(Pair pair, Pair localOffset) {
		
		Pair yOffsetDifferential = new Pair(localOffset.getX(), localOffset.getYbyXdifferential());

		Pair printPair = pair.merge(yOffsetDifferential);

		return getBasePrintCoords(printPair.getX(), printPair.getY());
	}

	private DPair realNW(DPair middle) {

		return new DPair(middle.getX() - Environment.HEX_BODY_WIDTH / 2, middle.getY() - Environment.HEX_HEIGHT / 2);
	}

	private DPair realNE(DPair middle) {

		return new DPair(middle.getX() + Environment.HEX_BODY_WIDTH / 2, middle.getY() - Environment.HEX_HEIGHT / 2);
	}

	private DPair realE(DPair middle) {

		return new DPair(middle.getX() + Environment.HEX_RADIUS, middle.getY());
	}

	private DPair realSE(DPair middle) {

		return new DPair(middle.getX() + Environment.HEX_BODY_WIDTH / 2, middle.getY() + Environment.HEX_HEIGHT
				/ 2);
	}

	private DPair realSW(DPair middle) {

		return new DPair(middle.getX() - Environment.HEX_BODY_WIDTH / 2, middle.getY() + Environment.HEX_HEIGHT
				/ 2);
	}

	private DPair realW(DPair middle) {

		return new DPair(middle.getX() - Environment.HEX_RADIUS, middle.getY());
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public DisplayType getDisplayType() {
		return displayType;
	}

	public void setDisplayType(DisplayType displayType) {
		this.displayType = displayType;
	}
	
	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isAutoSpin() {
		return autoSpin;
	}

	public void setAutoSpin(boolean autoSpin) {
		this.autoSpin = autoSpin;
	}

	public int getWaterChange() {
		return waterChange;
	}

	public void alterWaterChangeBy(int changeBy) {
		this.waterChange += changeBy;
	}
}