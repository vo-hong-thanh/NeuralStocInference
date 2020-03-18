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
public class RunPreyPredator {
    public static void main(String[] args) throws Exception {
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");
        
        System.out.println("Prey-Predator model ");
        
        String[] SpeciesInfo =   { "S1 = 50",
                                   "S2 = 100"
                                 };

        String[] reactionsInfo = { "S1 -> 2S1",
                                   "S1 + S2 -> 2S2",
                                   "S2 -> _"
                                 };

        double[] ratesInfo =   {1,
                                0.005,
                                0.6
                               };
        
        //build model
        StateList states = ComputingMachine.buildPopulationList(SpeciesInfo);
        ReactionList reactions = ComputingMachine.buildReactionList(reactionsInfo, ratesInfo, states);

        //simulator
        double simTime = 30;        
        StochasticSimulator sim;
        sim = new StochasticSimulator();
        sim.config(simTime, states, reactions);
        sim.printModelInfo();
        
        //abc inference
        String[] SpeciesData =   { "S1 = 145",
                                   "S2 = 40"
                                 };
        StateList data = ComputingMachine.buildPopulationList(SpeciesData);
        System.out.println("------------------------------------");
        System.out.println("Observed true data " + data);
        System.out.println("------------------------------------");
        //###################################################################
        //###################################################################
        //random generators for rates
        Uniform rate1Generator = new Uniform(-3, 3, RandomEngine.makeDefault());
        Uniform rate2Generator = new Uniform(-8, -2, RandomEngine.makeDefault());
        Uniform rate3Generator = new Uniform(-4, 2, RandomEngine.makeDefault());
        
        System.out.println("-------------------------------------------");
        System.out.println("ABC parameter inference ");
        double threshold = 0.1;
        NaiveABCEngine abc = new NaiveABCEngine(sim, threshold);        
        int numRunsABC = 100000;
        ArrayList<Double> acceptedRates = new ArrayList();
        for(int run = 0; run < numRunsABC; run++){
            System.out.println("@run : " + run);
            ratesInfo[0] = Math.pow(Math.E, rate1Generator.nextDouble());            
            reactions.getReaction(1).setRate(ratesInfo[0]);

            ratesInfo[1] = Math.pow(Math.E, rate2Generator.nextDouble());
            reactions.getReaction(2).setRate(ratesInfo[1]);

            ratesInfo[2] = Math.pow(Math.E, rate3Generator.nextDouble());
            reactions.getReaction(3).setRate(ratesInfo[2]);
              
            System.out.println("generate rates (" + ratesInfo[0]  + ", " + ratesInfo[1] + ", " + ratesInfo[2] + ")");
//            sim.printModelInfo();
            if(abc.isAcceptedReactionRate(data)){
                System.out.println("=> accept rates");
                acceptedRates.add(ratesInfo[0]);
                acceptedRates.add(ratesInfo[1]);
                acceptedRates.add(ratesInfo[2]);
            }
            //else{
            //    System.out.println(" - reject rates");
            //}
        }
        System.out.println("-------------------------------------------");        
        
        if(!acceptedRates.isEmpty()){      
            System.out.println("accepted rates");
            int i = 0;
            while(i < acceptedRates.size()){
                System.out.println("tuple (rate " + (i+1) + " = " + acceptedRates.get(i) + ", rate " + (i + 2) + " = " + acceptedRates.get(i+1) + ", rate " + (i + 2) + " = " + acceptedRates.get(i+2) + ")");
                i += 3;
            }
        }
        
//        //###################################################################
//        //###################################################################
//        System.out.println("Neural-net parameter inference ");
//        System.out.println("-------------------------------------------");
//        
//        double threshold = 0.1;
//        NeuralNetEngine neural = new NeuralNetEngine(sim, threshold);
//        double percentage = 0.01;
//        int numRunsNeuralNet = 100;
//        int epoch = 100;
//        double learningRate = 0.06;
//
//        ratesInfo[0] = Math.pow(Math.E, rate1Generator.nextDouble());
//        reactions.getReaction(1).setRate(ratesInfo[0]);
//
//        ratesInfo[1] = Math.pow(Math.E, rate2Generator.nextDouble());
//        reactions.getReaction(2).setRate(ratesInfo[1]);
//
//        ratesInfo[2] = Math.pow(Math.E, rate3Generator.nextDouble());
//        reactions.getReaction(3).setRate(ratesInfo[2]);
//
//        System.out.println("randomly generated rates (" + ratesInfo[0]  + ", " + ratesInfo[1] + ", " + ratesInfo[2] + ")");
//        
//        sim.printModelInfo();        
//        
//        if(neural.isAcceptedReactionRate(data)){
//            System.out.println("accept rates => optimize it");
////        for(int e = 1; e <= epoch; e++)
////        {
//            double[][][] absoluteDiffs = new double[3][numRunsNeuralNet][2];   
//            double[][] statistics = new double[3][2];               
////            for(Reaction r : reactions.getReactionList() ){
//                int reactionIndex = 1;// r.getReactionIndex();
//                Reaction r = reactions.getReaction(reactionIndex);
//                int processArrayIndex = reactionIndex - 1;
//                //compute absolute diff
//                System.out.println("perturbating reaction " + reactionIndex + " by " + (percentage*100) + "% => [" + (1 - percentage)*r.getRate() + ", " +(1+percentage)*r.getRate()+ "]");
//                for(int run = 0; run < numRunsNeuralNet; run++){
//                    System.out.println("--- run : " + run);
//                    absoluteDiffs[processArrayIndex][run] = neural.computeFiniteDifference(data, reactionIndex, percentage);
//                }
//                //compute gradient 
//                statistics[processArrayIndex] = neural.computeStatistics(processArrayIndex, numRunsNeuralNet, absoluteDiffs);
//                System.out.println("statistical distance by changing rate of reaction " +  reactionIndex + ": " +  statistics[processArrayIndex][0] + " +/- " + statistics[processArrayIndex][1]);
//               
//                //update rate by gradient descent                
//                ratesInfo[processArrayIndex] -= learningRate*(statistics[processArrayIndex][0] / (2*r.getRate()*percentage));                
//                
//                System.out.println("update rate of reaction " +  reactionIndex + " to " + ratesInfo[processArrayIndex]);
////            }            
////        }
//        }
//        else{
//            System.out.println("reject rates");
//        }
    }
}
