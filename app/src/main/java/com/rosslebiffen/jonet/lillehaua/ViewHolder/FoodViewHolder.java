package com.rosslebiffen.jonet.lillehaua.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rosslebiffen.jonet.lillehaua.Interface.ItemClickListener;
import com.rosslebiffen.jonet.lillehaua.R;

/**
 * Created by jonet on 21.09.2017.
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name, food_price;
    public ImageView food_image;
    public ImageView fav_image, quickCart;
    public ImageView btnA, btnB, btnE, btnF, btnG, btnH, btnJ, btnK, btnL;


    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super(itemView);
        food_name = (TextView)itemView.findViewById(R.id.food_name);
        food_price = (TextView)itemView.findViewById(R.id.food_price);
        food_image = (ImageView)itemView.findViewById(R.id.food_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);
        quickCart= (ImageView)itemView.findViewById(R.id.btn_quick_cart);
        btnA = (ImageView) itemView.findViewById(R.id.btnA);
        btnB = (ImageView) itemView.findViewById(R.id.btnB);
        btnE = (ImageView) itemView.findViewById(R.id.btnE);
        btnF = (ImageView) itemView.findViewById(R.id.btnF);
        btnG = (ImageView) itemView.findViewById(R.id.btnG);
        btnH = (ImageView) itemView.findViewById(R.id.btnH);
        btnJ = (ImageView) itemView.findViewById(R.id.btnJ);
        btnK = (ImageView) itemView.findViewById(R.id.btnK);
        btnL = (ImageView) itemView.findViewById(R.id.btnL);



        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
