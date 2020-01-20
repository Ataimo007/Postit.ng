package com.postit.classified.postit.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.R;
import com.postit.classified.postit.service.PostItService;

public class LoginUser extends AppCompatActivity
{
    private PostItService handler;
    private PostItService postit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        postit = PostItService.getInstance();
        init();
    }

    private void init()
    {
        handler = PostItService.getInstance();
    }

    public void loginUser(View view)
    {
        view.setEnabled( false );
        String email = ( (TextInputLayout)findViewById(R.id.login_email) ).getEditText().getText().toString().trim();

        Helper.Worker.executeAllTask( () -> {
            PostItService.PostItResponse response = postit.login(email);
            Log.d( "PostIt_Service", "the response is " + response );

            return () -> {
                response.respondToObject(object -> {
                    Intent user = new Intent( this, LoginPassword.class );
                    prepareUser( user, object );
                    startActivity( user );
                }, () -> {
                    view.setEnabled( true );
                });
            };
        });
    }

    private void prepareUser(Intent intent, JsonObject response)
    {
        intent.putExtra( "id", response.getAsJsonPrimitive("id" ).getAsInt() );
        intent.putExtra( "first_name", response.getAsJsonPrimitive("first_name" ).getAsString() );
        intent.putExtra( "photo", response.getAsJsonPrimitive("photo" ).getAsString() );
        intent.putExtra( "photo_link", response.getAsJsonPrimitive("photo_link" ).getAsString() );

        if ( getIntent().hasExtra( "from" ) )
            intent.putExtra( "from", getIntent().getStringExtra( "from" ) );
    }

    public void registerUser(View view)
    {
        Intent user = new Intent( this, Registration.class );
        startActivity( user );
    }
}
