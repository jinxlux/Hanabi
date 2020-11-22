package Hanabi;

public class Hint {

  String type;
  Boolean[] matches;
  Double myEV;

  Hint(String myType, Boolean[] myMatches){
    type = myType;
    matches = myMatches;
    myEV = 0.0;
  }

  /**
   * Calculates the expected value of a hint by giving the hint to a slave then checking
   * the slave to see how much he wants to play his card
   * @param CPTs The card
   * @param validMatrix the current validity matrix
   * @param playWeight the weight of plays over other moves
   */
  public void calculateEV(ConditionalProbabilityTable[] CPTs,
      Double[][] validMatrix,
      Double playWeight ){

    Double currentPlayEVsum = 0.0;
    Double postPlayEVsum = 0.0;
    Integer CPTLen = CPTs.length;

    Integer is = CPTs[0].CPT[0].length;
    Integer js = CPTs[0].CPT.length;

    // copy all of the current EVs over to keep track of them
    for(int i = 0; i < CPTLen; i++){
      currentPlayEVsum += CPTs[i].getPlayEV();
    }

     // Make a deep copy of the CPT passed in so we can do operations without altering
    // the original
    ConditionalProbabilityTable[] calcCPTs = new ConditionalProbabilityTable[CPTLen];
    for(int i = 0; i < CPTLen; i++){
      calcCPTs[i] = new ConditionalProbabilityTable();
    }
    for(int c = 0; c < CPTLen; c++) {
      for (int i = 0; i < is; i++) {
        for (int j = 0; j < js; j++) {
          calcCPTs[c].getCPT()[i][j] = CPTs[c].getCPT()[i][j];
          calcCPTs[c].CPS[i][j] = CPTs[c].CPS[i][j];
        }

      }
      calcCPTs[c].cardsLeft = CPTs[c].cardsLeft;
    }

    for(int i = 0; i < CPTLen; i++){
      if(matches[i]){
        //affirm the positive hint
        calcCPTs[i].receiveHint(type);
      }
      else{
        // affirm the negative hint
        calcCPTs[i].observeHint(type);
      }
    }

    for(int i = 0; i < CPTLen; i++){
      calcCPTs[i].expectedValuePlay(validMatrix);
    }

    for(int i = 0; i < CPTLen; i++){
      postPlayEVsum += playWeight * calcCPTs[i].getPlayEV();  // MAGIC NUMBER COULD GO HERE FOR OPTIMIZATION
    }

    Double diffPlayEV = Math.abs(currentPlayEVsum - postPlayEVsum);

    myEV = diffPlayEV;
  }


  @Override
  public String toString() {
    String s = "";
    for(int i = 0; i < 4; i++){
      if(matches[i]){
        s += "t";
      }
      else{
        s += "f";
      }
    }
    return type + s;
  }

  public static void main(String[] args){


  }

}
