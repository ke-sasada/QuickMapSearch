package jp.ac.titech.itpro.sdl.quickmapsearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一つのブックマークを表すクラス
 * Created by kengo on 16/07/09.
 */
public class SearchItemList implements Serializable{

    private static final String TAG = SearchItemList.class.getSimpleName();
    private static final long serialVersionUID = -4957644179490359233L;
    int id;
    List<SearchItem> itemList;
    private String name;

    public SearchItemList(int id,String name,List<SearchItem> itemList){
        this.itemList = new ArrayList<SearchItem>();
        this.itemList.addAll(itemList);
        this.name = name;
        this.id = id;
    }

    public SearchItemList(int id,String name){
        this.name = name;
        itemList = new ArrayList<SearchItem>();
        this.id = id;
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

    public int getId(){return id;}

}
