package com.example.jonet.lillehaua;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jonet.lillehaua.Common.Common;
import com.example.jonet.lillehaua.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText edtPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    TextView txtForgotPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        btnSignIn = (Button)findViewById(R.id.btnSignIn)    ;
        ckbRemember = (CheckBox)findViewById(R.id.ckbRemember);
        txtForgotPwd = (TextView)findViewById(R.id.txtForgotPwd);


        //init paper
        Paper.init(this);

        //Init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowForgotPwdDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick(View view) {

                if (Common.isConnectedToInternet(getBaseContext())) {


                    //Save loginInfo

                    if(ckbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());

                    }

                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Please Wait");
                    mDialog.show();


                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //Check if user doesn't exist in database
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                //Get user info
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString()); //Set phone
                                if (user.getPassword().equals(edtPassword.getText().toString())) {
                                    Intent homeIntent = new Intent(SignIn.this, Home.class);
                                    //store current user as variable
                                    Common.currentUser = user;
                                    startActivity(homeIntent);
                                    finish();

                                    table_user.removeEventListener(this);

                                } else {
                                    Toast.makeText(SignIn.this, "Sign in failed.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "User does not exist in database", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    Toast.makeText(SignIn.this, "Please check your internet connection ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            });
        }

    private void ShowForgotPwdDialog() {
        //forgot password dialog

    }
}

