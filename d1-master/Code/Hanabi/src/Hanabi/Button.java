package Hanabi;

import javafx.util.Pair;

public class Button {

  private Boolean selected;
  private Boolean clickable;
  private Boolean hovering;
  private String text;
  private Pair location;
  private double x, y, w, h;

  /**
   *
   * @param newLocation the location that this button object will appear on the screen
   * @param newText the text field for the button
   */
  Button(Pair newLocation, String newText){
    location = newLocation;
    text = newText;
  }

  Button(Pair newLocation, String newText, double x, double y, double w, double h){
    location = newLocation;
    text = newText;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  // GETTERS AND SETTERS //
  public void setLocation(Pair location) {
    this.location = location;
  }

  public Pair getLocation() {
    return location;
  }

  public void setHovering(Boolean hovering) {
    this.hovering = hovering;
  }

  public Boolean getHovering() {
    return hovering;
  }

  public Boolean getClickable() {
    return clickable;
  }

  public void setClickable(Boolean clickable) {
    this.clickable = clickable;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public Boolean getSelected() {
    return selected;
  }

  public double getX() {
    return x;
  }

  public double getY() {

    return y;
  }

  public double getW() {

    return w;
  }

  public double getH() {

    return h;
  }

  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {

    this.y = y;
  }

  public void setW(double w) {

    this.w = w;
  }

  public void setH(double h) {

    this.h = h;
  }

  public static void main(String args[]){
    System.out.println("Testing module: Button");
  }
}
