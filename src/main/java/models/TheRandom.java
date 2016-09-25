package models;

import java.util.Random;

public class TheRandom {

 //For singletonhood
   final private static TheRandom singleton = new TheRandom();
   final private Random rand = new Random();
   
   public TheRandom() {
      // Exists only to defeat instantiation.
   }
   
   public static TheRandom getInstance() {
      return singleton;
   }
   
    synchronized public Random get() {
      return rand;
   }
}
