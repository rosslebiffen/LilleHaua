package com.rosslebiffen.jonet.lillehaua.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.rosslebiffen.jonet.lillehaua.Interface.ItemClickListener;
import com.rosslebiffen.jonet.lillehaua.R;

/**
 * Created by jonet on 24.09.2017.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress;

    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderAddress =(TextView)itemView.findViewById(R.id.order_address);
        txtOrderStatus =(TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone =(TextView)itemView.findViewById(R.id.order_phone);
        txtOrderId =(TextView)itemView.findViewById(R.id.order_id);

        itemView.setOnClickListener(this);


    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {


    }
}
