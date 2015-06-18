package models;

public class DPair {
	
    private double x;
    private double y;

    public DPair(double x, double y) {
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

		return (int) (prime * x * x + y);
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof DPair){
			if (this.x == ((DPair) obj).x && this.y == ((DPair) obj).y){
				
				return true;
			}
		}
		
		return false;
	}

    public String toString()
    { 
           return "(" + x + ", " + y + ")"; 
    }

    public double getX() {
    	return x;
    }

    public void setX(double x) {
    	this.x = x;
    }

    public double getY() {
    	return y;
    }

    public void setY(double y) {
    	this.y = y;
    }
    
    public void sumX(double addX){
    	this.x += addX;
    }
    
    public void sumY(double addY){
    	this.y += addY;
    }
    
    public Pair toPair(){
    	
    	return new Pair((int) x, (int) y);
    }
}
