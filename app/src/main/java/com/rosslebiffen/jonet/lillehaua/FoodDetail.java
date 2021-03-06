package com.rosslebiffen.jonet.lillehaua;

import android.app.ActionBar;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.rosslebiffen.jonet.lillehaua.Common.Common;
import com.rosslebiffen.jonet.lillehaua.Database.Database;
import com.rosslebiffen.jonet.lillehaua.Model.Food;
import com.rosslebiffen.jonet.lillehaua.Model.Order;
import com.rosslebiffen.jonet.lillehaua.Model.Rating;
import com.rosslebiffen.jonet.lillehaua.ViewHolder.CartAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.w3c.dom.Text;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name, food_price, food_description;
    ImageView img_food;
    CollapsingToolbarLayout collapsingToolbarLayout;
    CounterFab btnCart, btnCheckout;
    FloatingActionButton btnRating;
    RatingBar ratingBar;
    ElegantNumberButton numberButton;


    String foodId = "";

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;


    Food currentFood;

    @Override
    protected void onResume() {
        super.onResume();
        if(btnCart!=null){

            btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
            checkButtons();

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        //Firebase

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTbl = database.getReference("Rating");



        btnCheckout = (CounterFab) findViewById(R.id.fab1);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent (FoodDetail.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        btnCheckout.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        //InitView
        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = (CounterFab) findViewById(R.id.btnCart1);
        btnRating = (FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();

            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ifExists = new Database(getBaseContext()).checkFoodExists(foodId,Common.currentUser.getPhone());
                if(ifExists){
                    Toast.makeText(FoodDetail.this, "Du har allerede denne i handlevognen", Toast.LENGTH_SHORT).show();
                }
                if(!ifExists) {
                new Database(getBaseContext()).addToCart(new Order(
                        Common.currentUser.getPhone(),
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ));
                Toast.makeText(FoodDetail.this, "Lagt til i handlevogn", Toast.LENGTH_SHORT).show();
                btnCart.setCount(new Database(getBaseContext()).getCountCart(Common.currentUser.getPhone()));
                checkButtons();

            }}
        });
        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        food_description = (TextView) findViewById(R.id.food_description);
        food_name = (TextView) findViewById(R.id.food_name);
        food_price = (TextView) findViewById(R.id.food_price);
        img_food = (ImageView) findViewById(R.id.img_food);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //get food id from intent

        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()) {
            if (Common.isConnectedToInternet(getBaseContext())) {
                getDetailFood(foodId);
                getRatingFood(foodId);
            } else {
                Toast.makeText(FoodDetail.this, "Vennligst sjekk nettverksforbindelsen din ", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        checkButtons();

    }

    private void checkButtons(){


        boolean ifExists = new Database(getBaseContext()).checkFoodExists(foodId,Common.currentUser.getPhone());
        if (ifExists){
            btnCart.setVisibility(View.INVISIBLE);
            btnCheckout.setVisibility(View.VISIBLE);
        }else
        {
            btnCart.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.INVISIBLE);
        }
        btnCheckout.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));


    }

    private void getRatingFood(String foodId) {
        com.google.firebase.database.Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Send")
                .setNegativeButtonText("Avbryt")
                .setNoteDescriptions(java.util.Arrays.asList("Dårlig", "Ikke bra", "Helt OK", "Veldig bra", "Deilig"))
                .setDefaultRating(1)
                .setTitle("Ranger maten")
                .setDescription("Vennligst velg antall stjerner og gi oss tilbakemelding")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Kommentar?")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();

    }


    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datasnapshot) {
                currentFood = datasnapshot.getValue(Food.class); //S or s

                //set image
                Picasso.with(getBaseContext()).load(currentFood.getImage())
                        .into(img_food);

                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice()+",-");
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //Get rating and upload to firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);

        //fix user can rate multiple times
        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, "Takk for din tilbakemelding!", Toast.LENGTH_SHORT).show();
                    }
                });
        /*
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //Remove old value
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else
                {
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(FoodDetail.this, "Thank you for submitting your feedback", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); */

    }


}
