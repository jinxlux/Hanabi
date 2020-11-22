package Hanabi;

import java.util.Arrays;

public class GradientMatrix extends Table {


  private Double[] myValueVector;
  /**
   * This matrix represents a gradient which has various values to help calculate the EV of a card
   * @param gameType
   */
  String myGameType;
  Double myReplacementValue;
  GradientMatrix(String gameType, Double[] valuesVector, Double replacementValue){
    super(gameType);
    for(int i = 0; i < table.length; i++){
      for(int j = 0; j < table[0].length; j++){
        table[i][j] = valuesVector[i];
      }
    }
    myGameType = gameType;
    myValueVector = valuesVector;
    myReplacementValue = replacementValue;
  }

  public void observePlay(Card card){
    setValueByCard(card, myReplacementValue);
    int[] indices = card.cardToIndex();
    if(indices[0] < 4) {
      table[indices[0] + 1][indices[1]] = myValueVector[0];
    }
    if(indices[0] < 3) {
      table[indices[0] + 2][indices[1]] = myValueVector[1];
    }
    if(indices[0] < 2) {
      table[indices[0] + 3][indices[1]] = myValueVector[2];
    }
    if(indices[0] < 1) {
      table[indices[0] + 4][indices[1]] = myValueVector[3];
    }

  }

  @Override
  public GradientMatrix clone() {

    Double[] valueVectorsCopy = new Double[myValueVector.length];
    for(int i = 0; i < myValueVector.length; i++){
      valueVectorsCopy[i] = myValueVector[i];
    }

    GradientMatrix myCopy = new GradientMatrix(myGameType, valueVectorsCopy, myReplacementValue);
    for(int i = 0; i < myCopy.table.length; i++){
      for(int j = 0; j < myCopy.table[0].length; j++){
        myCopy.table[i][j] = table[i][j];
      }
    }
    return myCopy;
  }


  @Override
  public String toString(){
    String returner = "";
    for(int i = 0; i < table.length; i++){
      returner += Arrays.deepToString(table[i]) + "\n";
    }
    return returner;
  }



  public static void main(String[] args){

    Double[] discardVector = {1.0, 0.0, 0.0, 0.0, 0.0};

    GradientMatrix myTestGradient = new GradientMatrix("none", discardVector, 1.0);
    System.out.println(myTestGradient);

    Card myTestCard = new Card("r", 1, "none");

    myTestGradient.observePlay(myTestCard);
    System.out.println(myTestGradient);

    Card myTestCard2 = new Card("r", 2, "none");

    System.out.println(myTestGradient);



  }

}


