package jp.ac.titech.itpro.sdl.quickmapsearch.directionapi;

/**
 * JSON形式の要素
 * Created by kengo on 16/07/10.
 */
public class PolyLine {
    private String points;

    public PolyLine(String points){
        this.points = points;
    }
    public String getPoints(){return points;}
    public void setPoints(String points){this.points = points;}
}
