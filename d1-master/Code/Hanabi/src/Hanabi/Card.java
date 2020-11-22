package Hanabi;

public class Card {

  public String color;
  public Integer number;
  private Boolean clickable;
  private Boolean selected;
  private Boolean hovering;
  public double x, y, width, height;
  private CPS myCPS;
  private String myGameType;
  private double myDiscardEV;
  private double myPlayEV;
  public double initX, initY;


  // Constructor for card
  Card(String c, Integer n, String gameType){

    // put in some logic for testing that card is handed only valid values
    // assert that c belongs to {'R', 'B', 'G', 'Y', 'W'}
    // assert that n belongs to { 1, 2, 3, 4, 5 }
    color = c;
    number = n;
    myCPS = new CPS(gameType);
    myGameType = gameType;
    myDiscardEV = 0.0;
    myPlayEV = 0.0;
    hovering = false;
    selected = false;
    clickable = true;

  }

  public String getCardAsString(){
    return color + number;
  }


  public void receiveHint(boolean isPositive, String hint){
    if(isPositive){
      myCPS.receivePositiveHint(hint);
      if(hint.matches("[0-9]")){
        number = Integer.parseInt(hint);
      }
      else{
        color = hint;
      }
    }
    else{
      myCPS.receiveNegativeHint(hint);
    }
  }


  public void observeDraw(Card card){
    myCPS.observeDrawCPS(card);
  }


  public Card clone(){
    CPS myCPSCopy = myCPS.clone();
    Card myCopy = new Card(color, number, myGameType);
    myCopy.setMyCPS(myCPSCopy);
    return myCopy;
  }


  public void calculateMyEVs(GradientMatrix discardGradient, GradientMatrix playGradient){
    myPlayEV = discardGradient.elementwiseProductSum(myCPS);
    myDiscardEV = playGradient.elementwiseProductSum(myCPS);
  }

  public double getMyDiscardEV(){
    return myDiscardEV;
  }

  public double getMyPlayEV(){
    return myPlayEV;
  }


  public String getColor() {
    return color;
  }

  public Integer getNumber() {
    return number;
  }

  public CPS getMyCPS(){
    return myCPS;
  }

  public void setMyCPS(CPS cpsIn){
    myCPS = cpsIn;
  }


  public Boolean getClickable() {
    return clickable;
  }

  public Boolean getSelected() {
    return selected;
  }

  public Boolean getHovering() {
    return hovering;
  }

  public double getX() {
    return x;
  }

  public double getY() {

    return y;
  }

  public double getW() {

    return width;
  }

  public double getHeight() {

    return height;
  }

  public void setClickable(Boolean clickable) {
    this.clickable = clickable;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public void setHovering(Boolean hovering) {
    this.hovering = hovering;
  }

  public int[] cardToIndex(){
    int cardIndices[] = new int[2];
    switch (color){
      case "r":
        cardIndices[1] = 0;
        break;
      case "g":
        cardIndices[1] = 1;
        break;
      case "b":
        cardIndices[1] = 2;
        break;
      case "y":
        cardIndices[1] = 3;
        break;
      case "w":
        cardIndices[1] = 4;
        break;
      case "m":
        cardIndices[1] = 5;
        break;
      case "?":
        cardIndices[1] = -1;
        break;
//      default:
//        cardIndices[0] = number - 1;
//        break;
    }
    cardIndices[0] = number -1;

    return cardIndices;
  }


  //////////////////////// View methods ///////////////////////

  // ---------------------------Added By Hugh-----------------------------
  public String getPath(){
    String path = null;
    if (color == "?"){
      if (number == 0){
        path = "Assets/Cards/CARDBACK.png";
      }
      else{
        path = "Assets/Cards/U"+number+".png";
      }
    }
    else{
      if (number == 0){
        path = "Assets/Cards/"+color+"?.png";
      }
      else{
        path = "Assets/Cards/"+color+number+".png";
      }
    }
    return path;
  }

  public String getString(){
    String path = null;
    if (color == "?"){
      if (number == 0){
        path = "CARDBACK";
      }
      else{
        path = "U" + number.toString();
      }
    }
    else{
      if (number == 0){
        path = color.toUpperCase() + "?";
      }
      else{
        path = color.toUpperCase() + number.toString();
      }
    }
    return path;
  }

  public void setXY(double newX, double newY){
    this.x = newX;
    this.y = newY;

  }

  public void setOrigin(double x, double y){
    this.initX = x;
    this.initY = y;
  }

  public boolean checkHit(double clickX, double clickY){
    return clickX >= x && clickX <= x+width && clickY >= y && clickY <= y+ height;
  }

  public void moveCard(double dx, double dy){
    x += dx;
    y += dy;
  }

  // ---------------------------Added By Hugh-----------------------------

  public static void main(String args[]){
    // testing goes here
    System.out.print("Testing");
  }


}
