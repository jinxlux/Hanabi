package Hanabi;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import javafx.util.Pair;

/**
 * This class is the AIs representation of a card, how the AI sees the card internally,
 * and how the AI can perform simple tasks like getting the sum of squares, the sum
 * of values, and the total number of cards that a card could be. 
 */
public class CPS{

  public int[][] myCPS;
  public int possibleCards;
  public double[][] myCPT;
  public String myGameType;
  public double myEV;

  /////////////HEURISTIC VALUES///////////////////

  Double staleness;
  Double hotness;


  CPS(String gameType) {
    int rows;
    myGameType = gameType;
    if (gameType.equals("none") || gameType.equals("firework")) {
      rows = 5;
    }
    else {
      rows = 6;
    }
    myCPS = new int[5][rows];
    myCPT = new double[5][rows];
    possibleCards = 50;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < rows; j++) {
        if (i == 0) {
          myCPS[i][j] = 3;
          myCPT[i][j] = 3 / (double) possibleCards;
        } else if (i == 4) {
          myCPS[i][j] = 1;
          myCPT[i][j] = 1 / (double) possibleCards;
        } else {
          myCPS[i][j] = 2;
          myCPT[i][j] = 2 / (double) possibleCards;
        }
      }
    }
  }


  /**
   * When a hint is received, recalculate the CPS and the possible cards that a card could be
   * @param hint the string representation of a hint
   */
  public void receivePositiveHint(String hint){

    Pair hintType = hintToIndex(hint);
    int index = (Integer)hintType.getValue();
    if(hintType.getKey().equals("rank")) {
      for (int i = 0; i < myCPS.length; i++) {
        for (int j = 0; j < myCPS[0].length; j++) {
          if(i != index) {
            possibleCards -= myCPS[i][j];
            myCPS[i][j] = 0;
          }
          if(possibleCards == 0){
            System.out.println("Something has gone terribly wrong");
          }
        }
      }
    }

    else if(hintType.getKey().equals("suit")){
      for (int i = 0; i < myCPS.length; i++) {
        for (int j = 0; j < myCPS[0].length; j++) {
          if(j != index) {
            possibleCards -= myCPS[i][j];
            myCPS[i][j] = 0;
          }
          if(possibleCards == 0){
            System.out.println("Something has gone terribly wrong");
          }
        }
      }
    }
    calculateCPT();
  }


  /**
   * When a hint is received about a card in the same hand (that isn't this one) we can
   * ensure that this card is no longer that card
   * @param hint the type of hint
   */
  public void receiveNegativeHint(String hint){

    Pair hintType = hintToIndex(hint);
    int index = (Integer)hintType.getValue();
    if(hintType.getKey().equals("rank")) {
      for (int i = 0; i < myCPS.length; i++) {
        for (int j = 0; j < myCPS[0].length; j++) {
          if(i == index) {
            possibleCards -= myCPS[i][j];
            myCPS[i][j] = 0;
          }
          if(possibleCards == 0){
            System.out.println("Something has gone terribly wrong");
          }
        }
      }
    }

    else if(hintType.getKey().equals("suit")){
      for (int i = 0; i < myCPS.length; i++) {
        for (int j = 0; j < myCPS[0].length; j++) {
          if(j == index) {
            possibleCards -= myCPS[i][j];
            myCPS[i][j] = 0;
          }
          if(possibleCards == 0){
            System.out.println("Something has gone terribly wrong");
          }
        }
      }
    }
    calculateCPT();
  }

  /**
   * calculate the CPT of a card
   */
  public void calculateCPT(){
    for(int i = 0; i < myCPS.length; i++){
      for(int j = 0; j < myCPS[0].length; j++){
        myCPT[i][j] = myCPS[i][j] / (double)possibleCards;
      }
    }
  }



  /**
   * when a new card is drawn, we know that our current observation state can
   * be updated with the new card
   * @param card string representation of the new card
   */
  public void observeDrawCPS(Card card){
    int[] cardIndices = card.cardToIndex();
    int rank_i = cardIndices[0];
    int suit_i = cardIndices[1];
    myCPS[rank_i][suit_i] -= 1;
    possibleCards--;

  }
  @Override
  public CPS clone(){
    CPS myCopy = new CPS(myGameType);
    for(int i = 0; i < myCPS.length ; i++){
      for(int j = 0; j < myCPS[0].length; j++){
        myCopy.myCPS[i][j] = myCPS[i][j];
        myCopy.myCPT[i][j] = myCPT[i][j];


      }
    }
    myCopy.possibleCards = possibleCards;
    return myCopy;
  }


  /**
   * internal method for multiplexing over the different types of hints and returning
   * the appropriate index
   * @param hint a string representation of a hint
   * @return a pair, with the type of hint, 
   */
  private static Pair<String, Integer> hintToIndex(String hint){
    Pair returnedPair;

    int indexer;
    switch(hint){
      case("r"):
        indexer = 0;
        break;
      case("g"):
        indexer = 1;
        break;
      case("b"):
        indexer = 2;
        break;
      case("y"):
        indexer = 3;
        break;
      case("w"):
        indexer = 4;
        break;
      default:
        returnedPair = new Pair("rank", Integer.parseInt(hint) - 1);
        return returnedPair;
    }
    returnedPair = new Pair("suit", indexer);
    return returnedPair;
  }


  /**
   * toString method, although complicated it's going to be very helpful in debugging.
   * @return String representation of the the CPS, the CPT, the number of possible cards, and the sum of squares
   */
  @Override
  public String toString() {
    String returnMe = "";
    for(int i = 0; i < myCPS.length; i++){
      returnMe += Arrays.toString(myCPS[i]) + " [";
      for(int j = 0; j < myCPS.length; j++){
        returnMe += String.format("%.2f", myCPT[i][j]);
        if(j != myCPS.length - 1){
          returnMe += ", ";
        }
      }
      returnMe += "] \n";
    }
    returnMe += possibleCards;
    return returnMe;
  }




  public static void main(String[] args){

    CPS myTestCPS1 = new CPS("none");
    CPS myTestCPS2 = new CPS("firework");

    if(myTestCPS1.myCPS[0][0] != 3){
      System.out.println("Error in creation of new CPS");
    }

    System.out.println(myTestCPS1);
    System.out.println(myTestCPS2);

    myTestCPS1.receivePositiveHint("r");

    System.out.println(myTestCPS1);

    CPS myTestCPS3 = new CPS("none");

    myTestCPS3.receiveNegativeHint("r");
    myTestCPS3.calculateCPT();
    System.out.println(myTestCPS3);

    CPS myTestCPS4 = new CPS("none");

    myTestCPS4.receiveNegativeHint("g");
    myTestCPS4.calculateCPT();
    System.out.println(myTestCPS4);

    CPS myTestCPS5 = new CPS("none");

    myTestCPS5.receiveNegativeHint("1");
    myTestCPS5.receiveNegativeHint("w");
    myTestCPS5.calculateCPT();
    System.out.println(myTestCPS5);

    CPS myTestCPS6 = new CPS("none");

    myTestCPS6.receivePositiveHint("r");
    myTestCPS6.receivePositiveHint("1");
    myTestCPS6.calculateCPT();
    System.out.println(myTestCPS6);

    CPS myTestCPS7 = new CPS("none");

    Card myTestCard = new Card("r", 1, "none");

    myTestCPS7.observeDrawCPS(myTestCard);

    myTestCPS7.calculateCPT();
    System.out.println(myTestCPS7);

    CPS myTestCPS8 = myTestCPS7.clone();
    myTestCPS8.observeDrawCPS(myTestCard);

    if(myTestCPS7.myCPS[0][0] == myTestCPS8.myCPS[0][0]){
      System.out.println("Deep copy method failed");
    }
    if(myTestCPS7.possibleCards == myTestCPS8.possibleCards){
      System.out.println("Deep copy method failed");
    }


  }

}