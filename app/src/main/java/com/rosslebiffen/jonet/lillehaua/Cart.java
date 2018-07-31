package com.rosslebiffen.jonet.lillehaua;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rosslebiffen.jonet.lillehaua.Common.Common;
import com.rosslebiffen.jonet.lillehaua.Database.Database;
import com.rosslebiffen.jonet.lillehaua.Helper.RecyclerItemTouchHelper;
import com.rosslebiffen.jonet.lillehaua.Helper.testDateRange;
import com.rosslebiffen.jonet.lillehaua.Interface.RecyclerItemTouchHelperListener;
import com.rosslebiffen.jonet.lillehaua.Model.DataMessage;
import com.rosslebiffen.jonet.lillehaua.Model.MonthlyOrders;
import com.rosslebiffen.jonet.lillehaua.Model.MyResponse;
import com.rosslebiffen.jonet.lillehaua.Model.Order;
import com.rosslebiffen.jonet.lillehaua.Model.Request;
import com.rosslebiffen.jonet.lillehaua.Model.Token;
import com.rosslebiffen.jonet.lillehaua.Remote.APIService;
import com.rosslebiffen.jonet.lillehaua.ViewHolder.CartAdapter;
import com.rosslebiffen.jonet.lillehaua.ViewHolder.CartViewHolder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.SnackBar;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    public int myPercentage = 2;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;
    DatabaseReference monthRef;
    DatabaseReference logRef;
    private FirebaseAnalytics mFirebaseAnalytics;

    public TextView txtTotalPrice, cart_item_name, cart_item_price ;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //init service
        mService = Common.getFCMService();
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);


        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        logRef = database.getReference("Logbook");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);





        recyclerView=(RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace =(FButton)findViewById(R.id.btnPlaceOrder);
        cart_item_price = (TextView)findViewById(R.id.cart_item_price);
        cart_item_name = (TextView)findViewById(R.id.cart_item_name);














        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cart.size()> 0){
                   int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);




                    if(currentDay == 1) {
                        //sunday
                        Calendar cal = Calendar.getInstance(); //Create Calendar-Object
                        cal.setTime(new Date());               //Set the Calendar to now
                        int hour = cal.get(Calendar.HOUR_OF_DAY); //Get the hour from the calendar
                        if(hour < 22 && hour >= 10)              // Check if hour is between 8 am and 11pm
                        {
                            showAlertDialog();
                        }else if(hour >=2)
                        {
                            showAlertDialog();
                        }
                        else{
                            Toast.makeText(Cart.this, "Beklager, vi har bare åpent mellom 10 og 22 på søndager", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else if (currentDay == 7){

                        //saturday
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        testDateRange clock  = new testDateRange();
                        if(clock.isTimeBetweenTwoHours(9, 2, cal)) {
                            showAlertDialog();
                        }
                        else{
                            Toast.makeText(Cart.this, "Beklager, vi har bare åpent mellom 9 og 02 på lørdager", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        //any day
                        Calendar cal = Calendar.getInstance(); //Create Calendar-Object
                        cal.setTime(new Date());               //Set the Calendar to now
                        int hour = cal.get(Calendar.HOUR_OF_DAY); //Get the hour from the calendar


                        if(hour <= 22 && hour >= 9)              // Check if hour is between 8 am and 11pm
                        {
                            showAlertDialog();
                        }else{
                            Toast.makeText(Cart.this, "Beklager, vi har bare åpent mellom 9 og 22 på hverdager", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else{
                    Toast.makeText(Cart.this, "Handlevognen din er tom", Toast.LENGTH_SHORT).show();
            }}
        });

        loadListFood();


    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Bare en ting til");
        alertDialog.setMessage("Legg ved kommentar:  ");


        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        final MaterialEditText edtComment = (MaterialEditText)order_address_comment.findViewById(R.id.edtComment) ;


        alertDialog.setView(order_address_comment); // Add edit Text to alert dialog
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayList<String> orderNames = new ArrayList<>();

                List<Order>orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                for (Order item: orders)
                    orderNames.add(item.getProductName());

                String orderName = "null";
                if(orderNames.size()<3){
                    orderName = orderNames.toString();

                }else
                {
                    orderName = orderNames.get(1)+", "+ orderNames.get(2)+"...";
                }

                //Create new Request
                final Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        orderName,//address
                        txtTotalPrice.getText().toString(),
                        "0", //status
                        edtComment.getText().toString(),
                        cart
                );

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                sdf.setTimeZone(TimeZone.getDefault());
                String currentDateandTime = sdf.format(new Date());

                Bundle params = new Bundle();
                params.putString("Timestamp", currentDateandTime);
                params.putString("Phone", Common.currentUser.getPhone());
                params.putString("OrderName", orderName);
                params.putString("TotalPrice", txtTotalPrice.getText().toString());
                params.putString("Comment", edtComment.getText().toString());
                params.putString("CartItems", cart.toString());
                mFirebaseAnalytics.logEvent("Order_submitted", params);

                Calendar c = Calendar.getInstance();
                int month = c.get(Calendar.MONTH);



                if(month == 1){
                    monthRef = database.getReference("Orders/1");
                }

                if (month == 2){
                    monthRef = database.getReference("Orders/2");
                    }

                if(month == 3){
                    monthRef = database.getReference("Orders/3");
                }

                if(month == 4){
                    monthRef = database.getReference("Orders/4");
                }

                if(month == 5){
                    monthRef = database.getReference("Orders/5");
                }

                if(month == 6){
                    monthRef = database.getReference("Orders/6");
                }

                if(month == 7){
                    monthRef = database.getReference("Orders/7");
                }

                if(month == 8){
                    monthRef = database.getReference("Orders/8");
                }

                if(month == 9){
                    monthRef = database.getReference("Orders/9");
                }

                if(month == 10){
                    monthRef = database.getReference("Orders/10");
                }

                if(month == 11){
                    monthRef = database.getReference("Orders/11");
                }

                if(month == 12){
                    monthRef = database.getReference("Orders/12");
                }

                logRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();

                        String strCount = Long.toString(count+1);
                        logRef.child(strCount)
                                .setValue(request);



                    }
                    public void onCancelled(DatabaseError databaseError) { }
                });

                logRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();

                        String strCount = Long.toString(count+1);

                        logRef.child(strCount)
                                .setValue(request);



                    }
                    public void onCancelled(DatabaseError databaseError) { }
                });




                //Submit to Firebase
                // med System.currentTimeMillis som key

               /* String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);*/

                logRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();

                        String strCount = Long.toString(count+1);

                        requests.child(strCount)
                                .setValue(request);



                    }
                    public void onCancelled(DatabaseError databaseError) { }
                });

                monthRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();

                        String strCount = Long.toString(count+1);
                        monthRef.child(strCount)
                                .setValue(request);


                        sendNotificationOrder(strCount);
                    }
                    public void onCancelled(DatabaseError databaseError) { }
                });










                //Delete cart
                new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());





                finish();

            }
        });
        alertDialog.setNegativeButton("Angre", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
            alertDialog.show();

    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        final Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Token serverToken = postSnapShot.getValue(Token.class);

                    //Create raw payload to send
                  //  Notification notification = new Notification("Lille Haua", "You have a new Order "+order_number);
                    //Sender content = new Sender(serverToken.getToken(),notification);
                    Map<String,String> dataSend = new HashMap<>();
                    dataSend.put("message", "Du har nye bestillinger "+ order_number);
                    DataMessage dataMessage = new DataMessage(serverToken.getToken(),dataSend);


                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if(response.code()==200){
                                    if(response.body().success ==1){
                                        Toast.makeText(Cart.this, "Takk, din bestilling er sendt", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(Cart.this, "Det var et problem ved sending av bestillingen din", Toast.LENGTH_LONG).show();
                                    }

                                }}

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculate total price
        int total = 0;
        for (Order order:cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));





        Locale locale = new Locale("no", "NO");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);



        if(total >0) {
            total = total + myPercentage;
            txtTotalPrice.setText(fmt.format(total));
        }else {
            total = total;
            txtTotalPrice.setText(fmt.format(total));
        }



    }




    private void deleteCart(int position) {
        cart.remove(position);
        //clear from database
        new Database(this).cleanCart(Common.currentUser.getPhone());
        for (Order item:cart)
            new Database(this).addToCart(item);

        //refresh
        loadListFood();

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof CartViewHolder)
        {
            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();
            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());


            //update txt total
            //calculate total price
            int total = 0;
            List<Order>orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item: orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("no", "NO");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            if(total >0) {
                total = total + myPercentage;
                txtTotalPrice.setText(fmt.format(total));
            }else {
                total = total;
                txtTotalPrice.setText(fmt.format(total));
            }

            //Make snackbar

            Snackbar snackBar = Snackbar.make(rootLayout, name + " fjernet fra handlevogn", Snackbar.LENGTH_LONG);
            snackBar.setAction("ANGRE", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //update txt total
                    //calculate total price
                    int total = 0;
                    List<Order>orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item: orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("no", "NO");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    if(total >0) {
                        total = total + myPercentage;
                        txtTotalPrice.setText(fmt.format(total));
                    }else {
                        total = total;
                        txtTotalPrice.setText(fmt.format(total));
                    }


                }
            });
            snackBar.setActionTextColor(Color.YELLOW);
            snackBar.show();

        }

    }
}

