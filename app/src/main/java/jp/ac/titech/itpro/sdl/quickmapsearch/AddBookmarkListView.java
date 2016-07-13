package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by kengo on 16/07/13.
 */
public class AddBookmarkListView extends ListView implements AdapterView.OnItemClickListener{
    private static final String TAG = BookmarkListView.class.getSimpleName();;
    private ArrayList<Integer> checkedNo = new ArrayList<Integer>();

    public ArrayList<Integer> getCheckedNo(){return checkedNo;}

    public AddBookmarkListView(Context context) {
        super(context);
    }

    public AddBookmarkListView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public AddBookmarkListView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
    }

    public void clearChecked(){
        checkedNo.clear();
    }

     @Override
     public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         Log.d(TAG,"onItemClick");
         CheckBox checkBox = (CheckBox)view.findViewById(R.id.bookmark_checkbox);
         if(checkBox.isChecked()){
             checkBox.setChecked(false);
             Iterator itr = checkedNo.iterator();
             while(itr.hasNext()){
                int next = (Integer)itr.next();
                 if(next == i ){
                     itr.remove();
                     break;
                 }
             }
         }else{
             checkBox.setChecked(true);
             checkedNo.add(i);
         }
    }
}
