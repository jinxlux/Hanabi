package Hanabi;

import java.util.Random;

public class AIOptimizer {

  AIOptimizer(){

  }


  public void Annealer() {

    for (int fx = 0; fx < 100; fx++) {

      Double[] Best = new Double[12];
      Integer Total = 0;
      Double Average;
      Double BestSoFar = 7.0;

      Double[] bestVector = new Double[5];
      Double[] validityVector = new Double[5];
      Double[] badVector = new Double[5];
      Double[] discardVector = new Double[5];
      Random random = new Random();

      Best[0] = 1.0770005878706448;
      Best[1] = 0.7839725968709069;
      Best[2] = 0.14931960392048166;
      Best[3] = 0.6135194803337682;
      Best[4] = 0.772361333203518;
      Best[5] = 1.038587176709482;
      Best[6] = 0.7566280744922742;
      Best[7] = 2.1784940909092505;
      Best[8] = 0.6017453761645587;
      Best[9] = 0.0662712834578455;
      bestVector[0] = 1.2887331005607592;
      bestVector[1] = 0.006938162050411617;
      bestVector[2] = 0.03586244595951221;
      bestVector[3] = 0.023457729720233817;
      bestVector[4] = 0.23132973375547553;
      badVector[0] = 0.04252812372446005;
      badVector[1] = 0.22204632403823016;
      badVector[2] = 0.04696525203347631;
      badVector[3] = 0.3476064662060657;
      badVector[4] = 0.010704392465602414;

      Double[] annealingSchedule = new Double[1001];

      for (int i = 799; i < 900; i++) {
        annealingSchedule[i] = 0.0005;
      }

      for (int i = 699; i < 800; i++) {
        annealingSchedule[i] = 0.001;
      }

      for (int i = 599; i < 700; i++) {
        annealingSchedule[i] = 0.002;
      }

      for (int i = 499; i < 600; i++) {
        annealingSchedule[i] = 0.004;
      }

      for (int i = 399; i < 500; i++) {
        annealingSchedule[i] = 0.008;
      }

      for (int i = 299; i < 400; i++) {
        annealingSchedule[i] = 0.016;
      }

      for (int i = 199; i < 300; i++) {
        annealingSchedule[i] = 0.032;
      }

      for (int i = 99; i < 200; i++) {
        annealingSchedule[i] = 0.064;
      }

      for (int i = 0; i < 100; i++) {
        annealingSchedule[i] = 0.128;
      }

      Average = 0.0;
      for (int i = 0; i < 890; i++) {

        double hot = Math.abs(
            Best[0] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i]
                * 2));  // generate the randoms
        double staleOther = Math
            .abs(Best[1] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double staleMine = Math
            .abs(Best[2] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));

        double risky = Math
            .abs(Best[3] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double lessRisky = Math
            .abs(Best[4] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));

        double playMe = Math
            .abs(Best[5] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double discMe = Math
            .abs(Best[6] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double diskRisk = Math
            .abs(Best[7] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double tokWeight = Math
            .abs(Best[8] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double variability = Math
            .abs(Best[9] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));

        double v0 = Math.abs(
            bestVector[0] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i]
                * 2));
        double v1 = Math.abs(
            bestVector[1] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i]
                * 2));
        double v2 = Math.abs(
            bestVector[2] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i]
                * 2));
        double v3 = Math.abs(
            bestVector[3] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i]
                * 2));
        double v4 = Math.abs(
            bestVector[4] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i]
                * 2));

        double b0 = Math.abs(
            badVector[0] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double b1 = Math.abs(
            badVector[1] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double b2 = Math.abs(
            badVector[2] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double b3 = Math.abs(
            badVector[3] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));
        double b4 = Math.abs(
            badVector[4] - annealingSchedule[i] + random.nextDouble() * (annealingSchedule[i] * 2));

        validityVector[0] = v0;
        validityVector[1] = v1;
        validityVector[2] = v2;
        validityVector[3] = v3;
        validityVector[4] = v4;

        discardVector[0] = b0;
        discardVector[1] = b1;
        discardVector[2] = b2;
        discardVector[3] = b3;
        discardVector[4] = b4;

        //System.out.println("\n");

        for (int g = 0; g < 100; g++) {

          Integer[] results = AI.TestMe(hot,
              staleOther,
              staleMine,
              risky,
              lessRisky,
              validityVector,
              playMe,
              discMe,
              diskRisk,
              tokWeight,
              discardVector);
          if (results[1] > 0) {
            Total += results[0];
//          if(results[0] > 0){
//            System.out.println("################## " + results[0] + " ##################" + results[1]);
//          }


          }
//          else{
//            System.out.println("################## " + 0 + " ##################" + results[1]);
//          }
        }

      /*

          Double staleOther,
      Double StaleObs,
      Double RiskyPlay,
      Double LessRisky,
      Double[] Vector,
      Double PlayWeight,
      Double DiscardWeight,
      Double DiscardRisk,
      Double TokenWeight){
*/

        Average = Total / 100.0;
        //System.out.println("Average for that round was " + Average + " but we can do better, right?");
        if (Average > BestSoFar) {
          BestSoFar = Average;
          Best[0] = hot;
          Best[1] = staleOther;
          Best[2] = staleMine;
          Best[3] = risky;
          Best[4] = lessRisky;
          Best[5] = playMe;
          Best[6] = discMe;
          Best[7] = diskRisk;
          Best[8] = tokWeight;
          Best[9] = variability;
          for (int z = 0; z < 5; z++) {
            bestVector[z] = validityVector[z];
          }
          for (int z = 0; z < 5; z++) {
            badVector[z] = discardVector[z];
          }
//          System.out.println("New Best Found \n" +
//              "Best[0] = " + Best[0] + ";\n" +
//              "Best[1] = " + Best[1] + ";\n" +
//              "Best[2] = " + Best[2] + ";\n" +
//              "Best[3] = " + Best[3] + ";\n" +
//              "Best[4] = " + Best[4] + ";\n" +
//              "Best[5] = " + Best[5] + ";\n" +
//              "Best[6] = " + Best[6] + ";\n" +
//              "Best[7] = " + Best[7] + ";\n" +
//              "Best[8] = " + Best[8] + ";\n" +
//              "Best[9] = " + Best[9] + ";\n" +
//              "bestVector[0] = " + bestVector[0] + ";\n" +
//              "bestVector[1] = " + bestVector[1] + ";\n" +
//              "bestVector[2] = " + bestVector[2] + ";\n" +
//              "bestVector[3] = " + bestVector[3] + ";\n" +
//              "bestVector[4] = " + bestVector[4] + ";\n" +
//              "badVector[0] = " + discardVector[0] + ";\n" +
//              "badVector[1] = " + discardVector[1] + ";\n" +
//              "badVector[2] = " + discardVector[2] + ";\n" +
//              "badVector[3] = " + discardVector[3] + ";\n" +
//              "badVector[4] = " + discardVector[4] + ";\n" +

//              Average
//          );


        }
        Total = 0;

      }
      System.out.println("New Best Found \n" +
          "Best[0] = " + Best[0] + ";\n" +
          "Best[1] = " + Best[1] + ";\n" +
          "Best[2] = " + Best[2] + ";\n" +
          "Best[3] = " + Best[3] + ";\n" +
          "Best[4] = " + Best[4] + ";\n" +
          "Best[5] = " + Best[5] + ";\n" +
          "Best[6] = " + Best[6] + ";\n" +
          "Best[7] = " + Best[7] + ";\n" +
          "Best[8] = " + Best[8] + ";\n" +
          "Best[9] = " + Best[9] + ";\n" +
          "bestVector[0] = " + bestVector[0] + ";\n" +
          "bestVector[1] = " + bestVector[1] + ";\n" +
          "bestVector[2] = " + bestVector[2] + ";\n" +
          "bestVector[3] = " + bestVector[3] + ";\n" +
          "bestVector[4] = " + bestVector[4] + ";\n" +
          "badVector[0] = " + discardVector[0] + ";\n" +
          "badVector[1] = " + discardVector[1] + ";\n" +
          "badVector[2] = " + discardVector[2] + ";\n" +
          "badVector[3] = " + discardVector[3] + ";\n" +
          "badVector[4] = " + discardVector[4] + ";\n" +
          BestSoFar);


    }
  }

  public static void main(String[] args){

    AIOptimizer TESTYBOI = new AIOptimizer();
    TESTYBOI.Annealer();
  }
}

