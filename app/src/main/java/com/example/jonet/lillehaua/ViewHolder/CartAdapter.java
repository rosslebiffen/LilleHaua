package com.example.jonet.lillehaua.ViewHolder;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.jonet.lillehaua.Cart;
import com.example.jonet.lillehaua.Common.Common;
import com.example.jonet.lillehaua.Database.Database;
import com.example.jonet.lillehaua.Model.Order;
import com.example.jonet.lillehaua.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        holder.txt_cart_name.setText(listData.get(position).getProductName());
        holder.txt_price.setText(listData.get(position).getPrice()+",-");
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
                int myCut  = (int)Math.round(0.012*total);
                total = total+myCut;


                cart.txtTotalPrice.setText(fmt.format(total));


            }
        });







    }

    @Override
    public int getItemCount() {

        return listData.size();

    }
    public Order getItem(int position)
    {
        return listData.get(position);
    }

    public void removeItem(int position){
        listData.remove(position);
        notifyItemRemoved(position);
    }
    public void restoreItem(Order item, int position)
    {
        listData.add(position, item);
        notifyItemInserted(position);
    }


}