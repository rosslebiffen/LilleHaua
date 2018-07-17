package com.example.jonet.lillehaua;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.andremion.counterfab.CounterFab;
import com.example.jonet.lillehaua.Common.Common;
import com.example.jonet.lillehaua.Database.Database;
import com.example.jonet.lillehaua.Interface.ItemClickListener;
import com.example.jonet.lillehaua.Model.Category;
import com.example.jonet.lillehaua.Model.Favorites;
import com.example.jonet.lillehaua.Model.Food;
import com.example.jonet.lillehaua.Model.Order;
import com.example.jonet.lillehaua.ViewHolder.FoodViewHolder;
import com.example.jonet.lillehaua.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    CounterFab fabFoodList;
    public  FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    String categoryId="";

    //Search functionality
    MaterialSearchBar materialSearchBar;
    public FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();

    //favorites
    Database localDB;


    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        //Local db - Favorites
        localDB = new Database(this);


        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get intent here
                if(getIntent()!=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null)
                {
                    if(Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else
                    {
                        Toast.makeText(FoodList.this, "Vennligst sjekk nettverksforbindelsen din ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //Get intent here
                if(getIntent()!=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null)
                {
                    if(Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else
                    {
                        Toast.makeText(FoodList.this, "Vennligst sjekk nettverksforbindelsen din  ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //search
                materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
                materialSearchBar.setHint("Søk igjennom menyen vår");
                //materialSearchBar.setSpeechMode(false); no need, because we already define it at xml
                loadSuggest();

                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //when user types, update suggestions

                        List<String> suggest = new ArrayList<>();
                        for (String search : suggestList) //loop in suggestlist
                        {
                            if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //when search bar is closed
                        //restore original adapter
                        if(!enabled){
                            recyclerView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        //when search finishes
                        //show result of searchadapter
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler_food);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        /*LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
                R.anim.layout_from_left);
        recyclerView.setLayoutAnimation(controller);*/





        fabFoodList = (CounterFab) findViewById(R.id.fabFoodList);
        fabFoodList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent (FoodList.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        fabFoodList.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));






    }




    private void startSearch(CharSequence text) {
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        FirebaseRecyclerOptions<Food>foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image) ;
                //Quick Cart


                viewHolder.quickCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean ifExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                        if(ifExists){
                            Toast.makeText(FoodList.this, "Du har allerede dette i handlevognen", Toast.LENGTH_SHORT).show();
                        }
                        if(!ifExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()
                            ));
                            Toast.makeText(FoodList.this, "Lagt til i handlevognen", Toast.LENGTH_SHORT).show();
                            fabFoodList.setCount(new Database(getBaseContext()).getCountCart(Common.currentUser.getPhone()));

                        } else {
                            new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(),adapter.getRef(position).getKey());

                        }

                    }
                });




                //Add favorites
                if(localDB.isFavorite(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //Click to change state of Favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(position).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(model.getPrice());





                        if(!localDB.isFavorite(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                        {
                            localDB.addToFavorites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" ble lagt til i favoritter", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" ble fjernet fra favoritter", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Food local = model;

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //startNewActivity
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey()); //Send food id to new activity
                        startActivity(foodDetail);
                        finish();
                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); // set adapter for recycler view = show search results
    }

    @Override
    protected void onResume() {
        super.onResume();
        //fix click back foodetail and get no item in foodlist
        if(adapter != null)
            adapter.startListening();

        fabFoodList.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        if(searchAdapter !=null)
            searchAdapter.startListening();

    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName());//Add name of food to suggestList
                }
                materialSearchBar.setLastSuggestions(suggestList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void loadListFood(final String categoryId) {
        //Create query by category id
        Query searchByName = foodList.orderByChild("menuId").equalTo(categoryId);
        // create options with query
        FirebaseRecyclerOptions<Food>foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {


                if(model.getDescription().contains("A)")){


                }else{
                    viewHolder.btnA.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnA.setAlpha(50);
                }
                if(model.getDescription().contains("B)")){


                }else{
                    viewHolder.btnB.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnB.setAlpha(50);
                }
                if(model.getDescription().contains("E)")){


                }else{
                    viewHolder.btnE.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnE.setAlpha(50);
                }
                if(model.getDescription().contains("F)")){


                }else{
                    viewHolder.btnF.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnF.setAlpha(50);
                }
                if(model.getDescription().contains("G)")){


                }else{
                    viewHolder.btnG.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnG.setAlpha(50);
                }
                if(model.getDescription().contains("H)")){


                }else{
                    viewHolder.btnH.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnH.setAlpha(50);
                }
                if(model.getDescription().contains("J)")){


                }else{
                    viewHolder.btnJ.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnJ.setAlpha(50);
                }
                if(model.getDescription().contains("K)")){


                }else{
                    viewHolder.btnK.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnK.setAlpha(50);
                }
                if(model.getDescription().contains("L)")){


                }else{
                    viewHolder.btnL.setColorFilter(ContextCompat.getColor(FoodList.this,R.color.colorAllergyOff));
                    viewHolder.btnL.setAlpha(50);
                }
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format(model.getPrice().toString()+",-"));
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image) ;



                    //Quick Cart
                    viewHolder.quickCart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean ifExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            if (ifExists) {
                                Toast.makeText(FoodList.this, "Du har allerede dette i handlevognen", Toast.LENGTH_SHORT).show();
                            }
                            if (!ifExists) {
                                new Database(getBaseContext()).addToCart(new Order(
                                        Common.currentUser.getPhone(),
                                        adapter.getRef(position).getKey(),
                                        model.getName(),
                                        "1",
                                        model.getPrice(),
                                        model.getDiscount(),
                                        model.getImage()
                                ));
                                Toast.makeText(FoodList.this, "Lagt til i handlevogn", Toast.LENGTH_SHORT).show();
                                fabFoodList.setCount(new Database(getBaseContext()).getCountCart(Common.currentUser.getPhone()));

                            } else {
                                new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());

                            }

                        }
                    });

                    //Add favorites
                    if(localDB.isFavorite(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                        viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                    //Click to change state of Favorites
                    viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Favorites favorites = new Favorites();
                            favorites.setFoodId(adapter.getRef(position).getKey());
                            favorites.setFoodName(model.getName());
                            favorites.setFoodDescription(model.getDescription());
                            favorites.setFoodDiscount(model.getDiscount());
                            favorites.setFoodImage(model.getImage());
                            favorites.setFoodMenuId(model.getMenuId());
                            favorites.setUserPhone(Common.currentUser.getPhone());
                            favorites.setFoodPrice(model.getPrice());





                            if(!localDB.isFavorite(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                            {
                                localDB.addToFavorites(favorites);
                                viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                                Toast.makeText(FoodList.this, ""+model.getName()+" ble lagt til i favoritter", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                localDB.removeFromFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                                viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                Toast.makeText(FoodList.this, ""+model.getName()+" ble fjernet fra favoritter", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });






                final Food local = model;

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //startNewActivity
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey()); //Send food id to new activity
                        startActivity(foodDetail);
                    }
                });
            }


            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);


    }

    private void displayGluten() {
        int count = 0;
        count++;
        Toast.makeText(FoodList.this, count+ " items contains gluten", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter !=null)
            adapter.stopListening();
        if(searchAdapter !=null)
            searchAdapter.stopListening();
    }
}
