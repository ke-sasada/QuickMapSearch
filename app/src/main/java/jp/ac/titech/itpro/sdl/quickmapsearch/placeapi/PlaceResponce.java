package jp.ac.titech.itpro.sdl.quickmapsearch.placeapi;

import java.util.List;

/**
 * Created by kengo on 16/07/09.
 */
public class PlaceResponce {
    private static final String TAG = PlaceResponce.class.getSimpleName();

    private List<PlaceResult> results;

    public PlaceResponce(List<PlaceResult> results){
        this.results = results;
    }

    public List<PlaceResult> getResults(){
        return results;
    }

    public void setResults(List<PlaceResult> results){
        this.results = results;
    }

}
