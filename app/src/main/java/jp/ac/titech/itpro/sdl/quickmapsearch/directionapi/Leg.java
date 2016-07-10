package jp.ac.titech.itpro.sdl.quickmapsearch.directionapi;

import java.util.List;

/**
 * Created by kengo on 16/07/10.
 */
public class Leg {
    Step[] steps;

    public Leg(Step[] steps){
        this.steps = steps;
    }

    public Step[] getSteps(){return steps;}
    public void setSteps(Step[] steps){this.steps = steps;}
}
