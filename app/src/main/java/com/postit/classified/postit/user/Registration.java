package com.postit.classified.postit.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.core.widget.NestedScrollView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.postit.classified.postit.R;
import com.postit.classified.postit.dialog.CountryFragment;
import com.postit.classified.postit.dialog.FilterBottomSheet;
import com.postit.classified.postit.service.PostItService;
import com.postit.classified.postit.helpers.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.postit.classified.postit.helpers.Helper.Worker.executeTask;

public class Registration extends AppCompatActivity {

    private static final int PICTURE_REQUEST = 42;
    private Uri currentUri;
    private PostItService postit;
    private TextInputEditText countryField;
    private Pair<Integer, String> countryPair;
    private RadioGroup genderGroup;

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_main);

//        initLoader();
        init();
        initCountrols();
//        initView();
    }

    private void initCountrols() {
        initCountry();
        genderGroup = findViewById( R.id.reg_gender );
    }

//    private void initLoader()
//    {
//        ImageView loader = findViewById(R.id.registration_loader);
//        GlideApp.with( this ).load( R.drawable.loader3 ).placeholder( R.drawable.loader3 )
//                .into( loader );
//    }
//
//    private void doneLoading() {
//        ImageView loader = findViewById(R.id.registration_loader);
//        loader.setVisibility( View.GONE );
//    }

    private void init()
    {
        postit = PostItService.getInstance();
    }

    private void countryInitialValues() {
        countryPair = new Pair<>( 160, "Nigeria" );
        countryField.setText( countryPair.second );
    }

    private void initView()
    {
//        initScroll();
//        initConfirmPassword();
//        initPassword();
    }

    private void initPassword() {

    }

    private void initConfirmPassword() {
        TextInputLayout text = findViewById(R.id.reg_password_confirm);
        text.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                TextInputLayout password =  findViewById(R.id.reg_password);
                if ( !password.getEditText().getText().toString().equals(s.toString()) )
                {
                    text.setBoxStrokeColor( 25202222 );
                }
            }
        });
    }

    private void initScroll() {
        NestedScrollView scroll = findViewById(R.id.reg_scroll);
        MaterialButton reg = findViewById(R.id.reg_register);
        scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
        {
            if ( scrollY - oldScrollY < 1 )
            {
                ScaleAnimation anim = new ScaleAnimation( 0, 1, 0, 1,
                        reg.getWidth() / 2, reg.getHeight() / 2 );
                anim.setDuration( 500 );
                reg.setAnimation(anim);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        reg.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim.startNow();
            }
            else
            {
                ScaleAnimation anim = new ScaleAnimation( 1, 0, 1, 0,
                        reg.getWidth() / 2, reg.getHeight() / 2);
                anim.setDuration( 500 );
                reg.setAnimation(anim);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        reg.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim.startNow();
            }
        });
    }

//    private void initCountryChoices()
//    {
//        Spinner countrySpinner = findViewById(R.id.reg_country);
//        executeTask(() ->
//        {
//            AppDatabase countries = AppDatabase.getInstance( this );
//            List<String> countryNames = countries.getAllCountryNames();
//            int currentIndex = countryNames.indexOf( getCurrentLocation() );
//            Log.d( "Country_Names", "The list his " + countryNames );
//            return (Helper.UIAction) () ->
//            {
//                countrySpinner.setAdapter( new ArrayAdapter<String>( this, R.layout.support_simple_spinner_dropdown_item,
//                        countryNames.toArray( new String[0] )));
//                countrySpinner.setSelection( currentIndex );
//            };
//        });
//    }

    private void initCountryOnline()
    {
        Spinner countrySpinner = findViewById(R.id.reg_country);
        executeTask(() ->
        {
            PostItService.PostItResponse response = postit.getCountries();
            return (Helper.UIAction) () ->
            {
                response.respondToArray( jsonElements -> {
                    ArrayList<String> countryNames = getCountries(jsonElements);

                    countrySpinner.setAdapter( new ArrayAdapter<String>( this, R.layout.support_simple_spinner_dropdown_item,
                            countryNames));

                    countrySpinner.setSelection( getCurrentLocation( countryNames ) );
                }, () -> {} );
            };
        });
    }

    private String[] getCountries2(JsonArray countries) {
        String[] countryNames = new String[ countries.size() ];
        for ( int i = 0; i < countries.size(); ++i )
            countryNames[ i ] = countries.get( i ).getAsJsonPrimitive().getAsString();
        return countryNames;
    }

    private ArrayList<String> getCountries(JsonArray countries) {
        ArrayList<String> list = new ArrayList<>( countries.size() );
        for (JsonElement country : countries) {
            list.add( country.getAsJsonPrimitive().getAsString() );
        }
        return list;
    }

    // use precise method e.g. gps or google map service or gprs
    private int getCurrentLocation(ArrayList<String> countryNames)
    {
        // Nigeria
        return countryNames.indexOf( "Nigeria" );
    }

    private String getCurrentLocation()
    {
        return "Nigeria";
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void changePicture(View view)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICTURE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
//                getFile( uri );
                showImage(uri);
            }
        }
    }

//    private void getFile( Uri uri )
//    {
//
//        try ( Cursor cursor = getContentResolver().query(uri, null, null, null, null, null); )
//        {
//            if (cursor != null && cursor.moveToFirst())
//            {
//                String displayName = cursor.getString(
//                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                Log.d( "Examing_URI","File_Display_Name: " + displayName);
//                Log.d( "Examing_URI","Column_Names: " + Arrays.toString(cursor.getColumnNames()));
//                for ( String column : cursor.getColumnNames() )
//                    Log.d( "Examing_URI","Column_Name: " + column );
//                try( FileOutputStream fileOutputStream = openFileOutput(displayName, MODE_PRIVATE);
//                     InputStream inputStream = getContentResolver().openInputStream(uri) )
//                {
//                    copy( inputStream, fileOutputStream, 1024 );
//                    File pic = getFileStreamPath(displayName);
//                    Log.d( "Examing_URI","New_File: " + pic.getAbsolutePath() );
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void copy( InputStream source, FileOutputStream dest, int buffesSize ) throws IOException {
//        do {
//            byte[] buffer = new byte[Math.min( source.available(), buffesSize )];
//            dest.write( source.read( buffer ) );
//        }   while   ( source.available() <= 0 );
//    }

    private void showBitmap( Uri uri, ImageView view )
    {
        executeTask( () ->
        {
            Bitmap image = null;
            try ( InputStream imageStream = getContentResolver().openInputStream(uri) )
            {
                image = BitmapFactory.decodeStream(imageStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap finalImage = image;
            return () ->
            {
                if ( finalImage != null )
                    view.setImageBitmap(finalImage);
            };
        });
    }

    private void showImage(Uri uri)
    {
        ImageView view = findViewById(R.id.app_bar_image);

        showBitmap( uri, view );
//        view.setImageURI( uri );

        currentUri = uri;
    }

//    private Bitmap getBitmapFromUri(Uri uri) throws IOException
//    {
////        getContentResolver().
//        ParcelFileDescriptor parcelFileDescriptor =
//                getContentResolver().openFileDescriptor(uri, "r");
//        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//        uri.q
//
//        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
//        parcelFileDescriptor.close();
//        return image;
//    }

    private void initCountry() {
        countryField = findViewById( R.id.reg_country_field );
        countryField.setCursorVisible( false );
        countryField.setOnTouchListener( (v, event) -> {
            if ( event.getAction() == MotionEvent.ACTION_UP )
            {
                Log.d( "Post_Activity", "Region is pressed" );
                FilterBottomSheet region = FilterBottomSheet.listFilters( this::handleCountry, new CountryFragment() );
                region.show( getSupportFragmentManager(), "REGION" );
            }
            return countryField.performClick();
        });
        countryInitialValues();
    }

    private void handleCountry(Pair<Integer, String>[] pairs) {
        countryPair = pairs[0];
        countryField.setText( countryPair.second );
    }

    private String getRadioValue( RadioGroup group )
    {
        RadioButton radio = findViewById(group.getCheckedRadioButtonId());
        return radio.getText().toString();
    }

    public void register(View view)
    {
        view.setEnabled( false );
        String firstName = ((TextInputLayout) findViewById( R.id.reg_first_name )).getEditText().getText().toString();
        String lastName = ((TextInputLayout) findViewById( R.id.reg_last_name )).getEditText().getText().toString();
        String email = ((TextInputLayout) findViewById( R.id.reg_email )).getEditText().getText().toString();
        String phone = ((TextInputLayout) findViewById( R.id.reg_phone_number )).getEditText().getText().toString();
        String password = ((TextInputLayout) findViewById( R.id.reg_password )).getEditText().getText().toString();
        String cPassword = ((TextInputLayout) findViewById( R.id.reg_password_confirm )).getEditText().getText().toString();
        String gender = getRadioValue( genderGroup );
        int countryId = countryPair != null ? countryPair.first : -1;
//        String photo = currentUri == null ? null : new File( currentUri.getPath() ).getAbsolutePath();
        Uri photo = currentUri;
        Log.d( "Registration_Details", String.format("%s, %s, %s, %s, %s, %s, %s", firstName, lastName, email, phone,
                password, gender, countryId ) );
        if ( !firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !password.isEmpty()
                && !gender.isEmpty() && countryId != -1 && password.equals(cPassword) )
        {
            register( firstName, lastName, email, phone, password, gender, countryId, photo, view);
        }
        else
        {
            Snackbar.make(view, "Please Ensure you fill the form properly", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            view.setEnabled( true );
        }
    }

//    private void register(String firstName, String lastName, String email, String phone, String password, String gender, String country,
//                          String photo) {
//        //register to database
//        Log.d( "Registrated", String.format("%s, %s, %s, %s, %s, %s, %s", firstName, lastName, email, phone,
//                password, gender, country ) );
//        handler.register( firstName, lastName, email, password, 160, gender, phone, photo, this,
//                handler.getDefaultSuccessResponce( this,
//                        "The email you entered his used by someone already", jsonObject -> {
//                    Toast toast = Toast.makeText(this, "Register Successfully", Toast.LENGTH_SHORT);
//                    toast.show();
//                    finish();
//                }));
//    }

    private void register(String firstName, String lastName, String email, String phone, String password, String gender, int countryId,
                          Uri photo, View view)
    {
        //register to database
        Log.d( "Registrated", String.format("%s, %s, %s, %s, %s, %s, %d", firstName, lastName, email, phone,
                password, gender, countryId ) );
        executeTask(() -> {
            PostItService.PostItResponse response = postit.register(firstName, lastName, email, password, countryId, gender, phone, photo, this);

            return () -> {
                response.respondToPrimitive(element -> {
                    Helper.toast( this, element.getAsString() );
                    finish();
                }, () -> {
                    view.setEnabled( true );
                });
            };
        });
    }
}
