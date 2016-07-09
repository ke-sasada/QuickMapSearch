package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by kengo on 16/07/09.
 */
public class SearchItem {
    private static final String TAG = SearchItem.class.getSimpleName();

    public enum SEARCH_TYPE{GENRE,WORD};

    private SEARCH_TYPE search_type;

    private MarkerOptions markerOptions;

    public SearchItem(SEARCH_TYPE type, String word, float color){
        this.search_type = type;
        this.word = word;
        markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
    }

    private String word;

    public String getWord(){return word;}
    public void setWord(String word){this.word = word;}
    public MarkerOptions getMarkerOptions(){return markerOptions;}

}
