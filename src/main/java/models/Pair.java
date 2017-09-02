package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import enums.Direction;

public class Pair {
	
    private int x;
    private int y;

    public Pair(int x, int y) {
    	super();
    	this.x = x;
      this.y = y;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return Environment.MAP_WIDTH * y + x;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Pair){
			if (this.x == ((Pair) obj).x && this.y == ((Pair) obj).y){
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
   public Pair clone() {

      return new Pair(x, y);
   }

    public String toString()
    { 
           return "(" + x + ", " + y + ")"; 
    }

    public int getX() {
    	return x;
    }

    public void setX(int x) {
    	this.x = x;
    }
    
    public int getYbyXdifferential(){
    	
    	return getY() + getX() / 2;
    }

    public int getY() {
    	return y;
    }

    public void setY(int y) {
    	this.y = y;
    }
    
    public void sumX(int addX){
    	this.x += addX;
    }
    
    public void sumY(int addY){
    	this.y += addY;
    }
    
    public Pair N() {

       return wrap(x, y - 1);
    }

    public Pair NE() {

       return wrap(x + 1, y);
    }

    public Pair SE() {

       return wrap(x + 1, y + 1);
    }

    public Pair S() {

       return wrap(x, y + 1);
    }

    public Pair SW() {

       return wrap(x - 1, y);
    }

    public Pair NW() {

       return wrap(x - 1, y - 1);
    }
    
    public Pair merge(Pair otherPair) {

       return wrap(x + otherPair.getX(), y + otherPair.getY());
    }
    
    public static Pair wrap(int argX, int argY){
       
       int newX = argX;
       int newY = argY;

       int mapX = Environment.MAP_GRID[0];
       int mapY = Environment.MAP_GRID[1] ;

       if (argX >= mapX) {

          newX %= mapX;
       }

       if (argX < 0) {

          newX += (mapX * (Math.abs(newX / mapX) + 1));
       }

       if (newY >= mapY + newX / 2) {

          newY %= mapY;
       }

       if (newY < newX / 2) {

          newY = (newY - newX / 2 + mapY * mapY) % mapY + newX / 2;
       }

       return new Pair(newX, newY);
    }
    
    public Pair wrap(){

       return wrap(x, y);
    }
    
    public Pair getHexIdFromDirection(Direction direction){

       Pair returnPair = this;

       if (direction != null){

          switch (direction){

          case north: 

             returnPair = N();

             break;

          case northeast: 

             returnPair = NE();

             break;

          case southeast: 

             returnPair = SE();

             break;

          case south: 

             returnPair = S();

             break;

          case southwest: 

             returnPair = SW();

             break;

          case northwest: 

             returnPair = NW();

             break;

          default:

             break;
          }
       }

       return returnPair;
    }
    
    public Pair getRandomNeighbor(){
    	
    	List<Pair> neighbors = getNeighbors();
    	int index = TheRandom.getInstance().get().nextInt(neighbors.size());
    	return neighbors.get(index);
    }
    
    public List<Pair> getNeighbors() {

       List<Pair> neighbors = new ArrayList<Pair>();

       neighbors.add(N());
       neighbors.add(NW());
       neighbors.add(SW());
       neighbors.add(S());
       neighbors.add(SE());
       neighbors.add(NE());
       
       return neighbors;
    }

    public Set<Pair> getNeighborsSet() {

       Set<Pair> neighbors = new HashSet<Pair>();

       neighbors.add(N());
       neighbors.add(NW());
       neighbors.add(SW());
       neighbors.add(S());
       neighbors.add(SE());
       neighbors.add(NE());

       return neighbors;
    }
    
    public boolean inBounds() {

       return this.equals(wrap());
    }
}
