package models;

public class PairOfBodies {

   private BodyOfWater bodyOne;
   private BodyOfWater bodyTwo;

   public PairOfBodies(BodyOfWater one, BodyOfWater two) {
     super();
     this.setBodyOne(one);
     this.setBodyTwo(two);
   }
   
   @Override
   public int hashCode() {

      return getBodyOne().hashCode() * getBodyTwo().hashCode();
   }

   public BodyOfWater getBodyOne() {
      return bodyOne;
   }

   public void setBodyOne(BodyOfWater bodyOne) {
      this.bodyOne = bodyOne;
   }

   public BodyOfWater getBodyTwo() {
      return bodyTwo;
   }

   public void setBodyTwo(BodyOfWater bodyTwo) {
      this.bodyTwo = bodyTwo;
   }
}
