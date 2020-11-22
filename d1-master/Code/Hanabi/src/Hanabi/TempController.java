package Hanabi;

import javafx.scene.input.MouseEvent;

public class TempController {

  double prevX, prevY;
  double initX, initY;

  private enum State {
    READY, DRAGGING, HINT
  }

  private State currentState;

  private GameState gameState;

  public TempController(){
    this.currentState = State.READY;
  }

  public void setGameState(GameState gameStateModel) {
    this.gameState = gameStateModel;
  }

  public void handlePressed(MouseEvent mouseEvent) {
    switch (currentState) {
      case READY:
        // hit detection
        System.out.println(mouseEvent.getX() + " " + mouseEvent.getY());
        boolean hit = gameState.checkHit(mouseEvent.getX(), mouseEvent.getY());
        if (hit) {
          gameState.setSelectedCard(gameState.whichCard(mouseEvent.getX(), mouseEvent.getY()));

          if (gameState.handContain(gameState.getPlayers().get(0).getHand(),
              (gameState.whichCard(mouseEvent.getX(), mouseEvent.getY())))){
            System.out.println("Card Hit!!");
            // set start location for drag

            prevX = mouseEvent.getX();
            prevY = mouseEvent.getY();

            initX = gameState.selectedCard.x;
            initY = gameState.selectedCard.y;

            gameState.setIsDragging();

            currentState = State.DRAGGING;
          }
          else{
            currentState = State.HINT;
          }

        }
        else{
          System.out.println("Card NOT Hit!!");
        }

        break;
    }
  }

  public void handleDrag(MouseEvent mouseEvent) {
    switch (currentState) {
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
        gameState.moveCard(gameState.selectedCard,dX, dY);
        // transition to new state: same state
        break;
    }
  }

  public void handleRelease(MouseEvent mouseEvent) {
    switch (currentState) {
      case DRAGGING:
        // context: none
        // side effects:
        // - set to no selection
        gameState.deSetIsDragging();

        if (gameState.playBox.checkInBox(mouseEvent.getX(), mouseEvent.getY())){
          //TODO
        }
        else if (gameState.discardBox.checkInBox(mouseEvent.getX(), mouseEvent.getY())){
          //TODO
        }

        else{
          gameState.setCardXY(gameState.selectedCard, initX, initY);
          gameState.selectedCard = null;
        }
        // transition to new state
        currentState = State.READY;
        break;

      case HINT:
        gameState.sethintXY(gameState.selectedCard);
        gameState.setHint();
        gameState.deSetHint();
        currentState = State.READY;
        break;
    }
  }

//    public void handleEntered(MouseEvent mouseEvent){
//        boolean hit = gameState.checkHit(mouseEvent.getX(), mouseEvent.getY());
//        System.out.println(mouseEvent.getX());
//        System.out.println(mouseEvent.getY());
//        if (hit) {
//            gameState.setHoveringCard(gameState.whichCard(mouseEvent.getX(), mouseEvent.getY()));
//            System.out.println("Hovering");
//        }
//        gameState.setCardHovering(gameState.hoveringCard);
//    }
//
//    public void handleExited(MouseEvent mouseEvent){
//        boolean hit = gameState.checkHit(mouseEvent.getX(), mouseEvent.getY());
//        if(!hit){
//            gameState.deSetCardHovering(gameState.hoveringCard);
//        }
//    }
}
