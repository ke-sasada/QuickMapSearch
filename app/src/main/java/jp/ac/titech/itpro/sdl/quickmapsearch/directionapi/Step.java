package jp.ac.titech.itpro.sdl.quickmapsearch.directionapi;

/**
 * JSON形式の要素
 * Created by kengo on 16/07/10.
 */
public class Step {
    private PolyLine polyline;
    public Step(PolyLine polyline){
        this.polyline = polyline;
    }
    public PolyLine getPolyline(){return polyline;}
}
