package models;

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
       int mapY = Environment.MAP_GRID[1];

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
}
