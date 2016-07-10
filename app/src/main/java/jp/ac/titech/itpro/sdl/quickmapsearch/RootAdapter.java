package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.content.Context;
import android.provider.DocumentsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kengo on 16/07/10.
 */
public class RootAdapter extends RecyclerView.Adapter<RootAdapter.ViewHolder> {


    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mDataList;

    public RootAdapter(Context context, ArrayList<String> dataList) {
        super();
        mLayoutInflater = LayoutInflater.from(context);
        mDataList = dataList;
    }

    @Override
    public RootAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.root_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RootAdapter.ViewHolder holder, int position) {
        String data = (String) mDataList.get(position);
        holder.text.setText(data);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public ViewHolder(View v) {
            super(v);
            // 2
            text = (TextView) v.findViewById(R.id.root_name);
        }
    }
}
