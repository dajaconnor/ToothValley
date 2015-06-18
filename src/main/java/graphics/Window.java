package graphics;

import impl.HexService;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import listeners.KeyHandler;
import models.Hex;
import models.HexMap;
import models.Pair;

import org.springframework.beans.factory.annotation.Autowired;

import enums.DisplayType;

public class Window extends JPanel {

	@Autowired
	HexService hexImpl;
	
	
	private static final long serialVersionUID = 1834258475327427948L;
	public static int Y = 700;
	public static int X = 1300;
	public static Random rand = new Random();
	
	
	public static int xBuffer = 5;
	public static int yBuffer = 5;
	

	
	public static Color water = new Color(68, 247, 235);
	public static Color dirt = new Color(108, 91, 46);
	public static Color grass = new Color(251, 251, 77);
	public static Color thicket = new Color(124, 186, 117);
	public static Color marsh = new Color(77, 193, 129);
	public static Color forest = new Color(21, 181, 51);
	public static Color jungle = new Color(6, 102, 23);
	
	public int zoom = 12;
	public int height = (int) ((double) zoom * Math.pow(3D, 0.5D));
	public int sideWidth = zoom / 2 ;
	public int bodyWidth = zoom;
	
	public int panx = 0;
	public int pany = 0;
	private boolean running = true;
	private DisplayType displayType = DisplayType.NORMAL;
	
	
	//To make singleton
    private static final Window INSTANCE = new Window(X, Y);

    public static Window getInstance() {
        return INSTANCE;
    }
	//****
	

    private BufferedImage img;
    
    
    private Window(int width, int height) {
    	
    	if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }

        img = new BufferedImage(width, height, 
            BufferedImage.TYPE_INT_ARGB_PRE);
        // do in preference to setting the frame size..
        setPreferredSize(new Dimension(width, height));
        JFrame frame = new JFrame();
        frame.add(this);

        frame.pack();

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // good call!
        
        frame.addKeyListener(new KeyHandler());
    }

    public void redrawMap(){
    	
    	HexMap map = HexMap.getInstance();
    	
    	for (Pair northId : map.getDisplayMap().keySet()){
    		
    		Pair northColorElev = map.getDisplayMap().get(northId);
    		
    		Pair southId = hexImpl.S(northId);
    		Pair southColorElev = map.getDisplayMap().get(southId);
    		
    		Pair eastId = hexImpl.SE(northId);
    		Pair eastColorElev = map.getDisplayMap().get(eastId);
    		
    		printTriangle(northId, southId, eastId, northColorElev, southColorElev, eastColorElev);
    	}
    	
    	
    }
    
    private void printTriangle(Pair northId, Pair southId, Pair eastId,
			Pair northColorElev, Pair southColorElev, Pair eastColorElev) {
		
    	
		
	}
    
    public void drawLine(){
    	
    	for (int i = 0; i < 1000; i++){
    		
    		drawPixel(i,i);
    	}
    }
    
    public void drawPixel(int x, int y){
    	
    	Graphics g = img.getGraphics();
    	
    	g.setColor(Color.black);
        g.drawLine(x, y, x, y);

        g.dispose();
        repaint();
    	
    }
    
    public void drawPixel(int x, int y, int color){
    	
    	Graphics g = img.getGraphics();
    	
    	Color c = new Color(color, color, color);
    	
    	g.setColor(c);
        g.drawLine(x, y, x, y);

        g.dispose();
        repaint();
    	
    }	
    
    /**
     * Get's coordinates that, when printed, will show a hex (can be negative)
     * @param x
     * @param y
     * @return
     */
    public Pair getPrintCoords(int x, int y) {

    	Pair pair = getBasePrintCoords(x, y);
		/*
    	if (x == 75){
    		
    		pair.setX(pair.getX() - (bodyWidth + sideWidth));
    	}*/
    	
    	pair.setX(wrapx(pair.getX(), x == 0));
    	pair.setY(wrapy(pair.getY()));
    	
		return new Pair(pair.getX() - sideWidth * 2 - bodyWidth, pair.getY() - height);
	}
    
    /**
     * Get's coordinates that exist in perlin map
     * @param x
     * @param y
     * @return
     */
    public Pair getBasePrintCoords(int x, int y){
    	
    	int xCoord = (x * (bodyWidth + sideWidth) + panx + X) % X;
    	int yCoord = (y * height + height/2 - x * height/2 + pany + Y) % Y;
    	
		return new Pair(xCoord, yCoord);
    }
    
    
    /**
     * This prints everything about the hex object given
     */
    
    public void printHex(Hex hex){
    	
    	Pair pair = getPrintCoords(hex.getHexID().getX(), hex.getHexID().getY());
    	
    	HexMap map = HexMap.getInstance();
    	
    	int mapWidth = map.getSize()[0] * (bodyWidth + sideWidth);
    	int mapHeight = map.getSize()[1] * height;
    	
    	Color color = hex.getColor();
    	
    	if (mapWidth > X - (bodyWidth + sideWidth) || mapHeight > Y - height){
    		
    		int currentX = pair.getX();
        	int currentY = pair.getY();
    		
    		List<Integer> allX = new ArrayList<Integer>();
        	List<Integer> allY = new ArrayList<Integer>();
        	

        	while (currentX < X){
        		
        		allX.add(currentX);
        		currentX += mapWidth;
        	}
        		
        	while (currentY < Y){
        		
        		allY.add(currentY);
        		currentY += mapHeight;
        	}
        	
        	for (int x : allX){
        		
        		for (int y : allY){
        			
        			printHexAt(x, y, color);
        		}
        	}
    	}
    	
    	else{
    		
    		printHexAt(pair.getX(), pair.getY(), color);
    	}
    }

    
    public void printHexAt(int x, int y, Color color){
    	
    	Graphics g = img.getGraphics();
    	
    	// Setup x for each point
    	int[] xPoints = new int[6];
    	xPoints[0] = x + sideWidth;
    	xPoints[1] = x + sideWidth + bodyWidth;
    	xPoints[2] = x + sideWidth * 2 + bodyWidth;
    	xPoints[3] = xPoints[1];
    	xPoints[4] = xPoints[0];
    	xPoints[5] = x;

    	// Setup y for each point
    	int[] yPoints = new int[6];
    	yPoints[0] = y;
    	yPoints[1] = y;
    	yPoints[2] = y + height/2;
    	yPoints[3] = y + height;
    	yPoints[4] = yPoints[3];
    	yPoints[5] = yPoints[2];
    	
    	Polygon poly = new Polygon(xPoints, yPoints, 6);
    	
    	g.setColor(color);
        g.fillPolygon(poly);
    	
    	//If its water, make it look like water
		if (color != water){
	        
			g.setColor(Color.WHITE);
		}
    	
	    g.drawPolygon(poly);

        g.dispose();
        repaint();
    	
    }

	/**
     * This takes the short coordinates x and y, and blits a hex
     * @param x
     * @param y
     */
    public void drawHex(int x, int y, int[][] noise){
    	
    	Graphics g = img.getGraphics();
    	
    	int xCoord = x * (bodyWidth + sideWidth) + xBuffer;
    	int yCoord = y * height + height/2 - x * height/2 + yBuffer;
    	
    	int color = noise[xCoord][yCoord];
    	
		if (color > 255){
			color = 255;
		}
		
		if (color < 0){
			color = 0;
		}
		
		color = 255 - color;
		Color c = new Color(color, color, color);
    	
    	// Setup x for each point
    	int[] xPoints = new int[6];
    	xPoints[0] = xCoord + sideWidth;
    	xPoints[1] = xCoord + sideWidth + bodyWidth;
    	xPoints[2] = xCoord + sideWidth * 2 + bodyWidth;
    	xPoints[3] = xPoints[1];
    	xPoints[4] = xPoints[0];
    	xPoints[5] = xCoord;

    	// Setup y for each point
    	int[] yPoints = new int[6];
    	yPoints[0] = yCoord;
    	yPoints[1] = yCoord;
    	yPoints[2] = yCoord + height/2;
    	yPoints[3] = yCoord + height;
    	yPoints[4] = yPoints[3];
    	yPoints[5] = yPoints[2];
    	
    	Polygon hex = new Polygon(xPoints, yPoints, 6);
    	
    	//If its water, make it look like water
		if (color > 150){
			c = new Color(68, 247, 235);
			
			g.setColor(c);
	        g.fillPolygon(hex);
	        
	        //g.setColor(Color.WHITE);
	        g.drawPolygon(hex);
		}
    	
		else{
	    	g.setColor(c);
	        g.fillPolygon(hex);
	        
	        g.setColor(Color.WHITE);
	        g.drawPolygon(hex);
		}

        g.dispose();
        repaint();
    	
    }
    
    public int wrapx(int x, boolean xIsZero){
    	
    	int newx = x;
    	
    	if (!(newx <= (panx + X * 10)%X && panx != 0 && !xIsZero)){
    		
    		newx += (bodyWidth + sideWidth);
    	}
    	
    	if (x > X){
    		
    		newx -= X - (bodyWidth + sideWidth);
    	}
    	
    	else if (newx < - (bodyWidth + sideWidth)){
    		
    		newx += X;
    	}
    	
    	return newx;
    }
    
    public int wrapy(int y){
    	
    	int newy = y;
    	
    	if (newy > Y){
    		
    		newy -= Y;
    	}
    	
    	else if (newy < - height){
    		
    		newy += Y;
    	}
    	
    	return newy;
    }
    
    public void moveX(int distance){
    	
    	panx += distance;
    	
    	if (panx < -X){
    		
    		panx += X;
    	}
    }
    
    public void moveY(int distance){
    	
    	pany += distance;
    	
    	if (pany < -Y){
    		
    		pany += Y;
    	}
    }
    
    @Override
    public void paintComponent(Graphics g) {
         g.drawImage(img, 0, 0, null);
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
}