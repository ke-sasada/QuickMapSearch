package jp.ac.titech.itpro.sdl.quickmapsearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kengo on 16/07/09.
 */
public class SearchItemList {
    private static final String TAG = SearchItemList.class.getSimpleName();

    List<SearchItem> itemList;

    private String name;

    public SearchItemList(String name,List<SearchItem> itemList){
        this.itemList = new ArrayList<SearchItem>();
        this.itemList.addAll(itemList);
        this.name = name;
    }

    public SearchItemList(String name){
        this.name = name;
        itemList = new ArrayList<SearchItem>();
    }

    public List<SearchItem> getItemList(){
        return itemList;
    }

    public void setItemList(List<SearchItem> itemList){
        itemList.clear();
        this.itemList.addAll(itemList);
    }

    public void addItem(SearchItem item){
        this.itemList.add(item);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

}
