package com.example.jonet.lillehaua.Service;

import com.example.jonet.lillehaua.Common.Common;
import com.example.jonet.lillehaua.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Jone on 03.03.2018.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService{
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        if(Common.currentUser !=null)
            updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        if(Common.currentUser !=null){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(tokenRefreshed,false); //False because client side Token
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }}
}
