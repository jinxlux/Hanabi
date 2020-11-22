package Hanabi;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class JoinGame extends GridPane {

  public JoinGame(double width, double height){
    HanabiView view = new HanabiView(width, height);

    setPrefSize(width, height);
    setAlignment(Pos.CENTER);
    setVgap(20);

    Label userName = new Label("NSID: ");
    this.add(userName, 0, 1);

    TextField userTextField = new TextField();
    this.add(userTextField, 1, 1);

    Label gameID = new Label("Game ID: ");
    this.add(gameID, 0, 2);

    TextField gameIDTextField = new TextField();
    this.add(gameIDTextField, 1, 2);

    Label secretToken = new Label("Secret Token: ");
    this.add(secretToken, 0, 3);

    TextField secretTokenTextField = new TextField();
    this.add(secretTokenTextField, 1, 3);


    HBox buttonHBox = new HBox();
    buttonHBox.setSpacing(10);
    buttonHBox.setAlignment(Pos.BOTTOM_RIGHT);
    this.add(buttonHBox, 1,4);

    javafx.scene.control.Button create = new javafx.scene.control.Button("Join");
    buttonHBox.getChildren().add(create);

    javafx.scene.control.Button cancel = new Button("Cancel");
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
