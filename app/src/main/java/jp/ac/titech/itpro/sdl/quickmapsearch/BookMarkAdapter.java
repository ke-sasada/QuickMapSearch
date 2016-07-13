package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by kengo on 16/07/13.
 */
public class BookMarkAdapter extends ArrayAdapter<String> {
    private final static String TAG = BookMarkAdapter.class.getSimpleName();

    private LayoutInflater inflater;
    String item;

    public BookMarkAdapter(Context context, int resource) {
        super(context, resource);
    }

    public BookMarkAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public BookMarkAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(R.layout.bookmark_itemlayout,null);
        }
        item = this.getItem(position);

        ImageButton imageButton = (ImageButton)convertView.findViewById(R.id.bookmark_delete);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Delete Button");
            }
        });

        TextView textView = (TextView) convertView.findViewById(R.id.bookmark_text);
        if(textView != null){
            textView.setText(item);
        }
        return convertView;

    }
}
