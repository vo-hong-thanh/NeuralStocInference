/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import model.MStateList;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import utils.ComputingMachine;

/**
 *
 * @author HongThanh
 */
public class StochasticSimulator {
    //random generator
    private Random randomGenerator = new Random();

    //model info
    private StateList states = new StateList();
    private ReactionList reactions = new ReactionList();

    //simulation time
    private double maxTime = 0;

    //data structure for one simulation
    private PropensityNode[] propensities;
    private Hashtable<Integer, PropensityNode> mapRactionIndexToNode = new Hashtable<Integer, PropensityNode>();
    //state list
    private StateList runningStates = new StateList();
    private double totalPropensity;
    
    //two simulation
    private int numTrajectory = 2;
    private double totalMaxPropensity = 0;
    private double[] totalPropensityByTrajectory = new double[numTrajectory];
    //multi-state list
    private MStateList runningMStates;
    
    public void config(double _maxTime, StateList states, ReactionList reactions) throws Exception {
        //set parameters
        this.maxTime = _maxTime;

        //build states
        this.states = states;

        //build reactions
        this.reactions = reactions;

        //build dependency graph
        ComputingMachine.buildReactionDependency(reactions);

        initializePropensityList();
    }

    private void initializePropensityList() {
        //build newPropensity       
//        System.out.println("[Build newPropensity array]");
        Reaction[] reactionList = reactions.getReactionList();
        propensities = new PropensityNode[reactionList.length];

        int i = 0;
        for (Reaction r : reactionList) {
            //propensity            
            int reactionIndex = r.getReactionIndex();
            propensities[i] = new PropensityNode(i, reactionIndex);

            mapRactionIndexToNode.put(r.getReactionIndex(), propensities[i]);
            i++;
        }
    }

    public double getSimTime(){
        return maxTime;
    }
    
    public void printModelInfo() {
        //print out model parameters
        System.out.println("---Model info---");

        System.out.print("Initial states: ");
        System.out.println(states);
        
        System.out.print("Reactions: ");
        System.out.println(reactions.toStringFull());
        System.out.println("---");
    }
    
    //return probability
    public StateList doSim(long seed) throws Exception {
//        System.out.println("[call simulation engine]");
        randomGenerator.setSeed(seed);

        //compute newPropensity values of reactions
        computePropensityList();

        //print out newPropensity values
//        System.out.println("Propensity values: ");
//        for(PropensityNode node : propensities)
//        {
//            System.out.println("Node index: " + node.getNodeIndex() + " holds reaction " + node.getReactionIndex());
//            System.out.println("  - newPropensity = " + node.getPropensity() );
//            
//        }
//        System.out.println("total newPropensity = " + totalPropensity);
        //reset time 
        double currentTime = 0;

        //run sim
//        long simTime = 0;
//        long updateTime = 0;
//        long searchTime = 0;
//        
//        //start simulation
//        long startSimTime = System.currentTimeMillis();      
//        System.out.println("---");
//        int step = 0;
        while (true) {
//            System.out.println(" step = " + step++);
//            for(PropensityNode node : propensities)
//            {
//                System.out.println("Node index: " + node.getNodeIndex() + " holds reaction " + node.getReactionIndex());
//                System.out.println("  - propensity = " + node.getPropensity() );
//            }
//            System.out.print("  total propensity: " + totalPropensity);
            
            if(totalPropensity <= 0.0){
//                System.out.println(" [negative propensity " + totalPropensity + " => finish simulation]");
                return runningStates;
            }
            if(totalPropensity > 1e7){
//                System.out.println(" [too big propensity " + totalPropensity + " => finish simulation]");
                return runningStates;
            }
            double delta = ComputingMachine.computeTentativeTime(randomGenerator, totalPropensity);            
//            System.out.print("  => delta: " + delta);            
                        
            //update time
            currentTime += delta;
//            System.out.println(" current simulation time " + currentTime);
            if (currentTime >= maxTime) {
                currentTime = maxTime;
//                System.out.println("[normal finish simumlation]");
//                System.out.print("States: ");
//                System.out.println(runningStates);  
//                
                return runningStates;
            }

//            System.out.println("Total newPropensity: " + totalPropensity);
            double partialSumPropensity = 0;
            double searchValue = randomGenerator.nextDouble() * totalPropensity;
            int fireReactionIndex = -1;

//            System.out.println("=> search value: " + searchValue );
//            long startSearchTime = System.currentTimeMillis();
            for (PropensityNode node : propensities) {
//                System.out.println("examine reaction: " + reactions.getReaction(node.getReactionIndex()) + " has newPropensity: " + node.getPropensity());
                partialSumPropensity += node.getPropensity();

                if (partialSumPropensity >= searchValue) {
                    fireReactionIndex = node.getReactionIndex();

//                    System.out.println(" => fire reaction: " + node.getReactionIndex());
                    break;
                }
            }
//            long endSearchTime = System.currentTimeMillis();
//            searchTime += (endSearchTime - startSearchTime);

            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, runningStates);

//            System.out.println("@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));
            //print trace
//            System.out.print("States: ");
//            System.out.println(runningStates);     

            //update array of newPropensity            
//            long startUpdateTime = System.currentTimeMillis();
            updatePropensityList(reactions.getReaction(fireReactionIndex).getDependentReactions());

//            long endUpdateTime = System.currentTimeMillis();
//            updateTime += (endUpdateTime - startUpdateTime);
//            System.out.println("---");
        }
//        long endSimTime = System.currentTimeMillis();
//        simTime = (endSimTime - startSimTime);     
//        System.out.println("Simulation took: " + simTime/1000 + "(sec)");
    }

    //return probability
    public MStateList doSim2(long seed, int perturbedReactionIndex, double percentage) throws Exception {
        randomGenerator.setSeed(seed);

        //compute newPropensity values of reactions
        computePropensityList2(perturbedReactionIndex, percentage);
        
//        //print information
//        System.out.println("---Multi-state list---");
//        for(Species s : runningMStates.getSpeciesList())
//        {
//            System.out.println("Species "+ s + " has population ");
//            for(int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++) {
//                System.out.println(" " + runningMStates.getPopulation(s, trajectoryIndex) + " in trajectory " + trajectoryIndex) ;
//            }            
//        }
//        //perturbing reaction
//        System.out.println("---Perturbating reaction " + perturbedReactionIndex + " by " + (percentage*100) + "%");
//        
        //reset time 
        double currentTime = 0;

        //run sim
//        long simTime = 0;
//        long updateTime = 0;
//        long searchTime = 0;
//        
//        //start simulation
//        long startSimTime = System.currentTimeMillis();     

//        System.out.println("---Run Sim---");
//        int step = 0;
        while (true) {
//            System.out.println("-- step = " + (step++) );            
//            //print out newPropensity values
//            for(PropensityNode node : propensities)
//            {
//                System.out.println("Node index: " + node.getNodeIndex() + " holds ");
//                System.out.println(" Reaction " + node.getReactionIndex());
//                System.out.println(" Propensity List");
//                for(int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++)
//                {
//                    System.out.println("  " + node.getPropensityByTrajectory(trajectoryIndex) + " @trajectory = " + trajectoryIndex);
//                }
//            }
        
//            System.out.println("Total max newPropensity: " + totalMaxPropensity);
            double delta = ComputingMachine.computeTentativeTime(randomGenerator, totalMaxPropensity);
//            System.out.println("=> delta: " + delta);            

            //update time
            currentTime += delta;

            if (currentTime >= maxTime) {
                currentTime = maxTime;
                return runningMStates;
            }

//            System.out.println("Total max newPropensity: " + totalMaxWeightedPropensity);
            double partialSumPropensity = 0;
            double searchValue = randomGenerator.nextDouble() * totalMaxPropensity;            
            
            int fireReactionIndex = -1;
            boolean isUpdateBoth = false;
            int residualTrajectoryIndex = -1;   
                            
//            System.out.println("=> search value: " + searchValue );
//            long startSearchTime = System.currentTimeMillis();
            for (PropensityNode node : propensities) {
//                System.out.println("consider node " + node.getNodeIndex() + " holding reaction: " + reactions.getReaction(node.getReactionIndex()));
                double[] propensityByTrajectory = node.getPropensityByTrajectory();                
                
                double minPropensity = Double.MAX_VALUE;
                double residualAmount = 0;                                             
                if(propensityByTrajectory[0] > propensityByTrajectory[1]){
                    minPropensity = propensityByTrajectory[1];
                    residualAmount = propensityByTrajectory[0] - propensityByTrajectory[1];
                    residualTrajectoryIndex = 0;
                }
                else{
                    minPropensity = propensityByTrajectory[0];
                    residualAmount = propensityByTrajectory[1] - propensityByTrajectory[0];
                    residualTrajectoryIndex = 1;
                }
                
//                System.out.println(" examine min newPropensity " + minPropensity );                
                partialSumPropensity += minPropensity;
                if (partialSumPropensity >= searchValue) {
                    fireReactionIndex = node.getReactionIndex();

                    isUpdateBoth = true;
                    
//                    System.out.println("@Time: " + currentTime + ": fire " + node.getReactionIndex() + " in both trajectories");
                    break;
                }
                
//                System.out.println(" examine residual newPropensity " + residualAmount );                
                partialSumPropensity += residualAmount;
                if (partialSumPropensity >= searchValue) {
                    fireReactionIndex = node.getReactionIndex();
//                      System.out.println("@Time: " + currentTime + ": fire " + node.getReactionIndex() + " only on trajectory " + residualTrajectoryIndex);
                    
                    break;
                }                
            }
//            long endSearchTime = System.currentTimeMillis();
//            searchTime += (endSearchTime - startSearchTime);

//            System.out.println("@Time: " + currentTime + ": (Fired)" + fireReactionIndex);

            //update population
            if(isUpdateBoth){
                for (int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++) {
                    ComputingMachine.executeReaction(fireReactionIndex, reactions, runningMStates, trajectoryIndex);                    
                }
            }
            else{
                ComputingMachine.executeReaction(fireReactionIndex, reactions, runningMStates, residualTrajectoryIndex);                
            }

            //print trace
//            System.out.println("Multi-state list");
//            for(Species s : runningMStates.getSpeciesList())
//            {
//                System.out.println("Species "+ s + " has population ");
//                for(int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++) {
//                    System.out.println(" " + runningMStates.getPopulation(s, trajectoryIndex) + " in trajectory " + trajectoryIndex) ;
//                }            
//            }            
                        
            //update array of newPropensity            
//            long startUpdateTime = System.currentTimeMillis();            
            updatePropensityList2(reactions.getReaction(fireReactionIndex).getDependentReactions(), perturbedReactionIndex, percentage);
//            long endUpdateTime = System.currentTimeMillis();
//            updateTime += (endUpdateTime - startUpdateTime);
//            System.out.println("------" );
        }
//        long endSimTime = System.currentTimeMillis();
//        simTime = (endSimTime - startSimTime);     
//        System.out.println("Simulation took: " + simTime/1000 + "(sec)");
    }
    
    //compute newPropensity reactionList
    private void computePropensityList() {
        //compute newPropensity reactionList
        runningStates = StateList.makeClone(states);

//        System.out.println("[Build newPropensity array]");
        Reaction[] reactionList = reactions.getReactionList();
        totalPropensity = 0;

        for (Reaction r : reactionList) {
            int reactionIndex = r.getReactionIndex();
            PropensityNode node = mapRactionIndexToNode.get(reactionIndex);

            //propensity
            double propensity = ComputingMachine.computePropensity(r, runningStates);
            node.setPropensity(propensity);
            
            //update total newPropensity
            totalPropensity += propensity;
        }
    }

    //update newPropensity reactionList
    private void updatePropensityList(HashSet<Integer> dependent) {
//        System.out.println("[update array]");
        for (int reactionIndex : dependent) {
            Reaction r = reactions.getReaction(reactionIndex);
            double newPropensity = ComputingMachine.computePropensity(r, runningStates);

            PropensityNode node = mapRactionIndexToNode.get(reactionIndex);

            double diff = newPropensity - node.getPropensity();
            totalPropensity += diff;

            node.setPropensity(newPropensity);
//            System.out.println("Node index: " + nodePos + " contains (reaction " + propensities[nodePos].getReactionIndex() +", newPropensity = "+ propensities[nodePos].getPropensity() +")");
        }
    }
    
    //compute newPropensity reactionList
    private void computePropensityList2(int perturbedReactionIndex, double percentage) {
        //multi-state list
        runningMStates = new MStateList();
        for (Species s : states.getSpeciesList()) {
            ArrayList<Integer> populationList = new ArrayList<Integer>(numTrajectory);
            int pop = states.getPopulation(s);

            for (int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++) {
                populationList.add(pop);
            }

            runningMStates.addSpecies(s, populationList);
        }
        
        totalMaxPropensity = 0;        
//        System.out.println("[Build newPropensity array]");
        Reaction[] reactionList = reactions.getReactionList();                
        for (Reaction r : reactionList) {
            int reactionIndex = r.getReactionIndex();
            PropensityNode node = mapRactionIndexToNode.get(reactionIndex);    
                        
            double maxPropensity = Double.MIN_VALUE;
            for (int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++) {
                //propensity
                double propensity = ComputingMachine.computePropensity(r, runningMStates, trajectoryIndex);
                if(reactionIndex == perturbedReactionIndex){
                    if(trajectoryIndex == 0){
                        propensity = (1 + percentage)*propensity;
                    }
                    else{
                        propensity = (1 - percentage)*propensity;
                    }
                }                   
                node.setPropensityByTrajectory(propensity, trajectoryIndex);
                
                if(maxPropensity < propensity){
                    maxPropensity = propensity;
                }                          
            }
            totalMaxPropensity += maxPropensity;      
        }
    }
     
//    private void updatePropensityList2(int perturbedReactionIndex, double percentage) { 
//        totalMaxPropensity = 0;        
////        System.out.println("[Build newPropensity array]");
//        Reaction[] reactionList = reactions.getReactionList();                
//        for (Reaction r : reactionList) {
//            int reactionIndex = r.getReactionIndex();
//            PropensityNode node = mapRactionIndexToNode.get(reactionIndex);    
//                        
//            double maxPropensity = Double.MIN_VALUE;
//            for (int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++) {
//                //propensity
//                double propensity = ComputingMachine.computePropensity(r, runningMStates, trajectoryIndex);
//                if(reactionIndex == perturbedReactionIndex){
//                    if(trajectoryIndex == 0){
//                        propensity = (1 + percentage)*propensity;
//                    }
//                    else{
//                        propensity = (1 - percentage)*propensity;
//                    }
//                }                   
//                node.setPropensityByTrajectory(propensity, trajectoryIndex);
//                
//                if(maxPropensity < propensity){
//                    maxPropensity = propensity;
//                }                          
//            }
//            totalMaxPropensity += maxPropensity;      
//        }
//    }
    
    private void updatePropensityList2(HashSet<Integer> dependent, int perturbedReactionIndex, double percentage) { 
        for (int reactionIndex : dependent) {
            Reaction r = reactions.getReaction(reactionIndex);
            PropensityNode node = mapRactionIndexToNode.get(reactionIndex);    
              
            //old propensity
            double oldMaxPropensity = Double.MIN_VALUE;
            for (int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++)
            {
                double propensityByTrajectory = node.getPropensityByTrajectory(trajectoryIndex);
                if(oldMaxPropensity < propensityByTrajectory){
                    oldMaxPropensity = propensityByTrajectory;
                }                          
            }

            //new propensity
            double newMaxPropensity = Double.MIN_VALUE;
            for (int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++)
            {
                double newPropensity = ComputingMachine.computePropensity(r, runningMStates, trajectoryIndex);
                if(reactionIndex == perturbedReactionIndex){
                    if(trajectoryIndex == 0){
                        newPropensity = (1 + percentage)*newPropensity;
                    }
                    else{
                        newPropensity = (1 - percentage)*newPropensity;
                    }
                }

                if(newMaxPropensity < newPropensity){
                    newMaxPropensity = newPropensity;
                }
                node.setPropensityByTrajectory(newPropensity, trajectoryIndex);
            }

            double diff = newMaxPropensity - oldMaxPropensity;

            totalMaxPropensity += diff;      
        }
    }
}
