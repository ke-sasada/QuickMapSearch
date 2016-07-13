package jp.ac.titech.itpro.sdl.quickmapsearch.placeapi;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kengo on 16/07/09.
 */
public class ResultList {
    private List<PlaceResult> placeResultList;
    private MarkerOptions markerOptions;

    public ResultList(List<PlaceResult> placeResults, MarkerOptions markerOptions){
        this.placeResultList = new ArrayList<PlaceResult>();
        this.placeResultList.addAll(placeResults);
        this.markerOptions = markerOptions;
    }
    public List<PlaceResult> getPlaceResultList(){return placeResultList;}
    public MarkerOptions getMarkerOptions(){return  markerOptions;}
}
