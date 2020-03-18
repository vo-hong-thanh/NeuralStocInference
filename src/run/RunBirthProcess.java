/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import inference.NaiveABCEngine;
import inference.NeuralNetEngine;
import java.io.File;
import java.util.ArrayList;
import model.MStateList;
import model.Reaction;
import model.ReactionList;
import model.StateList;
import simulator.StochasticSimulator;
import utils.ComputingMachine;

/**
 *
 * @author vot2
 */
public class RunBirthProcess {
    public static void main(String[] args) throws Exception {
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");
        
        System.out.println("Birth process model ");
        
        String[] SpeciesInfo =   { "S1 = 0"                                   
                                 };

        String[] reactionsInfo = { "_ -> S1"                                   
                                 };

        double[] ratesInfo =   {1
                               };
        
        //build model
        StateList states = ComputingMachine.buildPopulationList(SpeciesInfo);
        ReactionList reactions = ComputingMachine.buildReactionList(reactionsInfo, ratesInfo, states);

        //simulator
        double simTime = 100;        
        StochasticSimulator sim;
        sim = new StochasticSimulator();
        sim.config(simTime, states, reactions);
        sim.printModelInfo();
        
        //abc inference
        String[] SpeciesData =   { "S1 = 200"                                   
                                 };
        StateList data = ComputingMachine.buildPopulationList(SpeciesData);
        System.out.println("------------------------------------");
        System.out.println("Observed true data " + data);
        System.out.println("------------------------------------");
        //###################################################################
        //###################################################################
//        //random generators for rates
//        Uniform rate1Generator = new Uniform(0, 3, RandomEngine.makeDefault());
//                
//        System.out.println("-------------------------------------------");
//        System.out.println("ABC parameter inference ");
//        double abcThreshold = 0.1;
//        NaiveABCEngine abc = new NaiveABCEngine(sim, abcThreshold);        
//        int numRunsABC = 5000;
//        ArrayList<Double> acceptedRates = new ArrayList();
//        for(int run = 0; run < numRunsABC; run++){
//            System.out.println("@run : " + run);
//            ratesInfo[0] = rate1Generator.nextDouble();
//            reactions.getReaction(1).setRate(ratesInfo[0]);
//
//             System.out.println("generate rates " + ratesInfo[0]);
////            sim.printModelInfo();
//            if(abc.isAcceptedReactionRate(data)){
//                System.out.println(" + accept rates");
//                acceptedRates.add(ratesInfo[0]);
//                
//            }
//            else{
//                System.out.println(" - reject rates");
//            }
//            
////            if(run > 500)
////            {
////                System.out.println("press a key to continue...");
////                System.in.read();
////            }
//        }
//        System.out.println("-------------------------------------------");        
//        if(!acceptedRates.isEmpty()){            
//            int i = 0;
//            while(i < acceptedRates.size()){
//                System.out.println("rates for reaction " + (i+1) + " = " + acceptedRates.get(i));
//                i++;
//            }
//        }
//        
        //###################################################################
        //###################################################################
        double neuralThreshold = 0.1;
        NeuralNetEngine neural = new NeuralNetEngine(sim, neuralThreshold);
        double percentage = 0.05;
        int numRunsNeuralNet = 1000;
        int epoch = 1000;
        double learningRate = 0.006;
                        
        ratesInfo[0] = 1.45;
        reactions.getReaction(1).setRate(ratesInfo[0]);

//        System.out.println("randomly generated rates " + ratesInfo[0]);
        
        sim.printModelInfo();        
        
        System.out.println("-------------------------------------------");
        System.out.println("Neural-net parameter inference ");
        
//        if(neural.isAcceptedReactionRate(data)){
//            System.out.println("accept rates => optimize it");
        for(int e = 1; e <= epoch; e++)
        {
            System.out.println("@epoch: " + e);
            double[][][] absoluteDiffs = new double[1][numRunsNeuralNet][2];   
            double[][] statistics = new double[1][2];               
            for(Reaction r : reactions.getReactionList() ){
//                //int reactionIndex = 1;
//                //Reaction r = reactions.getReaction(reactionIndex);                                
                int reactionIndex = r.getReactionIndex();                
                int processArrayIndex = reactionIndex - 1;
                //compute absolute diff
                System.out.println("perturbating reaction " + reactionIndex + " by " + (percentage*100) + "% => [" + (1 - percentage)*r.getRate() + ", " +(1+percentage)*r.getRate()+ "]");
                for(int run = 0; run < numRunsNeuralNet; run++){
//                    System.out.println("--- run : " + run);
                    absoluteDiffs[processArrayIndex][run] = neural.computeFiniteDifference(data, reactionIndex, percentage);
                }
                //compute gradient 
                statistics[processArrayIndex] = neural.computeStatistics(processArrayIndex, numRunsNeuralNet, absoluteDiffs);
                System.out.println("statistical distance by changing rate of reaction " +  reactionIndex + ": " +  statistics[processArrayIndex][0] + " +/- " + statistics[processArrayIndex][1]);
               
                double gradient = statistics[processArrayIndex][0] / (2*r.getRate()*percentage);
                System.out.println(" => gradient w.r.t reaction " +  reactionIndex + ": " +  gradient);
                
                if(e == 1){
                    //update rate by gradient descent
                    double dividendFactor = Math.pow(10, (int)Math.log10(Math.abs(gradient)));   
                    learningRate /= dividendFactor;
                }
                                
                System.out.println(" learning rate " + learningRate);
                
                ratesInfo[processArrayIndex] = ratesInfo[processArrayIndex] - learningRate*gradient;                
                
                System.out.println("update rate of reaction " +  reactionIndex + " to " + ratesInfo[processArrayIndex]);
                
                r.setRate(ratesInfo[processArrayIndex]);
            }            
        }
//        }
//        else{
//            System.out.println("reject rates");
//        }
    }
}
