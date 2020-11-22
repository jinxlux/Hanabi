package Hanabi;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class ConditionalProbabilityTable extends Card {

  // How this matrix corresponds to a CPT
  //  R G B Y W     R1 = 0, 0
  //1 - - - - -     G1 = 0, 1
  //2 - - - - -
  //3 - - - - -
  //4 - - - - -
  //5 - - - - -

  // how many cards are left in this CPT
  public Integer cardsLeft;
  public float sumOfSquares;
  // a new, blank CPT
  public Integer[][] CPS =
      {
          {3, 3, 3, 3, 3},
          {2, 2, 2, 2, 2},
          {2, 2, 2, 2, 2},
          {2, 2, 2, 2, 2},
          {1, 1, 1, 1, 1}};
  public Double[][] CPT =
      {
          {0.0, 0.0, 0.0, 0.0, 0.0},
          {0.0, 0.0, 0.0, 0.0, 0.0},
          {0.0, 0.0, 0.0, 0.0, 0.0},
          {0.0, 0.0, 0.0, 0.0, 0.0},
          {0.0, 0.0, 0.0, 0.0, 0.0}};

  public Double playEV;
  private Double discardEV;
  private String rank;
  private String suit;

  // A double which holds onto how 'stale' a card is ie, the longer it goes without a hint
  // the worse it probably is
  public Double staleness;
  public Double hot;

  // [rank][suit]
  ConditionalProbabilityTable() {
    super("?", 0, "none");
    cardsLeft = 50;
    calcCPT();
    playEV = 0.0;
    discardEV = 0.0;
    staleness = 1.0;
    hot = 1.0;

  }

  /**
   * This constructor is reserved for receiving a new card in a currently active game, it takes in
   * an existing CPT and applies already observed instances into a new CPT
   */
  ConditionalProbabilityTable(ConditionalProbabilityTable CPT) {
    super("?", 0, "none");
    cardsLeft = CPT.cardsLeft;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        this.CPT[i][j] = CPT.CPT[i][j];
        this.CPS[i][j] = CPT.CPS[i][j];
      }
    }
    calcCPT();
    playEV = 0.0;
    discardEV = 0.0;
    staleness = 1.0;
    hot = 1.0;
  }

  /**
   * internal helper method to deal with the current representation of cards as strings
   */
  public String indexToSuit(Integer index) {

    switch (index) {
      case 0:
        return "R";
      case 1:
        return "G";
      case 2:
        return "B";
      case 3:
        return "Y";
      case 4:
        return "W";
      default:
        return "";
    }

  }


  public Double getPlayEV() {
    return playEV;
  }

  public Double getDiscardEV() {
    return discardEV;
  }

  /**
   * getter for the CPT
   */
  public Double[][] getCPT() {
    return CPT;
  }


  /**
   * Calculates the CPT, given the CPS, and the number of cards remaining in the hand
   */
  private void calcCPT() {
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        CPT[i][j] = (double) (CPS[i][j] / (float) cardsLeft);
      }
    }
  }

  /**
   * returns a random card which is sampled from the current CPS
   */
  public Card randomSample(String gameType) {

    LinkedList<Card> myListOfPossibleCards = new LinkedList<>();
    for (int i = 0; i < CPS.length; i++) {
      for (int j = 0; j < CPS[0].length; j++) {
        for (int c = 0; c < CPS[i][j]; c++) {
          Card aNewCard = new Card(indexToSuit(j), i + 1, gameType);
          myListOfPossibleCards.add(aNewCard);
        }
      }
    }
    Random rand = new Random();
    if (myListOfPossibleCards.size() == 0) {
      System.out.println("Something's gone wrong again my boy");
    }
    int random = rand.nextInt(myListOfPossibleCards.size());
    Card myMostProbable = myListOfPossibleCards.get(random);

    return myMostProbable;

  }

  /* ************** EXPECTED VALUE FUNCTIONS ********************/

  /**
   * Calculates the expected value of a discard, this is hand-wavy still as I haven't found a
   * concrete way to VALUE a discard.
   * @param discardMatrix the matrix which by which we can calculate the expected value of a DC
   * @param Tokens the amount of tokens left, normalize over +1
   */
  public void expectedValueDiscard(Double[][] discardMatrix, Integer Tokens) {
    Double rolling_EV = 0.0;
    Double val;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        // We could encode information about tokens left here, more tokens = better EV for DC
        val = CPT[i][j] * discardMatrix[i][j];
        rolling_EV += val;
      }
    }
    if (playEV.equals(0.0) || discardEV.isNaN()) {
      discardEV = 1.0;
    } else {
      this.discardEV = (rolling_EV / (Tokens + 1)) * this.staleness;
    }

  }

  /**
   * There's something so damn satisfying about this function in its simplicity. It's the exact
   * definition of expected value.  The sum of the probabilities multiplied by the value of those
   * probabilities being correct.
   * @param validMatrix The current matrix which captures the validity (with weights) of a
   * specific play
   */
  public void expectedValuePlay(Double[][] validMatrix) {
    Double rolling_EV = 0.0;
    Double val;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        val = CPT[i][j] * validMatrix[i][j];
        if (val > 0) {
          rolling_EV += val;
        }
      }
    }
    this.playEV = rolling_EV * this.hot;
  }

  /**
   * An function which exits only for the purpose of helping the other functions decipher string
   * values of cards
   * @param card the string representation of a card
   * @return an int which represents the value integer value of a card
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
      default:
        card_i = Integer.parseInt(card) - 1;
        break;
    }
    return card_i;
  }


  /**
   * A method that changes an individual element in a CPT when a draw is observed
   *
   * @param rank the rank of the observed draw
   * @param suit the suit of the observed draw (as a string)
   */
  public void observeDraw(String rank, String suit) {
    int suit_i;
    int rank_i;
    if (rank.equals("?")) {
      return;
    }
    suit_i = cardToIndex(suit);
    rank_i = cardToIndex(rank);
    // so the rank matches the index
    if (this.CPS[rank_i][suit_i] != 0) {
      this.CPS[rank_i][suit_i] -= 1;
      this.cardsLeft -= 1;
    }
    calcCPT();
  }


  /**
   * This is the operation that is performed on other cards when a hint is recieved about ones own
   * cards, it is to be applied to the cards that AREN'T the cards being hinted
   *
   * @param card the suit of the card where the hint was recieved
   */
  public void observeHint(String card) {
    if (card.matches("[0-9]")) {
      int index = cardToIndex(card);
      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
          if (index == i) {
            CPS[i][j] = 0;
          }
        }
      }
      cardsLeft = CPSsum();
      calcCPT();

    } else {
      int index = cardToIndex(card);
      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
          if (index == j) {
            CPS[i][j] = 0;
          }
        }
      }
      cardsLeft = CPSsum();
      calcCPT();
    }
  }


  /**
   * @param card the suit of the hint recieved
   */
  public void receiveHint(String card) {

    if (card.matches("[0-9]")) {
      int index = cardToIndex(card);
      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
          if (index != i) {
            cardsLeft -= CPS[i][j];
            CPS[i][j] = 0;
          }
        }
      }
      rank = card;
      calcCPT();
    } else {
      int suit_i = cardToIndex(card);
      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
          if (suit_i != j) {
            cardsLeft -= CPS[i][j];
            CPS[i][j] = 0;
          }
        }
      }
      suit = card;
      calcCPT();
    }
  }

  /* ****************** HELPER FUNCTIONS FOR DEBUGGING *************************/

  @Override
  public String toString() {
    String s = "";
    for (int i = 0; i < 5; i++) {
      s = s + (Arrays.toString(CPT[i]));
      s = s + "\n";
    }
    s = "\n" + s
        + "Sum of squares: " + this.sumOfSquares + "\n"
        + "Sum of values: " + this.sumOfVals() + "\n"
        + "Expected value (play) " + this.playEV + "\n"
        + "Expected value (discard) " + this.discardEV + "\n";
    return s;
  }

  /**
   * testing method to ensure that the sum of probabilities = 1
   *
   * @return a sum of the probabilities for a CPT
   */
  private float sumOfVals() { //TODO Remove this method and all associated methods when done
    float f = 0;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        f += CPT[i][j];
      }
    }
    return f;
  }

  /**
   * Recalculates the total number of cards that would be available after an inferred hint is
   * determined
   */
  private Integer CPSsum() {
    Integer total = 0;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        total += CPS[i][j];
      }
    }
    return total;
  }

  public static void main(String args[]) {

    // setting up testing
    int test_numbers = 3;
    Boolean[] Tests = new Boolean[test_numbers];
    for (int i = 0; i < test_numbers; i++) {
      Tests[i] = false;
    }

    ConditionalProbabilityTable test1 = new ConditionalProbabilityTable();
    ConditionalProbabilityTable test2 = new ConditionalProbabilityTable();
    ConditionalProbabilityTable test3 = new ConditionalProbabilityTable();

    //tests for editCPT
    System.out.println(test1);

    test1.observeDraw("1", "R");
    Tests[0] = test1.CPS[0][0] == 2;
    System.out.println(test1);

    test1.observeDraw("3", "W");
    Tests[1] = test1.CPS[2][4] == 1;
    System.out.println(test1);

    test1.observeDraw("5", "W");
    Tests[2] = test1.CPS[4][4] == 0;
    System.out.println(test1);

    test1.receiveHint("2");
    System.out.println(test1);

    test2.receiveHint("R");
    System.out.println(test2);

    test2.observeDraw("5", "R");
    test2.observeDraw("4", "R");
    test2.observeDraw("3", "R");

    test2.receiveHint("1");
    System.out.println(test2);

    test3.observeHint("R");
    System.out.println(test3);

    test3.observeHint("R");
    System.out.println(test3);

    test3.observeHint("5");
    System.out.println(test3);

    for (int i = 0; i < test_numbers; i++) {
      if (!Tests[i]) {
        System.out.println("Test " + i + " failed");
      }
    }
  }
}

