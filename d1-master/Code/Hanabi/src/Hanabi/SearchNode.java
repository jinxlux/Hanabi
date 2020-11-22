package Hanabi;


import javafx.util.Pair;

import java.util.LinkedList;

public class SearchNode {


    GameState myState;





    SearchNode(GameState gameState, Double value ){


        myState = gameState;

    }

    public GameState getMyState(){
        return myState;
    }


}