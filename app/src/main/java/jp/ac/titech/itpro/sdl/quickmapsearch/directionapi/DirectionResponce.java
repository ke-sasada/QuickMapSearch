package jp.ac.titech.itpro.sdl.quickmapsearch.directionapi;

import java.util.List;

/**
 * Created by kengo on 16/07/10.
 */
public class DirectionResponce {
    private static final String TAG = DirectionResponce.class.getSimpleName();

    private List<RootResult> routes;

    public DirectionResponce(List<RootResult> routes){
        this.routes = routes;
    }

    public List<RootResult> getResults(){
        return routes;
    }

    public void setResults(List<RootResult> routes){
        this.routes = routes;
    }
}
