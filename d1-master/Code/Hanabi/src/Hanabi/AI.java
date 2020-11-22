package Hanabi;


import static java.lang.Integer.parseInt;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.LinkedList;


public class AI {

  // *********** HEURISTIC VALUES FOR OPTIMIZATIONS ********************//
  /* Firstly, I believe that some of these heuristics could potentially be negatively affecting
  the performance of the AI, but searching for which values these weights collide with each other
  is the topic of further research.  For the time being, the values were picked in an attempt to
  approximate the naive, human intuitions about the game, not to necessarily create the strongest
  Hanabi AI in the world
   */

  // a card was hinted at, others probably want you to play it, this temporarily adjusts EV to reflect that
  private Double hotOnReceive = 1.0859230590731617;
  // observing that we didn't receive a hint makes us think that we already have a concise move
  private Double stalenessOnObservation = 0.683777908506891;
  // receiving a hint about a card makes it a good play, the ones that weren't selected may be bad plays
  private Double stalenessOnNegativeAff = 0.3316680876310207;
  // how likely are we to adopt a risky play? (3 fuses left)
  private Double riskyPlay = 0.6867532008995321;
  // how likely are we to ignore a risky play? (2 fuses left)
  private Double lessRiskyPlay = 0.7455500592736469;
  // this is how heavily we value play moves over other moves (discards and hints)
  private Double playWeight = 1.150781450808787;
  // this is how heavily we value discards over other moves ( hints and plays)
  private Double discardWeight = 0.9344919475952382;
  // this is the value that we give cards that we KNOW are valid to discard.
  private Double discardRisk = 2.2411161019275596;
  // this is the INVERSE value of a token, the idea is that we don't give tokens a strict value
  // rather, we weigh them more heavily the lower we're running on them.  This is a somewhat
  // conservative approach to valuing tokens (we prefer to keep some in the bank), and it's
  // an option to change this to a positive weight as a strict token value
  private Double tokenWeight = 0.36773513661945684;
  // this vector tries to capture the value of discards by rank (it's taken together with the CPT
  // of a card to give the EV of a play)
  private Double[] validityVector = {
      1.1103396280667235,
      0.0015554647414808044,
      0.047490583215578086,
      0.008066738046673504,
      0.008605019234005952};
  // this vector tries to capture the value of discards by rank.
  private Double[] discardVector = {
      0.29896460102850236,
      0.35490586278583075,
      0.0495481288298839,
      0.45187252564827407,
      0.07096653124508855};

  private Double myVariation = 0.05;

  // this matrix encodes the the basic idea of how we calculate expected value, we take it's
  // piecewise product sum with a CPT to get the EV of the card which that CPT represents
  private Double[][] validMatrix = new Double[5][5];
  // this matrix attempts to encode the 'possibility' of a stack being completed, so it is
  // supposed to reflect the idea of a suit becoming 'dead' after all of the cards which can
  // allow it to be built further to have a maximum discard value (as they're all immediately
  // worthless)
  private Double[][] possibilityMatrix = new Double[5][5];
  // This matrix encodes which discards are 'free' (cards already played) and helps
  // the AI decide EV on discards (takes the entrywise product of this and its CPTs)
  private Double[][] discardMatrix = new Double[5][5];
  // Many of these variables should be queried from the model rather than keeping track of them
  // here, these exist just for testing purposes
  private Integer playersInGame;
  private Integer cardsInHand;
  // A master AI is an instance of the AI which actually makes moves and has it's own slaves to
  // help with calculation.
  private Boolean master;
  // these are the subordinate AI which we use to calculate hints values with, essentially
  // they represent our belief state, of our partners belief states
  public AI[] slaves;
  private String[] slaveCards = new String[5];
  // this keeps track of a current CPS, it is an extra 'card' essentially that the AI holds
  // on to which only keeps track of observations (it can not be altered by hints)
  private ConditionalProbabilityTable myCPS;
  private ConditionalProbabilityTable[] myCards;
  // arrays with the card attributes we will use
  private String[] suits = {"R", "G", "B", "Y", "W"};
  private String[] ranks = {"1", "2", "3", "4", "5"};
  private Integer myNumber;
  private GameState myGameState;
  private Boolean mySimulationPrincipal;  // terrible pun, but do I live in a simulation or not?
  private Double bestAverageToHere = 10.0;


  // we can keep a track of the cards we've seen to help us draw random cards from a probability
  // distribution in some cases
  LinkedList<String> deck = new LinkedList<>(Arrays.asList(
      "R1", "R1", "R1", "R2", "R2", "R3", "R3", "R4", "R4", "R5",
      "G1", "G1", "G1", "G2", "G2", "G3", "G3", "G4", "G4", "G5",
      "B1", "B1", "B1", "B2", "B2", "B3", "B3", "B4", "B4", "B5",
      "Y1", "Y1", "Y1", "Y2", "Y2", "Y3", "Y3", "Y4", "Y4", "Y5",
      "W1", "W1", "W1", "W2", "W2", "W3", "W3", "W4", "W4", "W5"
  ));

  ///////////////////////////// HEURISTICS END HERE ////////////////////////////////////////////////

  /**
   * Default constructor for the AI
   *
   * @param Main a boolean which tells the AI if its the AI
   * @param gameState takes in the current gamestate or whatever who cares
   */
  AI(Boolean Main, GameState gameState, Boolean simulation) {

    mySimulationPrincipal = simulation; //
    myGameState = gameState;

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        validMatrix[i][j] = validityVector[i];
      }
    }
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        discardMatrix[i][j] = discardVector[i];
      }
    }
    for (int i = 0; i < 5; i++){
      for (int j = 0; j < 5; j++){
        possibilityMatrix[i][j] = 1.0;
      }
    }


    master = Main;
    cardsInHand = myGameState.getCardsInHand();
    myNumber = myGameState.getPosition();
    playersInGame = myGameState.getPlayersInGame();
    myCPS = new ConditionalProbabilityTable();
    // if there are more than 3 players, you get 4 cards, otherwise 5

    myCards = new ConditionalProbabilityTable[cardsInHand];
    for (int i = 0; i < cardsInHand; i++) {
      myCards[i] = new ConditionalProbabilityTable();
    }

    if (master) {
      slaves = new AI[playersInGame];
      for (int i = 0; i < playersInGame; i++) {
        slaves[i] = new AI(false, myGameState, false);
        slaves[i].myNumber = i;

      }
    }
  }

  public void setSlaveCards(String[] slaveCards) {
    this.slaveCards = slaveCards;
  }

  private ConditionalProbabilityTable[] getMyCards() {
    return myCards;
  }

  /* *********************************** UPDATE AI FUNCTIONS **************************************/


  public void setMyNumber(Integer myPosition) {
    myNumber = myPosition;
  }
  /**
   * Internal method for testing, but will probably be modified at a later date to become the
   * internal parsing method
   *
   * @param Event A hashmap event that has all of the information about the most recent move
   * @param Player The player who made the most recent move (absolute index)
   * @param Position The position of the card that was played or discarded
   */
  public void updateInternal(HashMap<String, String> Event, Integer Player, Integer Position) {

    // if we view a valid play
    if (Event.containsKey("Play")) {
      String cardPlayed = Event.get("Play");
      //rank
      int i = cardToIndex(String.valueOf(cardPlayed.charAt(1)));
      //suit
      int j = cardToIndex(String.valueOf(cardPlayed.charAt(0)));

      if (!validMatrix[i][j].equals(validityVector[0])) {
        myGameState.decrementFuses();

      }
      else {
        observePlay(String.valueOf(cardPlayed.charAt(1)), String.valueOf(cardPlayed.charAt(0)));
        myGameState.setScore(myGameState.getScore() + 1);
        myGameState.getCardsOnTable()[i][j] = true;
        if(j == 4){
          myGameState.incrementHints();
        }
      }
    }

    // if we view a valid discard, and subsequent draw
    else if (Event.containsKey("Discard")) {

      myGameState.incrementHints();

    } else if (Event.containsKey("Card Drawn")) {
      String cardDrawn = Event.get("Card Drawn");

      deck.removeFirstOccurrence(cardDrawn);/// removes the card from our current belief state of the deck

      if (Player.equals(myNumber)) {
        receiveNewCard(Position);
      } else {
        observeNewCard(String.valueOf(cardDrawn.charAt(1)),
            String.valueOf(cardDrawn.charAt(0)), Player, Position);
      }
    }

    // decode the hint
    else if (Event.containsKey("Hint")) {
      myGameState.decrementHints();
      String hintReceived = Event.get("Hint");

      Integer PlayerNumber = Integer.parseInt(String.valueOf(Event.get("Hint").charAt(6)));
      Boolean[] hints = new Boolean[cardsInHand];

      // decode the string and turn it into a list of booleans
      for (int i = 0; i < cardsInHand; i++) {
        hints[i] = hintReceived.charAt(i + 1) == 't';
      }

      // the type of hint is encoded at the end of the string
      if (PlayerNumber.equals(myNumber)) {
        receiveHint(hints, String.valueOf(hintReceived.charAt(0)));
      } else {

        observeHint(hints, String.valueOf(hintReceived.charAt(0)), PlayerNumber);

      }
    }
    if(mySimulationPrincipal) {
      myGameState.addToAllCard();
    }
  }

  /**
   * This method updates the entire AI hand when a hint is given, both positive and negative
   * information is inferred and respective methods are applied
   *
   * @param cards an array which encodes which cards are getting positive information conveyed
   * @param card the suit of the card to which a hint is given
   */
  private void receiveHint(Boolean[] cards, String card) {
    for (int i = 0; i < cardsInHand; i++) {
      if (cards[i]) {
        //affirm the positive hint
        myCards[i].receiveHint(card);
        // A hint was given to this card, it might be a strong play if others are optimizing for points
        myCards[i].hot = myCards[i].hot * hotOnReceive;
      } else {
        // affirm the negative hint
        myCards[i].observeHint(card);
        // increase the staleness of a card when no hint is received about it
        myCards[i].staleness = myCards[i].staleness * stalenessOnNegativeAff;
      }
    }
    updateEVs();
  }

  /**
   * This method updates the CPTs for the slaves when a hint is made, so accurate hinting info can
   * be deduced
   *
   * @param cards an array of booleans encoding the hint information given
   * @param card the string representation of a card
   * @param player the absolute index of the player who made the move
   */
  private void observeHint(Boolean[] cards, String card, Integer player) {
    for (int i = 0; i < cardsInHand; i++) {
      if (cards[i]) {
        //slightly increase the staleness of all of our cards (since the hint wasn't directed at us)
        // this heuristic might be detrimental (further testing required)
        myCards[i].staleness = myCards[i].staleness * stalenessOnObservation;
      }
    }
    slaves[player].receiveHint(cards, card);
  }

  private void receiveNewCard(Integer Position) {
    myCards[Position] = new ConditionalProbabilityTable(myCPS);  // called with secondary constructor
  }

  /**
   * This method is called whenever a new card is drawn by a player, all of the AI CPT are updated
   * with the information of the new card
   */
  private void observeNewCard(String rank, String suit) {
    for (int i = 0; i < cardsInHand; i++) {
      // apply the observation to all cards
      if((myGameState.getTurn() % playersInGame) != myNumber) {
        myCards[i].observeDraw(rank, suit);
      }
      // update all slaves
      // recompute the EV for discards and plays
      updateEVs();
    }
    if((myGameState.getTurn() % playersInGame) != myNumber) {
      myCPS.observeDraw(rank, suit);
      }
  }

  /**
   * This method is called whenever a new card is drawn by a player, all of the AI CPT are updated
   * with the information of the new card
   */
  private void observeNewCard(String rank, String suit, Integer player, Integer position) {
    for (int i = 0; i < cardsInHand; i++) {
      // apply the observation to all cards
      if((myGameState.getTurn() % playersInGame) != myNumber) {
        myCards[i].observeDraw(rank, suit);
      }
      // recompute the EV for discards and plays
      updateEVs();
    }
    if((myGameState.getTurn() % playersInGame) != myNumber) {
      myCPS.observeDraw(rank, suit);
    }
    if (master) {
      // set the 'other' card to the new observed card so we can generate hints properly
      // since the card at this position is brand new we need a new CPT for it
      // use the hidden CPS to give a card that already has basic inferences
      slaves[player].slaveCards[position] = suit + rank;
      // care has to be taken that the slave doesn't observe his own cards, EVEN in his CPS
      // which holds the top card, this means extra bookkeeping must be done to keep track
      // of every slaves topcard
      slaves[player].myCards[position] = new ConditionalProbabilityTable(slaves[player].myCPS);
      for (int i = 0; i < playersInGame; i++) {
        if(player != i) { // the slave cannot observe his own card
          slaves[i].observeNewCard(rank, suit);
          slaves[i].myCPS.observeDraw(rank, suit);
        }
      }
    }
  }

  /**
   * Updates the EVs for both discards, and for plays in the AIs CPTs
   */
  private void updateEVs() {
    for (int i = 0; i < cardsInHand; i++) {
      myCards[i].expectedValueDiscard(discardMatrix, myGameState.getHintsRemaining());
      myCards[i].expectedValuePlay(validMatrix);
    }
  }

  /**
   * Sets up all the CPTs for the AI and its slave as soon as a game is started
   */
  public void initialObservations() {
    for (int i = 0; i < playersInGame; i++) {
      for (int j = 0; j < cardsInHand; j++) {

        String card = slaves[i].slaveCards[j];
        deck.remove(slaves[i].slaveCards[j]);
        if (!slaves[i].slaveCards[0].equals("??") && i != myNumber) {
          observeNewCard(String.valueOf(card.charAt(1)), String.valueOf(card.charAt(0)));
          slaves[i].observeNewCard(String.valueOf(card.charAt(1)), String.valueOf(card.charAt(0)));
        }
      }
    }
  }

  /**
   * Changes the internal validity and discard matrices to reflect the state of the current
   * board, allowing us to calculated expected value
   * @param rank the rank of the
   * @param suit
   */
  private void observePlay(String rank, String suit) {
    int suit_i = cardToIndex(suit);
    int rank_i = cardToIndex(rank);
    if (rank_i + 1 < 5) {
      validMatrix[rank_i + 1][suit_i] = validityVector[0];
    }
    if (rank_i + 2 < 5) {
      validMatrix[rank_i + 2][suit_i] = validityVector[1];
    }
    if (rank_i + 3 < 5) {
      validMatrix[rank_i + 3][suit_i] = validityVector[2];
    }
    if (rank_i + 4 < 5) {
      validMatrix[rank_i + 4][suit_i] = validityVector[3];
    }


    if (rank_i + 1 < 5) {
      discardMatrix[rank_i + 1][suit_i] = discardVector[0];
    }
    if (rank_i + 2 < 5) {
      discardMatrix[rank_i + 2][suit_i] = discardVector[1];
    }
    if (rank_i + 3 < 5) {
      discardMatrix[rank_i + 3][suit_i] = discardVector[2];
    }
    if (rank_i + 4 < 5) {
      discardMatrix[rank_i + 4][suit_i] = discardVector[3];
    }


    validMatrix[rank_i][suit_i] = 0.0;
    discardMatrix[rank_i][suit_i] = discardRisk;
    updateEVs();
    if (master) {
      for (int i = 0; i < playersInGame; i++) {
        slaves[i].observePlay(rank, suit);
      }
    }
  }


  /* ****************************** FIND BEST FUNCTIONS ************************************/


  /**
   * This method finds the best move by starting 3 threads and calling bestDiscard, bestPlay, and
   * bestHint, then it makes the move
   */
  public HashMap<String, String> findBestMove() {
    updateEVs();
    Integer Best_Play;
    Integer Best_Discard = 0;
    Pair<Hint, Integer> Best_Hint = null;
    Double HintEV = 0.0;
    Double DiscardEV = 0.0;
    HashMap<String, String> Event = new HashMap<>();

    Best_Play = findBestPlay();  // we always prefer to play a high value card if it exists
    if (!Best_Play.equals(-1)) {
      Event.put("Play", Best_Play.toString());
      return Event;
    }
    if (myGameState.getHintsRemaining() > 0) { // next, we look at 'good' hints
      Best_Hint = findBestHint();
      HintEV = Best_Hint.getKey().myEV;
    }
    if (myGameState.getHintsRemaining() != 8) { // okay so, no good hints? discard
      Best_Discard = findBestDiscard();
      DiscardEV = myCards[Best_Discard].getDiscardEV();
    }

    if (HintEV > DiscardEV) { // finally, if we can do either, we must choose based on EV
      Event.put("Hint", Best_Hint.toString());
      return Event;
    } else {
      Event.put("Discard", Best_Discard.toString());
      return Event;
    }

  }

  /**
   * Find the best discard by examining the expected value of every discard, pick the highest
   *
   * @return The index of the best discard to make
   */
  private Integer findBestDiscard() {

    Double max_EV = 0.0;  // find the card with the highest expected value
    int discard_index = 0;
    for (int c = 0; c < cardsInHand; c++) {
      if (myCards[c].getDiscardEV() > max_EV) {
        max_EV = myCards[c].getDiscardEV();
        discard_index = c;
      }

    }
    return discard_index;
  }

  /**
   * Find the best hint, by enumerating every possible hint, then calculating the expected value of
   * each one of those hints, return the one with the highest EV
   *
   * @return A hint object
   */
  private Pair<Hint, Integer> findBestHint() {

    LinkedList<Hint> HintList;
    int best_player = 0;
    Boolean[] b = {true};
    Hint best = new Hint("a", b);
    best.myEV = 0.0;
    for (int i = 0; i < playersInGame; i++) {

      HintList = findHints(slaves[i].slaveCards, slaves[i].getMyCards());
      for (Hint h : HintList) {
        if (h.myEV > best.myEV) {
          best = h;
          best_player = i;
        }
      }
    }
    Pair hint_pair = new Pair(best, best_player);
    return hint_pair;
  }


  /**
   * Find the best card to play by calculating the expected value of each card, and then returning
   * the card with the highest EV.
   *
   * @return The index of the card to play
   */
  private Integer findBestPlay() {
    // update EV for each card in hand
    Double max_EV = 0.0;  // find the card with the highest expected value
    Integer play_index = 0;
    for (int c = 0; c < cardsInHand; c++) {
      if (myCards[c].getPlayEV() > max_EV) {
        max_EV = myCards[c].getPlayEV();
        play_index = c;
      }
    }
    if (myCards[play_index].getPlayEV()
        >= (float) 1) { // if the card with the highest EV is greater
      return play_index;                              // than an EV of 1, pick that (it can't be wrong)
    }
    // STATISTICAL OPERATIONS
    // this number is probably subject to change and maybe some risk analysis with fuses and turns
    if (myCards[play_index].getPlayEV() >= riskyPlay && myGameState.getFusesRemaining() == 3) {
      return play_index;
    } else if (myCards[play_index].getPlayEV() >= lessRiskyPlay && myGameState.getFusesRemaining() == 2) {
      return play_index;
    }

    // Here we can try to decide what to do if the EV of the best card is lower than some specific
    // (predetermined) value.
    return -1;
  }


  /**
   * Method that enumerates every possible hint given a hand and returns a linked list of every
   * possible hint in the current game state.
   * @param observedCards When evaluating all of the different discards, we need a list of all of
   * the cards that a player has to find every discard from
   */
  private LinkedList<Hint> findHints(String[] observedCards,
      ConditionalProbabilityTable[] otherCPTs) {

    int attributes = suits.length;
    LinkedList<Hint> Hints = new LinkedList<>();

    for (int i = 0; i < attributes; i++) {
      Boolean[] Hint = new Boolean[cardsInHand];
      boolean isHintValid = false;
      for (int j = 0; j < cardsInHand; j++) {
        if (observedCards[j].contains(suits[i])) {
          Hint[j] = true;
          isHintValid = true;  // This flag is set so only hints that are actually valid make it through
        } else {
          Hint[j] = false;
        }
      }
      if (isHintValid) {
        Hint p = new Hint(suits[i], Hint);
        p.calculateEV(otherCPTs, validMatrix,
            playWeight);
        Hints.add(p);
      }

    }
    for (int i = 0; i < attributes; i++) {
      Boolean[] Hint = new Boolean[cardsInHand];
      boolean isHintValid = false;
      for (int j = 0; j < cardsInHand; j++) {
        if (observedCards[j].contains(ranks[i])) {
          Hint[j] = true;
          isHintValid = true;
        } else {
          Hint[j] = false;
        }
      }
      if (isHintValid) {
        Hint p = new Hint(ranks[i], Hint);
        p.calculateEV(otherCPTs, validMatrix,
            playWeight);
        Hints.add(p);
      }
    }
    return Hints;
  }


  /* *********************************** HELPER FUNCTIONS *****************************************/

  /**
   * takes a suit value and transforms it to its corresponding index in a CPD
   *
   * @param card card info to transform to index
   * @return an integer (represents suit)
   */
  private int cardToIndex(String card) {
    int card_i;
    switch (card) {
      case "R":
        card_i = 0;
        break;
      case "G":
        card_i = 1;
        break;
      case "B":
        card_i = 2;
        break;
      case "Y":
        card_i = 3;
        break;
      case "W":
        card_i = 4;
        break;
      case "?":
        card_i = -1;
        break;
      default:
        card_i = Integer.parseInt(card) - 1;
        break;
    }
    return card_i;
  }



  /* ******************************** Main / Testing*********************************************/


  protected static Integer[] TestMe(
      Double hot,
      Double staleOther,
      Double StaleObs,
      Double RiskyPlay,
      Double LessRisky,
      Double[] Vector,
      Double PlayWeight,
      Double DiscardWeight,
      Double DiscardRisk,
      Double TokenWeight,
      Double[] discardVector){

    Integer[] returns = new Integer[2];


    String[] puCards = {"??", "??", "??", "??"};



    LinkedList<String> deck = new LinkedList<>(Arrays.asList(
        "R1", "R1", "R1", "R2", "R2", "R3", "R3", "R4", "R4", "R5",
        "G1", "G1", "G1", "G2", "G2", "G3", "G3", "G4", "G4", "G5",
        "B1", "B1", "B1", "B2", "B2", "B3", "B3", "B4", "B4", "B5",
        "Y1", "Y1", "Y1", "Y2", "Y2", "Y3", "Y3", "Y4", "Y4", "Y5",
        "W1", "W1", "W1", "W2", "W2", "W3", "W3", "W4", "W4", "W5"
    ));

    Card[] pC0Cards = new Card[4];
    Card[] pC1Cards = new Card[4];
    Card[] pC2Cards = new Card[4];
    Card[] pC3Cards = new Card[4];
    Card[] pC4Cards = new Card[4];

    String[] p0Cards = new String[4];
    String[] p1Cards = new String[4];
    String[] p2Cards = new String[4];
    String[] p3Cards = new String[4];
    String[] p4Cards = new String[4];

    String[][] allCards = {p0Cards, p1Cards, p2Cards, p3Cards, p4Cards};

    Card[][] allCardsCard = {pC0Cards, pC1Cards, pC2Cards, pC3Cards, pC4Cards};

    int random;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        random = ThreadLocalRandom.current().nextInt(0, deck.size());
        String cardRep = deck.get(random);
        Card newCard = new Card(cardRep.substring(0, 1),
            Integer.parseInt(cardRep.substring(1, 2)), "none");
        allCards[i][j] = cardRep;
        allCardsCard[i][j] = newCard;
        deck.remove(random);

       // System.out.println(newCard.getString());

      }
    }

    ArrayList<Player> PlayersList = new ArrayList<>();

    Player Player0 = new Player(pC0Cards, true);
    Player Player1 = new Player(pC1Cards, false);
    Player Player2 = new Player(pC2Cards, false);
    Player Player3 = new Player(pC3Cards, false);
    Player Player4 = new Player(pC4Cards, false);

    PlayersList.add(Player0);
    PlayersList.add(Player1);
    PlayersList.add(Player2);
    PlayersList.add(Player3);
    PlayersList.add(Player4);




    GameState gameState0 = new GameState(PlayersList, "none", 2);
    GameState gameState1 = new GameState(PlayersList, "none", 2);
    GameState gameState2 = new GameState(PlayersList, "none", 2);
    GameState gameState3 = new GameState(PlayersList, "none", 2);
    GameState gameState4 = new GameState(PlayersList, "none", 2);

    gameState0.setPosition(0);
    gameState1.setPosition(1);
    gameState2.setPosition(2);
    gameState3.setPosition(3);
    gameState4.setPosition(4);


    AI AITest0 = new AI(true, gameState0, true);
    AI AITest1 = new AI(true, gameState1, true);
    AI AITest2 = new AI(true, gameState2, true);
    AI AITest3 = new AI(true, gameState3, true);
    AI AITest4 = new AI(true, gameState4, true);



    AI[] AIs = {AITest0, AITest1, AITest2, AITest3, AITest4};


    AITest0.slaves[0].setSlaveCards(puCards); // Player 1
    AITest0.slaves[1].setSlaveCards(p1Cards);
    AITest0.slaves[2].setSlaveCards(p2Cards);
    AITest0.slaves[3].setSlaveCards(p3Cards);
    AITest0.slaves[4].setSlaveCards(p4Cards);

    AITest1.slaves[0].setSlaveCards(p0Cards);
    AITest1.slaves[1].setSlaveCards(puCards); // Player 2
    AITest1.slaves[2].setSlaveCards(p2Cards);
    AITest1.slaves[3].setSlaveCards(p3Cards);
    AITest1.slaves[4].setSlaveCards(p4Cards);

    AITest2.slaves[0].setSlaveCards(p0Cards);
    AITest2.slaves[1].setSlaveCards(p1Cards);
    AITest2.slaves[2].setSlaveCards(puCards); // Player 3
    AITest2.slaves[3].setSlaveCards(p3Cards);
    AITest2.slaves[4].setSlaveCards(p4Cards);

    AITest3.slaves[0].setSlaveCards(p0Cards);
    AITest3.slaves[1].setSlaveCards(p1Cards);
    AITest3.slaves[2].setSlaveCards(p2Cards);
    AITest3.slaves[3].setSlaveCards(puCards); // Player 4
    AITest3.slaves[4].setSlaveCards(p4Cards);

    AITest4.slaves[0].setSlaveCards(p0Cards);
    AITest4.slaves[1].setSlaveCards(p1Cards);
    AITest4.slaves[2].setSlaveCards(p2Cards);
    AITest4.slaves[3].setSlaveCards(p3Cards);
    AITest4.slaves[4].setSlaveCards(puCards); // Player 5

    AITest0.initialObservations();
    AITest1.initialObservations();
    AITest2.initialObservations();
    AITest3.initialObservations();
    AITest4.initialObservations();


    for(int i = 0; i<5; i++){
      AIs[i].hotOnReceive = hot;
      AIs[i].stalenessOnObservation = staleOther;
      AIs[i].stalenessOnNegativeAff = StaleObs;
      AIs[i].riskyPlay = RiskyPlay;
      AIs[i].lessRiskyPlay = LessRisky;
      AIs[i].playWeight = PlayWeight;
      AIs[i].discardWeight = DiscardWeight;
      AIs[i].discardRisk = DiscardRisk;
      AIs[i].tokenWeight = TokenWeight;


      for(int c = 0; c< 5; c++){
        AIs[i].slaves[c].hotOnReceive = hot;
        AIs[i].slaves[c].stalenessOnObservation = staleOther;
        AIs[i].slaves[c].stalenessOnNegativeAff = StaleObs;
        AIs[i].slaves[c].riskyPlay = RiskyPlay;
        AIs[i].slaves[c].lessRiskyPlay = LessRisky;
        AIs[i].slaves[c].playWeight = PlayWeight;
        AIs[i].slaves[c].discardWeight = DiscardWeight;
        AIs[i].slaves[c].discardRisk = DiscardRisk;
        AIs[i].slaves[c].tokenWeight = TokenWeight;
      }
      for(int p = 0; p<5; p++){
        for(int q = 0; q<5; q++){
          AIs[i].validMatrix[p][q] = Vector[p];
          AIs[i].discardMatrix[p][q] = discardVector[p];
          AIs[i].validityVector[p] = Vector[p];
          AIs[i].discardVector[p] = discardVector[p];
          for(int c = 0; c < 5; c++){
            AIs[i].slaves[c].validMatrix[p][q] = Vector[p];
            AIs[i].slaves[c].discardMatrix[p][q] = discardVector[p];
            AIs[i].slaves[c].validityVector[p] = Vector[p];
            AIs[i].slaves[c].discardVector[p] = discardVector[p];
          }
        }
      }
    }

    HashMap<String, String> lastMove;

    int turn = 0;



    /////////////////////// SIMULATION TIME BOIS //////////////////////////////////

    while (deck.size() != 0) {



      lastMove = AIs[turn % 5].findBestMove();

      //System.out.println(lastMove);

      //System.out.println(turn);
      if (lastMove.containsKey("Hint")) {
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, 0);
        }
      } else if (lastMove.containsKey("Play")) {
        random = ThreadLocalRandom.current().nextInt(0, deck.size());
        String newCard = deck.get(random);


        deck.remove(random);
        Integer position = parseInt(lastMove.get("Play"));
        String cardPlayed = allCards[turn % 5][position];
        lastMove.clear();
        lastMove.put("Play", cardPlayed);
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, position);
        }
        allCards[turn % 5][position] = newCard;
        lastMove.clear();
        lastMove.put("Card Drawn", newCard);
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, position);

        }
        AIs[0].myGameState.updatePlayerHand(AIs[0].myGameState.getPlayers().get(turn % 5),
            newCard.substring(0,1), Integer.parseInt(newCard.substring(1,2)), position);


      } else if (lastMove.containsKey("Discard")) {

        random = ThreadLocalRandom.current().nextInt(0, deck.size());
        String newCard = deck.get(random);

        Integer position = parseInt(lastMove.get("Discard"));

        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, position);
        }



        deck.remove(random);

        Card dicCard = new Card(AIs[0].myGameState.getPlayers().get(turn %5).getCard(position).getColor(),
            AIs[0].myGameState.getPlayers().get(turn %5).getCard(position).getNumber(), "none");
        allCards[turn % 5][position] = newCard;
        lastMove.clear();
        lastMove.put("Card Drawn", newCard);
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, position);

        }

        AIs[0].myGameState.updatePlayerHand(AIs[0].myGameState.getPlayers().get(turn % 5), newCard.substring(0,1),
            Integer.parseInt(newCard.substring(1,2)), position);

        AIs[0].myGameState.getDiscardPile().add(dicCard);
      }

      turn++;

      for(int i = 0; i < 5; i++){
        AIs[i].myGameState.incrementTurn();
      }

    }

    // the final rounds
    turn = 0;
    while (turn < 5) {
      lastMove = AIs[turn % 5].findBestMove();

      if (lastMove.containsKey("Hint")) {
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, 0);
        }
      } else if (lastMove.containsKey("Play")) {
        String newCard = "??";
        Integer position = parseInt(lastMove.get("Play"));
        String cardPlayed = allCards[turn % 5][position];
        lastMove.clear();
        lastMove.put("Play", cardPlayed);
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, position);
        }
        allCards[turn % 5][position] = newCard;
        lastMove.clear();
        lastMove.put("Card Drawn", newCard);
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, position);

        }

      } else if (lastMove.containsKey("Discard")) {
        String newCard = "??";
        Integer position = parseInt(lastMove.get("Discard"));
        allCards[turn % 5][position] = newCard;
        lastMove.clear();
        lastMove.put("Discard", newCard);
        for (int i = 0; i < 5; i++) {
          AIs[i].updateInternal(lastMove, turn % 5, position);

        }

      }

      turn++;
      for(int i = 0; i < 5; i++){
        AIs[i].myGameState.incrementTurn();
      }

    }

    returns[0] = AIs[0].myGameState.getScore();
    returns[1] = AIs[0].myGameState.getFusesRemaining();
    return returns;

  }

}