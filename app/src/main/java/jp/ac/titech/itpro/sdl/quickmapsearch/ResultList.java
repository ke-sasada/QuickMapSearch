package jp.ac.titech.itpro.sdl.quickmapsearch;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kengo on 16/07/09.
 */
public class ResultList {

    private List<Result> resultList;
    private MarkerOptions markerOptions;

    public ResultList(List<Result> results, MarkerOptions markerOptions){
        this.resultList = new ArrayList<Result>();
        this.resultList.addAll(results);
        this.markerOptions = markerOptions;
    }

    public List<Result> getResultList(){return resultList;}
    public MarkerOptions getMarkerOptions(){return  markerOptions;}
}
