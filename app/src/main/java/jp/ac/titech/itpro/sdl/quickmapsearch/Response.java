package jp.ac.titech.itpro.sdl.quickmapsearch;

import java.util.List;

/**
 * Created by kengo on 16/07/09.
 */
public class Response {
    private static final String TAG = Response.class.getSimpleName();

    private List<Result> results;

    public Response(List<Result> results){
        this.results = results;
    }

    public List<Result> getResults(){
        return results;
    }

    public void setResults(List<Result> results){
        this.results = results;
    }

}
