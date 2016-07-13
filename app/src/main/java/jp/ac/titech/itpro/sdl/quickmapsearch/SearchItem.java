package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

/**
 * Created by kengo on 16/07/09.
 */
public class SearchItem implements Serializable{
    private static final String TAG = SearchItem.class.getSimpleName();

    public enum SEARCH_TYPE{GENRE,WORD};

    private SEARCH_TYPE search_type;

    private float color;

    public SearchItem(SEARCH_TYPE type, String word, float color){
        this.search_type = type;
        this.word = word;
        this.color = color;
    }

    private String word;

    public String getWord(){return word;}
    public void setWord(String word){this.word = word; }
    public float getColor(){return color;}
    public SEARCH_TYPE getSearch_type(){return search_type;}

}
