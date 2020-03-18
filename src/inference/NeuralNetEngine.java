/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inference;

import java.util.ArrayList;
import model.MStateList;
import model.Species;
import model.StateList;
import simulator.StochasticSimulator;
import utils.ComputingMachine;

/**
 *
 * @author vot2
 */
public class NeuralNetEngine {
    StochasticSimulator sim;
    private double threshold;
    
    public NeuralNetEngine(StochasticSimulator sim, double threshold){
        this.sim = sim;
        this.threshold = threshold;
    }
    
    public boolean isAcceptedReactionRate(StateList data) throws Exception {
        StateList simData = sim.doSim(System.nanoTime());
        
        System.out.println("simulated states at time T = " + sim.getSimTime() +" is " + simData);

        double distance = ComputingMachine.computeDistance(data, simData);
        
        System.out.println("=> distance: " + distance);
        
        if (distance <= threshold) {
            return true;
        }

        return false;
    }    
    
    public double[] computeFiniteDifference(StateList data, int reactionIndex, double percentage) throws Exception
    {
        MStateList simMStates = sim.doSim2(System.nanoTime(), reactionIndex, percentage);
        
//        System.out.println(" simulated Multi-States ");  
//        for (Species s : data.getSpeciesList()) {
//            System.out.print(s + " = [");
//            ArrayList<Integer> popList = simMStates.getPopulation(s);
//            for(int i = 0; i < popList.size(); i++){
//                System.out.print(popList.get(i) + " ");
//            }
//            System.out.println("]");
//        }
        
        double[] distances = ComputingMachine.computeDistance(data, simMStates, 2);
//        System.out.println("=> distances from true data " + distances[0] + ", " + distances[1]);   
        return distances;
    }
    
    public double[] computeStatistics(int processingIndex, int numRuns, double[][][] distances){
        //compute average prob.
        double[] averageDistance = new double[2];
        for (int j = 0; j < numRuns; j++) {
            averageDistance[0] += distances[processingIndex][j][0];            
            averageDistance[1] += distances[processingIndex][j][1];
        }
        averageDistance[0] /= numRuns;
        averageDistance[1] /= numRuns;
        
        //compute variance
        double[] varianceDistance = new double[2];
        for (int j = 0; j < numRuns; j++) {
            varianceDistance[0] += Math.pow(distances[processingIndex][j][0] - averageDistance[0], 2);
            varianceDistance[1] += Math.pow(distances[processingIndex][j][1] - averageDistance[0], 2);
        }
        varianceDistance[0] /= (numRuns - 1);
        varianceDistance[1] /= (numRuns - 1);
        
        return new double[] {averageDistance[0] - averageDistance[1], Math.pow(varianceDistance[0], 0.5) - Math.pow(varianceDistance[1], 0.5)};        
    }
}
