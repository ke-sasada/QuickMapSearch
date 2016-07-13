package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

/**
 * Created by kengo on 16/07/13.
 */
public class BookmarkListView extends ListView implements AdapterView.OnItemClickListener{
    private static final String TAG = BookmarkListView.class.getSimpleName();
    private View selectView;
    private int selectNo;

    public int getSelectNo(){return selectNo;}
    public void setSelectView(View v){this.selectView = v;}
    public View getSelectView(){return selectView;}
    public void setSelectNo(int No){selectNo = No;}

    public BookmarkListView(Context context) {
        super(context);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG,"onItemClick");
        if(!view.equals(selectView)){
            CheckBox checkBox = (CheckBox)view.findViewById(R.id.bookmark_checkbox);
            checkBox.setChecked(true);
            if(selectView != null) {
                checkBox = (CheckBox) selectView.findViewById(R.id.bookmark_checkbox);
                checkBox.setChecked(false);
            }
            selectView = view;
            selectNo = i;
        }
    }

}
