/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.StringTokenizer;
import model.MStateList;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;


/**
 *
 * @author vot2
 */
public class ComputingMachine {    
    public static final double BASE = 2;
    
    //compute exponent
    public static int calculateGroupExponent(double propensityValue) {
        int exponent = (int) Math.ceil(Math.log(propensityValue) / Math.log(BASE));
        return exponent;
    }    
    
    public static double[] computeDistance(StateList data, MStateList simData, int numTrajectory) {
        double[] distances = new double[numTrajectory];
        
        for(int trajectoryIndex = 0; trajectoryIndex < numTrajectory; trajectoryIndex++){
            for (Species s : data.getSpeciesList()) {
                int dataPop = data.getPopulation(s);
                int simPop = simData.getPopulation(s, trajectoryIndex);

                distances[trajectoryIndex] += Math.pow(dataPop - simPop, 2);
            }
            distances[trajectoryIndex] = Math.pow(distances[trajectoryIndex], 0.5);
        }
        return distances;        
    }
    
    public static double computeDistance(StateList data, StateList simData) {
        double distance = 0;
        for (Species s : data.getSpeciesList()) {
            int dataPop = data.getPopulation(s);
            int simPop = simData.getPopulation(s);

            distance += Math.pow(dataPop - simPop, 2);
        }
        distance = Math.pow(distance, 0.5);

        return distance;
    }
    
    //compute tentative time
    public static double computeTentativeTime(Random rand) {
        return Math.log(1/rand.nextDouble());
    }
    
    public static double computeTentativeTime(Random rand, double propensity) {
        double time = Double.MAX_VALUE;
        if (propensity > 0) {
//            double randomValue = rand.nextDouble();            
//            System.out.print("  propensity " + propensity + ", random value " + randomValue + " ");            
//            time = Math.log(1/randomValue)/propensity;
            
//            System.out.println("=> delta = " + time);            
            time = Math.log(1/rand.nextDouble())/propensity;            
        }       
        
        return time;
    }
    
    public static double computeTentativeTime(int k, Random rand) {
        double random = 1;
        for (int i = 0; i < k; i++) {
            random *= rand.nextDouble();
        }

        return Math.log(1 / random);
    }
    
    public static double computeTentativeTime(int k, Random rand, double propensity) {
        double random = 1;
        for (int i = 0; i < k; i++) {
            random *= rand.nextDouble();
        }

        double time = Double.MAX_VALUE;
        if (propensity > 1.0) {
            time = Math.log(1 / random) / propensity;
        }

        return time;
    }
    
    public static double computePropensity(Reaction r, StateList stateList) {
        long combination = computeCombination(r, stateList);
        
        if(combination <= 0){
            return 0;
        }
        return r.getRate()*combination;
    }
    
    public static double computePropensity(Reaction r, MStateList mstates, int position) {
        long combination = computeCombination(r, mstates, position);
        
        if(combination <= 0){
            return 0;
        }
        return r.getRate()*combination;
    }
    
    public static long computeCombination(Reaction r, StateList stateList){
        ArrayList<Term> reactants = r.getReactants();
        
        //zeroth reaction
        if(reactants.isEmpty()){
            return 1;
        }
        //other reaction types
        else{
            long numerator = 1;
            long denominator = 1;
            for (Term reactant : reactants) {
                int pop = stateList.getPopulation(reactant.getSpecies());
                int coff = -reactant.getCoff();
                int base = pop - coff + 1;
                
                if (pop <= 0 || base <= 0) {
                    return 0;
                } else {
                    for (int run = base; run <= pop; run++) {
                        numerator *= run;
                    }
                    for (int run = 2; run <= coff; run++) {
                        denominator *= run;
                    }
                }
            }
            return numerator / denominator;
        }        
    }
    
    public static long computeCombination(Reaction r, MStateList mstates, int position){
        ArrayList<Term> reactants = r.getReactants();
        
        //zeroth reaction
        if(reactants.isEmpty()){
            return 1;
        }
        //other reaction types
        else{
            long numerator = 1;
            long denominator = 1;
            for (Term reactant : reactants) {
                int pop = mstates.getPopulation(reactant.getSpecies(), position);
                int coff = -reactant.getCoff();
                int base = pop - coff + 1;
                
                if (pop <= 0 || base <= 0) {
                    return 0;
                } else {
                    for (int run = base; run <= pop; run++) {
                        numerator *= run;
                    }
                    for (int run = 2; run <= coff; run++) {
                        denominator *= run;
                    }
                }
            }
            return numerator / denominator;
        }        
    }

    //execute reaction
    public static void executeReaction(int fireReactionIndex, ReactionList reactions, StateList states) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        //update population
        for (Term reactant : r.getReactants()) {
            states.updateSpecies(reactant.getSpecies(), states.getPopulation(reactant.getSpecies()) + reactant.getCoff());
        }
        for (Term product : r.getProducts()) {
            states.updateSpecies(product.getSpecies(), states.getPopulation(product.getSpecies()) + product.getCoff());
        }
    }
    
    public static void executeReaction(int fireReactionIndex, ReactionList reactions, MStateList mstates, int position) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        //update population
        for (Term reactant : r.getReactants()) {
                mstates.updatePopulation(reactant.getSpecies(), position, mstates.getPopulation(reactant.getSpecies(), position) + reactant.getCoff());
        }
        for (Term product : r.getProducts()) {
            mstates.updatePopulation(product.getSpecies(), position, mstates.getPopulation(product.getSpecies(), position) + product.getCoff());
        }
    }
   
    public static HashSet<Species> executeReaction(int fireReactionIndex, ReactionList reactions, MStateList mstates, int position, StateList lowerState, StateList upperState) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        Species s;
        int updatedPop;
                
        HashSet<Species> updateSpecies = new HashSet<Species>();

        //update population
        for (Term reactant : r.getReactants()) {
            s = reactant.getSpecies();
            
            updatedPop = mstates.getPopulation(s, position) + reactant.getCoff() ;
                        
            mstates.updatePopulation(s, position, updatedPop);
            
            if ( (updatedPop != 0 && updatedPop < lowerState.getPopulation(s)) || updatedPop <= 0) {
                updateSpecies.add(s);
            }
        }

        for (Term product : r.getProducts()) {
            s = product.getSpecies();

            updatedPop = mstates.getPopulation(s, position) + product.getCoff();
                        
            mstates.updatePopulation(s, position, updatedPop);
            
            if (!s.isProductOnly() && updatedPop > upperState.getPopulation(s)) {
                updateSpecies.add(s);
            }
        }

        return updateSpecies;
    }
    
    //build model
    //state vector
    public static StateList buildPopulationList(String[] populationList){        
        StateList states = new StateList();
        //read initial state vector
        StringTokenizer spo = null;
        for(int i = 0; i < populationList.length; i++)
        {
            spo = new StringTokenizer(populationList[i].trim(), "=");
            Species s = new Species(spo.nextToken().trim());
            states.updateSpecies(s, Integer.parseInt(spo.nextToken().trim()));
        }
        return states;
    }

    //reaction list
    public static ReactionList buildReactionList(String[] reactionList, double[] rates, StateList states){
        ReactionList reactions = new ReactionList();
               
        Reaction r = null;
        int reactionIndex = 1;
        for(int i = 0; i < reactionList.length; i++){
            r = buildReaction(reactionList[i].trim(), rates[i], reactionIndex, states);
                     
            reactions.addReaction(r);
            reactionIndex++;
        }
        return reactions;
    }

    private static Reaction buildReaction(String reactionInfo, double rate, int reactionIndex, StateList states){
        //reaction data
        StringTokenizer rp = new StringTokenizer(reactionInfo, "->");

        ArrayList<Term> reactants = buildPart(states, rp.nextToken(), -1);
        ArrayList<Term> products = buildPart(states, rp.nextToken(), 1);

        //set property of reactant species to be not product
        for(Term t : reactants){
            t.getSpecies().setIsProductOnly(false);
        }
        
        return new Reaction(reactionIndex, reactants, products, rate);
    }
    
    private static ArrayList<Term> buildPart(StateList states, String part, int mul) {
        ArrayList<Term> partTerm = new ArrayList<Term>();
        if (part.trim().equals("_")) {
            return partTerm;
        }

        StringTokenizer tokens = new StringTokenizer(part, "+");
        while (tokens.hasMoreTokens()) {
            String piece = tokens.nextToken().trim();
            int i;
            for (i = 0; i < piece.length(); i++) {
                if (Character.isLetter(piece.charAt(i))) {
                    break;
                }
            }
            String name = piece.substring(i).trim();
            int coff = mul;
            if (!piece.substring(0, i).equals("")) {
                coff *= Integer.parseInt(piece.substring(0, i));
            }
//            partTerm.add(new Term(new Species(name), coff));
            if(states.getSpecies(name) == null){
                throw new RuntimeException("Species " + name + " does not exist!");
            }else{
                partTerm.add(new Term(states.getSpecies(name), coff));
            }            
        }
        return partTerm;
    }
    
    //build dependency Graph and Priority Queue
    public static void buildReactionDependency(ReactionList reactions) {
        Reaction[] list = reactions.getReactionList();
        
        for (int i = 0; i < list.length; i++) {
            Reaction r = list[i];
            //add dependency to other reaction, allowed self reference
            for (Reaction o : list) {
                if (r.equals(o) || r.affect(o)) {
                    r.addDependentReaction(o.getReactionIndex());
                }
            }
        }
    }
    
    //build bipertie graph
    public static void buildSpecieReactionDependency(ReactionList reactions, StateList states) {
        Reaction[] list = reactions.getReactionList();

        for (int i = 0; i < list.length; i++) {
            Reaction r = list[i];
            //add dependency to other reaction, allowed self reference
            for(Term t : r.getReactants()){
                Species s = t.getSpecies();
                states.getSpecies(s.getName()).addAffectReaction(r.getReactionIndex());
            }            
        }
    }    
    
//    //check numeric
//    private static boolean isNumeric(String strNum) {
//        return strNum.matches("-?\\d+(\\.\\d+)?");
//    }    
}
