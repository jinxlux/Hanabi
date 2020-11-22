package Hanabi;

import java.util.Arrays;

public class Table {

  /** This class is a generalized table class that all tables will extend **/

  public String tableType;
  public Double[][] table;

  public Table(String GameType){

    tableType = GameType;
    // if the game type is standard, or if it is a wildcard game, our tables are 5x5
    // R G B Y W
    // 1
    // 2
    // 3
    // 4
    // 5
    if(GameType.equals("none") || GameType.equals("wild")){
      table = new Double[5][5];
    }
    // if the game type uses rainbows as an extra suit, our tables are 5x6
    // R G B Y W M
    // 1
    // 2
    // 3
    // 4
    // 5
    else if(GameType.equals("firework")){
      table = new Double[5][6];

    }

    for(int i = 0; i < table.length; i++) {
      for(int j = 0; j < table[0].length; j++) {
        table[i][j] = 0.0;
      }
    }
  }

  public Double[][] getTable(){
    return table;
  }

  /**
   * method that given a card type, and a value, alters the value at that card index in the table
   * @param card
   * @param value
   */
  public void setValueByCard(Card card, Double value){
    int[] indices = card.cardToIndex();
    table[indices[0]][indices[1]] = value;
  }

  /**
   * given a pair (i,j) sets the value at that location by index
   * @param i
   * @param j
   * @param value
   */
  public void setValueByIndex(int i, int j, Double value){
    table[i][j] = value;
  }


  /**
   * helper method for calculating the EV of a card
   * @param cpt, the CPS that we're going to return the ewPS of
   */
  public Double elementwiseProductSum(CPS cpt){
    Double value = 0.0;
    for(int i = 0; i < table.length; i++) {
      for (int j = 0; j < table[0].length; j++) {
         value += this.table[i][j] * cpt.myCPT[i][j];
      }
    }
    return value;
  }


  @Override
  public Table clone(){
    Table myCopy = new Table(tableType);

    for(int i = 0; i < table.length; i++){
      for(int j = 0; j < table[0].length; j++){
        myCopy.table[i][j] = table[i][j];
      }
    }
    return myCopy;
  }


  @Override
  public String toString(){
    String s = "";
    int TableSize = table.length;
    for(int i = 0; i < TableSize; i++){
      s += Arrays.toString(table[i]) + "\n";
    }
    return s;
  }



  public static void main(String[] args){

    Table testTable1 = new Table("none");
    Table testTable2 = new Table("wild");
    Table testTable3 = new Table("firework");


    if(testTable1.getTable().length != 5){
      System.out.println("Table length wrong in standard instance");
    }

    if(testTable2.getTable().length != 5){
      System.out.println("Table length wrong in wildcard instance");
    }

    if(testTable3.getTable().length != 5){
      System.out.println("Table length wrong in extra suit instance");
    }

    if(testTable1.getTable()[0].length != 5){
      System.out.println("Table length wrong in standard instance");
    }

    if(testTable2.getTable()[0].length != 5){
      System.out.println("Table length wrong in wildcard instance");
    }

    if(testTable3.getTable()[0].length != 6){
      System.out.println("Table length wrong in extra suit instance");
    }

    Table testTable4 = testTable3.clone();
    testTable4.table[0][0] = 1.0;
    testTable3.table[0][0] = 3.0;

    if(testTable4.table[0][0].equals(testTable3.table[0][0])){
      System.out.println("Deep clone failed, the tables have the same value");
    }


  }


}
