package jp.ac.titech.itpro.sdl.quickmapsearch.placeapi;

/**
 * JSON解析の要素
 * Created by kengo on 16/07/09.
 */
public class Location {
    private static final String TAG = Location.class.getSimpleName();
    private double lat;
    private double lng;
    public Location(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }
    public double getLat(){
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLng(){
        return lng;
    }
    public void setLng(double lng){
        this.lng = lng;
    }
}
