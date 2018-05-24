package com.example.jonet.lillehaua;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jonet.lillehaua.Common.Common;
import com.example.jonet.lillehaua.Model.User;
import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {
    Button btn_continue;
    TextView txtSlogan;

    private static final int REQUEST_CODE = 7171;

    FirebaseDatabase database;
    DatabaseReference users;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FacebookSdk.sdkInitialize(getApplicationContext());
    AccountKit.initialize(this);

    setContentView(R.layout.activity_main);


    btn_continue = (Button)findViewById(R.id.btn_continue);

    txtSlogan = (TextView)findViewById(R.id.txtSlogan);

    printKeyHash();



    //init firebase
    database = FirebaseDatabase.getInstance();
    users = database.getReference("User");




    btn_continue.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        startLoginSystem();

        }
    });


   //Check session FB account kit
    if(AccountKit.getCurrentAccessToken() !=null)
    {
        //create dialog
        final AlertDialog waitingDialog = new SpotsDialog(this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait");
        waitingDialog.setCancelable(false);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                //login
                users.child(account.getPhoneNumber().toString())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User localUser = dataSnapshot.getValue(User.class);

                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                //store current user as variable
                                Common.currentUser = localUser;
                                startActivity(homeIntent);
                                finish();
                                waitingDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                waitingDialog.dismiss();
                            }
                        });
                waitingDialog.dismiss();

            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
    }
}



    private void startLoginSystem() {
        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE)
        {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(result.getError() !=null)
            {
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                return;
            }
            else if(result.wasCancelled())
            {
                Toast.makeText(this, "Cancel",Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                if(result.getAccessToken()!=null)
                {
                    //Show dialog
                    AlertDialog waitingDialog = new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Please wait");
                    waitingDialog.setCancelable(false);

                    //Get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone = account.getPhoneNumber().toString();

                            //Check if user exists in firebase
                            users.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.child(userPhone).exists())
                                            {
                                                //Create new user and login
                                                User newUser = new User();
                                                newUser.setPhone(userPhone);
                                                newUser.setName("");

                                                //submit to firebase
                                                users.child(userPhone)
                                                        .setValue(newUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                    Toast.makeText(MainActivity.this, "User Registered successfully",Toast.LENGTH_SHORT).show();

                                                                //login
                                                                users.child(userPhone)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                User localUser = dataSnapshot.getValue(User.class);

                                                                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                                //store current user as variable
                                                                                Common.currentUser = localUser;
                                                                                startActivity(homeIntent);
                                                                                finish();
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                            }
                                                        });
                                            }
                                            else //if exists
                                            {
                                                //login
                                                users.child(userPhone)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                User localUser = dataSnapshot.getValue(User.class);

                                                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                //store current user as variable
                                                                Common.currentUser = localUser;
                                                                startActivity(homeIntent);
                                                                finish();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this,""+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

    }

    private void printKeyHash() {
       try{
           PackageInfo info = getPackageManager().getPackageInfo("com.example.jonet.lillehaua",
                   PackageManager.GET_SIGNATURES);
           for (Signature signature:info.signatures)
           {
               MessageDigest md = MessageDigest.getInstance("SHA");
               md.update(signature.toByteArray());
               Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
           }
       } catch (PackageManager.NameNotFoundException e) {
           e.printStackTrace();
       } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
       }
    }


}

