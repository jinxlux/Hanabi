package Hanabi;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents the current state of the client.
 * It keeps track of all of the client side operations such as: cards selected, actions selected,
 * menus open etc. etc. In other words, this class makes up the part of the model which is separate
 * from the actual game being played. The reason this class was created was to decouple the client logic,
 * from the game logic within the model.
 */
public class ClientState implements Cloneable{

  private Integer playerSelected;
  private ArrayList<Integer> cardsSelected;
  private HashMap<String, Boolean> menusOpen;
  private String actionSelected;
  private ArrayList<Integer> notes;  // integers to track what the options showing in note board
  private ArrayList<Button> buttons;

  private ArrayList<Player> players;

  private String gameType;
  private Button[][] noteMatrix;
  private Button[][][] notePanel;

  /**
   * Constructor
   * @param players An array of the players, with their hands
   */
  public ClientState(ArrayList<Player> players) {
    this.players = players;

    this.playerSelected = 99;
    this.cardsSelected = new ArrayList<>();
    this.menusOpen = new HashMap<>();
    this.actionSelected = null;
    this.notes = new ArrayList<>();
    this.buttons = new ArrayList<>();

    this.menusOpen.put("mainMenu", false);
    this.menusOpen.put("playerJoined", false);
    this.menusOpen.put("playerLeft", false);
    this.menusOpen.put("gameCancelled", false);
    this.menusOpen.put("gameEnds", false);
    this.menusOpen.put("noSuchGame", false);
    this.menusOpen.put("gameFull", false);
    this.menusOpen.put("hintAnimation", false);


  }

  /**
   * Returns the index of the currently selected player.
   * @return An integer, the index of the currently selected player
   */
  public Integer getPlayerSelected() {
    return playerSelected;
  }

  /**
   * Returns a list of integer indices of the cards currently selected.
   * @return A list of integer which is a list of indices of currently selected cards
   */
  public ArrayList<Integer> getCardsSelected(){
    return cardsSelected;
  }

  public HashMap<String, Boolean> getMenusOpen() {
    return menusOpen;
  }

  /**
   * Returns the currently selected action by the player as a string.
   * @return A string encoding the current action selected
   */
  public String getActionSelected() {
    return actionSelected;
  }

  public ArrayList<Integer> getNotes() {
    return notes;
  }

  public ArrayList<Button> getButtons() {
    return buttons;
  }

  // getters above

  // setters

//  public void setPlayerSelected(Integer playerSelected) {
//    this.playerSelected = playerSelected;
//  }

  public void setCardsSelected(ArrayList<Integer> cardsSelected) {

    this.cardsSelected = cardsSelected;
  }

  public void setMenusOpen(HashMap<String, Boolean> menusOpen) {

    this.menusOpen = menusOpen;
  }

  /**
   * Takes in a string and sets the current action selected value to that string.
   * @param actionSelected A String that is the name of an action done by the player
   */
  public void setActionSelected(String actionSelected) {

    this.actionSelected = actionSelected;
  }

  public void setNotes(ArrayList<Integer> notes) {

    this.notes = notes;
  }

  public void setButtons(ArrayList<Button> buttons) {

    this.buttons = buttons;
  }

  // setters above

  /**
   * sets player's index playerSelected
   * @param index the selected player index in the player array
   */
  private void selectPlayer(Integer index){
    this.playerSelected = index;
  }

  /**
   * sets player index to 99 which means no player is selected
   */
  private void deSelectPlayer(){

    // once the player wants to un-select the selected player,
    // must to clear cards selected first, then clear value of playerSelected in the state
    this.playerSelected = 99;
  }

  /**
   * an internal helper method which adds selected card index to the array,
   * and it sets the selected cards to be selected in their references
   * @param index
   */
  private void addCardsSelected(Integer index){
    if (!this.getCardsSelected().contains(index)){
      // change the actual card selected value to true,
      // then to add the new selected card index to the array
      this.players.get(this.getPlayerSelected()).getCard(index).setSelected(true);
      this.getCardsSelected().add(index);
    }
  }

  /**
   * an internal helper method which removes selected card index to the array,
   * and it sets the selected cards to be un-selected in their references
   * @param cardIndex
   */
  private void deSelectedCard(Integer cardIndex){
    if (this.getCardsSelected().contains(cardIndex)){
      // change the actual card selected value to false,
      // then to remove the selected card index from the array
      this.players.get(this.getPlayerSelected()).getCard(cardIndex).setSelected(false);
      this.getCardsSelected().remove(this.getCardsSelected().indexOf(cardIndex));
    }
  }

  /**
   * an internal helper method to clear all selected cards
   */
  private void clearCardsSelected(){
    while (!this.getCardsSelected().isEmpty()){
      Integer cardIndex = this.getCardsSelected().get(0);
      this.deSelectedCard(cardIndex);
    }
  }

  /**
   * A method which takes in a player index, and a card index,
   * and sets the card at that array index to Selected.
   * @param playerIndex An integer which indexes into the player array
   * @param cardIndex An integer which indexes into a hand array
   */
  public void selectCard(Integer playerIndex, Integer cardIndex) {

    if (playerIndex != this.getPlayerSelected()){

      // to select a different player, playerSelected should change to new player
      this.clearCardsSelected();
      this.deSelectPlayer();
      this.selectPlayer(playerIndex);

      // then to add the new selected card into the array
      this.addCardsSelected(cardIndex);

    }else {

      // still the selected player, then to add a new selected card
      // if the card has been selected already, to un-selected the card
      if (!this.getCardsSelected().contains(cardIndex)){

        this.addCardsSelected(cardIndex);

      }else{

        this.deSelectedCard(cardIndex);

        // if deselect the last card, to deselect the player as well
        if (this.getCardsSelected().isEmpty()){
          this.deSelectPlayer();
        }
      }
    }
  }



  /**
   * Takes in a string and indexes into the MenusOpen dictionary, toggling the boolean value inside.
   * @param menuName A string that is the key for the dictionary of open menus
   */
  public void toggleMenu(String menuName) {
    HashMap<String, Boolean> menu = this.getMenusOpen();
    if (menu.get(menuName)){
      menu.replace(menuName, false);
    }else{
      menu.replace(menuName, true);
    }
  }

  /**
   * an internal method to check if mouse coordinates in a desired area
   * @param x the area x coordinate
   * @param y the area y coordinate
   * @param w wide length of the desired area
   * @param h height of the desired area
   * @param mx mouse x coordinate
   * @param my mouse y coordinate
   * @return a boolean indicates if the mouse does any action in desired area
   */
  private boolean checkHit(double x, double y, double w, double h, double mx, double my){
    if (mx < x || my < y || mx > (x+w) || my > (y+h)){
      return false;
    }else {
      return true;
    }
  }

  /**
   * checks if a card has an interaction with the player's mouse
   * @param card the card is interacting with mouse
   * @param mx mouse x coordinate
   * @param my mouse y coordinate
   * @return a boolean indicates if the mouse does any action with the specific card
   */
  public boolean checkCardHit(Card card, double mx, double my){

    double x = card.getX();
    double y = card.getY();
    double w = card.getW();
    double h = card.getHeight();
    return checkHit(x, y, w, h, mx, my);

  }

  /**
   * checks if a card has an interaction with the player's mouse
   * @param b the button is interacting with mouse
   * @param mx mouse x coordinate
   * @param my mouse y coordinate
   * @return a boolean indicates if the mouse does any action with the specific button
   */
  public boolean checkButtonHit(Button b, double mx, double my){

    double x = b.getX();
    double y = b.getY();
    double w = b.getW();
    double h = b.getH();
    return checkHit(x, y, w, h, mx, my);
  }

  @Override
  protected ClientState clone() throws CloneNotSupportedException {
    return (ClientState) super.clone();
  }

  public static void main (String[] args) throws CloneNotSupportedException{

    // unit tests
    System.out.println("-------\n"
        + "Testing\n"
        + "-------");

    // set up
    Integer nError = 0;
    Integer nFail = 0;

    Card r1 = new Card("r", 1, "Standard");
    Card r2 = new Card("r", 2,"Standard");
    Card r3 = new Card("r", 3,"Standard");
    Card[] cards1 = {r1, r2, r3};

    Card b1 = new Card("b", 1,"Standard");
    Card b2 = new Card("b", 2,"Standard");
    Card b3 = new Card("b", 3,"Standard");
    Card[] cards2 = {b1, b2, b3};

    Card g1 = new Card("g", 1,"Standard");
    Card g2 = new Card("g", 2, "Standard");
    Card g3 = new Card("g", 3, "Standard");
    Card[] cards3 = {g1, g2, g3};

    ArrayList<Player> players = new ArrayList<>(5);
    Player p1 = new Player(cards1, false);
    Player p2 = new Player(cards2, false);
    Player p3 = new Player(cards3, false);
    players.add(p1);
    players.add(p2);
    players.add(p3);

    ClientState c = new ClientState(players);

    // 1: select/deselect player
    try{
      c.selectPlayer(0);
      if (c.getPlayerSelected() != 0) {
        System.out.println("ERROR 1: The selected player should be index 0");
        nError++;
      }

      c.deSelectPlayer();
      if (c.getPlayerSelected() != 99){
        System.out.println("ERROR 1: The selected player is un-selected now");
        nError++;
      }
    } catch (Exception e){
      System.out.println("BAD TEST 1!");
      nFail++;
    }


    // 2: select cards with correct player
    try{
      c.selectCard(0, 0);
      if (c.getPlayerSelected() != 0) {
        System.out.println("ERROR 2: The player selected should be index 0");
        nError++;
      }

      c.selectCard(1, 0);
      if (c.getPlayerSelected() != 1){
        System.out.println("ERROR 2: the player selected is changed by selecting a new player, "
            + "and it should be index 1");
        nError++;
      }
    } catch (Exception e){
      System.out.println("BAD TEST 2!");
      nFail++;
    }


    // 3: select cards with correct cards
    try{
      // reset test
      c.clearCardsSelected();
      c.deSelectPlayer();
      c.selectCard(0, 0);
      c.selectCard(0, 1);
      // to test if cardsSelected array has correct values
      if ((c.getCardsSelected().get(0) != 0) || (c.getCardsSelected().get(1) != 1)){
        System.out.println("ERROR 3: The list should contain [0, 1] indicating the first and "
            + "the second cards have been selected");
        nError++;
      }
      // to test if cards have selected values been changed to true
      if (!(c.players.get(0).getHand()[0].getSelected()) || !(c.players.get(0).getHand()[1].getSelected())){
        System.out.println("ERROR 3: the cards selected have not changed selected values to true yet");
        nError++;
      }

      // to select a different player to test if the method does proper work
      c.selectCard(1, 0);
      c.selectCard(1, 1);
      // to test if old cards and player have been un-selected
      if (c.getPlayerSelected() != 1){
        System.out.println("ERROR 3: A new player has been selected");
        nError++;
      }
      if ((c.players.get(0).getCard(0).getSelected()) || (c.players.get(0).getCard(1).getSelected())){
        System.out.println("ERROR 3: the previous selected cards have been un-selected");
        nError++;
      }
      if (!(c.players.get(1).getHand()[0].getSelected()) || !(c.players.get(1).getHand()[1].getSelected())){
        System.out.println("ERROR 3: the cards selected have not changed selected values to true yet");
        nError++;
      }

    } catch (Exception e){
      System.out.println("BAD TEST 3!");
      nFail++;
    }

    // 4: set selected cards
    try{
      c.selectCard(1, 1);     // we want the 2st player's 1st and 2nd cards to be
      c.selectCard(1, 2);     // selected
      if (!players.get(1).getCard(1).getSelected() && !players.get(1).getCard(2).getSelected()){
        System.out.println("ERROR 4: the selected cards from the player are not correctly selected ");
        nError++;
      }
    } catch (Exception e){
      System.out.println("BAD TEST 4!");
      nFail++;
    }

    // 5: test the toggle
    try{
      String menuName = "dummyMenu";
      c.getMenusOpen().put(menuName, false);
      if (!c.getMenusOpen().isEmpty()){
        Boolean notTriggered = c.getMenusOpen().get(menuName);

        // the first test
        c.toggleMenu(menuName);
        if (c.getMenusOpen().get(menuName) == notTriggered){
          System.out.println("ERROR 5: menu toggle fails, the menu should be triggered");
          nError++;
        }

        // the second test
        c.toggleMenu(menuName);
        if (c.getMenusOpen().get(menuName) != notTriggered){
          System.out.println("ERROR 5: menu toggle fails, the menu not triggered");
          nError++;
        }

      } else {
        System.out.println("ERROR 5: menu not able being put into the HashMap");
        nError++;
      }
    } catch (Exception e) {
      System.out.println("BAD TEST 5!");
      nFail++;
    }

    // test results
    System.out.println("\n--------\nTest results:\n");
    if (nFail != 0){
      System.out.println(nFail + " bad test(s), INVALID TESTS");
    }else {
      System.out.println("Total error: " + nError);
    }
    System.out.println("--------");


    // test clone()
    System.out.println("\n\nObject clones\n");
    ClientState cCopy = c;
    System.out.println(c.toString());
    System.out.println(cCopy.toString());
    System.out.println();
    ClientState cDeepcopy = c.clone();
    System.out.println(c.toString());
    System.out.println(cDeepcopy.toString());
  }


}