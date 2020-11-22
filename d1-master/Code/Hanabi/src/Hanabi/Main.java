package Hanabi;

import static java.lang.Integer.parseInt;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedList;
import org.ietf.jgss.ChannelBinding;
import org.json.simple.JSONObject;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

//    StartMenu sm = new StartMenu(1200, 750);
//    Scene scene = new Scene(sm);
//    primaryStage.setScene(scene);


    HanabiView view = new HanabiView(1200, 750);
    HanabiController controller = new HanabiController();

    GameState tempGameState = new GameState(2, 2, "none");
    controller.setGameState(tempGameState);

    Scene scene = new Scene(view);
    primaryStage.setScene(scene);


    Socket socket = new Socket("gpu2.usask.ca", 10219); // the Hanabi server
    DataInputStream in = new DataInputStream(socket.getInputStream()); // input stream
    PrintStream out = new PrintStream(socket.getOutputStream()); // output stream
    BufferedReader buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    controller.setSocket(socket);
    controller.setBufferedReader(buf);
    controller.setInputStream(in);
    controller.setOutputStream(out);

    // controller now setup to create game
    String nsid = "mek838";
    String rainbow = "none";
    String md5 = "5899f88f3fcab344ae136d9eda597538";
    int players = 2;
    int timeout = 119;
    //String toSend = controller.createGame(nsid, players, timeout, rainbow, md5);
    String toSend = controller.createGame(nsid, players, timeout, rainbow, md5);
    controller.sendMove(toSend); // sending the message

    /// so we want to read the result back from the server
    for (JSONObject msg : controller.receiveServerData()) {
      controller.evalEvent(msg);
    }





    // wait until the game is started then get the game state, set it as a global
    // gamestate for everyone to access, and start the program, yes this
    // busywait is ugly but for the time being it should work
    while(!controller.getGameState().getGameStarted()){
      // we have to keep reading from the server to see if the game has started
      for (JSONObject msg : controller.receiveServerData()) {
        controller.evalEvent(msg);
      }
    }




    // now that the game is started, the controller should have access to a consistent
    // gamestate and client that represents a real game,
    GameState gameState = controller.getGameState();
    ClientState clientState = controller.getClientState();
    // now that we have valid gamestates and clientstates, we can point the view towards them

    gameState.setTimeRemaining(5);

    // configure the view properly
    gameState.addToAllCard();
    gameState.addSubscriber(view);



    view.setGameState(gameState);
    view.setClientState(clientState);


    // give the view the access to the controller that it needs
    view.setHanabiController(controller);

    gameState.setCardCoordinates(view.canvasWidth, view.canvasHeight);
    gameState.setPlayDiscardBox(view.canvasWidth, view.canvasHeight);
    // so we can start our threads to run



    GameLoop(controller, view, gameState);
    primaryStage.show();



  }


  /**
   * This is the main gameloop thread it loops around and plays the game by looping around the
   * threads
   * @param myController a controller for the game
   * @param myView a view for the game
   * @param myGameState the game state of the game
   */
  public void GameLoop(HanabiController myController, HanabiView myView, GameState myGameState){

    Runnable updateView = () -> {
//      while(myController.getGameState().getGameStarted()) {
      myGameState.notifySubscribers();
      myView.drawGameTable();
      //System.out.println("You're in the view");
//      }
    };
    // Run the task in a background thread
    Thread viewThread = new Thread(updateView);
    // Terminate the running thread if the application exits
    viewThread.setDaemon(true);
    // Start the thread
    viewThread.start();


    Runnable AITask = () -> {
      // while the game is started
      if(myController.getGameState().getGameStarted()) {
        // and if the AI is on, we grab a move from it and send it to the server with
        // our controller
        if (myController.getGameState().getAIOn()) {

          HashMap<String, String> recentAIMove = new HashMap<>();
          recentAIMove = myController.getGameState().getMyAI().findBestMove();
          /// turn this move into JASON and send it
          ///myController.sendMove(recentAIMove);
        }
      }
    };
    // Run the task in a background thread
    Thread AIThread = new Thread(AITask);
    // Terminate the running thread if the application exits
    AIThread.setDaemon(true);
    // Start the thread
    AIThread.start();



    Runnable task = () -> {

      try { // while the game state is NOT the terminal state of the game
        while (!myController.getClientState().getMenusOpen().get("gameEnds")) {


          //System.out.println("You're in the controller");
          // grab the new stuff from the server
          LinkedList<JSONObject> messages = myController.receiveServerData();
          do { // update the game state
            JSONObject msg = messages.remove();
            myController.evalEvent(msg);
            viewThread.run();
            //AIThread.run();
          }
          while (!messages.isEmpty());
        }
      }
      catch(Exception e){
        System.out.println(e);
        }
      };
    // Run the task in a background thread
    Thread backgroundThread = new Thread(task);
    // Terminate the running thread if the application exits
    backgroundThread.setDaemon(true);
    // Start the thread
    backgroundThread.start();


  }



  public static void main(String[] args) {
    if (args.length > 1) {
      // command line launch


    } else {
      launch(args);
    }
  }

}

