package Hanabi;

public class PlayDiscardBox {
  double x, y;
  double width, height;

  public PlayDiscardBox(double x, double y, double width, double height){
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public boolean checkInBox(double mouseX, double mouseY){
    return mouseX>=x && mouseX<=x+width && mouseY>=y && mouseY<=y+height;
  }
}
