package Hanabi;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class CreateGame extends GridPane {

  public CreateGame(double width, double height){
    HanabiView view = new HanabiView(width, height);

    setPrefSize(width, height);
    setAlignment(Pos.CENTER);
    setVgap(20);

    Label userName = new Label("NSID: ");
    this.add(userName, 0, 1);

    TextField userTextField = new TextField();
    this.add(userTextField, 1, 1);

    Label numberPlayer = new Label("Players: ");
    this.add(numberPlayer, 0, 2);

    ComboBox numberPlayerBox = new ComboBox();
    numberPlayerBox.setPrefWidth(170);
    numberPlayerBox.getItems().addAll("3", "4", "5");
    this.add(numberPlayerBox, 1, 2);

    Label rainbowCard = new Label("Rainbow Card: ");
    this.add(rainbowCard, 0, 3);

    ComboBox rainbowCardBox = new ComboBox();
    rainbowCardBox.getItems().addAll("Yes", "No");
    rainbowCardBox.setPrefWidth(170);
    this.add(rainbowCardBox, 1, 3);

    HBox buttonHBox = new HBox();
    buttonHBox.setSpacing(10);
    buttonHBox.setAlignment(Pos.BOTTOM_RIGHT);
    this.add(buttonHBox, 1,4);

    Button create = new Button("Create");
    buttonHBox.getChildren().add(create);

    Button cancel = new Button("Cancel");
    buttonHBox.getChildren().add(cancel);
    cancel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        StackPane root = new StackPane(view.drawStartMenu());
        Stage stage = (Stage)cancel.getScene().getWindow();
        stage.setTitle("Hanabi Game");
        Scene scene = new Scene(root, cancel.getScene().getWidth(), cancel.getScene().getHeight());
        stage.setScene(scene);
        stage.show();
      }
    });
  }
}
