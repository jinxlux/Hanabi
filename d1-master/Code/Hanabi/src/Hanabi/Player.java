package Hanabi;

public class Player {

  private Card hand[];
  private boolean isTurn;

  Player(Card Hand[], boolean Turn){
    hand = Hand;
    isTurn = Turn;
  }

  /**
   * @param Pos index into the array of cards
   * @return the card at the position (Pos) in the hand
   */
  public Card getCard(Integer Pos){
    return hand[Pos];
  }

  /**
   *
   * @return A bool that tells us if it is a current players turn or not
   */
  public Boolean getTurn(){
    return isTurn;
  }

  /**
   *
   * @return the hand that is stored for the current player
   */
  public Card[] getHand() { return hand; }


  /**
   * For the player whose current turn it is, it sets their 'isTurn' value to True
   */
  public void startTurn(){
    this.isTurn = true;
  }

  /**
   * For the player whose turn just ended, it sets their 'isTurn' value to False
   */
  public void endTurn(){
    this.isTurn = false;
  }

  /**
   * updates the hand with the hint that has been'st receive'd
   * @param hintArray the boolean array that is the cards that you are hinted
   * @param type and this is the type of the hint that is hinted
   */
  public void receiveHint(Boolean[] hintArray, String type){
    for(int i = 0; i < hintArray.length; i++){
      hand[i].receiveHint(hintArray[i], type);
    }
  }


  @Override
  public Player clone(){
    Card[] handCopy = new Card[hand.length];
    for(int i = 0; i < hand.length; i++){
      handCopy[i] = hand[i].clone();
    }
    Player myCopy = new Player(handCopy, isTurn);
    return myCopy;
  }

  public static void main(String args[]){
    // testing goes here
    System.out.print("Testing");

    //TODO Write these tests
    // 1: Card played and replaced

    // 2: New card in hand



  }

}
