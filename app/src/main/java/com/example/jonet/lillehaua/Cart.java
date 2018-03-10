package com.example.jonet.lillehaua;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jonet.lillehaua.Common.Common;
import com.example.jonet.lillehaua.Database.Database;
import com.example.jonet.lillehaua.Model.MyResponse;
import com.example.jonet.lillehaua.Model.Notification;
import com.example.jonet.lillehaua.Model.Order;
import com.example.jonet.lillehaua.Model.Request;
import com.example.jonet.lillehaua.Model.Sender;
import com.example.jonet.lillehaua.Model.Token;
import com.example.jonet.lillehaua.Remote.APIService;
import com.example.jonet.lillehaua.ViewHolder.CartAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity {
    public int numberOfItems;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //init service
        mService = Common.getFCMService();


        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView=(RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace =(FButton)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cart.size()> 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();


    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step");
        alertDialog.setMessage("Leave a comment:  ");


        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        final MaterialEditText edtComment = (MaterialEditText)order_address_comment.findViewById(R.id.edtComment) ;


        alertDialog.setView(order_address_comment); // Add edit Text to alert dialog
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Create new Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        "null",//address
                        txtTotalPrice.getText().toString(),
                        "0", //status
                        edtComment.getText().toString(),
                        cart
                );
                //Submit to Firebase
                // Use System.currentTimeMillis as key
               String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart();
                sendNotificationOrder(order_number);


                finish();

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
            alertDialog.show();

    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Token serverToken = postSnapShot.getValue(Token.class);

                    //Create raw payload to send
                    Notification notification = new Notification("Lille Haua", "You have a new Order "+order_number);
                    Sender content = new Sender(serverToken.getToken(),notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if(response.code()==200){
                                    if(response.body().success ==1){
                                        Toast.makeText(Cart.this, "Thank you, your order has been placed", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(Cart.this, "There was a problem submitting your order", Toast.LENGTH_LONG).show();
                                    }

                                }}

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
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
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        //calculate total price
        int total = 0;
        for (Order order:cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        //clear from database
        new Database(this).cleanCart();
        for (Order item:cart)
            new Database(this).addToCart(item);

        //refresh
        loadListFood();

    }
}

