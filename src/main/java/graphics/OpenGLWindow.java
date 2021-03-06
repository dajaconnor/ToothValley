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
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import enums.ControlDirection;
import enums.DisplayType;
import impl.HexService;
import models.DPair;
import models.Environment;
import models.HexMap;
import models.Pair;
import models.UserActions;

public class OpenGLWindow {

	HexService hexService = new HexService();

	public static int xBuffer = 5;
	public static int yBuffer = 5;

	public static Color water = new Color(68, 247, 235);
	public static Color dirt = new Color(108, 91, 46);
	public static Color sand = new Color(225,191,146);
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
	private Pair offset = new Pair(0,0);
	private boolean drawLinesToggle = false;
	private boolean fullScreen = false;
	private int rotation = 90;
	private boolean toggleFlyoverType = true;
	private DisplayType displayType = DisplayType.NORMAL;


	// To make singleton
	private static final OpenGLWindow INSTANCE = new OpenGLWindow();

	public static OpenGLWindow getInstance() {
		return INSTANCE;
	}

	// ****

	private OpenGLWindow() {

		if (INSTANCE != null) {
			throw new IllegalStateException("Already instantiated");
		}

		try {
			init(false);

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
	 * if init fails
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

		// center view
		glTranslatef(-200, 100, -2000);

		// roll into view
		glRotatef(-50f,1f,0f,0f);
		// initial rotation
		glRotatef(90,0f,0f,1f);

		glMatrixMode(GL_MODELVIEW);

		glEnable(GL_DEPTH_TEST);

		// Setup Keyboard
		Keyboard.enableRepeatEvents(false);
		//setupShaders();
	}

	public void drawTriangle(DPair centerVertice1, int centerElev1, DPair vertice2, 
			int elev2, DPair vertice3, int elev3, int color) {

		// Begin drawing
		glBegin(GL11.GL_TRIANGLES);

		// Set triangle color
		int red = HexMap.intToRed(color);
		int green = HexMap.intToGreen(color);
		int blue = HexMap.intToBlue(color);

		glColor3f((float) red / 256, (float) green / 256,
				(float) blue / 256);

		// Elevations should be: colorElev.getY(), elev2, elev3
		// Middle
		glVertex3d(centerVertice1.getX(), centerVertice1.getY(), centerElev1);

		// Edges
		glVertex3d(vertice2.getX(), vertice2.getY(), elev2);
		glVertex3d(vertice3.getX(), vertice3.getY(), elev3);

		glEnd();
	}

	public void drawLine(DPair vertice1, DPair vertice2, int elev1, int elev2, float red, float green, float blue, boolean translucent) {

		// Begin drawing
		glBegin(GL11.GL_LINES);

		if (translucent){
			glEnable (GL11.GL_BLEND); GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		}

		glLineWidth(0.1f);
		glColor3f(red, green, blue);

		glVertex3d(vertice1.getX(), vertice1.getY(), elev1);

		glVertex3d(vertice2.getX(), vertice2.getY(), elev2);

		glEnd();
	}

	public void drawLine(DPair vertice1, DPair vertice2, int elev1, int elev2){
		drawLine(vertice1, vertice2, elev1, elev2, 0.5f, 0.5f, 0.5f, false);
	}

	public void printMap() {

		HexMap map = HexMap.getInstance();

		if(isAutoSpin()){

			rotate(1, false);
		}

		// Clear the screen.
		GL11.glDepthMask(true);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		Map<Pair,Pair> displayMap = map.getDisplayMap();

		printAllPairs(displayMap, offset);

		// Check inputs
		keyInput();
		mouseInput();

		// Flip display
		Display.update();

	}

	private void printAllPairs(Map<Pair, Pair> displayMap, Pair localOffset) {
		for (Pair hexId : displayMap.keySet()) {

			printHex(hexId, localOffset, displayMap);
		}
	}

	public void mouseInput(){

		while (Mouse.next()){

			if(Mouse.getEventButtonState()){

				int event = Mouse.getEventButton();

				// LEFT(0), MIDDLE(2), RIGHT(1), SCROLL_BUTTON(4), UP(3);
				switch(event){

				case 0:
					System.out.println("LEFT " + Mouse.getEventX() + ", " + Mouse.getEventY());
					break;

				case 1:
					System.out.println("RIGHT " + Mouse.getEventX() + ", " + Mouse.getEventY());
					break;

				case 2:
					System.out.println("MIDDLE " + Mouse.getEventX() + ", " + Mouse.getEventY());
					break;
				}

			}
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

				case Keyboard.KEY_F7:

					drawLinesToggle = !drawLinesToggle;

					break;

				case Keyboard.KEY_F11:

					fullScreen = !fullScreen;

					try{
						Display.setFullscreen(fullScreen);
						if (fullScreen) Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
					}catch(Exception e){
						System.out.println("Setting to fullscreen didn't work...  " + e.getMessage());
					}


					break;

				case Keyboard.KEY_SPACE:

					if (isPaused()){

						setPaused(false);
					}

					else{

						setPaused(true);
					}

					break;

				case Keyboard.KEY_G:

					if (isAutoSpin()){

						setAutoSpin(false);
					} else{

						setAutoSpin(true);
					}

					break;

				case Keyboard.KEY_Z:

					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
						UserActions.getInstance().incrementWaterChangedByUser(10);
					}

					else UserActions.getInstance().incrementWaterChangedByUser(20);
					break;

				case Keyboard.KEY_X:

					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
						UserActions.getInstance().incrementWaterChangedByUser(-2);
					}

					else UserActions.getInstance().incrementWaterChangedByUser(-40);
					break;

				case Keyboard.KEY_V:

					UserActions.getInstance().incrementTectonicActivity(10);
					break;

				case Keyboard.KEY_C:

					UserActions.getInstance().toggleRealisticWaterFlow();
					break;

				case Keyboard.KEY_T:

					toggleFlyoverType = !toggleFlyoverType;
					break;

				case Keyboard.KEY_1:

					UserActions.getInstance().addPlants(0);
					break;

				case Keyboard.KEY_2:

					UserActions.getInstance().addPlants(1);
					break;

				case Keyboard.KEY_3:

					UserActions.getInstance().addPlants(2);
					break;

				case Keyboard.KEY_4:

					UserActions.getInstance().addPlants(3);
					break;

				default:

				}
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_Z)) UserActions.getInstance().incrementWaterChangedByUser(20);
		if (Keyboard.isKeyDown(Keyboard.KEY_X)) UserActions.getInstance().incrementWaterChangedByUser(-40);
		if (Keyboard.isKeyDown(Keyboard.KEY_V)) UserActions.getInstance().incrementTectonicActivity(4);

		if (!isAutoSpin()){

			if (Keyboard.isKeyDown(Keyboard.KEY_R)){

				moveMapVertically(true);
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_F)){

				moveMapVertically(false);
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)){

				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){

					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

						changeVantage(ControlDirection.DOWN, Environment.SLOW_PAN);

					}else{

						changeVantage(ControlDirection.DOWN, Environment.FAST_PAN);
					}

				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

					pan(ControlDirection.UP, Environment.SLOW_PAN);
				}else{

					pan(ControlDirection.UP, Environment.FAST_PAN);
				}
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)){

				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){

					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

						changeVantage(ControlDirection.UP, Environment.SLOW_PAN);
					}else{

						changeVantage(ControlDirection.UP, Environment.FAST_PAN);
					}

				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

					pan(ControlDirection.DOWN, Environment.SLOW_PAN);

				}else{

					pan(ControlDirection.DOWN, Environment.FAST_PAN);
				}
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)){

				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){

					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

						changeVantage(ControlDirection.RIGHT, Environment.SLOW_PAN);

					}else{

						changeVantage(ControlDirection.RIGHT, Environment.FAST_PAN);
					}

				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

					pan(ControlDirection.LEFT, Environment.SLOW_PAN);

				}else{

					pan(ControlDirection.LEFT, Environment.FAST_PAN);
				}
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){

				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){

					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

						changeVantage(ControlDirection.LEFT, Environment.SLOW_PAN);
					}else{

						changeVantage(ControlDirection.LEFT, Environment.FAST_PAN);
					}

				}else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

					pan(ControlDirection.RIGHT, Environment.SLOW_PAN);

				}else{

					pan(ControlDirection.RIGHT, Environment.FAST_PAN);
				}
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_Q)){

				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

					rotate(Environment.SLOW_PAN, false);
				} else{

					rotate(Environment.FAST_PAN, false);
				}
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_E)){

				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){

					rotate(Environment.SLOW_PAN, true);

				} else{
					rotate(Environment.FAST_PAN, true);
				}
			}
		}
	}

	private void moveMapVertically(boolean UP) {

		if (toggleFlyoverType) UP = !UP;

		int amount = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
				? Environment.MOVE_MULTIPLIER*Environment.SLOW_PAN
						: Environment.MOVE_MULTIPLIER*Environment.FAST_PAN;

		if (!UP) amount = -amount;

		glTranslatef(0, 0, amount);
	}

	private void rotate(int amount, boolean clockwise){

		if (toggleFlyoverType) clockwise = !clockwise;

		if (clockwise) {
			glRotatef(-amount,0f,0f,1f);
			rotation -= amount;
		}
		else {
			glRotatef(amount,0f,0f,1f);
			rotation += amount;
		}
	}

	private void changeVantage(ControlDirection direction, int amount){

		direction = direction.correctForRotation(rotation, toggleFlyoverType);

		switch(direction){

		case UP:

			glTranslatef(0, Environment.MOVE_MULTIPLIER*amount, 0);
			break;

		case DOWN:

			glTranslatef(0, -Environment.MOVE_MULTIPLIER*amount, 0);
			break;

		case LEFT:

			glTranslatef(-Environment.MOVE_MULTIPLIER*amount, 0, 0);
			break;

		case RIGHT:

			glTranslatef(Environment.MOVE_MULTIPLIER*amount, 0, 0);
			break;
		}
	}

	private void pan(ControlDirection direction, int amount){

		direction = direction.correctForRotation(rotation, toggleFlyoverType);

		switch(direction){

		case UP:

			shiftUp(amount);
			break;

		case DOWN:

			shiftDown(amount);
			break;

		case LEFT:

			shiftLeft(amount);
			break;

		case RIGHT:

			shiftRight(amount);
			break;
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

	private void shiftN(int amount) {

		offset.setY(offset.getY() - amount);
	}

	private void shiftNE(int amount) {

		offset.setX(offset.getX() + amount);
	}

	private void shiftSE(int amount) {

		offset.setX(offset.getX() + amount);
		offset.setY(offset.getY() + amount);
	}

	private void shiftS(int amount) {

		offset.setY(offset.getY() + amount);		
	}

	private void shiftSW(int amount) {

		offset.setX(offset.getX() - amount);
	}

	private void shiftNW(int amount) {

		offset.setX(offset.getX() - amount);
		offset.setY(offset.getY() - amount);		
	}

	private int getGroundVerticeElevation(Pair one, Pair two, Pair three, Map<Pair, Pair> display){
		return (display.get(one).getY() + display.get(two).getY() + display.get(three).getY()) / 3;
	}

	private void printDiamond(Pair top, DPair baseTop, Pair bottom, DPair baseBottom, Pair left, DPair baseLeft, Pair right, DPair baseRight, Map<Pair, Pair> normalDisplayMap){

		try {
			// draw ground first
			int topColor = normalDisplayMap.get(top).getX();
			int bottomColor = normalDisplayMap.get(bottom).getX();
			int topGroundElev = normalDisplayMap.get(top).getY();
			int leftGroundElev = getGroundVerticeElevation(top, left, bottom, normalDisplayMap);
			int bottomGroundElev = normalDisplayMap.get(bottom).getY();
			int rightGroundElev = getGroundVerticeElevation(top, right, bottom, normalDisplayMap);
		
		
			drawTriangle(baseTop, topGroundElev, baseLeft, leftGroundElev, baseRight, rightGroundElev, topColor);
			drawTriangle(baseBottom, bottomGroundElev, baseLeft, leftGroundElev, baseRight, rightGroundElev, bottomColor);
	
			// prints the line as long as both aren't water
			if (drawLinesToggle 
					&& !dontDraw(topColor, bottomColor, leftGroundElev, rightGroundElev)){
				//Border
				drawLine(baseLeft, baseRight, leftGroundElev, rightGroundElev, 0.5f, 0.5f, 0.5f, true);
			}
		} catch(Exception e) {
			
			System.out.println("Couldn't print " + bottom.toString());
		}
	}

	private boolean dontDraw(int topColor, int bottomColor, int leftGroundElev, int rightGroundElev) {
		return displayType == DisplayType.NORMAL && 
				(Math.abs(leftGroundElev - rightGroundElev) <= Environment.DRAW_LINE_TOLERANCE 
				|| topColor != bottomColor);
	}

	private void printHex(Pair hexId, Pair localOffset, Map<Pair, Pair> normalDisplayMap) {

		DPair baseHex = getBasePrintCoords(hexId, localOffset);
		DPair baseVertSW = realSW(baseHex);
		DPair baseVertSE = realSE(baseHex);
		DPair baseVertE = realE(baseHex);
		DPair baseVertNE = realNE(baseHex);
		DPair baseNE = realE(baseVertNE);
		DPair baseSE = realE(baseVertSE);
		DPair baseS = realSE(baseVertSW);

		// South diamond
		printDiamond(hexId, baseHex, hexId.S(), baseS, hexId.SW(), baseVertSW, hexId.SE(), baseVertSE, normalDisplayMap);
		// Southeast diamond
		printDiamond(hexId, baseHex, hexId.SE(), baseSE, hexId.S(), baseVertSE, hexId.NE(), baseVertE, normalDisplayMap);
		// Northeast
		printDiamond(hexId, baseHex, hexId.NE(), baseNE, hexId.SE(), baseVertE, hexId.N(), baseVertNE, normalDisplayMap);
	}

	/**
	 * Get's coordinates that exist in perlin map
	 * 
	 * @param x
	 * @param y
	 * @param localOffset 
	 * @return
	 */
	public DPair getBasePrintCoords(double x, double y, boolean includeMapCenterOffset) {

		double xCoord = x * (Environment.HEX_BODY_WIDTH + Environment.HEX_SIDE_WIDTH);
		double yCoord = y * Environment.HEX_HEIGHT + Environment.HEX_HEIGHT / 2 - x * Environment.HEX_HEIGHT / 2;

		if(includeMapCenterOffset){
			//xCoord -= (Environment.MAP_GRID[0]/2) * (Environment.HEX_BODY_WIDTH + Environment.HEX_SIDE_WIDTH) + Environment.HEX_SIDE_WIDTH;
			xCoord -= Environment.TRUE_CENTER[0];
			//yCoord -= (Environment.MAP_GRID[1]/2) * Environment.HEX_HEIGHT + Environment.HEX_HEIGHT / 2;
			yCoord -= Environment.TRUE_CENTER[1];
		}

		return new DPair(xCoord, yCoord);
	}

	public DPair getBasePrintCoords(Pair pair, Pair localOffset) {

		Pair yOffsetDifferential = new Pair(localOffset.getX(), localOffset.getYbyXdifferential());

		Pair printPair = pair.merge(yOffsetDifferential);

		return getBasePrintCoords(printPair.getX(), printPair.getY(), true);
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

	private static void setupShaders(){

		int shaderProgram = glCreateProgram();
		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

		StringBuilder fragmentShaderSource = new StringBuilder();

		try{
			BufferedReader reader = new BufferedReader(new FileReader("src/main/shaders/lighting.frag"));
			String line;

			while((line = reader.readLine()) != null){

				fragmentShaderSource.append(line).append("\n");
			}

			reader.close();

		} catch(IOException e){
			System.err.println("Shader failed to load from file");
		}

		glShaderSource(fragmentShader, fragmentShaderSource);
		glCompileShader(fragmentShader);

		if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == 0){
			System.err.println("Shader failed to compile");
		}

		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);

		glUseProgram(shaderProgram);
		// to disable, glUseProgram(0); // uses default shader

		System.out.println(fragmentShaderSource);

		// to cleanup, use the following:
		//glDeleteShader(fragmentShader);
		//glDeleteProgram(shaderProgram);
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
}