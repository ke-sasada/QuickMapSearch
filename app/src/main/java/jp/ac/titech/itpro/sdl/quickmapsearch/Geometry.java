package jp.ac.titech.itpro.sdl.quickmapsearch;

/**
 * Created by kengo on 16/07/09.
 */
public class Geometry {
    private static final String TAG = Geometry.class.getSimpleName();

    private Location location;

    public Geometry(Location location){this.location = location;}

    public Location getLocation(){return location;}

    public void setLocation(Location location){
        this.location = location;
    }

}