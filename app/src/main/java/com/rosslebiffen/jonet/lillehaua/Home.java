package com.rosslebiffen.jonet.lillehaua;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.rosslebiffen.jonet.lillehaua.Common.Common;
import com.rosslebiffen.jonet.lillehaua.Database.Database;
import com.rosslebiffen.jonet.lillehaua.Interface.ItemClickListener;
import com.rosslebiffen.jonet.lillehaua.Model.Banner;
import com.rosslebiffen.jonet.lillehaua.Model.Category;
import com.rosslebiffen.jonet.lillehaua.Model.Token;
import com.rosslebiffen.jonet.lillehaua.ViewHolder.MenuViewHolder;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    CounterFab fabHome;
    private static final String TAG = Home.class.getName();
    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    SwipeRefreshLayout swipeRefreshLayout;




    //slider
    HashMap<String, String> image_list;
    SliderLayout mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);





        //View
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();

                }
                else
                {
                    Toast.makeText(Home.this, "Vennligst sjekk nettverksforbindelsen din  ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();

                }
                else
                {
                    Toast.makeText(Home.this, "Vennligst sjekk nettverksforbindelsen din  ", Toast.LENGTH_SHORT).show();
                }
            }
        });







        //Default run for the first time



        //Init firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        //Init paper
        Paper.init(this);



        fabHome = (CounterFab) findViewById(R.id.fabHome);
        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent (Home.this,Cart.class);
                startActivity(cartIntent);
            }
        });

        fabHome.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set name for user

        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Load menu

        recycler_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get CategoryId and send to new Activity

                        Intent foodList = new Intent(Home.this,FoodList.class);

                        //Get category key from item
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);

                    }
                });
            }

            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(itemView);
            }
        };

        if(Common.isConnectedToInternet(getBaseContext())) {
            loadMenu();

        }
        else
        {
            Toast.makeText(Home.this, "Vennligst sjekk nettverksforbindelsen din ", Toast.LENGTH_SHORT).show();

            swipeRefreshLayout.setRefreshing(false);
        }




        //Token service
        updateToken(FirebaseInstanceId.getInstance().getToken());

        //slider
        setupSlider();



    }

    private void setupSlider() {
        mSlider = (SliderLayout)findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banner = database.getReference("Banner");
        banner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Banner banner = postSnapShot.getValue(Banner.class);
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for(String key:image_list.keySet())
                {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this, FoodDetail.class);
                                    //send food id to foodDetail
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);

                                }
                            });
                    //Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId", idOfFood);

                    mSlider.addSlider(textSliderView);

                    //remove event after finishing
                    banner.removeEventListener(this);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fabHome.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        //fix click back button from food dont see categories
        if(adapter != null)
            adapter.startListening();
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        mSlider.startAutoCycle();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter !=null)
            adapter.stopListening();
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        mSlider.stopAutoCycle();
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false); //False because client side Token
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }


    private void loadMenu() {



        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_search)
            startActivity(new Intent(Home.this, SearchActivity.class));

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);

        }
        else if (id == R.id.nav_settings) {
            showSettingsDialog();

        }
        else if (id == R.id.nav_favorites) {
            Intent favoritesIntent = new Intent(Home.this,FavoritesActivity.class);
            startActivity(favoritesIntent);

        }else if (id == R.id.nav_log_out) {

            //Delete stored login info
            AccountKit.logOut();


            Intent signIn = new Intent(Home.this,MainActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);


        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Innstillinger");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_settings = inflater.inflate(R.layout.settings_layout, null);

        final CheckBox ckb_subscribe_news = (CheckBox)layout_settings.findViewById(R.id.ckb_sub_new);
        //Remember state of checkbox
        Paper.init(this);
        String isSubscribed = Paper.book().read("sub_new");
        if(isSubscribed == null || TextUtils.isEmpty(isSubscribed) || isSubscribed.equals("false"))
            ckb_subscribe_news.setChecked(false);
        else
            ckb_subscribe_news.setChecked(true);

        alertDialog.setView(layout_settings);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(ckb_subscribe_news.isChecked())
                {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);

                    //Write value
                    Paper.book().write("sub_new", "true");



                }
                else
                {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                    //Write value
                    Paper.book().write("sub_new", "false");
                }


            }
        });


        alertDialog.show();


    }


}
