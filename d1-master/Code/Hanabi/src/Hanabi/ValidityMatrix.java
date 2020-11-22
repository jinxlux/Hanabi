package Hanabi;

public class ValidityMatrix extends Table {

  /**
   * This class keeps track of the current valid moves
   * @param gameType
   */

  String myGameType;
  ValidityMatrix(String gameType){
    super(gameType);
    for(int i = 0; i < table[0].length; i++){
      table[0][i] = 1.0;
    }
    myGameType = gameType;

  }

  /**
   * updates the validity matrix when a card is played
   * @param card a card object
   */
  public void updateValidity(Card card){
    int[] indices = card.cardToIndex();
    // only update the table if the move was valid
   if(indices[0] == 4){
     setValueByCard(card, 0.0);
   }
   else if(table[indices[0]][indices[1]].equals(1.0)){
      setValueByCard(card, 0.0);
      setValueByIndex(indices[0] + 1,indices[1] , 1.0);
    }
  }


  public boolean isValid(Card card){
    int[] indices = card.cardToIndex();
    return (table[indices[0]][indices[1]] == 1.0);
  }


  @Override
  public ValidityMatrix clone() {
    ValidityMatrix myCopy = new ValidityMatrix(myGameType);
    for(int i = 0; i < myCopy.table.length; i++){
      for(int j = 0; j < myCopy.table[0].length; j++){
        myCopy.table[i][j] = table[i][j];
      }
    }
    return myCopy;
  }

  public static void main(String[] args){

    ValidityMatrix myTestValidMatrix = new ValidityMatrix("none");
    System.out.println(myTestValidMatrix);

    Card myTestCard = new Card("r", 1, "none");
    System.out.println(myTestValidMatrix.isValid(myTestCard));

    myTestValidMatrix.updateValidity(myTestCard);
    System.out.println(myTestValidMatrix);

    System.out.println(myTestValidMatrix.isValid(myTestCard));

    Card myTestCard2 = new Card("b", 1, "none");
    Card myTestCard3 = new Card("b", 2, "none");

    System.out.println(myTestValidMatrix.isValid(myTestCard2));

    myTestValidMatrix.updateValidity(myTestCard2);
    System.out.println(myTestValidMatrix);
    System.out.println(myTestValidMatrix.isValid(myTestCard2));
    System.out.println(myTestValidMatrix.isValid(myTestCard3));

    myTestValidMatrix.updateValidity(myTestCard3);
    System.out.println(myTestValidMatrix);
    System.out.println(myTestValidMatrix.isValid(myTestCard3));

    Card myTestCard4 = new Card("b", 3, "none");
    Card myTestCard5 = new Card("b", 4, "none");
    Card myTestCard6 = new Card("b", 5, "none");

    myTestValidMatrix.updateValidity(myTestCard4);
    myTestValidMatrix.updateValidity(myTestCard5);
    myTestValidMatrix.updateValidity(myTestCard6);
    System.out.println(myTestValidMatrix);

    myTestValidMatrix.updateValidity(myTestCard2);
    System.out.println(myTestValidMatrix);
  }


}
