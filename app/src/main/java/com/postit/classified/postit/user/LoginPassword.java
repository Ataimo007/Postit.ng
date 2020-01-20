package com.postit.classified.postit.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.postit.classified.postit.PostActivity;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.MainActivity;
import com.postit.classified.postit.R;
import com.postit.classified.postit.service.PostItService;


import androidx.appcompat.app.AppCompatActivity;

public class LoginPassword extends AppCompatActivity {

    private int id = -1;
    private PostItService postit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);

        init();
        initView();
    }

    private void  init()
    {
        postit = PostItService.getInstance();
    }

    private void  initView()
    {
//        Log.d( "Log_In_Extra_Info", "The bundle is " + bundle );
        id = getIntent().getIntExtra("id", -1 );
        TextView name = findViewById( R.id.login_name );
        name.setText( String.format( "Welcome %s", getIntent().getStringExtra( "first_name" ) ));
        setImage();
    }

    private void setImage()
    {
        String mediaUri = getIntent().getStringExtra("photo_link");
        if  ( mediaUri.isEmpty() )
            return;

        ImageView photo = findViewById( R.id.login_image );
        Uri media = Uri.parse( mediaUri );
        GlideApp.with( LoginPassword.this ).load( media ).placeholder( R.drawable.avatar2 )
                .fitCenter().into( photo );
//        photo.setImageURI(Uri.fromFile( new File( photoPath )) );
    }

    public void loginUser(View view)
    {
        view.setEnabled( false );
        String password = ( (TextInputLayout)findViewById(R.id.login_password) ).getEditText().getText().toString();

        Helper.Worker.executeAllTask(() -> {
            PostItService.PostItResponse response = postit.login(getId(), password);


            return () -> {
                response.respondToObject( result -> {
                    Intent user = createIntent();
                    Helper.showBundle( user.getExtras(), "Post_Ads" );
                    user.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                    prepareUser( user, result );
                    startActivity( user );
                }, () -> {
                  view.setEnabled( true );
                });
            };
        });
    }

    private Intent createIntent()
    {
        Intent user;
        Intent previous = getIntent();
        if ( previous.hasExtra("from") &&
                previous.getStringExtra("from").equals(PostActivity.class.getName()) )
        {
            user = new Intent( this, PostActivity.class );
            user.putExtra( "return_from", LoginPassword.class.getName() );
        }
        else
            user = new Intent( this, MainActivity.class );
        return user;
    }

    private void prepareUser(Intent intent, JsonObject response)
    {
        intent.putExtra( "id", response.getAsJsonPrimitive("id" ).getAsInt() );
        intent.putExtra( "gender", response.getAsJsonPrimitive("gender" ).getAsString() );
        intent.putExtra( "first_name", response.getAsJsonPrimitive("first_name" ).getAsString() );
        intent.putExtra( "last_name", response.getAsJsonPrimitive("last_name" ).getAsString() );

        JsonElement userName = response.get("user_name");
        Log.d( "Login_Password", "user name is " + userName );

        if ( userName.isJsonPrimitive() )
            intent.putExtra( "user_name", userName.getAsJsonPrimitive().getAsString() );
        else
            intent.putExtra( "user_name", intent.getStringExtra( "first_name" ) + " "
                + intent.getStringExtra( "last_name" ) );

        Log.d( "Login_Password", "user name is " + intent.getStringExtra("user_name") );

        intent.putExtra( "email", response.getAsJsonPrimitive("email" ).getAsString() );
        intent.putExtra( "phone", response.getAsJsonPrimitive("phone" ).getAsString() );
        intent.putExtra( "country_id", response.getAsJsonPrimitive("country_id" ).getAsInt() );
        intent.putExtra( "photo_link", response.getAsJsonPrimitive("photo_link" ).getAsString() );
        intent.putExtra( "user_type", response.getAsJsonPrimitive("user_type" ).getAsString() );
        intent.putExtra( "last_login", response.getAsJsonPrimitive("last_login" ).getAsString() );
        intent.putExtra( "created_at", response.getAsJsonPrimitive("created_at" ).getAsString() );
        intent.putExtra( "user_action", "LOGIN" );
    }

    public int getId() {
        return id;
    }

    public void resetPassword(View view)
    {

    }
}
