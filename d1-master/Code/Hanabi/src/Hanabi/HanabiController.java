package Hanabi;

import com.google.gson.Gson;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.scene.input.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

@SuppressWarnings("Duplicates")
public class HanabiController {

  private GameState gameState;
  private ClientState clientState;
  private Socket socket;
  private BufferedReader BufferedReader;
  private DataInputStream InputStream;
  private PrintStream outputStream;
  private JSONObject event;
  private LinkedList<JSONObject> eventStack;
  private String currentState;
  double prevX, prevY;
  double initX, initY;

  private enum State {
    READY, DRAGGING, HINT
  }

  private State currentViewState = State.READY;


  /**
   * Constructs controller object one string value representing current state.
   *
   * @param gameStateModel A game state model, one of the models to keep game attributes.
   * @param clientStateModel A client state model, one of the models to keep game attributes.
   * @param socket A socket object which has a port number connecting to the server, and it needs an
   * integer to construct itself.
   * @param bufferedReader An data reader reads message from the server.
   * @param inputStream An input data receiver which receives server data through the socket
   * connection, and this object needs InputReader and Socket objects as constructors.
   * @param outputStream An object which outputs data to the server through the socket connection,
   * and this object needs a Socket object as a constructor.
   * @param event A map data structure which contains server messages parsed by ParseJSON method.
   * @param eventStack A linked list data structure in which each element contains an event received
   * from server.
   * @param currentState A string which keeps track of the current game state.
   */
  public HanabiController(
      GameState gameStateModel,
      ClientState clientStateModel,
      Socket socket,
      BufferedReader bufferedReader,
      DataInputStream inputStream,
      PrintStream outputStream,
      JSONObject event,
      LinkedList<JSONObject> eventStack,
      String currentState) {
    this.gameState = gameStateModel;
    this.clientState = clientStateModel;
    this.socket = socket;
    this.outputStream = outputStream;
    this.event = event;
    this.eventStack = eventStack;
    this.currentState = currentState;
    this.BufferedReader = bufferedReader;
    this.InputStream = inputStream;
    this.currentViewState = State.READY;
  }

  public HanabiController() {
    this.eventStack = new LinkedList<>();

  }

  public GameState getGameState() {
    return gameState;
  }

  public void setGameState(GameState gameStateModel) {
    this.gameState = gameStateModel;
  }

  public ClientState getClientState() {
    return clientState;
  }

  public void setClientState(ClientState clientStateModel) {
    this.clientState = clientStateModel;
  }

  public Socket getSocket() {
    return socket;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  public BufferedReader getBufferedReader() {
    return BufferedReader;
  }

  public void setBufferedReader(BufferedReader bufferedReader) {
    BufferedReader = bufferedReader;
  }

  public DataInputStream getInputStream() {
    return InputStream;
  }

  public void setInputStream(DataInputStream inputStream) {
    InputStream = inputStream;
  }

  public PrintStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(PrintStream outputStream) {
    this.outputStream = outputStream;
  }

  public JSONObject getEvent() {
    return event;
  }

  public void setEvent(JSONObject event) {
    this.event = event;
  }

  public LinkedList<JSONObject> getEventStack() {
    return eventStack;
  }

  public void setEventStack(LinkedList<JSONObject> eventStack) {
    this.eventStack = eventStack;
  }

  public String getCurrentState() {
    return currentState;
  }

  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }

  /**
   * Reads a single message from the incoming message buffer
   *
   * @return A JSON String which contains server messages
   */
  public String readMessage() {
    String toReturn = "";
    try {
      char curChar = 0;

      curChar = (char) this.BufferedReader.read();
      while (curChar != '}') {
        toReturn = toReturn + curChar;
        curChar = (char) this.BufferedReader.read();
      }
      toReturn = toReturn + '}';
    } catch (IOException e) {

    }
    return toReturn;
  }

  /**
   * Read all available message from the input buffer at the time, puts each one in a JSONObject,
   * adds them  to a linkedlist and returns it
   *
   * @return the linked list contained all JSONObjects
   */
  public LinkedList<JSONObject> receiveServerData() {
    LinkedList<JSONObject> jsonArr = new LinkedList<>();

    try {
      do {
        String curMsg = readMessage();
        jsonArr.add(parseJSON(curMsg));
      } while (this.BufferedReader.ready());
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return jsonArr;
  }

  /**
   * Converts JSON string (server messages) into a JSONObjective
   *
   * @param jsonString A JSON string which contains event data from the server
   * @return A JSON object which contains an event data from the server
   */
  public JSONObject parseJSON(String jsonString) {
    JSONParser parser = new JSONParser();
    try {
      return (JSONObject) parser.parse(jsonString);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * Sends move messages to the server
   *
   * @param jsonMessage JSON formatted string containing the players' move messages
   */
  public void sendMove(String jsonMessage) {
    this.eventStack.add(parseJSON(jsonMessage));
    this.outputStream.println(jsonMessage);
  }

  /**
   * function responsible for computing the md5 hash of a given string
   *
   * @param msg the string to compute the hash of
   * @return the computed hash
   */
  private static String computeHash(String msg) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(msg.getBytes());
      return new BigInteger(1, md.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      return ("MD5 ... what's MD5?");
    }
  }

  /**
   * Responsible for creating the message to the server for creating a game
   *
   * @param nsid the nsid of the user
   * @param players the number of players in the game
   * @param timeout the maximum time allotted for each turn
   * @param md5 the unique hash associated with each nsid
   * @return A json formatted string to send off
   */
  public String createGame(String nsid, int players, int timeout, String rainbow, String md5) {
    LinkedHashMap map = new LinkedHashMap();
    map.put("cmd", "create");
    map.put("nsid", nsid);
    map.put("players", players);
    map.put("timeout", timeout);
    map.put("rainbow", rainbow);
    map.put("force", true);
    map.put("timestamp", (int) Instant.now().getEpochSecond());
    map.put("md5hash", md5);

    String json = new Gson().toJson(map);
    String hash = computeHash(json);
    map.put("md5hash", hash); // replacing the actual hash with the computed one
    return new Gson().toJson(map);
  }

  /**
   * Responsible for creating the message to the server for joining a game
   *
   * @param nsid the nsid of the user
   * @param gameID the id of the game (received from the game creator)
   * @param token the token of the game (received from the game creator)
   * @param md5 the unique hash associated with each nsid
   */
  public String joinGame(String nsid, int gameID, String token, String md5) {
    LinkedHashMap map = new LinkedHashMap();
    map.put("cmd", "join");
    map.put("nsid", nsid);
    map.put("game-id", gameID);
    map.put("token", token);
    map.put("timestamp", (int) Instant.now().getEpochSecond());
    map.put("md5hash", md5);

    String json = new Gson().toJson(map);
    String hash = computeHash(json);
    map.put("md5hash", hash); // replacing the actual hash with the computed one
    return new Gson().toJson(map);
  }

  /**
   * Given a position makes the JSONObject containing information about a discard
   *
   * @param pos the position of the card to be discarded
   * @return the JSON object containing the move
   */
  public JSONObject doDiscard(int pos) {
    JSONObject map = new JSONObject();
    map.put("action", "discard");
    map.put("position", pos);
    return map;
  }

  /**
   * Given a player and a hint string makes the JSONObject containing information about a hint
   *
   * @param player the position of the player to give a hint to
   * @param hint the hint to give (e.g "1", "5", "g", "r", etc.)
   * @return the JSON object containing the move
   */
  public JSONObject doHint(int player, String hint) {
    JSONObject map = new JSONObject();
    map.put("action", "inform");
    map.put("player", player + 1);
    if ("12345".contains(hint)) { // if the hint is about a rank of a card
      map.put("rank", Integer.parseInt(hint));
    } else {
      map.put("suit", hint);
    }
    return map;
  }

  /**
   * Given a position makes the JSONObject containing information about a play
   *
   * @param pos the position of the card to be played
   * @return the JSON object containing the move
   */
  public JSONObject doPlay(int pos) {
    JSONObject map = new JSONObject();
    map.put("action", "play");
    map.put("position", pos);
    return map;
  }

  /**
   * Convert a move message from the player's action into a JSON object which can be read by the
   * server
   *
   * @param move A map which contains the player's move message
   * @return A JSON object ready to be sent to the server
   */
  public String packStream(JSONObject move) {
    return move.toJSONString();
  }

  /**
   * Helper function for evalEvent, handles the player joined notice
   */
  public void playerJoined() {
    this.clientState.getMenusOpen().replace("playerJoined", true);

    if ((event.get("needed")).equals("0")) {
      this.clientState.getMenusOpen().replace("playerJoined", false);
    }
  }

  /**
   * Helper function for evalEvent, handles the player left notice
   */
  public void playerLeft() {
    this.clientState.getMenusOpen().replace("playerLeft", true);
  }

  /**
   * Sets the turn to the current player
   */
  public void yourMove() {
    int pos = this.gameState.getPosition(); // position in players array
    this.gameState.getPlayers().get(pos).startTurn(); // set the player's individual turn var
    this.gameState.setTurn(pos); // set the turn var in game state
  }

  /**
   * Handles the game cancelled notice
   */
  public void gameCancelled() {
    this.clientState.getMenusOpen().replace("gameCancelled", true);
  }

  /**
   * Handles the game starts message received from the server
   */
  public void gameStarts() {
    // removing the beginning open and close braces to remove ambiguity while parsing
    String phrase = (event.get("hands").toString()).replace("[[", "[");
    phrase = phrase.replace("]]", "]");

    // server has to send at least 2 hands, and they are delimited with whitespace followed by a ","
    String delims = ",\\[";
    String[] tokens = phrase.split(delims);

    ArrayList<Player> players = new ArrayList<>(tokens.length); // will hold all players

    int numCards = 5;  // need to figure out how many cards each hand will have and make it parametric
    if (tokens.length > 3) {
      numCards = 4;
    }

    for (int i = 0; i < tokens.length; i++) {

      Card[] hand = new Card[numCards]; // the new hand being made

      if (tokens[i].contains("[]")) { // this will be your own hand
        for (int j = 0; j < numCards; j++) {
          hand[j] = new Card("?", 0,
              this.gameState.getMode()); // create an array of unknown cards for yourself
        }
        this.gameState.setPosition(i); // set your own index in the game state to i

      } else {
        delims = ",";
        String[] handTokens = tokens[i].split(delims); // holds the strings split by spaces

        int handIdx = 0;

        for (String card : handTokens) {
          if (card.contains("\"")) {
            card = card.replace("\"", "");
            // at this point card should hold the color and number of a card (e.g "b1")

            hand[handIdx] = new Card(card.substring(0, 1),
                Character.getNumericValue(card.charAt(1)), this.gameState.getMode());
            handIdx++;
          }
        }
      }

      Player newPlayer = new Player(hand, i == 0);

      players.add(newPlayer);
    }

    GameState newGS = new GameState(players, this.gameState.getMode(),
        this.gameState.getTimePerTurn());
    newGS.setTurn(this.gameState.getTurn());
    newGS.setPosition(this.gameState.getPosition());
    this.gameState = newGS; // changing the games state for the one that includes cards
    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer

    ArrayList<Player> setPlayers = this.gameState.getPlayers();
    for (int j = 0; j < setPlayers.size(); j++) {
      if (setPlayers.get(j).getTurn()) {
        this.gameState.setTurn(j);
      }

    }
  }

  /**
   * Handles the server telling the client about another player having made a discard
   *
   * @param pos the position of the card in player's hand to discard
   * @param drawnCard the card to replace the discard with
   */
  public void discardedNotice(int pos, Card drawnCard) {
    int turn = this.gameState.getTurn(); // the player who made the discard
    Card cardAtPos = this.gameState.getPlayers().get(turn).getCard(pos);

    // appending the previous card to the discard pile
    this.gameState.appendDiscard(cardAtPos.clone());

    // replacing the discarded card by a new one
//    this.gameState.updatePlayerHand(this.gameState.getPlayers().get(turn), drawnCard, pos);
    cardAtPos.color = drawnCard.getColor();
    cardAtPos.number = drawnCard.getNumber();
    cardAtPos.setMyCPS(this.gameState.topCard.clone());

    this.gameState.decrementCards();
    this.gameState.setScore(this.gameState.getScore() + 1);
    this.gameState.incrementHints();

    this.gameState.getPlayers().get(turn).endTurn(); // end the previous player's turn
    this.gameState.incrementTurn();
    this.gameState.getPlayers().get(this.gameState.getTurn()).startTurn(); // set new player's turn

    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer

    this.gameState.notifySubscribers();
  }

  /**
   * Handles the server telling the client about the confirmation of a previously sent discard
   *
   * @param discardedCard the card that was discarded (the player can now see it)
   */
  public void acceptedReply(Card discardedCard) {
    int turn = this.gameState.getTurn(); // the player who made the discard

    if (this.gameState.getCardsRemaining() > 0) {
      // figure out the position from the eventStack, because a move message about to be sent to the
      // server will be stored in the eventStack
      int pos = ((Long) this.eventStack.getLast().get("position")).intValue() - 1;
      this.gameState.appendDiscard(discardedCard);
      // setting the newly drawn card in the player's hand to a blank one
      this.gameState.getPlayers().get(this.gameState.getTurn()).getCard(pos).color = "?";
      this.gameState.getPlayers().get(this.gameState.getTurn()).getCard(pos).number = 0;
      this.gameState.getPlayers().get(this.gameState.getTurn()).getCard(pos).setMyCPS(this.gameState.topCard.clone());
//      this.gameState.updatePlayerHand(this.gameState.getPlayers().get(turn), newCard, pos);

    }

    // appending the previous card to the discard pile
    this.gameState.appendDiscard(discardedCard);

    this.gameState.getPlayers().get(turn).endTurn(); // end the previous player's turn
    this.gameState.incrementTurn();
    this.gameState.getPlayers().get(this.gameState.getTurn()).startTurn(); // set new player's turn

    if (this.gameState.getCardsRemaining() > 0) {
      this.gameState.decrementCards();
    }

    this.gameState.incrementHints();

    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer
    this.gameState.notifySubscribers();
  }

  /**
   * Handles the server telling the client about another player playing a card
   *
   * @param pos the position of the played card
   * @param drawnCard the newly drawn card
   */
  public void playedNotice(int pos, Card drawnCard) {
    int turn = this.gameState.getTurn(); // the player who made the play
    boolean[][] cardsOnTable = this.gameState.getCardsOnTable();
    Card cardAtPos = this.gameState.getPlayers().get(turn).getCard(pos);
    int[] cardIndices = cardAtPos.cardToIndex(); // g3 = [2][1]


    // replacing the discarded card by a new one
//    this.gameState.updatePlayerHand(this.gameState.getPlayers().get(turn), drawnCard, pos);

    this.gameState.getPlayers().get(turn).endTurn(); // end the previous player's turn
    this.gameState.incrementTurn();
    this.gameState.getPlayers().get(this.gameState.getTurn()).startTurn(); // set new player's turn

    if (this.gameState.getCardsRemaining() > 0) { // if the deck isn't already empty decrease cards
      this.gameState.decrementCards();
    }

    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer

    boolean colorStackEmpty = true; // checking if the color stack on the table is empty
    for (int i = 0; i < 5; i++) {
      if (cardsOnTable[i][cardIndices[1]]) {
        colorStackEmpty = false;
      }
    }

    // means we have a rank 1 card of a given suit that makes up for a valid play
    if (cardIndices[0] == 0) {
      if (colorStackEmpty) {
        this.gameState.getCardsOnTable()[cardIndices[0]][cardIndices[1]] = true;

        // increment the score on a valid play
        this.gameState.setScore(this.gameState.getScore() + 1);
      }
    } else if (cardsOnTable[cardIndices[0] - 1][cardIndices[1]]) {
      // means the played card was valid

      // setting the previous card boolean for the stack to false
//      this.gameState.getCardsOnTable()[cardIndices[0] - 1][cardIndices[1]] = false;

      this.gameState.getCardsOnTable()[cardIndices[0]][cardIndices[1]] = true; //setting card on tbl

      if (cardAtPos.getNumber() == 5) { // increment hints when the played card completes a stack
        this.gameState.incrementHints();
      }
      // increment the score on a valid play
      this.gameState.setScore(this.gameState.getScore() + 1);
    } else { // mean the card played was invalid
      this.gameState.decrementFuses();
      if (this.gameState.getFusesRemaining() < 1) {
        this.clientState.getMenusOpen().replace("gameEnds", true);
      }
      // appending the previous card to the discard pile
      this.gameState.appendDiscard(cardAtPos.clone());
    }

    cardAtPos.color = drawnCard.getColor();
    cardAtPos.number = drawnCard.getNumber();
    cardAtPos.setMyCPS(this.gameState.topCard.clone());

    this.gameState.notifySubscribers();
  }

  /**
   * Handles the server telling the client about the client playing a valid card
   *
   * @param playedCard the card that the client played
   */
  public void builtReply(Card playedCard) {
    int turn = this.gameState.getTurn(); // the player who made the play

    if (this.gameState.getCardsRemaining() > 0) {
      // figure out the position from the eventStack, because a move message about to be sent to the
      // server will be stored in the eventStack
      int pos = ((Long) this.eventStack.getLast().get("position")).intValue() - 1;
      // setting the newly drawn card in the player's hand to a blank one
      Card newCard = this.gameState.getPlayers().get(this.gameState.getTurn()).getCard(pos);
      newCard.color = "?";
      newCard.number = 0;
      newCard.setMyCPS(this.gameState.topCard.clone());
//      this.gameState.updatePlayerHand(this.gameState.getPlayers().get(turn), newCard, pos);
    }

    boolean[][] cardsOnTable = this.gameState.getCardsOnTable();
    int[] cardIndices = playedCard.cardToIndex(); // g3 = [2][1]
    boolean colorStackEmpty = true; // checking if the color stack on the table is empty
    for (int i = 0; i < 5; i++) { // determine if the current color stack is empty
      if (cardsOnTable[i][cardIndices[1]]) {
        colorStackEmpty = false;
      }
    }
    // means we have a rank 1 card of a given suit that makes up for a valid play
    if (cardIndices[0] == 0) {
      if (colorStackEmpty) {
        this.gameState.getCardsOnTable()[cardIndices[0]][cardIndices[1]] = true;
      }
    } else if (cardsOnTable[cardIndices[0] - 1][cardIndices[1]]) {
      // means the played card was valid

      // setting the previous card boolean for the stack to false
//      this.gameState.getCardsOnTable()[cardIndices[0] - 1][cardIndices[1]] = false;

      this.gameState.getCardsOnTable()[cardIndices[0]][cardIndices[1]] = true; //setting card on tbl


      if (playedCard.getNumber() == 5) { // increment hints when the played card completes a stack
        this.gameState.incrementHints();
      }
    }
    this.gameState.getPlayers().get(turn).endTurn(); // end the previous player's turn
    this.gameState.incrementTurn();
    this.gameState.getPlayers().get(this.gameState.getTurn()).startTurn(); // set new player's turn

    this.gameState.setScore(this.gameState.getScore() + 1);
    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer

    if (this.gameState.getCardsRemaining() > 0) { // if the deck isn't already empty decrease cards
      this.gameState.decrementCards();
    }

    this.gameState.notifySubscribers();
  }

  /**
   * Handles the server telling the client about the client playing an invalid card
   *
   * @param playedCard the card that was played
   */
  public void burnedReply(Card playedCard) {
    int turn = this.gameState.getTurn(); // the player who made the play

    if (this.gameState.getCardsRemaining() > 0) {
      // figure out the position from the eventStack, because a move message about to be sent to the
      // server will be stored in the eventStack)
      int pos = ((Long) this.eventStack.getLast().get("position")).intValue() - 1;
      // setting the newly drawn card in the player's hand to a blank one
      Card newCard = this.gameState.getPlayers().get(this.gameState.getTurn()).getCard(pos);
      newCard.color = "?";
      newCard.number = 0;
      newCard.setMyCPS(this.gameState.topCard.clone());
      this.gameState.updatePlayerHand(this.gameState.getPlayers().get(turn), newCard, pos);
    }

    this.gameState.getPlayers().get(turn).endTurn(); // end the previous player's turn
    this.gameState.incrementTurn();
    this.gameState.getPlayers().get(this.gameState.getTurn()).startTurn(); // set new player's turn

    if (this.gameState.getCardsRemaining() > 0) { // if the deck isn't already empty decrease cards
      this.gameState.decrementCards();
    }
    this.gameState.appendDiscard(playedCard);

    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer

    this.gameState.decrementFuses();
    if (this.gameState.getFusesRemaining() < 1) {
      this.clientState.getMenusOpen().replace("gameEnds", true);
    }
  }

  /**
   * Handles the server telling the client about a received hint
   *
   * @param hintArray the array corresponding to each card in player's hand
   * @param hint the rank or suit the hint is about
   */
  public void informReceivingPlayer(Boolean[] hintArray, String hint) {
    int turn = this.gameState.getTurn(); // the player who made the play
    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer
    this.gameState.decrementHints();

    this.gameState.getPlayers().get(turn).endTurn(); // end the previous player's turn
    this.gameState.incrementTurn();
    this.gameState.getPlayers().get(this.gameState.getTurn()).startTurn(); // set new player's turn

    this.gameState.getPlayers().get(this.gameState.getPosition()).receiveHint(hintArray, hint);
    this.gameState.notifySubscribers();
  }

  /**
   * Handles the server telling the client about another player receiving a hint
   *
   * @param hintArray the array corresponding to each card in player's hand
   * @param hint the rank or suit the hint is about
   * @param playerPos the position of the player that receives a hint
   */
  public void informOtherPlayer(Boolean[] hintArray, String hint, int playerPos) {
    int turn = this.gameState.getTurn(); // the player who made the play
    this.gameState.setTimeRemaining(this.gameState.getTimePerTurn()); // resetting the timer
    this.gameState.decrementHints();

    this.gameState.getPlayers().get(turn).endTurn(); // end the previous player's turn
    this.gameState.incrementTurn();
    this.gameState.getPlayers().get(this.gameState.getTurn()).startTurn(); // set new player's turn
  }

  /**
   * Analyzes messages, executes actions in the game
   *
   * @param event An event which makes changes in system
   */
  @SuppressWarnings("Duplicates")
  public void evalEvent(JSONObject event) throws Exception {

    String eventVal = null; // contains the type of information received
    this.event = event;
    for (Object key : event.keySet()) {
      if (key.equals("notice") || key.equals("reply")) {
        eventVal = (String) event.get(key);
      }
    }

    System.out.println(event.toJSONString());

    if (eventVal.equals("player joined")) {  // player joined
      playerJoined();
    } else if (eventVal.equals("player left")) { // player left
      playerLeft();
    } else if (eventVal.equals("game starts")) {
      gameStarts();
    } else if (eventVal.equals("your move")) {
      yourMove();
    } else if (eventVal.equals("invalid")) {
      throw new Exception("Error: invalid move sent to the server");
    } else if (eventVal.equals("game cancelled")) {
      gameCancelled();
    } else if (eventVal.equals("discarded")) {
      int pos = ((Long) event.get("position")).intValue() - 1;

      Card drawnCard = null;
      if (event.containsKey("card")) { // if card was drawn have to parse the string and make the object
        String cardString = (String) event.get("card");
        drawnCard = new Card(cardString.substring(0, 1),
            Character.getNumericValue(cardString.charAt(1)), this.gameState.getMode());
      }
      discardedNotice(pos, drawnCard);
    } else if (eventVal.equals("accepted")) {
      String cardString = (String) event.get("card");
      Card discardedCard = new Card(cardString.substring(0, 1),
          Character.getNumericValue(cardString.charAt(1)), this.gameState.getMode());
      acceptedReply(discardedCard);
    } else if (eventVal.equals("played")) {
      int pos = ((Long) event.get("position")).intValue() - 1;

      Card drawnCard = null;
      if (event.containsKey("card")) { // if card was drawn have to parse the string and make the object
        String cardString = (String) event.get("card");
        drawnCard = new Card(cardString.substring(0, 1),
            Character.getNumericValue(cardString.charAt(1)), this.gameState.getMode());
      }
      playedNotice(pos, drawnCard);
    } else if (eventVal.equals("built")) {
      String cardString = (String) event.get("card");
      Card playedCard = new Card(cardString.substring(0, 1),
          Character.getNumericValue(cardString.charAt(1)), this.gameState.getMode());

      builtReply(playedCard);
    } else if (eventVal.equals("burned")) {
      String cardString = (String) event.get("card");
      Card playedCard = new Card(cardString.substring(0, 1),
          Character.getNumericValue(cardString.charAt(1)), this.gameState.getMode());

      burnedReply(playedCard);
    } else if (eventVal.equals("inform")) {
      String hint = null;

      if (event.containsKey("suit")) {
        hint = (String) event.get("suit");
      } else {
        hint = (((Long) event.get("rank"))) + ""; // getting the attribute the hint was about
      }

      if (event.get("info") != null) {// receiving player
        JSONArray arr = (JSONArray) event.get("info");
        Boolean[] res = new Boolean[arr.size()]; // the array that will hold the result


        for (int i = 0; i < res.length; i++) {
          res[i] = (Boolean) arr.get(i);
        }

        // TODO need to pass the resulting boolean array along with the type of hint (rank or suit)
        //  to an animate function that draws the hint
        informReceivingPlayer(res, hint);

      } else {
        // other player
        int pos = ((Long) event.get("player")).intValue() - 1;
        Card[] pCards = this.gameState.getPlayers().get(pos).getHand();
        Boolean[] res = new Boolean[pCards.length];
        Arrays.fill(res, Boolean.FALSE);

        for (int i = 0; i < pCards.length; i++) {
          Card c = pCards[i];
          if ("rgbywm".contains(hint)) { // meaning the hint was about the suit
            if (c.getColor().equals(hint)) {
              res[i] = true;
            }
          } else {
            if (c.getNumber() == Integer.parseInt(hint)) {
              res[i] = true;
            }
          }
        }
        informOtherPlayer(res, hint, pos);
        // TODO again, need to draw an animation here, manually replacing the client state
        //  menusOpen field for now
        this.clientState.getMenusOpen().replace("hintAnimation", true);
      }
    } else if (eventVal.equals("game ends")) {
      this.clientState.getMenusOpen().replace("gameEnds", true);
      this.socket.close();
      this.gameState = null;
      Thread.sleep(5000); // wait for 5 seconds before redirecting to main menu
      this.clientState.getMenusOpen().replace("mainMenu", true);

    } else if (eventVal.equals("joined")) {
      initModel(event);
    } else if (eventVal.equals("created")) {
      System.out.println(event.get("game-id"));
      System.out.println(event.get("token"));
      // TODO set model variables to the token and game-id acquired from here
    } else if (eventVal.equals("no such game")) {
      this.clientState.getMenusOpen().replace("noSuchGame", true);
    } else if (eventVal.equals("game full")) {
      this.clientState.getMenusOpen().replace("gameFull", true);
    }

    // add to server confirmed moves to the event stack
    if (eventVal.equals("accepted") || eventVal.equals("discarded") || eventVal.equals("played")
        || eventVal.equals("built") || eventVal.equals("burned") || eventVal.equals("inform")) {
      eventStack.add(event);
    }
  }

  /**
   * Initializes or establishes a game table with some game settings specified by the game master
   *
   * @param event An event which contains all messages needed to initialize and start a game
   */
  public void initModel(JSONObject event) {
    Integer playersNeeded;
    Integer timeout;
    String mode;

    playersNeeded = ((Long) event.get("needed")).intValue();
    timeout = ((Long) event.get("timeout")).intValue();
    mode = (String) event.get("rainbow");
    this.gameState = new GameState(playersNeeded, timeout, mode);
    this.clientState = new ClientState(this.gameState.getPlayers());
  }

  /**
   * handle the mouseEvent of the player from the view
   *
   * @param mouseEvent an mouseEvent when the player press the mouse button
   */
  public void handlePressed(MouseEvent mouseEvent) {
    switch (currentViewState) {
      case READY:
        // hit detection
        System.out.println(mouseEvent.getX() + " " + mouseEvent.getY());
        boolean hit = gameState.checkHit(mouseEvent.getX(), mouseEvent.getY());
        if (hit) {
          gameState.setSelectedCard(gameState.whichCard(mouseEvent.getX(), mouseEvent.getY()));
          gameState.selectedCard.setSelected(true);
          // TODO fuck this shit
          if (gameState.handContain(gameState.getPlayers().get(0).getHand(),
              (gameState.whichCard(mouseEvent.getX(), mouseEvent.getY())))) {
            System.out.println("Card Hit!!");
            // set start location for drag

            prevX = mouseEvent.getX();
            prevY = mouseEvent.getY();

            initX = gameState.selectedCard.x;
            initY = gameState.selectedCard.y;

            gameState.setIsDragging();

            currentViewState = State.DRAGGING;
          } else {
            currentViewState = State.HINT;
          }

        } else {
          System.out.println("Card NOT Hit!!");
        }

        break;
    }
  }

  /**
   * handle the drag action when player drag the card
   *
   * @param mouseEvent an mouseEvent following player's drag action
   */
  public void handleDrag(MouseEvent mouseEvent) {
    switch (currentViewState) {
      case DRAGGING:
        System.out.println("Dragging!!");
        // context: none
        // side effects:
        // - calculate amount of movement
        double dX = mouseEvent.getX() - prevX;
        double dY = mouseEvent.getY() - prevY;
        prevX = mouseEvent.getX();
        prevY = mouseEvent.getY();
        // - move blob
        gameState.moveCard(gameState.selectedCard, dX, dY);
        // transition to new state: same state
        break;
    }
  }

  /**
   * handle the drag action when player release the card
   *
   * @param mouseEvent an mouseEvent when player releases the card
   */
  public void handleRelease(MouseEvent mouseEvent) {
    switch (currentViewState) {
      case DRAGGING:
        // context: none
        // side effects:
        // - set to no selection
        gameState.deSetIsDragging();

        if (gameState.playBox.checkInBox(mouseEvent.getX(), mouseEvent.getY())) {
          Player me = this.gameState.getPlayers().get(this.gameState.getPosition());
          int position = 0;
          for (Card c : me.getHand()) {
            position++;
            if (c.getSelected()) {
              break;
            }
          }
          sendMove(doPlay(position).toJSONString());
          gameState.setCardXY(gameState.selectedCard, gameState.selectedCard.initX, gameState.selectedCard.initY);
          gameState.selectedCard.setSelected(false);

        } else if (gameState.discardBox.checkInBox(mouseEvent.getX(), mouseEvent.getY())) {
          Player me = this.gameState.getPlayers().get(this.gameState.getPosition());
          int position = 0;
          for (Card c : me.getHand()) {
            position++;
            if (c.getSelected()) {
              break;
            }
          }
          sendMove(doDiscard(position).toJSONString());
          gameState.setCardXY(gameState.selectedCard, gameState.selectedCard.initX, gameState.selectedCard.initY);
          gameState.selectedCard.setSelected(false);

        } else {
          gameState.setCardXY(gameState.selectedCard, gameState.selectedCard.initX, gameState.selectedCard.initY);
          gameState.selectedCard.setSelected(false);
          gameState.selectedCard = null;
        }
        // transition to new state
        currentViewState = State.READY;
        break;

      case HINT:
        gameState.sethintXY(gameState.selectedCard);
        gameState.setHint();
        gameState.deSetHint();
        currentViewState = State.READY;
        break;
    }
  }

  @SuppressWarnings("Duplicates")
  public static void main(String args[]) throws Exception {
    System.out.println("Testing");

    int t = 0; // will be used to indicate the number of the test
    int numTest = 100; // total number of tests on the controller, will set to 100 for now
    boolean[] testsPassed = new boolean[numTest]; // boolean array to keep track of the passed tests
    JSONObject map = new JSONObject(); // will be used to hold evalEvent inputs
    for (int i = 0; i < testsPassed.length; i++) {          // initialized to all true values
      testsPassed[i] = true;
    }

    // hashmaps for converting a Card's color and number to indices for the cards on table matrix
    HashMap<String, Integer> colNumToIdx = new HashMap<>();
    colNumToIdx.put("r", 0);
    colNumToIdx.put("g", 1);
    colNumToIdx.put("b", 2);
    colNumToIdx.put("y", 3);
    colNumToIdx.put("w", 4);
    colNumToIdx.put("1", 0);
    colNumToIdx.put("2", 1);
    colNumToIdx.put("3", 2);
    colNumToIdx.put("4", 3);
    colNumToIdx.put("5", 4);

    // sample 2 player game for testing
    Card[] hand1 = {
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
    };

    Player player1 = new Player(hand1, false);

    Card[] hand2 = {
        new Card("b", 2, "none"),
        new Card("w", 1, "none"),
        new Card("b", 5, "none"),
        new Card("g", 5, "none"),
        new Card("r", 2, "none"),
    };
    Player player2 = new Player(hand2, true);
    ArrayList<Player> players = new ArrayList<>(2); // sample player list
    players.add(player1);
    players.add(player2);

    GameState gameStateModel = new GameState(players, "none", 60); // sample gameState
    LinkedList<Card> discardPile = new LinkedList<>();
    discardPile.add(new Card("y", 1, "none"));
    discardPile.add(new Card("y", 2, "none"));
    gameStateModel.setDiscardPile(discardPile);

    gameStateModel.setHintsRemaining(4); // in this sample situation there 7 more hint tokens left
    gameStateModel.setCardsRemaining(39); // 39 more cards in the pile
    gameStateModel.setTurn(1);
    gameStateModel.setScore(3);
    gameStateModel.setFusesRemaining(2);

    // initializing the cards currently on the table
    boolean[][] cardsOnTable = new boolean[5][5];
//    ArrayList<Boolean> red, blue, green, yellow, white;
//    red = new ArrayList<>(Arrays.asList(false, true, false, false, false));
//    green = new ArrayList<>(Arrays.asList(false, false, false, true, false));
//    blue = new ArrayList<>(Arrays.asList(true, false, false, false, false));
//    yellow = new ArrayList<>(Arrays.asList(false, false, false, true, false));
//    white = new ArrayList<>(Arrays.asList(false, false, false, false, false));

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        cardsOnTable[i][j] = false;
      }
    }

    cardsOnTable[1][0] = true;
    cardsOnTable[3][1] = true;
    cardsOnTable[0][2] = true;
    cardsOnTable[3][3] = true;

    gameStateModel.setCardsOnTable(cardsOnTable);

    ClientState clientStateModel = new ClientState(players); // sample clientState

    // hashmap for menus open
    HashMap<String, Boolean> menusOpen = new HashMap<>();
    menusOpen.put("playerLeft", false);
    menusOpen.put("playerJoined", false);
    menusOpen.put("gameCancelled", false);
    menusOpen.put("gameEnds", false);
    menusOpen.put("mainMenu", false);
    clientStateModel.setMenusOpen(menusOpen);

    JSONObject event = new JSONObject(); //event key value pair needed for construction
    LinkedList<JSONObject> events = new LinkedList<>(); // the list of events so far
    String curState = ""; // current state of the game

    // default null values, because server is unresponsive right now
    Socket socket = null;
    BufferedReader buf = null;
    DataInputStream in = null;
    PrintStream out = null;
    try {
      // tries to establish a connection with the Hanabi server (unreliable right now) and continues
      // with the null values on failure
      socket = new Socket("gpu2.usask.ca", 10219); // the Hanabi server
//      socket = new Socket("localhost", 8080);

      in = new DataInputStream(socket.getInputStream()); // input stream
      out = new PrintStream(socket.getOutputStream()); // output stream
      buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      // this is not included in the test plan but will be useful to put here for now to
      // guarantee an open socket sending and receiving data
      System.out.println(

          "Test#" + t + ": Unable to create a socket connection with the server: " + e
              .getMessage());
      testsPassed[t] = false;
    }

    // at this point a dummy controller should be created and test should be possible to do on it
    HanabiController cont = new HanabiController(gameStateModel, clientStateModel,
        socket, buf, in,
        out, event, events, curState);

    // TODO setting time remaining to 10 for now, so it compiles, will remove after the timer
    //  implementation is complete
    cont.getGameState().setTimeRemaining(10);
    // readMessage()
    t++;
    try {
      cont.getOutputStream().println("{ \"notice\" : \"game ends\" }" + '\n');

      String jsonVal = cont.readMessage();  // dummy value used for JSON related tests

      if (jsonVal == null || jsonVal.isEmpty()) {
        System.out
            .println("Test#" + t + ": readMessage() has failed: returned null or empty");
        testsPassed[t] = false;
      }
    } catch (NullPointerException e) {
      System.out.println("Test#" + t + ": readMessage() has failed" + e.getMessage());
    }
    // parseJson()
    t++;
    String[] jsonMessages = {"{ \"notice\" : \"player left\"\n"
        + ", \"needed\" : 3\n"
        + "}\n",
        "{ \"reply\"   : \"joined\"\n"
            + "        , \"needed\"  : 2\n"
            + "\t, \"timeout\" : 60\n"
            + "        }"};
    JSONObject expected1 = new JSONObject();
    JSONObject expected2 = new JSONObject();
    expected1.put("notice", "player left");
    expected1.put("needed", "3");
    expected2.put("reply", "joined");
    expected2.put("needed", "2");
    expected2.put("timeout", "60");

    JSONObject[] expected = {expected1, expected2};
    JSONObject exp, res; //expected and actual JSONObject output holders
    String st1, st2;

    // checking 2 scenarios for json messages against their expected outputs
    for (int i = 0; i < expected.length; i++) {
      exp = expected[i];
      res = cont.parseJSON(jsonMessages[i]);
      for (Object key : exp.keySet()) {
        st1 = String.valueOf(exp.get(key)); // converting JSONOBject received values to Strings
        st2 = String.valueOf(res.get(key));
        if (!st1.equals(st2)) {
          testsPassed[t] = false;
          break; // no need to continue with the rest of the values
        }
      }
    }

    // error print statement is outside of the loops, because the same message would get printed
    // multiple times
    if (!testsPassed[t]) {
      System.out.println("Test#" + t + ": parseJson: expected value is not returned");
    }

    // Player joined
    t++;
    map.put("notice", "player joined");
    map.put("needed", "2");

    cont.evalEvent(map); // server tells the player another player has joined the game
    if (!cont.getClientState().getMenusOpen().get("playerJoined")) {
      System.out.println(
          "Test#" + t + ": evalEvent(player joined): the popup notification has not been sent");
      testsPassed[t] = false;
    }

    // Player left
    t++;
    map.clear(); // make sure to clean the sample hashmap before each new event test
    map.put("notice", "player left");
    map.put("needed", "3");

    cont.evalEvent(map); // server tells the player another player has left the game
    if (!cont.getClientState().getMenusOpen().get("playerLeft")) {
      System.out.println(
          "Test#" + t + ": evalEvent(player left): the popup notification has not been sent");
      testsPassed[t] = false;
    }

    // Game Starts(notice)
    Card[] cards = {
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
    };

    Player p2 = new Player(cards, true);
    cont.getGameState().setPlayers(new ArrayList<>(Arrays.asList(player1, p2)));

    t++;
    map.clear();
    map.put("notice", "game starts");
    map.put("hands", "[[],[\"b2\",\"w1\",\"b5\",\"g5\",\"r2\"]]");
    cont.evalEvent(map); // server tells the player the game is starting and give info on hands

    // the hand of the second player after evalEvent has been called (should be full)
    Card[] resHand = cont.getGameState().getPlayers().get(1).getHand();

    try {
      for (int i = 0; i < hand2.length; i++) {
        if (!(resHand[i].getNumber().equals(hand2[i].getNumber()) && resHand[i].getColor()
            .equals(hand2[i].getColor()))) {
          System.out.println(
              "Test#" + t
                  + ": evalEvent(game starts): 1 or more of the cards in p2's hand are incorrect");
          testsPassed[t] = false;
          break;
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println(
          "Test#" + t
              + ": evalEvent(game starts): in player's hand array," + e.getMessage());
      testsPassed[t] = false;
    }

    // resetting the players Arraylist in case the game starts fails
    cont.getGameState().setPlayers(players);

    // Your move
    t++;
    map.clear();
    map.put("notice", "your move");

    cont.evalEvent(map); // server tells you it's your move

    ArrayList<Player> p = cont.getGameState().getPlayers();
    int playerIdx = 0; // will hold the index of the player in the player array
    // this loop figures out what is your own index in the player array for the next test
    for (int i = 0; i < p.size(); i++) {
      if (p.get(i).getHand().length == 0) {
        playerIdx = i;
        break;
      }
    }
    if (!cont.getGameState().getPlayers().get(playerIdx).getTurn()) {
      System.out.println("Test#" + t + ": evalEvent(your move): player's turn not set");
      testsPassed[t] = false;
    }

    // Reply invalid
    t++;
    map.clear();
    map.put("reply", "invalid");
    try {
      cont.evalEvent(map); // server tells you the move you made is invalid
      // at this point the program hasn't thrown an exception (which is the expected behaviour
      System.out
          .println("Test#" + t + ": evalEvent(reply: invalid): Exception expected, but not thrown");
      testsPassed[t] = false;
    } catch (Exception e) {
      // throwing an exception here is correct behaviour as the GUI should prevent this ever
      // happening
      e.getMessage();
    }

    // Game cancelled
    t++;
    map.clear();
    map.put("notice", "game cancelled");

    cont.evalEvent(map); // server tells you that the game has been cancelled
    if (!cont.getClientState().getMenusOpen().get("gameCancelled")) {
      System.out.println(
          "Test#" + t + ": evalEvent(game cancelled): the popup notification has not been sent");
      testsPassed[t] = false;
    }

    // Discarded(notice)
    map.clear();

    int discardPos = 1; // player 2 will discard w1
    Card drawnCard = new Card("g", 3, "none");

    map.put("notice", "discarded");
    map.put("position", discardPos + ""); // make this a string for format consistency
    map.put("drew", drawnCard.getColor() + drawnCard.getNumber());
    cont.getGameState().setTurn(1);

    // Discarded(notice): the card that was previously on top of the discard pile
    Card oldDiscard = cont.getGameState().getDiscardPile().getLast();
    int oldTurn = cont.getGameState()
        .getTurn(); // this will be the player that is about to discard a card
    Card oldCard = cont.getGameState().getPlayers().get(oldTurn).getCard(discardPos);
    int oldCardCount = cont.getGameState().getCardsRemaining();
    int oldTokenCount = cont.getGameState().getHintsRemaining();
    // newCardDrawn, visual confirmation

    cont.evalEvent(map); // server tells you another player has discarded a card

    // Discarded(notice): update player
    t++;
    Card newCard = cont.getGameState().getPlayers().get(oldTurn).getCard(discardPos);
    if (!(newCard.getNumber().equals(drawnCard.getNumber()) && newCard.getColor()
        .equals(drawnCard.getColor()))) {
      System.out.println("Test#" + t
          + ": evalEvent(discard notice): the discarded card wasn't replaced by a new one");
      testsPassed[t] = false;
    }

    // Discarded(notice): top card on discard pile
    t++;
    // the card that is now at the top of the discard pile
    Card newDiscard = cont.getGameState().getDiscardPile().getLast();
    if (!(newDiscard.getNumber().equals(oldCard.getNumber()) && newDiscard.getColor()
        .equals(oldCard.getColor()))) {
      System.out.println("Test#" + t
          + ": evalEvent(discard notice): the top card on the discard pile did not get updated");
      testsPassed[t] = false;
    }

    int oldTop;
    // Discarded(notice): Old discard not in second position
    t++;
    // this test makes sure that the card that was previously on top of the discard pile is now in
    // the second position (1st idx)
    if (oldDiscard != null) {
      oldTop = cont.getGameState().getDiscardPile().size() - 2; // the index of the old top discard
      boolean rank = cont.getGameState().getDiscardPile().get(oldTop).getNumber()
          .equals(oldDiscard.getNumber());
      boolean suit = cont.getGameState().getDiscardPile().get(oldTop).getColor()
          .equals(oldDiscard.getColor());

      if (rank && suit) {
        assert true; // means that the condition is satisfied and the test passes,
        // need this here to avoid compiler warning
      } else {
        System.out.println("Test#" + t
            + ": evalEvent(discard notice): the old discard not in the second position");
        testsPassed[t] = false;
      }
    }

    // Discarded(notice): Turn ended
    t++;
    if (oldTurn == cont.getGameState().getTurn() || cont.getGameState().getPlayers().get(oldTurn)
        .getTurn()) {
      System.out.println("Test#" + t
          + ": evalEvent(discard notice): the turn was not updated after the discard");
      testsPassed[t] = false;
    }

    // Discarded(notice): Card count decremented
    t++;
    if (oldCardCount != 0 && oldCardCount - 1 != cont.getGameState()
        .getCardsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(discard notice): the card count was not decremented");
      testsPassed[t] = false;
    }

    // Discarded(notice): Hint count incremented
    t++;
    if (oldTokenCount + 1 != cont.getGameState().getHintsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(discard notice): the number of hints wasn't incremented after discard");
      testsPassed[t] = false;
    }

    // Discarded(notice): Timer reset
    t++;
    if (!(cont.getGameState().getTimePerTurn() - cont.getGameState().getTimeRemaining() < 2)) {
      System.out.println("Test#" + t
          + ": evalEvent(discard notice): the timer was not reset after the move");
      testsPassed[t] = false;
    }

    // Accepted(reply):
    t++;
    map.clear();
    oldCard = new Card("g", 2, cont.getGameState().getMode());
    map.put("reply", "accepted");
    map.put("card", oldCard.getColor() + oldCard.getNumber());
    map.put("replaced", "true");

    cont.getGameState().setTurn(0); // set turn to the current player, so they can do the discard
    oldDiscard = cont.getGameState().getDiscardPile().getLast();
    oldTurn = cont.getGameState()
        .getTurn(); // this will be the player that is about to discard a card
    oldCardCount = cont.getGameState().getCardsRemaining();
    oldTokenCount = cont.getGameState().getHintsRemaining();
    cont.getGameState().setTimeRemaining(10);

    JSONObject madeMove = new JSONObject();
    madeMove.put("action", "discard");
    madeMove.put("position", "1");
    cont.getEventStack().add(madeMove); // for now artificially adding the made move into the
    // eventStack to be used by acceptedReply()

    cont.evalEvent(map); // server tells you your discard was accepted and tells you the card you
    // just played

    // Accepted(reply): the card that is now at the top of the discard pile
    newDiscard = cont.getGameState().getDiscardPile().getLast();
    if (!(newDiscard.getNumber().equals(oldCard.getNumber()) && newDiscard.getColor()
        .equals(oldCard.getColor()))) {
      System.out.println("Test#" + t
          + ": evalEvent(Accepted reply): the top card on the discard pile did not get updated");
      testsPassed[t] = false;
    }

    // Accepted(reply): Old discard not in second position
    t++;
    // this test makes sure that the card that was previously on top of the discard pile is now in
    // the second position (1st idx)
    if (oldDiscard != null) {
      oldTop = cont.getGameState().getDiscardPile().size() - 2; // the index of the old top discard
      boolean rank = cont.getGameState().getDiscardPile().get(oldTop).getNumber()
          .equals(oldDiscard.getNumber());
      boolean suit = cont.getGameState().getDiscardPile().get(oldTop).getColor()
          .equals(oldDiscard.getColor());

      if (rank && suit) {
        assert true; // means that the condition is satisfied and the test passes,
        // need this here to avoid compiler warning
      } else {
        System.out.println("Test#" + t
            + ": evalEvent(Accepted reply): the old discard not in the second position");
        testsPassed[t] = false;
      }
    }

    // Accepted(reply): Turn ended
    t++;
    if (oldTurn == cont.getGameState().getTurn() || cont.getGameState().getPlayers().get(oldTurn)
        .getTurn()) {
      System.out.println("Test#" + t
          + ": evalEvent(Accepted reply): the turn was not updated after the discard");
      testsPassed[t] = false;
    }

    // Accepted(reply): Card count decremented
    t++;
    if (oldCardCount != 0 && oldCardCount - 1 != cont.getGameState()
        .getCardsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(Accepted reply): the card count was not decremented");
      testsPassed[t] = false;
    }

    // Accepted(reply): Hint count incremented
    t++;
    if (oldTokenCount + 1 != cont.getGameState().getHintsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(Accepted reply): the number of hints wasn't incremented after discard");
      testsPassed[t] = false;
    }

    // Accepted(reply): Timer reset
    t++;
    if (!(cont.getGameState().getTimePerTurn() - cont.getGameState().getTimeRemaining() < 2)) {
      System.out.println("Test#" + t
          + ": evalEvent(Accepted reply): the timer was not reset after the move");
      testsPassed[t] = false;
    }

    // Played(notice)
    map.clear();

    map.put("notice", "played");
    map.put("position", discardPos + "");
    map.put("drew", drawnCard.getColor() + drawnCard.getNumber());

    cont.getGameState().setTurn(1); // set to the other player's turn

    oldTurn = cont.getGameState()
        .getTurn(); // this will be the player that is about to discard a card
    oldCardCount = cont.getGameState().getCardsRemaining();
    int oldScore = cont.getGameState().getScore();
    int oldFuse = cont.getGameState().getFusesRemaining();

    cont.evalEvent(map); // server tells you another player has played a card

    // Played(notice): Update Player
    t++;
    newCard = cont.getGameState().getPlayers().get(oldTurn).getCard(discardPos);
    if (!(oldCard.getNumber().equals(drawnCard.getNumber()) && oldCard.getColor()
        .equals(drawnCard.getColor()))) {
      if (!(newCard.getNumber().equals(drawnCard.getNumber()) && newCard.getColor()
          .equals(drawnCard.getColor()))) {
        System.out.println("Test#" + t
            + ": evalEvent(played notice): the played card wasn't replaced by a new one");
        testsPassed[t] = false;
      }
    }

    // Played(notice): Turn ended
    t++;
    if (oldTurn == cont.getGameState().getTurn() || cont.getGameState().getPlayers().get(oldTurn)
        .getTurn()) {
      System.out.println("Test#" + t
          + ": evalEvent(played notice): the turn was not updated after the play");
      testsPassed[t] = false;
    }

    // Played(notice): Card count decremented
    t++;
    if (oldCardCount != 0 && oldCardCount - 1 != cont.getGameState()
        .getCardsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(played notice): the card count was not decremented");
      testsPassed[t] = false;
    }

    // Played(notice): Timer reset
    t++;
    if (!(cont.getGameState().getTimePerTurn() - cont.getGameState().getTimeRemaining() < 2)) {
      System.out.println("Test#" + t
          + ": evalEvent(played notice): the timer was not reset after the move");
      testsPassed[t] = false;
    }

    // Played(notice): in this case p2 tried to play his g3 at idx 1, which is an invalid move
    t++;
    if (oldFuse - 1 != cont.getGameState().getFusesRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(played notice): the fuses were not decremented on an invalid play");
      testsPassed[t] = false;
    }

    // going to assume that previous tests have passed at this point ( the tests referring to
    // Played(notice) like replacing a card and resetting the time etc.
    // here will only test if the table and the score were updated on a valid play
    int numIdx = colNumToIdx.get("2"); // converting to string to match the
    int colIdx = colNumToIdx.get("b"); // hashmap format

    map.clear();
    map.put("notice", "played");
    map.put("position", "0"); //b1 is at the 0 position, meaning this is a valid move
    map.put("drew", drawnCard.getColor() + drawnCard.getNumber());
    cont.getGameState().setTurn(1); // it is player 2's turn again

    cont.evalEvent(map);

    boolean[][] newTable = cont.getGameState().getCardsOnTable();

    // Played(notice): check if cardsOnTable was updated
    t++;
    if (!cont.getGameState().getCardsOnTable()[numIdx][colIdx]) {
      System.out.println("Test#" + t
          + ": evalEvent(played notice): the cards on table did not update on a valid play");
      testsPassed[t] = false;
    }

    // Played(notice): check if the previous card on the top of the stack was set to false in the 2d
    // array omit if there weren't any cards for the stack before the move
    t++;
    if (numIdx != 0 && newTable[numIdx - 1][colIdx]) {
      System.out.println("Test#" + t
          + ": evalEvent(played notice): the bool for the previous top card still set to true in "
          + "cardsOnTable 2d array on a valid play");
      testsPassed[t] = false;
    }

    // Played(notice): check if the score went up
    t++;
    if (oldScore + 1 != cont.getGameState().getScore()) {
      System.out.println("Test#" + t
          + ": evalEvent(played notice): the score was not increased on a valid play");
      testsPassed[t] = false;
    }

    Card playedCard = new Card("g", 5, "none");
    oldTokenCount = cont.getGameState().getHintsRemaining();
    cont.getGameState().setTurn(1); // it is player 2's turn again
    map.replace("position", "3"); // the green 5 is in the 5th position

    cont.evalEvent(map); // p2 plays green 5, which is valid

    // Played(notice): when the card played was of rank 5 check that the hints were incremented
    t++;
    if (playedCard.getNumber() == 5 && oldTokenCount < 8 && oldTokenCount + 1 != cont.getGameState()
        .getHintsRemaining()) {
      testsPassed[t] = false;
      System.out.println("Test#" + t
          + ": evalEvent(played notice): hints not incremented when rank 5 card was played");
    }

    // Built(reply)
    map.clear();

    playedCard = new Card("r", 3, "none"); // the card about to be played (not visible to player)

    map.put("reply", "built");
    map.put("card", playedCard.getColor() + playedCard.getNumber());
    map.put("replaced", "true");
    cont.getGameState().setTurn(0); // set turn to self

    oldCardCount = cont.getGameState().getCardsRemaining();
    oldTurn = cont.getGameState().getTurn();
    oldScore = cont.getGameState().getScore();

    madeMove.clear();
    madeMove.put("action", "play");
    madeMove.put("position", "4");
    cont.getEventStack().add(madeMove);

    cont.evalEvent(map); // server tells you your card play was accepted and tells you the card

    newTable = cont.getGameState().getCardsOnTable();
    colIdx = colNumToIdx.get(playedCard.getColor());
    numIdx = colNumToIdx.get(playedCard.getNumber() + ""); // have to convert to string again for hm

    // Built(reply): Turn ended
    t++;
    if (oldTurn == cont.getGameState().getTurn() || cont.getGameState().getPlayers().get(oldTurn)
        .getTurn()) {
      System.out.println("Test#" + t
          + ": evalEvent(built reply): the turn was not updated after the discard");
      testsPassed[t] = false;
    }

    // Built(reply): Card count decremented
    t++;
    if (oldCardCount != 0 && oldCardCount - 1 != cont.getGameState()
        .getCardsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(built reply): the card count was not decremented");
      testsPassed[t] = false;
    }

    // Built(reply): Timer reset
    t++;
    if (!(cont.getGameState().getTimePerTurn() - cont.getGameState().getTimeRemaining() < 2)) {
      System.out.println("Test#" + t
          + ": evalEvent(built reply): the timer was not reset after the move");
      testsPassed[t] = false;
    }

    // Built(Reply): check if the score went up
    t++;
    if (oldScore + 1 != cont.getGameState().getScore()) {
      System.out.println("Test#" + t
          + ": evalEvent(built reply): the score was not increased on a valid play");
      testsPassed[t] = false;
    }

    // Built(Reply): check if cardsOnTable was updated
    t++;
    if (!cont.getGameState().getCardsOnTable()[numIdx][colIdx]) {
      System.out.println("Test#" + t
          + ": evalEvent(built reply): the cards on table did not update on a valid play");
      testsPassed[t] = false;
    }

    // Built(Reply): check if the previous card on the top of the stack was set to false in the 2d
    // array omit if there weren't any cards for the stack before the move
    t++;
    if (numIdx != 0 && newTable[numIdx - 1][colIdx]) {
      System.out.println("Test#" + t
          + ": evalEvent(built reply): the bool for the previous top card still set to true in "
          + "cardsOnTable 2d array on a valid play");
      testsPassed[t] = false;
    }

    // Built(Reply): will again assume the previous tests of built(reply) about other
    // model fields have passed
    map.clear();
    playedCard = new Card("y", 5, "none");
    map.put("reply", "built");
    map.put("card", playedCard.getColor() + playedCard.getNumber());
    map.put("replaced", "true");
    oldTokenCount = cont.getGameState().getHintsRemaining();

    madeMove.clear();
    madeMove.put("action", "play");
    madeMove.put("position", "4");
    cont.getEventStack().add(madeMove);

    cont.evalEvent(map);

    // Built(Reply): when the card played was of rank 5 check that the hints were incremented
    t++;
    if (playedCard.getNumber() == 5 && oldTokenCount < 8 && oldTokenCount + 1 != cont.getGameState()
        .getHintsRemaining()) {
      testsPassed[t] = false;
      System.out.println("Test#" + t
          + ": evalEvent(built reply): hints not incremented when rank 5 card was played");
    }

    // Burned(reply)

    map.clear();

    cont.getGameState().setTurn(0); // set turn to self
    Card burnedCard = new Card("w", 3, "none");
    map.put("reply", "burned");
    map.put("card", burnedCard.getColor() + burnedCard.getNumber());

    oldTurn = cont.getGameState().getTurn();
    oldFuse = cont.getGameState().getFusesRemaining();
    oldCardCount = cont.getGameState().getCardsRemaining();
    oldDiscard = cont.getGameState().getDiscardPile().getLast();

    madeMove.clear();
    madeMove.put("action", "play");
    madeMove.put("position", "4");
    cont.getEventStack().add(madeMove);

    cont.evalEvent(map); // server tells you that you played an invalid card, the card was w3

    // Burned(reply): Turn ended
    t++;
    if (oldTurn == cont.getGameState().getTurn() || cont.getGameState().getPlayers().get(oldTurn)
        .getTurn()) {
      System.out.println("Test#" + t
          + ": evalEvent(burned reply): the turn was not updated after the play");
      testsPassed[t] = false;
    }

    // Burned(reply): Card count decremented
    t++;
    if (oldCardCount != 0 && oldCardCount - 1 != cont.getGameState()
        .getCardsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(burned reply): the card count was not decremented");
      testsPassed[t] = false;
    }

    // Burned(reply): Timer reset
    t++;
    if (!(cont.getGameState().getTimePerTurn() - cont.getGameState().getTimeRemaining() < 2)) {
      System.out.println("Test#" + t
          + ": evalEvent(burned reply): the timer was not reset after the move");
      testsPassed[t] = false;
    }

    // Burned(reply): fuses decremented
    t++;
    if (oldFuse - 1 != cont.getGameState().getFusesRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(burned reply): the fuses were not decremented on an invalid play");
      testsPassed[t] = false;
    }

    // Burned(reply): top card on discard pile
    t++;
    // the card that is now at the top of the discard pile
    newDiscard = cont.getGameState().getDiscardPile().getLast();
    if (!(newDiscard.getNumber().equals(burnedCard.getNumber()) && newDiscard.getColor()
        .equals(burnedCard.getColor()))) {
      System.out.println("Test#" + t
          + ": evalEvent(burned reply): the top card on the discard pile did not get updated");
      testsPassed[t] = false;
    }

    // Burned(reply): Old discard not in second position
    t++;
    // this test makes sure that the card that was previously on top of the discard pile is now in
    // the second position (1st idx)
    if (oldDiscard != null) {
      oldTop = cont.getGameState().getDiscardPile().size() - 2; // the index of the old top discard
      boolean rank = cont.getGameState().getDiscardPile().get(oldTop).getNumber()
          .equals(oldDiscard.getNumber());
      boolean suit = cont.getGameState().getDiscardPile().get(oldTop).getColor()
          .equals(oldDiscard.getColor());

      if (rank && suit) {
        assert true; // means that the condition is satisfied and the test passes,
        // need this here to avoid compiler warning
      } else {
        System.out.println("Test#" + t
            + ": evalEvent(burned reply): the old discard not in the second position");
        testsPassed[t] = false;
      }
    }

    // Inform(notice to receiving player)
    map.clear();

    map.put("notice", "inform");
    map.put("rank", "2");
    map.put("cards", "[ false, false, false, false, true ]");
    cont.gameState.setTimeRemaining(10);
    oldTurn = cont.getGameState().getTurn();
    oldTokenCount = cont.getGameState().getHintsRemaining();

    cont.evalEvent(map); // you get informed about your rank 2 card

    // Inform(notice to receiving player): Turn ended
    t++;
    if (oldTurn == cont.getGameState().getTurn() || cont.getGameState().getPlayers().get(oldTurn)
        .getTurn()) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(receiving player)): the turn was not updated after the discard");
      testsPassed[t] = false;
    }

    // Inform(notice to receiving player): Timer reset
    t++;
    if (!(cont.getGameState().getTimePerTurn() - cont.getGameState().getTimeRemaining() < 2)) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(receiving player)): the timer was not reset after the move");
      testsPassed[t] = false;
    }

    // Inform(notice to receiving player): Hint count decremented
    t++;
    if (oldTokenCount - 1 != cont.getGameState().getHintsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(receiving player)): the number of hints wasn't decremented "
          + "after the hint");
      testsPassed[t] = false;
    }

    // Inform(notice to receiving player): update card (rank)
    t++;
    if (cont.getGameState().getPlayers().get(0).getCard(4).getNumber() != 2) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(receiving player)): the card that was given rank information about "
          + "was not updated");
      testsPassed[t] = false;
    }

    // will assume that gameState fields change appropriately at this point and only test the suit
    map.clear();

    map.put("notice", "inform");
    map.put("suit", "y");
    map.put("cards", "[ false, false, false, false, true ]");

    cont.evalEvent(map);

    // Inform(receiving player): update card (suit)
    t++;
    if (!(cont.getGameState().getPlayers().get(0).getCard(4).getColor().equals("y"))) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(receiving player)): the card that was given suit information about "
          + "was not updated");
      testsPassed[t] = false;
    }

    // Inform(other players):
    // have to setup a new game scenario here as receiving info about a hint that wasn't given to
    // you or by requires a minimum of 3 players
    players = new ArrayList<>();

    Card[] hintHand1 = {
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
        new Card("?", 0, "none"),
    };

    Card[] hintHand2 = {
        new Card("b", 1, "none"),
        new Card("b", 3, "none"),
        new Card("b", 5, "none"),
        new Card("g", 2, "none"),
        new Card("w", 4, "none"),
    };

    Card[] hintHand3 = {
        new Card("b", 1, "none"),
        new Card("b", 3, "none"),
        new Card("g", 1, "none"),
        new Card("g", 2, "none"),
        new Card("r", 1, "none"),
    };

    Player hintPlayer1 = new Player(hintHand1, false); // you
    Player hintPlayer2 = new Player(hintHand2, false); // player to receive hint
    Player hintPlayer3 = new Player(hintHand3, true);  // player to give the hint

    players = new ArrayList<>();
    players.add(hintPlayer1);
    players.add(hintPlayer2);
    players.add(hintPlayer3);

    gameStateModel = new GameState(players, "none", 60);

    gameStateModel.setHintsRemaining(5); // set to less than 8 so that a hint is a possible action
    gameStateModel.setTurn(2); // set the turn to player 3 (index 2)

    clientStateModel = new ClientState(players);
    menusOpen.put("hintAnimation", false);
    clientStateModel.setMenusOpen(menusOpen);

    cont = new HanabiController(gameStateModel, clientStateModel, socket, buf, in, out, event,
        events, curState);

    map.clear();
    map.put("notice", "inform");
    map.put("player", "2");
    map.put("suit", "w");

    oldTurn = cont.getGameState().getTurn();
    oldTokenCount = cont.getGameState().getHintsRemaining();

    // TODO again setting the time remaining other than 0, will delete after implementation exists
    cont.getGameState().setTimeRemaining(10);
    cont.evalEvent(map); // p3 tells p2 about white suit cards

    // Inform(other player): Turn ended
    t++;
    if (oldTurn == cont.getGameState().getTurn() || cont.getGameState().getPlayers().get(oldTurn)
        .getTurn()) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(other player)): the turn was not updated after the hint");
      testsPassed[t] = false;
    }

    // Inform(other player): Timer reset
    t++;
    if (!(cont.getGameState().getTimePerTurn() - cont.getGameState().getTimeRemaining() < 2)) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(other player)): the timer was not reset after the hint");
      testsPassed[t] = false;
    }

    // Inform(other player): Hint count decremented
    t++;
    if (oldTokenCount - 1 != cont.getGameState().getHintsRemaining()) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(other player)): the number of hints wasn't decremented after hint");
      testsPassed[t] = false;
    }

    // Inform(other player): Animation drawn
    t++;
    if (!cont.getClientState().getMenusOpen().get("hintAnimation")) {
      System.out.println("Test#" + t
          + ": evalEvent(inform(other player)): the hint animation was not drawn");
      testsPassed[t] = false;
    }

    // Game ends
    map.clear();
    map.put("notice", "game ends");
    cont.getClientState().getMenusOpen().replace("gameEnds", false);

    cont.evalEvent(map); // server tells you that the game has ended

    // Game ends: game end menu is shown
    t++;
    if (!cont.getClientState().getMenusOpen().get("gameEnds")) {
      System.out.println("Test#" + t
          + ": evalEvent(game ends): game ends menu was not shown");
      testsPassed[t] = false;
    }

    // Game ends: connection closed
    t++;
    if (!cont.getSocket().isClosed()) {
      System.out.println("Test#" + t
          + ": evalEvent(game ends): the socket was not closed");
      testsPassed[t] = false;
    }

    // Game ends: main menu
    t++;
    try {
      Thread.sleep(6000); // wait until the score menu is gone
    } catch (InterruptedException e) {
      System.out.println("Test#" + t
          + ": evalEvent(game ends): testing of main menu interrupted");
      testsPassed[t] = false;
    }
    if (!cont.getClientState().getMenusOpen().get("mainMenu")) {
      System.out.println("Test#" + t
          + ": evalEvent(game ends): user not redirected to the main menu after the game ended");
      testsPassed[t] = false;
    }

    // Game ends: game state to null check
    t++;
    if (cont.getGameState() != null) {
      System.out.println("Test#" + t
          + ": evalEvent(game ends): game state not reset to null after the game ends");
      testsPassed[t] = false;
    }

    // Pack Stream

    map.clear();
    // the message to send to the server
    map.put("action", "hint");
    map.put("player", "1");
    map.put("rank", "2");

    String jsonMessage = cont.packStream(map);
    // the json string to compare against
    String resString = "{\"action\":\"hint\",\"rank\":\"2\",\"player\":\"1\"}";

    t++;
    if (!jsonMessage.equals(resString)) {
      System.out.println("Test#" + t
          + ": packStream: JSONObject wasn't properly converted into a string");
      testsPassed[t] = false;
    }

    // close the socket for next test
    try {
      socket.close();
    } catch (IOException | NullPointerException e) {
      System.out.println(e.getMessage());
    }

    // counting the amount of failed tests
    int failures = 0;
    for (boolean bool : testsPassed) {
      if (!bool) {
        failures++;
      }
    }
    System.out.println("Finished Unit Tests: " + failures + "/" + (t + 1) + " tests failed.");

  }
}


