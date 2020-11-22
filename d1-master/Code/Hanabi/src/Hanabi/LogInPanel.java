package Hanabi;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class LogInPanel extends GridPane {
  public LogInPanel(double Width, double Height){
    setPrefSize(Width, Height);
    setAlignment(Pos.CENTER);

    Label userName = new Label("User Name: ");
    this.add(userName, 0, 1);

    TextField userTextField = new TextField();
    this.add(userTextField, 1, 1);

    Label pw = new Label("Secret Token: ");
    this.add(pw, 0, 2);

    PasswordField pwBox = new PasswordField();
    this.add(pwBox, 1, 2);
  }
}
