/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inference;

import model.StateList;
import simulator.StochasticSimulator;
import utils.ComputingMachine;

/**
 *
 * @author vot2
 */
public class NaiveABCEngine {
    private StochasticSimulator sim;
    private double threshold;

    public NaiveABCEngine(StochasticSimulator sim, double threshold) {
        this.sim = sim;
        this.threshold = threshold;
    }

    public boolean isAcceptedReactionRate(StateList data) throws Exception {
        StateList simData = sim.doSim(System.nanoTime());
        
        System.out.println(" simulated states at time T = " + sim.getSimTime() +" is " + simData);

        double distance = ComputingMachine.computeDistance(data, simData);
        
        System.out.println(" => distance: " + distance);
        
        if (distance <= threshold) {
            return true;
        }

        return false;
    }    
}
