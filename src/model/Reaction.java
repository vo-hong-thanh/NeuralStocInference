package model;

import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.HashSet;

/**
 *
 * @author Hong Thanh
 */
public class Reaction {
    private int reactionIndex;
    private ArrayList<Term> reactants;
    private ArrayList<Term> products;
    
    private double rate;
            
    private HashSet<Integer> dependentReactions = new HashSet<Integer>();

    public Reaction(int index, ArrayList<Term> reactants, ArrayList<Term> products) {
        this.reactionIndex = index;
        this.reactants = reactants;
        this.products = products;        
        
        //default: mass-action kinetics with rate 0
        rate = 0;        
    }
    
    public Reaction(int index, ArrayList<Term> reactants, ArrayList<Term> products, double _rate) {
        this.reactionIndex = index;
        this.reactants = reactants;
        this.products = products;   
        rate = _rate;
    }
    
    public int getReactionIndex() {
        return reactionIndex;
    }
    
    public ArrayList<Term> getReactants() {
        return reactants;
    }
    
    public ArrayList<Term> getProducts() {
        return products;
    }

    public void addDependentReaction(int index) {
        dependentReactions.add(index);
    }

    public HashSet<Integer> getDependentReactions() {
        return dependentReactions;
    }

    public double getRate(){
        return rate;   
    }
    
    public void setRate(double _rate){
        rate = _rate;   
    }
    
    public boolean reactantsContainSpecies(Species s) {
        for (Term r : reactants) {
            if (r.getSpecies().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean productsContainSpecies(Species s) {
        for (Term pr : products) {
            if (pr.getSpecies().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean affect(Reaction o) {
        boolean isAffect = false;

        for (Term r : reactants) {
            if (!isCatalyst(r) && o.reactantsContainSpecies(r.getSpecies())) {
                isAffect = true;
                break;
            }
        }

        for (Term p : products) {
            if (!isCatalyst(p) && o.reactantsContainSpecies(p.getSpecies())) {
                isAffect = true;
                break;
            }
        }

        return isAffect;
    }
    
    public boolean isCatalyst(Term t) {
        boolean result = false;

        int coffBackup = t.getCoff();
        
        t.setCoff(-coffBackup);
        if(coffBackup < 0 && products.contains(t)){
            result = true;            
        }
        else if(coffBackup > 0 && reactants.contains(t)){
            result = true;
        }
        
        t.setCoff(coffBackup);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(reactionIndex +": ");
        
        if(reactants.size() == 0){
            result.append("_ ");
        }
        else{
            for (Term t : reactants) {
                result.append(t.toString() + " + ");
            }
            result.delete(result.length() - 2, result.length());
        }
        result.append("->");

        if(products.size() == 0){
            result.append("_");
        }
        else{
            for (Term t : products) {
                result.append(t.toString() + " + ");
            }
            result.delete(result.length() - 2, result.length());
        }
        
        result.append(" , " + rate);
        
        return result.toString();
    }    
}
