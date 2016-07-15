package org.hackathon_ocw.androidclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.hackathon_ocw.androidclient.R;
import org.hackathon_ocw.androidclient.domain.Item;

import java.util.List;

/**
 * Created by Foam on 2016/7/15.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyItemHolder> {
    private List<Item> itemList;

    public class MyItemHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail;

        public MyItemHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public MyItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row, parent, false);

        return new MyItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyItemHolder holder, int position) {
        Item item = itemList.get(position);
        holder.title.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
