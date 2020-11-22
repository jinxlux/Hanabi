package Hanabi;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class keeps track of the state of the game being played.
 */
public class GameState implements Cloneable{

  private Integer hintsRemaining;
  private Integer fusesRemaining;
  private Integer cardsRemaining;
  private Integer timeRemaining;
  private Integer score;
  private boolean[][] cardsOnTable;
  private LinkedList<Card> discardPile;
  private ArrayList<Player> players;
  private Integer turn;
  private String mode;
  private Integer timePerTurn;


  private Integer cardsInHand;
  private Integer playersInGame;
  private Integer position;

  public CPS topCard;
  private ValidityMatrix myValidityMatrix;
  private GradientMatrix myDiscardGradient;
  private GradientMatrix myPlayGradient;

  private AI myAI;
  private boolean gameStarted;
  private Integer playersNeeded;
  private Integer timeout;
  private boolean AIsOn;

  //----------------- HEURISTIC VALUES --------------------//
  private Double[] discardVector = {0.0, 0.1, 0.2, 0.3, 0.4};
  private Double[] playVector = {1.0, 0.0, 0.0, 0.0, 0.0};

  //----------------- VIEW Fields ------------------------//

  public ArrayList<Card> allCards;
  public Card selectedCard;
  public ArrayList<GameStateListener> subscribers;

  Card hoveringCard = new Card("?", 0, "none");
  boolean isDargging = false;

  PlayDiscardBox playBox;
  PlayDiscardBox discardBox;
  PlayDiscardBox turnBox;

  boolean hint = false;

  double hintX;
  double hintY;

  ///////////////////////// Class Constructors ///////////////////////////

  /**
   * The constructor method for GameState that initiates a new GameState object given an array of Player objects
   * @param players The Array of initialized players
   */
  public GameState(ArrayList<Player> players, String mode, Integer timePerTurn){
    this.players = players;
    this.playersInGame = this.getPlayers().size();
    this.mode = mode;
    this.hintsRemaining = 8;
    this.fusesRemaining = 3;
    this.score = 0;
    this.discardPile = new LinkedList<>();
    this.turn = 0;
    this.timePerTurn = timePerTurn;

    if (this.getPlayersInGame() > 3){
      this.cardsInHand = 4;
    }else {
      this.cardsInHand = 5;
    }

    topCard = new CPS(mode); // this is how we can generate a random card for search


    myDiscardGradient = new GradientMatrix(mode, discardVector, 1.0);
    myPlayGradient = new GradientMatrix(mode, playVector, 0.0);


    if (this.mode.equals("firework")){
      // To initialize the cards on table:
      // the legal played cards on table are indicated in 2D array
      // and all elements are false, indicating no cards have been played yet
      // a true indicates the specific legal card is played
      // each row represents a rank: 1, 2, 3, 4, 5
      // each column represents a suit: R, G, B, Y, W
      // The initial cards on table would like:
      //    {
      //        {false, false, false, false, false, false},
      //        {false, false, false, false, false, false},
      //        {false, false, false, false, false, false},
      //        {false, false, false, false, false, false},
      //        {false, false, false, false, false, false}
      //     }


      cardsOnTable = new boolean[5][6];

      for (int i=0; i<5; i++){
        for (int j=0; j<6; j++) {
          this.cardsOnTable[i][j] = false;
        }
      }


    }else{
      // The initial cards on table would like:
      //    {
      //        {false, false, false, false, false},
      //        {false, false, false, false, false},
      //        {false, false, false, false, false},
      //        {false, false, false, false, false},
      //        {false, false, false, false, false}
      //    };
      this.cardsOnTable = new boolean[5][5];
      for (int i=0; i<5; i++){
        for (int j=0; j<5; j++) {
          this.cardsOnTable[i][j] = false;
        }
      }
    }


    if(this.mode.equals("none")) {
      this.cardsRemaining = 50;
    }
    else {
      this.cardsRemaining = 60;
    }

    this.myValidityMatrix = new ValidityMatrix(mode);

    this.gameStarted = true;

    //----------------------- VIEW ------------------------//

    this.subscribers = new ArrayList<>();
    this.allCards = new ArrayList<>();
    this.selectedCard = null;

  }

  /**
   * Default constructor which exists to show the player an empty table when they successfully
   * join a game, but it hasn't been started
   */
  public GameState(Integer playersNeeded, Integer timeout, String mode){
    this.playersNeeded = playersNeeded;
    this.mode = mode;
    this.hintsRemaining = 8;
    this.fusesRemaining = 3;
    this.score = 0;
    this.discardPile = new LinkedList<>();
    this.turn = 0;
    this.gameStarted = false;
    this.timePerTurn = timeout;

    this.subscribers = new ArrayList<>();
    this.allCards = new ArrayList<>();
    this.selectedCard = null;

    if (playersNeeded > 3){
      this.cardsInHand = 4;
    }else {
      this.cardsInHand = 5;
    }

    ArrayList<Player> players = new ArrayList<>(playersNeeded);
    for (int i = 0; i < playersNeeded; i++) {
      Card[] hand = new Card[cardsInHand];
      for (int j = 0; j < cardsInHand; j++) {
        hand[j] = new Card("?",0, mode);
      }
      Player newPlayer = new Player(hand, false);
      players.add(newPlayer);
    }
    this.players = players;
  }


  //////////////////// Class Gatters and Setters //////////////////

  // getters
  public Integer getHintsRemaining(){
    return hintsRemaining;
  }

  public Integer getFusesRemaining() {
    return fusesRemaining;
  }

  public Integer getCardsRemaining() {
    return cardsRemaining;
  }

  public Integer getTimeRemaining() {
    return timeRemaining;
  }

  public void setGameStarted(boolean s) {
    this.gameStarted = s;
  }

  public boolean getGameStarted() {
    return this.gameStarted;
  }

  public Integer getScore() {
    return score;
  }

  public boolean[][] getCardsOnTable() {

    return cardsOnTable;
  }

  public LinkedList<Card> getDiscardPile() {
    return discardPile;
  }

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public Integer getTurn() {
    return turn;
  }

  public String getMode() {
    return mode;
  }

  public Integer getTimePerTurn() {
    return timePerTurn;
  }

  public Integer getCardsInHand() {

    return cardsInHand;
  }

  public ValidityMatrix getMyValidityMatrix(){
    return myValidityMatrix;
  }


  public Integer getPlayersInGame() {

    return playersInGame;
  }

  public Integer getPosition() {

    return position;
  }


  // setters
  public void setHintsRemaining(Integer hintsRemaining) {
    this.hintsRemaining = hintsRemaining;
    notifySubscribers();
  }

  public void setFusesRemaining(Integer fusesRemaining) {
    this.fusesRemaining = fusesRemaining;
    notifySubscribers();
  }

  public void setCardsRemaining(Integer cardsRemaining) {
    this.cardsRemaining = cardsRemaining;
    notifySubscribers();
  }

  public void setTimeRemaining(Integer timeRemaining) {
    this.timeRemaining = timeRemaining;
    notifySubscribers();
  }

  public void setScore(Integer score) {
    this.score = score;
    notifySubscribers();
  }

  public void setCardsOnTable(boolean[][] cardsOnTable) {
    this.cardsOnTable = cardsOnTable;
    notifySubscribers();
  }

  public void setDiscardPile(LinkedList<Card> discardPile) {
    this.discardPile = discardPile;
    notifySubscribers();
  }

  public void setPlayers(ArrayList<Player> players) {
    this.players = players;
    notifySubscribers();
  }

  public void setTurn(Integer turn) {
    this.turn = turn;
    notifySubscribers();
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void setTimePerTurn(Integer timePerTurn) {

    this.timePerTurn = timePerTurn;
  }

  public void setCardsInHand(Integer cardsInHand) {

    this.cardsInHand = cardsInHand;
  }

  public void setPlayersInGame(Integer playersInGame) {

    this.playersInGame = playersInGame;
  }

  public void setPosition(Integer position) {

    this.position = position;
    //notifySubscribers();
  }

  public boolean getAIOn(){
    return this.AIsOn;
  }

  public AI getMyAI(){
    return myAI;
  }

  /**
   * simple method that toggles the AI from being able to make moves and not being able to make them
   */
  public void toggleAI(){
    if(AIsOn){
      AIsOn = false;
    }
    else{
      AIsOn = true;
    }
  }

  /**
   * Appends a discarded card to the linked list of discards
   * @param card The Card which was discarded
   */
  public void appendDiscard(Card card){
    discardPile.addLast(card);
  }


  /**
   * this should ALWAYS be on, the AI, should be observing the game even when it's not actively
   * playing
   */
  public void startAI(Integer myPosition){
    myAI.setMyNumber(myPosition);
    String[] puCards = new String[cardsInHand];
    for(int i = 0; i < cardsInHand; i++){
      puCards[i] = "??";
    }

    for(int i = 0; i < playersInGame; i++){
      String[] currentCardsArray = new String[cardsInHand];

      for(int c = 0; c < cardsInHand; c++){
        currentCardsArray[c] = players.get(i).getHand()[c].getCardAsString();

      }

      if(i != myPosition) {
        myAI.slaves[i].setSlaveCards(currentCardsArray);
      }

      else {
        myAI.slaves[myPosition].setSlaveCards(puCards);
      }
    }

    myAI.initialObservations();

  }


  /////////////////////// AI related methods ///////////////////////////


  /**
   * Updates the cards which lay face up on the table after a valid card was played.
   * @param card The Card which was played
   */
  public void updateCardsOnTable(Card card){
    int[] is = card.cardToIndex();
    this.cardsOnTable[is[0]][is[1]] = true;

    myValidityMatrix.updateValidity(card);
    myDiscardGradient.observePlay(card);
    myPlayGradient.observePlay(card);
    notifySubscribers();

    // update validity matrix
    // update the gradients
  }


  //////////////////////////// GameState methods ///////////////////////////


  /**
   * Updates the hand of a player in the array of players.
   * @param player The player that made the valid move
   * @param card The new card that was drawn
   * @param pos The card position in the player’s hand that should be replaced by the new card
   */
  public void updatePlayerHand(Player player, Card card, Integer pos){
    player.getHand()[pos] = card;
    notifySubscribers();
  }


  public void updatePlayerHand(Player player, String myColor, Integer myNumber, Integer pos){
    player.getHand()[pos].color = myColor;
    player.getHand()[pos].number = myNumber;
    notifySubscribers();
  }

  /**
   * Increments the hints remaining by one.
   */
  public void incrementHints(){
    if (this.hintsRemaining < 8) {
      this.hintsRemaining++;
    }
    notifySubscribers();
  }

  /**
   * Decreases the hints remaining by one.
   */
  public void decrementHints(){
    if (this.hintsRemaining > 0){
      this.hintsRemaining--;
    }
    notifySubscribers();
  }

  /**
   * Decreases the fuses remaining by one.
   */
  public void decrementFuses(){
    this.fusesRemaining--;
    notifySubscribers();
  }

  /**
   * Decreases the number of cards re- maining in the draw pile by one.
   */
  public void decrementCards(){
    this.cardsRemaining--;
    notifySubscribers();
  }

  /**
   * Decreases the time remaining for
   * the current turn by one second.
   */
  public void decrementTime(){
    if (this.timeRemaining == 0){
      this.setTimeRemaining(this.timePerTurn);
    }else{
      this.timeRemaining--;
    }
    notifySubscribers();
  }

  /**
   * This increments the turn goes in the game
   */
  public void incrementTurn(){

    int nPs = this.players.size();

    this.setTurn((this.turn+1) % nPs);
    notifySubscribers();

  }


  @Override
  protected GameState clone() throws CloneNotSupportedException {

    GameState copy = (GameState) super.clone();
    ValidityMatrix vmCopy = this.myValidityMatrix.clone();
    GradientMatrix dgCopy = this.myDiscardGradient.clone();
    GradientMatrix pgCopy = this.myDiscardGradient.clone();

    copy.myValidityMatrix = vmCopy;
    copy.myDiscardGradient = dgCopy;
    copy.myPlayGradient = pgCopy;

    ArrayList<Player> playersCopy = new ArrayList<>();
    for(int i = 0; i < players.size(); i++){
      playersCopy.add(i, players.get(i).clone());
    }

    copy.players = playersCopy;
    copy.topCard = topCard.clone();

    return copy;
  }


  ////////////////////////////// View related methods //////////////////////////

  // ---------------------------Added By Hugh Start-----------------------------

  public void sethintXY(Card card){
    this.hintX = card.x + (card.width/3);
    this.hintY = card.y + card.height;
  }

  public void setHint(){
    this.hint = true;
    notifySubscribers();
  }

  public void deSetHint(){
    this.hint = false;
    notifySubscribers();
  }

  public boolean handContain(Card[] hand, Card card){
    for(Card singleCard : hand){
      if (singleCard.equals(card)){
        return true;
      }
    }
    return false;
  }

  public void setTurnBox(double canvasWidth, double canvasHeight){
    int currentTurn = this.getTurn()%this.getPlayers().size();
    switch (this.getPlayers().size()){

      case 2:
        switch (currentTurn){
          case 0:
            turnBox = new PlayDiscardBox(canvasWidth/6, 5*(canvasHeight/6),
                canvasWidth-(canvasWidth/3), canvasHeight-5*(canvasHeight/6));
            break;

          case 1:
            turnBox = new PlayDiscardBox(0,0,canvasWidth/2,
                canvasHeight-5*(canvasHeight/6));
            break;

        }
        break;


      case 3:
        switch (currentTurn){
          case 0:
            turnBox = new PlayDiscardBox(canvasWidth/6, 5*(canvasHeight/6),
                canvasWidth-(canvasWidth/3), canvasHeight-5*(canvasHeight/6));
            break;

          case 1:
            turnBox = new PlayDiscardBox(0,0,canvasWidth/2,
                canvasHeight-5*(canvasHeight/6));
            break;

          case 2:
            turnBox = new PlayDiscardBox(canvasWidth/2, 0,
                canvasWidth-(canvasWidth/3), canvasHeight-5*(canvasHeight/6));
            break;
        }
        break;
      case 4:
        switch (currentTurn){
          case 0:
            turnBox = new PlayDiscardBox(canvasWidth/6, 5*(canvasHeight/6),
                canvasWidth-(canvasWidth/3), canvasHeight-5*(canvasHeight/6));
            break;

          case 2:
            turnBox = new PlayDiscardBox(canvasWidth/6, 0, canvasWidth-(canvasWidth/3),
                canvasHeight-5*(canvasHeight/6));
            break;

          case 1:
            turnBox = new PlayDiscardBox(0, canvasHeight-5*(canvasHeight/6),
                canvasWidth/6, 4*(canvasHeight/6));
            break;

          case 3:
            turnBox = new PlayDiscardBox(5*(canvasWidth/6), canvasHeight-5*(canvasHeight/6),
                canvasWidth/6, 4*(canvasHeight/6));
            break;
        }
        break;

      case 5:
        switch (currentTurn){
          case 0:
            turnBox = new PlayDiscardBox(canvasWidth/6, 5*(canvasHeight/6),
                canvasWidth-(canvasWidth/3), canvasHeight-5*(canvasHeight/6));
            break;

          case 1:
            turnBox = new PlayDiscardBox(0, canvasHeight-5*(canvasHeight/6) + 70,
                canvasWidth/6, 4*(canvasHeight/6));
            break;

          case 2:
            turnBox = new PlayDiscardBox(0,0,canvasWidth/2,
                canvasHeight-5*(canvasHeight/6));
            break;

          case 3:
            turnBox = new PlayDiscardBox(canvasWidth/2, 0,
                canvasWidth-(canvasWidth/3), canvasHeight-5*(canvasHeight/6));
            break;

          case 4:
            turnBox = new PlayDiscardBox(5*(canvasWidth/6), canvasHeight-5*(canvasHeight/6) + 70,
                canvasWidth/6, 4*(canvasHeight/6));
            break;
        }
        break;
    }
  }

  public void setPlayDiscardBox(double canvasWidth, double canvasHeight){
    playBox = new PlayDiscardBox(canvasWidth / 6, canvasHeight / 6,
        4 * (canvasWidth / 12), 4 * (canvasHeight / 6));

    discardBox = new PlayDiscardBox(canvasWidth / 6 + 4 * (canvasWidth / 12), canvasHeight / 6,
        4 * (canvasWidth / 12), 4 * (canvasHeight / 6));
  }

  public void setCardCoordinates(double canvasWidth, double canvasHeight){
    switch (getPlayers().size()){

      case 2:
        double player1_2i = canvasWidth/6;
        for (Card card : getPlayers().get(0).getHand()){
          card.x = player1_2i;
          card.y = 5*(canvasHeight/6);
          card.initX = player1_2i;
          card.initY = 5*(canvasHeight/6);
          card.width = (canvasWidth-(canvasWidth/3))/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player1_2i+=(canvasWidth-(canvasWidth/3))/4;
        }

        double player2_2i = 0;
        for (Card card : getPlayers().get(1).getHand()){
          card.x = player2_2i;
          card.y = 0;
          card.initX = player2_2i;
          card.initY = 0;
          card.width = (canvasWidth/2)/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player2_2i+=(canvasWidth/2)/4;
        }


        break;



      case 3:
        double player1_3i = canvasWidth/6;
        for (Card card : getPlayers().get(0).getHand()){
          card.x = player1_3i;
          card.y = 5*(canvasHeight/6);
          card.initX = player1_3i;
          card.initY = 5*(canvasHeight/6);
          card.width = (canvasWidth-(canvasWidth/3))/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player1_3i+=(canvasWidth-(canvasWidth/3))/4;
        }

        double player2_3i = 0;
        for (Card card : getPlayers().get(1).getHand()){
          card.x = player2_3i;
          card.y = 0;
          card.initX = player2_3i;
          card.initY = 0;
          card.width = (canvasWidth/2)/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player2_3i+=(canvasWidth/2)/4;
        }

        double player3_3i = canvasWidth/2;
        for (Card card : getPlayers().get(2).getHand()){
          card.x = player3_3i;
          card.y = 0;
          card.initX = player3_3i;
          card.initY = 0;
          card.width = (canvasWidth/2)/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player3_3i+=(canvasWidth/2)/4;
        }

        break;

      case 4:
        double player1_4i = canvasWidth/6;
        for (Card card : getPlayers().get(0).getHand()){
          card.x = player1_4i;
          card.y = 5*(canvasHeight/6);
          card.initX = player1_4i;
          card.initY = 5*(canvasHeight/6);
          card.width = (canvasWidth-(canvasWidth/3))/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player1_4i+=(canvasWidth-(canvasWidth/3))/4;
        }

        double player2_4i = canvasWidth/6;
        for (Card card : getPlayers().get(1).getHand()){
          card.x = player2_4i;
          card.y = 0;
          card.initX = player2_4i;
          card.initY = 0;
          card.width = (canvasWidth-(canvasWidth/3))/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player2_4i+=(canvasWidth-(canvasWidth/3))/4;
        }

        double player3_4i = canvasHeight-5*(canvasHeight/6);
        for (Card card : getPlayers().get(2).getHand()){
          card.x = 0;
          card.y = player3_4i;
          card.initX = 0;
          card.initY = player3_4i;
          card.width = canvasWidth/6;
          card.height = (4*(canvasHeight/6))/4;
          player3_4i+=(4*(canvasHeight/6))/4;
        }

        double player4_4i = canvasHeight-5*(canvasHeight/6);
        for (Card card : getPlayers().get(3).getHand()){
          card.x = 5*(canvasWidth/6);
          card.y = player4_4i;
          card.initX = 5*(canvasWidth/6);
          card.initY = player4_4i;
          card.width = canvasWidth/6;
          card.height = (4*(canvasHeight/6))/4;
          player4_4i+=(4*(canvasHeight/6))/4;
        }

        break;

      case 5:
        double player1_5i = canvasWidth/6;
        for (Card card : getPlayers().get(0).getHand()){
          card.x = player1_5i;
          card.y = 5*(canvasHeight/6);
          card.initX = player1_5i;
          card.initY = 5*(canvasHeight/6);
          card.width = (canvasWidth-(canvasWidth/3))/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player1_5i+=(canvasWidth-(canvasWidth/3))/4;
        }

        double player2_5i = canvasHeight-5*(canvasHeight/6) + 70;
        for (Card card : getPlayers().get(1).getHand()){
          card.x = 0;
          card.y = player2_5i;
          card.initX = 0;
          card.initY = player2_5i;
          card.width = canvasWidth/6;
          card.height = (4*(canvasHeight/6))/4;
          player2_5i+=(4*(canvasHeight/6))/4;
        }


        double player3_5i = 0;
        for (Card card : getPlayers().get(2).getHand()){
          card.x = player3_5i;
          card.y = 0;
          card.initX = player3_5i;
          card.initY = 0;
          card.width = (canvasWidth/2)/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player3_5i+=(canvasWidth/2)/4;
        }

        double player4_5i = canvasWidth/2;
        for (Card card : getPlayers().get(3).getHand()){
          card.x = player4_5i;
          card.y = 0;
          card.initX = player4_5i;
          card.initY = 0;
          card.width = (canvasWidth/2)/4;
          card.height = canvasHeight-5*(canvasHeight/6);
          player4_5i+=(canvasWidth/2)/4;
        }

        double player5_5i = canvasHeight-5*(canvasHeight/6) + 70;
        for (Card card : getPlayers().get(4).getHand()){
          card.x = 5*(canvasWidth/6);
          card.y = player5_5i;
          card.initX = 5*(canvasWidth/6);
          card.initY = player5_5i;
          card.width = canvasWidth/6;
          card.height = (4*(canvasHeight/6))/4;
          player5_5i+=(4*(canvasHeight/6))/4;
        }



    }
    notifySubscribers();
  }

  public void addToAllCard(){
    for (int i=0; i<getPlayers().size(); i++){
      for (Card card : getPlayers().get(i).getHand()){
        allCards.add(card);
      }
    }
    notifySubscribers();
  }

  public boolean checkHit(double clickX, double clickY){
    return allCards.stream()
        .anyMatch(card -> card.checkHit(clickX, clickY));
  }

  public Card whichCard(double x, double y) {
    Card found = null;
    for (Card card : allCards) {
      if (card.checkHit(x,y)) {
        found = card;
      }
    }
    return found;
  }

  public void setSelectedCard(Card card){
    selectedCard = card;
    notifySubscribers();
  }

  public void moveCard(Card card, double dX, double dY){
    card.moveCard(dX, dY);
    notifySubscribers();
  }

  public void setCardXY(Card card, double newX, double newY){
    card.setXY(newX, newY);
    notifySubscribers();
  }

  public void addSubscriber(GameStateListener sub){
    subscribers.add(sub);
  }

  public void notifySubscribers() {
    subscribers.forEach(sub -> sub.modelChanged());
  }

  private void cardNotifySubscribers(){
    subscribers.forEach(sub -> sub.modelChanged());
  }

  public void setIsDragging(){
    this.isDargging = true;
    notifySubscribers();
  }

  public void deSetIsDragging(){
    this.isDargging = false;
    notifySubscribers();
  }

  public void setCardHovering(Card card){
    card.setHovering(true);
    notifySubscribers();
  }

  public void deSetCardHovering(Card card){
    card.setHovering(false);
    notifySubscribers();
  }

  public void setHoveringCard(Card card){
    hoveringCard = card;
    notifySubscribers();
  }
  // ---------------------------Added By Hugh Start-----------------------------


  ////////////////////////////// Class Testing //////////////////////////////

  public static void main(String[] args) throws CloneNotSupportedException{

    // unit tests
    System.out.println("-------\n"
        + "Testing\n"
        + "-------");

    // set up
    Integer nError = 0;
    Integer nFail = 0;

    Card r1 = new Card("r", 1,"none");
    Card r2 = new Card("r", 2,"none");
    Card r3 = new Card("r", 3,"none");
    Card r4 = new Card("r", 4,"none");
    Card[] cards1 = {r1, r2, r3, r4};

    Card b1 = new Card("b", 1,"none");
    Card b2 = new Card("b", 2,"none");
    Card b3 = new Card("b", 3,"none");
    Card b4 = new Card("b", 4,"none");
    Card[] cards2 = {b1, b2, b3, b4};

    Card g1 = new Card("g", 1,"none");
    Card g2 = new Card("g", 2,"none");
    Card g3 = new Card("g", 3,"none");
    Card g4 = new Card("g", 4,"none");
    Card[] cards3 = {g1, g2, g3, g4};

    Card y1 = new Card("y", 1,"none");
    Card y2 = new Card("y", 2,"none");
    Card y3 = new Card("y", 3,"none");
    Card y4 = new Card("y", 4,"none");
    Card[] cards4 = {y1, y2, y3, y4};

    Card w1 = new Card("w", 1,"none");
    Card w2 = new Card("w", 2,"none");
    Card w3 = new Card("w", 3,"none");
    Card w4 = new Card("w", 4,"none");
    Card[] cards5 = {w1, w2, w3, w4};

    Player p1 = new Player(cards1, false);
    Player p2 = new Player(cards2, false);
    Player p3 = new Player(cards3, false);
    Player p4 = new Player(cards4, false);
    Player p5 = new Player(cards5, false);

    ArrayList<Player> players = new ArrayList<>(3);
    players.add(0, p1);
    players.add(1, p2);
    players.add(2, p3);
    players.add(3, p4);
    players.add(4, p5);

    GameState testObj = new GameState(players, "firework", 60);

    // 1: linked list length increment
    try{
      testObj.appendDiscard(r3);
      if (testObj.discardPile.size() != 1){
        System.out.println("ERROR 1: discardPile should have value 1");
        nError++;
      }

    }catch (Exception e){
      System.out.println("BAD TEST 1!");
      nFail++;
    }

    // 2: the last element
    try{
      if (testObj.discardPile.getLast() != r3){
        System.out.println("ERROR 2: ");
        nError++;
      }

    } catch (Exception e){
      System.out.println("BAD TEST 2!");
      nFail++;
    }

    // 3: new card in array
    try{
      testObj.updateCardsOnTable(r1);
      if (!testObj.cardsOnTable[0][0]){
        System.out.println("ERROR 3: Card red 1 is played, the value in the array is not true");
        nError++;
      }

    } catch (Exception e){
      System.out.println("BAD TEST 3!");
      nFail++;
    }

    // 4: correct index
    try{
      testObj.updateCardsOnTable(w3);

      // card white 3 should be at the index [2][4]
      if (!testObj.getCardsOnTable()[2][4]){
        System.out.println("ERROR 4: Card white 3 is played, the value in the array is not true");
        nError++;
      }

    } catch (Exception e){
      System.out.println("BAD TEST 4!");
      nFail++;
    }

    // 5: card played and replaced
    try{
      testObj.updatePlayerHand(players.get(0), w1, 0);
      if (testObj.players.get(0).getCard(0) != w1){
        System.out.println("ERROR 5: the first card in the first player should be white 1");

        testObj.updatePlayerHand(players.get(0), w2, 0);
        if (testObj.players.get(0).getCard(0) != w2){
          System.out.println("ERROR 5: the first card in the first player should be white 2");
        }

        nError++;
      }

    } catch (Exception e){
      System.out.println("BAD TEST 5!");
      nFail++;
    }

    // 6: new card in hand
    try{
      testObj.updatePlayerHand(players.get(0), w3, 1);
      if (testObj.players.get(0).getCard(1) != w3){
        System.out.println("ERROR 6: the second card in the first player should be white 3");
        nError++;
      }
    } catch (Exception e){
      System.out.println("BAD TEST 6!");
      nFail++;
    }

    // test result
    System.out.println("\n--------\nTest results:\n");
    if (nFail != 0){
      System.out.println(nFail + " bad test(s), INVALID TESTS");
    }else {
      System.out.println("Total error: " + nError);
    }
    System.out.println("--------");


    // test clone()
    System.out.println("\n\nObject clones\n");

    GameState gCopy = testObj;
    System.out.println(testObj.toString());
    System.out.println(gCopy.toString());

    System.out.println();
    GameState gDeepcopy = testObj.clone();
    System.out.println(testObj.toString());
    System.out.println(gDeepcopy.toString());

    Boolean[] myTestHint = {true, false, false};
    gDeepcopy.getPlayers().get(0).receiveHint(myTestHint, "r");
    gDeepcopy.getPlayers().get(0).receiveHint(myTestHint, "1");

    if(testObj.getPlayers().get(0).getHand()[0].getMyCPS().myCPT[0][0] == 1.0){
      System.out.println("Deep copy failed on player CPT");
    }




  }

}
