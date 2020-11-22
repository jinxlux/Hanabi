package Hanabi;

import java.util.HashMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class StartMenu extends BorderPane {

  Image image = new Image(Main.class.getResourceAsStream("Assets/Screens/titlescreens.png"));


  public StartMenu(double width, double height){
    //Create a new Hanabi View
    HanabiView view = new HanabiView(width, height);

    //Set BoarderPane default size and margin

    ImageView imageView = new ImageView(image);
    imageView.setFitWidth(width);
    imageView.setFitHeight(height);
    this.getChildren().add(imageView);
    this.setPadding(new Insets(50));
    setPrefSize(width, height);

    //Create a new text field for title
    Text title = new Text("HANABI");
    title.setFill(Color.WHITE);
    title.setFont(Font.font("Apple Chancery", FontWeight.NORMAL, 80));

    // Button for create a new game
    Button createButton = new Button("Create Game");
    createButton.setPrefSize(100, 20);
    createButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        StackPane root = new StackPane(view.drawCreateGame());
        Stage stage = (Stage)createButton.getScene().getWindow();
        stage.setTitle("Hanabi Game");
        Scene scene = new Scene(root, createButton.getScene().getWidth(), createButton.getScene().getHeight());
        stage.setScene(scene);
        stage.show();
      }
    });

    // Button for join an existing game
    Button joinButton = new Button("Join Game");
    joinButton.setPrefSize(100, 20);
    joinButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        StackPane root = new StackPane(view.drawJoinGame());
        Stage stage = (Stage)joinButton.getScene().getWindow();
        stage.setTitle("Hanabi Game");
        Scene scene = new Scene(root, joinButton.getScene().getWidth(), joinButton.getScene().getHeight());
        stage.setScene(scene);
        stage.show();
      }
    });

    // Button for changing game settings
    Button settingButton = new Button("Settings");
    settingButton.setPrefSize(100, 20);

    //Button for exit the program
    Button exitButton = new Button("Exit");
    exitButton.setPrefSize(100, 20);
    exitButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        Stage stage = (Stage)exitButton.getScene().getWindow();
        stage.close();
      }
    });

    //VBox to organize all buttons
    VBox vBox = new VBox();
    vBox.setAlignment(Pos.CENTER);
    vBox.setSpacing(10);

    vBox.getChildren().add(createButton);
    vBox.getChildren().add(joinButton);
    vBox.getChildren().add(settingButton);
    vBox.getChildren().add(exitButton);

    //Set all element to the BorderPane
    setTop(title);
    setAlignment(title, Pos.CENTER);
    setCenter(vBox);
  }
}
