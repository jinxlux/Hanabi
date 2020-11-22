package Hanabi;

import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class HanabiView extends Pane implements GameStateListener {

  /**
   * @param height This double keeps the information about the height of the screen
   * @param width This double keeps the information about the width of the screen
   * @param gameState This field keeps track of the state of the game and its fields
   * @param clientStateModel This field keeps track of the state of the client and its fields
   * @param controller The controller from MVC that is responsible for communicating with the server
   * and get- ting/setting information from/to the Model and the user
   * @param canvas Canvas is an image that can be drawn on using a set of graphics commands provided
   * by a GraphicsContext
   * @param graphicsContext This field is used to issue draw calls to the Canvas.
   */

  private HashMap<String, Image> myImages = new HashMap<>();

  private double width, height;
  private GameState gameState;
  private ClientState clientState;
  private HanabiController tempController;
  private Canvas canvas;
  private GraphicsContext graphicsContext;


  double canvasWidth;
  double canvasHeight;

  public HanabiView(double newWidth, double newHeight) {

    width = newWidth;
    height = newHeight;

    canvasWidth = width - 200;
    canvasHeight = height;

    canvas = new Canvas(canvasWidth, canvasHeight);
    setStyle("-fx-background-color: white");
    graphicsContext = canvas.getGraphicsContext2D();
    getChildren().add(canvas);

    String[] suits = {"R", "G", "B", "Y", "W"};
    String[] ranks = {"1", "2", "3", "4", "5", "?"};

    for (int i = 0; i < suits.length; i++) {
      for (int j = 0; j < ranks.length; j++) {
        Image tableCard = new Image(
            Main.class.getResourceAsStream("Assets/Cards/" + suits[i] + ranks[j] + ".png"));
        myImages.put(suits[i] + ranks[j], tableCard);
      }
    }
    Image cardback = new Image(Main.class.getResourceAsStream("Assets/Cards/CARDBACK.png"));
    myImages.put("CARDBACK", cardback);

    for (int i = 0; i < 5; i++) {
      Image tableCard = new Image(
          Main.class.getResourceAsStream("Assets/Cards/U" + ranks[i] + ".png"));
      myImages.put("U" + (i + 1), tableCard);
    }

    Image bg = new Image(Main.class.getResourceAsStream("Assets/Screens/background.png"));
    myImages.put("Background", bg);

  }

  public void setGameState(GameState newGameState) {
    this.gameState = newGameState;

  }

  public void setClientState(ClientState newClientState) {
    this.clientState = newClientState;
  }

  public void setHanabiController(HanabiController newController) {
    this.tempController = newController;
    setOnMousePressed(tempController::handlePressed);
    setOnMouseDragged(tempController::handleDrag);
    setOnMouseReleased(tempController::handleRelease);
//        setOnMouseEntered(tempController::handleEntered);
//        setOnMouseExited(tempController::handleExited);
  }




  /**
   * Draws the initial selection screen with Join and Create game options
   */
  public StartMenu drawStartMenu() {
    return new StartMenu(width, height);
  }

  /**
   * Draws the Interface for creating a game
   */
  public CreateGame drawCreateGame() {
    return new CreateGame(width, height);
  }

  /**
   * Draws the Interface for joining a game
   */
  public JoinGame drawJoinGame() {
    return new JoinGame(width, height);
  }

  /**
   * Draws the card piles that are face up on the table along with all players’ cards
   */
//    public void reDrawSingleCard(Card card){
//        selectedCard= new Image(Main.class.getResourceAsStream(card.getPath()));
//        graphicsContext.drawImage(selectedCard, card.x, card.y, card.width, card.height);
//    }
  public void drawGameTable() {
    //System.out.println("Draw game table 2 times??");
//        double canvasWidth = width - 200;
//        double canvasHeight = height;
//
//        canvas = new Canvas(canvasWidth, canvasHeight);
//        setStyle("-fx-background-color: seagreen");
//        graphicsContext = canvas.getGraphicsContext2D();
//        this.setPrefSize(canvasWidth, canvasHeight);

    this.gameState = this.tempController.getGameState();

    graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);
    Image back = myImages.get("Background");
    graphicsContext.drawImage(back, 0, 0, width, height);

    double tableWidth = 4 * (canvasWidth / 6);
    double tableHeight = 4 * (canvasHeight / 6);

    String tempColor = null;
    int tempNumber = 0;
    Image tableCard;
    double tempX = 0;
    double tempY = 0;
    double marginXY = 20;
    double marginWH = 40;

    for (int i = 0; i < gameState.getCardsOnTable().length; i++) {
      for (int j = 0; j < gameState.getCardsOnTable()[i].length; j++) {
        if (gameState.getCardsOnTable()[i][j]) {
          switch (j) {
            case 0:
              tempColor = "R";
              tempX = canvasWidth / 6 + marginXY;
              tempY = canvasHeight / 6 + marginXY;
              tempNumber = i + 1;
              tableCard = myImages.get(tempColor + tempNumber);
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;
            case 1:
              tempColor = "G";
              tempX = canvasWidth / 6 + tableWidth / 3 + marginXY;
              tempY = canvasHeight / 6 + marginXY;
              tempNumber = i + 1;
              tableCard = myImages.get(tempColor + tempNumber);
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;
            case 2:
              tempColor = "B";
              tempX = canvasWidth / 6 + tableWidth / 3 * 2 + marginXY;
              tempY = canvasHeight / 6 + marginXY;
              tempNumber = i + 1;
              tableCard = myImages.get(tempColor + tempNumber);
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;
            case 3:
              tempColor = "Y";
              tempX = canvasWidth / 6 + tableWidth / 6 + marginXY;
              tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
              tempNumber = i + 1;
              tableCard = myImages.get(tempColor + tempNumber);
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;
            case 4:
              tempColor = "W";
              tempX = canvasWidth / 6 + tableWidth / 6 * 3 + marginXY;
              tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
              tempNumber = i + 1;
              tableCard = myImages.get(tempColor + tempNumber);
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;
            //case 5:
            //tempColor = "M";
          }
        } else if (!gameState.getCardsOnTable()[0][j]) {
          tableCard = myImages.get("CARDBACK");
          switch (j) {
            case 0:
              tempX = canvasWidth / 6 + marginXY;
              tempY = canvasHeight / 6 + marginXY;
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;

            case 1:
              tempX = canvasWidth / 6 + tableWidth / 3 + marginXY;
              tempY = canvasHeight / 6 + marginXY;
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;

            case 2:
              tempX = canvasWidth / 6 + tableWidth / 3 * 2 + marginXY;
              tempY = canvasHeight / 6 + marginXY;
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;

            case 3:
              tempX = canvasWidth / 6 + tableWidth / 6 + marginXY;
              tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;

            case 4:
              tempX = canvasWidth / 6 + tableWidth / 6 * 3 + marginXY;
              tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
              graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                  tableHeight / 2 - marginWH);
              break;
          }
        }
      }
    }

    //gameState.setPlayDiscardBox(canvasWidth, canvasHeight);

    PlayDiscardBox playBox = gameState.playBox;
    PlayDiscardBox discardBox = gameState.discardBox;

    if (gameState.isDargging) {
      graphicsContext.setFill(Color.LIGHTYELLOW);
      graphicsContext.setGlobalAlpha(0.5);
      graphicsContext.fillRect(playBox.x, playBox.y, playBox.width, playBox.height);
      graphicsContext.setFill(Color.BLACK);
      graphicsContext.fillText("PLAY", playBox.x + playBox.width / 2,
          playBox.y + playBox.height / 2);
      graphicsContext.setTextAlign(TextAlignment.CENTER);

      graphicsContext.setFill(Color.LIGHTGRAY);
      graphicsContext.fillRect(discardBox.x, discardBox.y, discardBox.width, discardBox.height);
      graphicsContext.setFill(Color.BLACK);
      graphicsContext.fillText("DISCARD", discardBox.x + discardBox.width / 2,
          discardBox.y + discardBox.height / 2);
      graphicsContext.setTextAlign(TextAlignment.CENTER);
    } else {
      for (int i = 0; i < gameState.getCardsOnTable().length; i++) {
        for (int j = 0; j < gameState.getCardsOnTable()[i].length; j++) {
          if (gameState.getCardsOnTable()[i][j]) {
            switch (j) {
              case 0:
                tempColor = "R";
                tempX = canvasWidth / 6 + marginXY;
                tempY = canvasHeight / 6 + marginXY;
                tempNumber = i + 1;
                tableCard = myImages.get(tempColor + tempNumber);
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;
              case 1:
                tempColor = "G";
                tempX = canvasWidth / 6 + tableWidth / 3 + marginXY;
                tempY = canvasHeight / 6 + marginXY;
                tempNumber = i + 1;
                tableCard = myImages.get(tempColor + tempNumber);
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;
              case 2:
                tempColor = "B";
                tempX = canvasWidth / 6 + tableWidth / 3 * 2 + marginXY;
                tempY = canvasHeight / 6 + marginXY;
                tempNumber = i + 1;
                tableCard = myImages.get(tempColor + tempNumber);
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;
              case 3:
                tempColor = "Y";
                tempX = canvasWidth / 6 + tableWidth / 6 + marginXY;
                tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
                tempNumber = i + 1;
                tableCard = myImages.get(tempColor + tempNumber);
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;
              case 4:
                tempColor = "W";
                tempX = canvasWidth / 6 + tableWidth / 6 * 3 + marginXY;
                tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
                tempNumber = i + 1;
                tableCard = myImages.get(tempColor + tempNumber);
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;
              //case 5:
              //tempColor = "M";
            }
          } else if (!gameState.getCardsOnTable()[0][j]) {
            tableCard = myImages.get("CARDBACK");
            switch (j) {
              case 0:
                tempX = canvasWidth / 6 + marginXY;
                tempY = canvasHeight / 6 + marginXY;
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;

              case 1:
                tempX = canvasWidth / 6 + tableWidth / 3 + marginXY;
                tempY = canvasHeight / 6 + marginXY;
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;

              case 2:
                tempX = canvasWidth / 6 + tableWidth / 3 * 2 + marginXY;
                tempY = canvasHeight / 6 + marginXY;
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;

              case 3:
                tempX = canvasWidth / 6 + tableWidth / 6 + marginXY;
                tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;

              case 4:
                tempX = canvasWidth / 6 + tableWidth / 6 * 3 + marginXY;
                tempY = canvasHeight / 6 + tableHeight / 2 + marginXY;
                graphicsContext.drawImage(tableCard, tempX, tempY, tableWidth / 3 - marginWH,
                    tableHeight / 2 - marginWH);
                break;
            }
          }
        }
      }
    }

    gameState.setTurnBox(canvasWidth, canvasHeight);
    graphicsContext.setGlobalAlpha(0.5);
    graphicsContext.setFill(Color.BROWN);
    graphicsContext.fillRect(gameState.turnBox.x, gameState.turnBox.y,
        gameState.turnBox.width, gameState.turnBox.height);

    graphicsContext.setGlobalAlpha(1);

    //gameState.setCardCoordinates(canvasWidth, canvasHeight);

    for (int i = 0; i < gameState.getPlayers().size(); i++) {
      for (Card card : gameState.getPlayers().get(i).getHand()) {
        Image currentCard = myImages.get(card.getString());
        if (card.getHovering()) {
          graphicsContext.drawImage(currentCard, card.x, card.y, card.width, card.height);
        } else {
          graphicsContext
              .drawImage(currentCard, card.x + 10, card.y + 10, card.width - 20, card.height
                  - 20);
        }


      }
    }

    if (gameState.hint) {
      Button hintButton = new Button("Give Hint");
      hintButton.setLayoutX(gameState.hintX);
      hintButton.setLayoutY(gameState.hintY);
      this.getChildren().add(hintButton);

      Pane all = this;
      hintButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
          all.getChildren().remove(hintButton);
        }
      });
    }

    VBox sideVBox = new VBox();
    sideVBox.setLayoutX(width - 150);
    sideVBox.setLayoutY(0);

    javafx.scene.control.Button discardPile = new Button("Discards");
    discardPile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        StackPane root = new StackPane();
        Canvas discardCanvas = new Canvas(800, 600);
        GraphicsContext disGraphicsContext = discardCanvas.getGraphicsContext2D();
        root.getChildren().add(discardCanvas);

        double discardX = 55;
        double discardY = 55;
        for (int i = 0; i < gameState.getDiscardPile().size(); i++) {
          Image discard = myImages.get(gameState.getDiscardPile().get(i).getString());
          disGraphicsContext.drawImage(discard, discardX, discardY, 65, 95);
          if (discardX == 685) {
            discardX = 55;
            discardY += 100;
          } else {
            discardX += 70;
          }
        }

        Scene scene = new Scene(root, 800, 600);
        Stage stage = new Stage();
        stage.setTitle("Discard Pile");
        stage.setScene(scene);
        stage.show();
      }
    });

    Label time = new Label("Time Remaining: ");
    Label timeRem = new Label(gameState.getTimeRemaining().toString());

    Label cardLeft = new Label("Card Left: ");
    Label cardRem = new Label(gameState.getCardsRemaining().toString());

    Label hint = new Label("Hint Token: ");
    Label hintRem = new Label(gameState.getHintsRemaining().toString());

    Label fuses = new Label("Fuses: ");
    Label fusesRem = new Label(gameState.getFusesRemaining().toString());

    sideVBox.getChildren().addAll(discardPile, time, timeRem, cardLeft, cardRem,
        hint, hintRem, fuses, fusesRem);
    sideVBox.setAlignment(Pos.CENTER);

    try { //TODO Make this not janky
      this.getChildren().add(sideVBox);
    } catch (Exception e) {

    }
  }


  /**
   * This method is used for notifying the View about changes in the Model and redrawing only the
   * changed parts, which are identified by the Event argument.
   */
  public void modelChanged() {
    drawGameTable();
  }

  public static void main(String[] args) {

  }
}
