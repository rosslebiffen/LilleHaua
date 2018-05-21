package com.example.jonet.lillehaua.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.jonet.lillehaua.Cart;
import com.example.jonet.lillehaua.Common.Common;
import com.example.jonet.lillehaua.Database.Database;
import com.example.jonet.lillehaua.Interface.ItemClickListener;
import com.example.jonet.lillehaua.Model.Order;
import com.example.jonet.lillehaua.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.System.load;

/**
 * Created by jonet on 23.09.2017.
 */


class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener{

    public TextView txt_cart_name,txt_price;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;

    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(View itemView) {
        super(itemView);
        txt_cart_name = (TextView)itemView.findViewById(R.id.cart_item_name);
        txt_price = (TextView)itemView.findViewById(R.id.cart_item_price);
        btn_quantity = (ElegantNumberButton)itemView.findViewById(R.id.btn_quantity);
        cart_image = (ImageView)itemView.findViewById(R.id.cart_image);

        itemView.setOnCreateContextMenuListener(this);

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select Action");
        contextMenu.add(0,0,getAdapterPosition(),Common.DELETE);



    }
}


public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>  {

    private List<Order> listData = new ArrayList<>();
    private Cart cart;


    public CartAdapter (List<Order> listData, Cart cart)
    {
        this.listData = listData;
        this.cart = cart;
    }


    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView = inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {

        Picasso.with(cart.getBaseContext())
                .load(listData.get(position).getImage())
                .resize(70,70) //70dp matching layout xml
                .centerCrop()
                .into(holder.cart_image);

     /*   TextDrawable drawable = TextDrawable.builder()
                .buildRound(""+listData.get(position).getQuantity(), Color.RED);
        holder.img_cart_count.setImageDrawable(drawable);  */
     holder.btn_quantity.setNumber(listData.get(position).getQuantity());
     holder.btn_quantity.setNumber(listData.get(position).getQuantity());
     holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
         @Override
         public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
             Order order = listData.get(position);
             order.setQuantity(String.valueOf(newValue));
             new Database(cart).updateCart(order);
                  //update txt total
                  //calculate total price
                         int total = 0;
                         List<Order>orders = new Database(cart).getCarts(Common.currentUser.getPhone());
                         for (Order item: orders)
                             total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(item.getQuantity()));
                         Locale locale = new Locale("en", "US");
                         NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                         cart.txtTotalPrice.setText(fmt.format(total));


         }
     });







    }

    @Override
    public int getItemCount() {

        return listData.size();

    }


}