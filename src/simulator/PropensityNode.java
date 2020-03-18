/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simulator;

/**
 *
 * @author HongThanh
 */
public class PropensityNode /*implements Comparable*/{
    private int reactionIndex;
    private double propensity;
    private int nodeIndex;
    
    private double[] propensityByTrajectory = new double[2];
    
    public PropensityNode(int nodeIndex, int reactionIndex)
    {
        this.reactionIndex = reactionIndex;
        this.nodeIndex = nodeIndex;
    }
    
    public int getReactionIndex()
    {
        return reactionIndex;
    }
    
    public int getNodeIndex()
    {
        return nodeIndex;
    }
    
    public double getPropensity()
    {
        return propensity;
    }
    
    public void setPropensity(double propensity)
    {
        this.propensity = propensity;
    }
    
    public void setPropensityByTrajectory(double prop, int pos)
    {
        this.propensityByTrajectory[pos] = prop;
    }
    
    public double[] getPropensityByTrajectory()
    {
        return propensityByTrajectory;
    }
    
    public double getPropensityByTrajectory(int pos)
    {
        return propensityByTrajectory[pos];
    }
    
    //decending sort
    public int compareTo(Object t) {
        if(t instanceof PropensityNode)
        {
            PropensityNode cast = (PropensityNode)t;
            if(propensity > cast.propensity)
                return 1;
            else if(propensity < cast.propensity)
                return -1;
            else return 0;
        }
        else return -1;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof PropensityNode)
        {
            return ((PropensityNode)o).reactionIndex == reactionIndex;
        }
        return false;
    }    
}
