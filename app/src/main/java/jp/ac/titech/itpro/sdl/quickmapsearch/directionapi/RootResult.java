package jp.ac.titech.itpro.sdl.quickmapsearch.directionapi;

import java.util.List;

/**
 *
 * Created by kengo on 16/07/10.
 */
public class RootResult {
    private Leg[] legs;
    public RootResult(Leg[] legs){
        this.legs = legs;
    }
    public Leg[] getLegs(){return legs;}
    public void setLegs(Leg[] legs){this.legs = legs;}
}
