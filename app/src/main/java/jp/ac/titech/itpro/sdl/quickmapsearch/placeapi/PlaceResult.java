package jp.ac.titech.itpro.sdl.quickmapsearch.placeapi;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by kengo on 16/07/09.
 */
public class PlaceResult {

    private static final String TAG = PlaceResult.class.getSimpleName();

    private Geometry geometry;
    private String icon;
    private String id;
    private String name;
    private String place_id;
    private String rating;
    private String reference;
    private String[] types;
    private String vicinity;
    private MarkerOptions markerOptions;

    public PlaceResult(Geometry geometry, String icon,
                       String id, String  name, String place_id, String rating, String reference, String[] types, String vicinity){
        this.geometry = geometry;
        this.icon = icon;
        this.id = id;
        this.name = name;
        this.place_id = place_id;
        this.rating = rating;
        this.reference = reference;
        this.types = types;
        this.vicinity = vicinity;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public void setMarkerOptions(MarkerOptions m){ this.markerOptions = m;}

    public MarkerOptions getMarkerOptions(){return markerOptions;}

}
