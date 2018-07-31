package com.rosslebiffen.jonet.lillehaua.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.rosslebiffen.jonet.lillehaua.Common.Common;
import com.rosslebiffen.jonet.lillehaua.Interface.ItemClickListener;
import com.rosslebiffen.jonet.lillehaua.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txt_cart_name,txt_price;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;

    public RelativeLayout view_background;
    public LinearLayout view_foreground;

    private ItemClickListener itemClickListener;



    public CartViewHolder(View itemView) {
        super(itemView);
        txt_cart_name = (TextView)itemView.findViewById(R.id.cart_item_name);
        txt_price = (TextView)itemView.findViewById(R.id.cart_item_price);
        btn_quantity = (ElegantNumberButton)itemView.findViewById(R.id.btn_quantity);
        cart_image = (ImageView)itemView.findViewById(R.id.cart_image);
        view_background = (RelativeLayout)itemView.findViewById(R.id.view_background);
        view_foreground = (LinearLayout) itemView.findViewById(R.id.view_foreground);



    }
    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public void setTxt_price(TextView txt_price) {
        this.txt_price = txt_price;
    }

    @Override
    public void onClick(View view) {

    }

}