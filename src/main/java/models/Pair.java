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
		final int prime = 31;

		return prime * x * x + y;
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
    	
    	/*if (offset.getX() % 2 == 0){
            offset.setY(offset.getY() - 1);
         }*/
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
}
