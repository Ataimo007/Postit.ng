package com.postit.classified.postit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.postit.classified.postit.helpers.GifSizeFilter;
import com.postit.classified.postit.helpers.Glide4Engine;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.GsonHelpers;
import com.postit.classified.postit.dialog.BodyStyleFragment;
import com.postit.classified.postit.dialog.CategoryFragment;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.dialog.BrandFragment;
import com.postit.classified.postit.dialog.CityFragment;
import com.postit.classified.postit.dialog.CountryFragment;
import com.postit.classified.postit.dialog.FilterBottomSheet;
import com.postit.classified.postit.dialog.ModelFragment;
import com.postit.classified.postit.dialog.StateFragment;
import com.postit.classified.postit.dialog.SubCategoryFragment;
import com.postit.classified.postit.service.PostItService;
import com.postit.classified.postit.user.LoginPassword;
import com.postit.classified.postit.user.LoginUser;
import com.synnapps.carouselview.CarouselView;
import com.whiteelephant.monthpicker.MonthPickerDialog;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.joda.time.DateTime;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

public class PostActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_CHOOSE = 100;
    private Pair<Integer, String>[] region;

    // Text fields
    private TextInputEditText regionField;
    private TextInputEditText categoryField;
    private TextInputEditText brandField;
    private TextInputEditText modelField;

    // Text fields layouts
    private TextInputLayout brandLayout;
    private TextInputLayout modelLayout;
    private Pair<Integer, String> categoryPair;
    private Pair<Integer, String> brandPair;
    private Pair<Integer, String> modelPair;
    private PostItService postit;
    private Pair<Integer, String> subCategoryPair;

    private View vehicleExtras;
    private TextInputEditText yearField;
    private TextInputEditText bodyStyleField;
    private TextInputEditText mileageField;
    private RadioGroup transmissionsGroup;
    private Pair<Integer, String> bodyStylePair;
    private MonthPickerDialog yearPicker;

    private ArrayList< Uri > selectedImages = new ArrayList<>( 10 );

    private CarouselView imageSelector;
    private ImageView emptyImage;

    private final int MAX_SELECTABLE = 10;
    private ConstraintLayout emptyLabel;
    private FloatingActionButton removeBottom;
    private TextInputEditText titleField;
    private TextInputEditText descField;
    private RadioGroup typeGroup;
    private RadioGroup conditionGroup;
    private CheckBox negotiable;
    private TextInputEditText priceField;
    private TextInputEditText addressField;
    private HashMap<String, Integer> transmissions;
    private Years year;

    private TextInputLayout titleLayout;
    private TextInputLayout priceLayout;
    private TextInputLayout regionLayout;
    private TextInputLayout categoryLayout;
    private TextInputLayout descriptionLayout;
    private TextInputLayout addressLayout;
    private boolean vehicle = false;
    private TextInputLayout yearLayout;
    private TextInputLayout bodyStyleLayout;
    private TextInputLayout mileageLayout;

    private boolean hasBrand;
    private boolean hasModel;
    private boolean hasTrans;
    private boolean hasBodyStyle;
    private ProgressBar progress;
    private FloatingActionButton postButton;
    private FloatingActionButton addBottom;
//    private ArrayList<Uri> currentMedias;
    private HashMap< Uri, Integer> currentMedias;
    private ProgressInformer progressInformer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.post_ad );
        init();
        initControls();
        processAction();
    }

    private void processAction() {
        if ( isEdit() )
        {
            initializeFields();
        }
    }

    private boolean isEdit()
    {
        String action = getIntent().getAction();
        return action != null && action.equals("edit");
    }

    private void init()
    {
        postit = PostItService.getInstance();
    }

    private void initProgress() {
        progressInformer = new ProgressInformer(this, progress);
    }

    private void initControls() {
        initLayouts();
        initViews();
        initRegion();
        initCategory();
        initBrand();
        initModel();
        initImageCarousel();
        initPrice();
        initProgress();
    }

    private void initMileage() {
        mileageField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mileageField.removeTextChangedListener( this );
                Helper.updateNumberField( mileageField, s.toString() );
                mileageField.addTextChangedListener( this );
            }
        });
    }

    private void initPrice() {
        priceField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                priceField.removeTextChangedListener( this );
                Helper.updateNumberField( priceField, s.toString() );
                priceField.addTextChangedListener( this );
            }
        });
    }

    private void initImageCarousel() {
        imageSelector.setImageListener((position, imageView) -> {
            Uri uri = selectedImages.get(position);
            if ( URLUtil.isNetworkUrl( uri.toString() ) )
            {
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                GlideApp.with( this ).load( uri ).placeholder( R.drawable.logo2 )
                        .fitCenter().into( imageView );
            }
            else
                imageView.setImageURI(uri);
        });
    }

    private void hideRemoveButton()
    {
        removeBottom.hide();
    }

    private void hideAddButton()
    {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) addBottom.getLayoutParams();
        p.setAnchorId(View.NO_ID);
        addBottom.setLayoutParams(p);
        addBottom.hide();
    }

    private void showCarousel()
    {
        emptyImage.setVisibility( View.GONE );
        emptyLabel.setVisibility( View.GONE );
        removeBottom.show();
    }

    private void hideCarousel()
    {
        emptyImage.setVisibility( View.VISIBLE );
        emptyLabel.setVisibility( View.VISIBLE );
        removeBottom.hide();
    }

    private void reloadImageCarousel() {
        if ( selectedImages.size() > 0 )
            showCarousel();
        else
            hideCarousel();

        imageSelector.setPageCount( selectedImages.size() );

//        if  ( selectedImages.size() == MAX_SELECTABLE )
//            hideAddButton();
//
//        if  ( selectedImages.size() == 0 )
//            hideRemoveButton();
    }

    private void initViews() {
        priceField = findViewById(R.id.ad_price_field);
        regionField = findViewById(R.id.ad_region_field);
        titleField = findViewById(R.id.ad_title_field);
        descField = findViewById(R.id.ad_description_field);
        categoryField = findViewById(R.id.ad_category_field);
        brandField = findViewById(R.id.ad_brand_field);
        addressField = findViewById(R.id.ad_address_field);
        modelField = findViewById(R.id.ad_model_field);

        typeGroup = findViewById(R.id.ad_type_group);
        conditionGroup = findViewById(R.id.ad_conditions);
        negotiable = findViewById(R.id.ad_negotiable);

        imageSelector = findViewById(R.id.product_image_viewer);
        emptyImage = findViewById(R.id.empty_shuttle);
        emptyLabel = findViewById(R.id.empty_image_label);
        removeBottom = findViewById(R.id.ad_remove_select);
        addBottom = findViewById(R.id.ad_select_image);

        progress = findViewById( R.id.post_progress );
        postButton = findViewById( R.id.post_ad );

//        initGroups();
    }

    private void initLayouts()
    {
        titleLayout = findViewById(R.id.ad_title );
        priceLayout = findViewById(R.id.ad_price );
        regionLayout = findViewById(R.id.ad_region );
        categoryLayout = findViewById(R.id.ad_category );
        descriptionLayout = findViewById(R.id.ad_description );
        addressLayout = findViewById(R.id.ad_address );
        brandLayout = findViewById(R.id.ad_brand);
        modelLayout = findViewById(R.id.ad_model);
    }

    private void initGroups()
    {
        typeGroup.check( 0 );
        conditionGroup.check( 0 );
    }

    private void initRegion() {
        regionField.setCursorVisible( false );
        regionField.setOnTouchListener( (v, event) -> {
            if ( event.getAction() == MotionEvent.ACTION_UP )
            {
                Log.d( "Post_Activity", "Region is pressed" );
                FilterBottomSheet region = FilterBottomSheet.listFilters( this::handleRegion, new CountryFragment(), new StateFragment(),
                        new CityFragment() );
                region.setShowFilterButton( true );
                region.show( getSupportFragmentManager(), "REGION" );
            }
            return regionField.performClick();
        });
    }

    private void showBrand()
    {
        brandLayout.setVisibility( View.VISIBLE );
        hasBrand = true;
    }

    private void hideBrand()
    {
        brandLayout.setVisibility( View.GONE );
        hasBrand = false;
    }

    private void showModel()
    {
        modelLayout.setVisibility( View.VISIBLE );
        hasModel = true;
    }

    private void hideModel()
    {
        modelLayout.setVisibility( View.GONE );
        hasModel = false;
    }

    private void initCategory() {
        categoryField.setCursorVisible( false );
        categoryField.setOnTouchListener( (v, event) -> {
            if ( event.getAction() == MotionEvent.ACTION_UP )
            {
                Log.d( "Post_Activity", "Category is pressed" );
                FilterBottomSheet region = FilterBottomSheet.listFilters( this::handleCategory, new CategoryFragment(),
                        new SubCategoryFragment() );
                region.show( getSupportFragmentManager(), "CATEGORY" );
            }
            return categoryField.performClick();
        });
    }

    private void initBrand() {
        brandField.setCursorVisible( false );
        brandField.setOnTouchListener( (v, event) -> {
            if ( event.getAction() == MotionEvent.ACTION_UP )
            {
                Log.d( "Post_Activity", "Category is pressed" );
                Bundle firstArg = new Bundle();
                firstArg.putInt( "category_id", subCategoryPair.first );
                FilterBottomSheet region = FilterBottomSheet.listFilters( this::handleBrand, firstArg, new BrandFragment(), new ModelFragment() );
                region.show( getSupportFragmentManager(), "BRAND" );
            }
            return brandField.performClick();
        });
    }

    private void initModel() {
        modelField.setCursorVisible( false );
        modelField.setOnTouchListener( (v, event) -> {
            if ( event.getAction() == MotionEvent.ACTION_UP )
            {
                Log.d( "Post_Activity", "Category is pressed" );
                Bundle firstArg = new Bundle();
                firstArg.putInt( "brand_id", brandPair.first );
                FilterBottomSheet region = FilterBottomSheet.listFilters( this::handleModel, firstArg, new ModelFragment() );
                region.show( getSupportFragmentManager(), "MODEL" );
            }
            return modelField.performClick();
        });
    }

    private void initBodyStyles() {
        bodyStyleField.setCursorVisible( false );
        bodyStyleField.setOnTouchListener( (v, event) -> {
            if ( event.getAction() == MotionEvent.ACTION_UP )
            {
                Log.d( "Post_Activity", "Body Style is pressed" );
                FilterBottomSheet region = FilterBottomSheet.listFilters( this::handleBodyStyle, new BodyStyleFragment());
                region.show( getSupportFragmentManager(), "BODY_STYLE" );
            }
            return bodyStyleField.performClick();
        });
    }

    private void initYear() {
        initYearPicker();
        yearField.setCursorVisible( false );
        yearField.setOnTouchListener( (v, event) -> {
            if ( event.getAction() == MotionEvent.ACTION_UP )
            {
                yearPicker.show();
            }
            return yearField.performClick();
        });
    }

    private void initYearPicker()
    {
        DateTime now = DateTime.now();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this, (selectedMonth, selectedYear) -> {
            year = Years.years(selectedYear);
            yearField.setText( String.valueOf( year.getYears() ) );
        }, now.getYear(), now.getMonthOfYear());
        yearPicker = builder.showYearOnly().setActivatedYear(now.getYear()).setTitle("The Year of Car").build();
    }

    private void initTransmissions( int ...props )
    {
        Helper.Worker.executeTask( () -> {
            PostItService.PostItResponse transmissions = postit.getTransmissions();
            return () -> {
                transmissions.respondToArray( jsonElements -> {
                    initTransmissionValues();
                    transmissionsGroup.removeAllViews();
                    for (JsonElement element : jsonElements)
                    {
                        String transmission = element.getAsJsonObject().getAsJsonPrimitive("transmission").getAsString();
                        int transmissionId = element.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
                        addTransmission( transmissionId, transmission );

                        RadioButton radioButton = new RadioButton(this);
                        RadioGroup.LayoutParams radioParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                                RadioGroup.LayoutParams.WRAP_CONTENT, 1);
                        radioButton.setLayoutParams( radioParams );
                        radioButton.setClickable( true );
                        radioButton.setFocusable( true );
                        radioButton.setText( transmission );

                        transmissionsGroup.addView( radioButton );
                    }

                    if ( props != null && props.length != 0 )
                    {
                        setRadioValue( transmissionsGroup, getTransmission( props[0] ) );
                    }
                    RadioButton first = (RadioButton) transmissionsGroup.getChildAt(0);
                    first.setChecked( true );
                });
            };
        });

    }

    private void initTransmissionValues() {
        transmissions = new HashMap<>(2);
    }

    private void addTransmission(int transmissionId, String transmission) {
        transmissions.put( transmission, transmissionId );
    }

    private int getTransmission(String transmission )
    {
        return transmissions.get( transmission );
    }

    private String getTransmission( int transmission )
    {
        for (Map.Entry<String, Integer> entry : transmissions.entrySet() )
        {
            if ( entry.getValue() == transmission )
                return entry.getKey();
        }
        return null;
    }

    private void handleBodyStyle(Pair<Integer, String>[] pairs) {
        bodyStylePair = pairs[ 0 ];
        bodyStyleField.setText( pairs[ 0 ].second );
    }

    private void handleModel(Pair<Integer, String>[] result) {
        modelPair = result[0];
        modelField.setText( modelPair.second );
    }

    private void handleBrand(Pair<Integer, String>[] result) {
        brandPair = result[0];
        brandField.setText( brandPair.second );
        modelPair = result[1];
        modelField.getText().clear();
        if ( modelPair != null )
        {
            showModel();
            modelField.setText( modelPair.second );
        }
        else
        {
            hideModel();
            modelField.setText( "" );
        }
    }

    private void handleRegion(Pair<Integer, String>[] result)
    {
        region = result;
        for ( int i = result.length - 1; i >= 0; --i )
        {
            if ( result[ i ] != null )
            {
                regionField.setText( result[ i ].second );
                break;
            }
        }
    }

    private PostItService.PostItResponse getSubcategory()
    {
        return postit.getSubCategory( subCategoryPair.first );
    }

    private void handleCategory(Pair<Integer, String>[] result)
    {
        categoryPair = result[0];
        subCategoryPair = result[1];
        if ( subCategoryPair != null )
            categoryField.setText( subCategoryPair.second );
        else
            categoryField.setText( categoryPair.second );


        if ( subCategoryPair != null )
        {
            Helper.Worker.executeTask(() -> {
                PostItService.PostItResponse subcategory = getSubcategory();
                return () -> {
                    subcategory.respondToObject( jsonObject -> {
                        Log.d( "Post_Activity", "subcategory is " + jsonObject );
                        int brandCount = jsonObject.getAsJsonPrimitive("brand_count").getAsInt();

                        hasTrans = jsonObject.getAsJsonPrimitive("has_trans").getAsInt() == 1;
                        hasBodyStyle = jsonObject.getAsJsonPrimitive("has_body_style").getAsInt() == 1;

                        Log.d( "Post_Activity", "has transmission " + hasTrans );
                        Log.d( "Post_Activity", "has body style " + hasBodyStyle );

                        if ( brandCount > 0 )
                        {
                            brandPair = null;
                            brandField.getText().clear();

                            showBrand();
                        }
                        else
                            hideBrand();

                        if (hasTrans)
                            initVehicleExtras();
                        else
                            hideVehicleExtras();

                    });
                };
            });
        }
    }

    private void initVehicleLayouts()
    {
        vehicle = true;
        yearLayout = findViewById(R.id.ad_year);
        bodyStyleLayout = findViewById(R.id.ad_body_style);
        mileageLayout = findViewById(R.id.ad_mileage);
    }

    private void initVehicleExtras( int ...props )
    {
        vehicleExtras = findViewById(R.id.vehicle_extras);
        vehicleExtras.setVisibility( View.VISIBLE );

        initVehicleLayouts();
        yearField = vehicleExtras.findViewById(R.id.ad_year_field);
        bodyStyleField = vehicleExtras.findViewById(R.id.ad_body_style_field);
        mileageField = vehicleExtras.findViewById( R.id.ad_mileage_field);
        transmissionsGroup = vehicleExtras.findViewById(R.id.ad_transmission_group);

        if ( !hasBodyStyle )
            bodyStyleField.setVisibility( View.GONE );

        initYear();
        initBodyStyles();
        initTransmissions( props );
        initMileage();
    }

    private void hideVehicleExtras()
    {
        vehicle = false;
        hasTrans = false;
        hasBodyStyle = false;

        vehicleExtras = findViewById(R.id.vehicle_extras);
        vehicleExtras.setVisibility( View.GONE );
    }

    public void selectImage(View view) {
        if  ( acquireStoragePermision() )
        {
            if   ( selectedImages.size() < MAX_SELECTABLE )
                selectImage();
            else
                Helper.toast( this, "Max Images Uploaded" );

        }

    }

    private void selectImage()
    {
        Matisse.from(this)
                .choose(MimeType.ofAll())
                .countable(true)
                .maxSelectable(MAX_SELECTABLE - selectedImages.size())
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .capture( true )
                .captureStrategy(new CaptureStrategy(true, "postit.provider" ))
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request.
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if ( requestCode == 0 )
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            }
        }
    }

    private boolean acquireStoragePermision() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0 );
            return false;
        }
        return true;
    }

//    private void acquireStoragePermision() {
//        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(thisActivity,
//                Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Permission is not granted
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
//                    Manifest.permission.READ_CONTACTS)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed; request the permission
//                ActivityCompat.requestPermissions(thisActivity,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        } else {
//            // Permission has already been granted
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> mSelected = Matisse.obtainResult(data);
            addImages( mSelected );
            reloadImageCarousel();
            Log.d("Matisse", "mSelected: " + mSelected);
        }
    }

    private void addImages(List<Uri> mSelected) {
        selectedImages.addAll( mSelected );
    }

    public void removeImage(View view) {
        int currentItem = imageSelector.getCurrentItem();
        selectedImages.remove( currentItem );
        reloadImageCarousel();
    }

    private String getRadioValue( RadioGroup group )
    {
        RadioButton radio = findViewById(group.getCheckedRadioButtonId());
        return radio.getText().toString();
    }

    private void setRadioValue( RadioGroup group, String value )
    {
        if ( value != null && !value.isEmpty() )
        {
            ArrayList<View> radios = new ArrayList<>();
            group.findViewsWithText( radios, value, View.FIND_VIEWS_WITH_TEXT );
            RadioButton radio = (RadioButton) radios.get( 0 );
            radio.setChecked( true );
        }
        else
        {
            RadioButton radio = ( RadioButton ) group.getChildAt( 0 );
            radio.setChecked( true );
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Helper.showBundle( intent.getExtras(), "Post_Ads" );
        String returnFrom = intent.getStringExtra("return_from");
        if ( returnFrom.equals(LoginPassword.class.getName()) )
            postAd( intent );
    }

    private boolean isLogin()
    {
        int userId = getIntent().getIntExtra("id", -1  );
        if ( userId == -1 && !isEdit() )
        {
            Intent intent = new Intent( this, LoginUser.class );
            intent.putExtra( "from", PostActivity.class.getName() );
            Helper.toast( this, "Please Login to Post Your Ad" );
            startActivity( intent );
            return false;
        }
        return true;
    }

    private void processing()
    {
        progress.setIndeterminate( true );
        postButton.hide();
    }

    private void endProcessing()
    {
        progress.setIndeterminate( false );
        postButton.show();
    }

    public void postAd(View view)
    {
        processing();

        postAd();
    }

    private boolean hasIntProperty(JsonObject detail, String name)
    {
        return detail.has( name ) && detail.get( name ).getAsInt() != 0;
    }

    private void initializeFields()
    {
        long id = getIntent().getLongExtra( "ad_id", -1 );

        Helper.Worker.executeAllTask( () -> {
            PostItService.PostItResponse response = postit.getAdsInfos( id );

            return () -> {

                response.respondToObject( object -> {
                    Log.d( "Populating_Entries", object.toString() );
                    JsonObject detail = object.getAsJsonObject("ad");
                    JsonArray medias = object.getAsJsonArray("medias");

                    initializeMedias( medias );
                    initializeFields( detail );

                }, () -> {});
            };
        });
    }

    private void initializeMedias(JsonArray medias) {
        currentMedias = new HashMap<>();
        for ( JsonElement element : medias )
        {
            JsonObject media = element.getAsJsonObject();
            Uri link = Uri.parse(GsonHelpers.getString(media, "media_link"));
            int id = GsonHelpers.getInt(media, "id");
            GlideApp.with(this).load(link).preload();
            currentMedias.put(link, id);
        }

        addImages( Lists.newArrayList( currentMedias.keySet() ) );
        reloadImageCarousel();
//        hideCarouselButtons();
    }

    private void initializeFields( JsonObject detail )
    {
        titleField.setText( GsonHelpers.getString( detail, "title" ) );
        descField.setText( GsonHelpers.getString( detail, "description" ) );
        addressField.setText( GsonHelpers.getString( detail, "address" ) );
        priceField.setText( GsonHelpers.getString( detail, "price" ) );
        categoryPair = new Pair<>( GsonHelpers.getInt( detail, "category_id" ),
                GsonHelpers.getString( detail, "category_name" )  );
        subCategoryPair = new Pair<>( GsonHelpers.getInt( detail, "sub_category_id" ),
                GsonHelpers.getString( detail, "sub_category_name" ) );
        categoryField.setText( GsonHelpers.getString( detail, "category_name" ) );

        if ( GsonHelpers.getInt( detail, "transmission_id" ) != 0 )
        {
            hasBodyStyle = hasIntProperty(detail, "body_style_id" );

            initVehicleExtras( GsonHelpers.getInt( detail, "transmission_id" ) );

            year = Years.years( GsonHelpers.getInt( detail, "year" ) );
            yearField.setText( String.valueOf( GsonHelpers.getInt( detail, "year" ) ) );

            if ( hasIntProperty(detail, "body_style_id" ) )
            {
                bodyStylePair = new Pair<>( GsonHelpers.getInt( detail, "body_style_id" ),
                        GsonHelpers.getString( detail, "body_style_name" ) );
                bodyStyleField.setText( GsonHelpers.getString( detail, "body_style_name" ) );
            }

            mileageField.setText( GsonHelpers.getString( detail, "mileage" ) );

//            setRadioValue( transmissionsGroup, getTransmission( GsonHelpers.getInt( detail, "transmission_id" ) ) );

        }

        Log.d( "Edit_Post", "show brand " + GsonHelpers.getInt( detail, "brand_count" ) );
        if ( GsonHelpers.getInt( detail, "brand_count" ) != 0 )
        {
            if ( hasIntProperty( detail, "brand_id" ) )
                brandPair = new Pair<>( GsonHelpers.getInt( detail, "brand_id" ),
                        GsonHelpers.getString( detail, "brand_name" ) );
            showBrand();
            Log.d( "Edit_Post", "showing brand" );
            brandField.setText( brandPair.second );
        }

        Log.d( "Edit_Post", "show model " + GsonHelpers.getInt( detail, "model_count" ) );
        if ( GsonHelpers.getInt( detail, "model_count" ) != 0 )
        {
            if ( hasIntProperty(detail, "brand_model_id" ) )
                modelPair = new Pair<>( GsonHelpers.getInt( detail, "brand_model_id" ),
                        GsonHelpers.getString( detail, "model_name" ) );
            showModel();
            Log.d( "Edit_Post", "showing model" );
            modelField.setText( modelPair.second );
        }

        region = new Pair[ 3 ];
        region[ 0 ] = new Pair<>( GsonHelpers.getInt( detail, "country_id" ),
                GsonHelpers.getString( detail, "country_name" ) );
        region[ 1 ] = new Pair<>( GsonHelpers.getInt( detail, "state_id" ),
                GsonHelpers.getString( detail, "state_name" ) );
        region[ 2 ] = new Pair<>( GsonHelpers.getInt( detail, "city_id" ),
                GsonHelpers.getString( detail, "city_name" ) );

        for ( int i = region.length - 1; i >= 0; --i )
        {
            if ( region[ i ] != null )
            {
                regionField.setText( region[ i ].second );
                break;
            }
        }

        setRadioValue( typeGroup, GsonHelpers.getString( detail, "type" ) );
        setRadioValue( conditionGroup, GsonHelpers.getString( detail, "ad_condition" ) );
//        setRadioValue( typeGroup, "private" );
//        setRadioValue( conditionGroup, "nigerian used" );

        if (GsonHelpers.getString( detail, "is_negotiable" ).equals( "1" ) )
            negotiable.setChecked( true );
    }

    private void postAd( Intent intent )
    {
        Helper.Worker.executeTask( () -> {

            String title = titleField.getText().toString();
            String slug = title.toLowerCase();
            String address = addressField.getText().toString();
            String description = descField.getText().toString();
            String price = Helper.valueRepresentation( priceField.getText().toString() );

            String condition = getRadioValue( conditionGroup );
            String negotiable = this.negotiable.isChecked() ? "1" : "0";

            int cat = categoryPair.first;
            int subCat = 0;
            if ( subCategoryPair != null )
                subCat = subCategoryPair.first;

            int brand = 0;
            if ( hasBrand )
                brand = brandPair != null && brandPair.first != null ? brandPair.first : 0;

            int model = 0;
            if ( hasModel )
                model = modelPair != null && modelPair.first != null ? modelPair.first : 0;


            String type = getRadioValue( typeGroup );

            String mileage = "";
            int transmission = 0, years = 0;

            if ( isVehicle() )
            {
                mileage = this.mileageField.getText().toString();
                transmission = getTransmission( getRadioValue( transmissionsGroup ) );
                years = year != null ? year.getYears() : 0;
            }

            int body = 0;
            if  ( hasBodyStyle )
                body = bodyStylePair != null && bodyStylePair.first != null ? bodyStylePair.first : 0;

            String sellerName = intent.getStringExtra( "user_name" );
            String sellerEmail = intent.getStringExtra( "email" );
            String sellerPhone = intent.getStringExtra( "phone" );
            int userId = intent.getIntExtra("id", -1  );

            int country = region[ 0 ] != null ? region[ 0 ].first : 0;
            int state = region[ 1 ] != null ? region[ 1 ].first : 0;
            int city = region[ 2 ] != null ? region[ 2 ].first : 0;

            // for each action
            if ( isEdit() )
            {
                long adId = getIntent().getLongExtra("ad_id", -1 );

//                ArrayList< Integer > removeMedias = new ArrayList<>();
                Iterator<Uri> iterator = selectedImages.iterator();

                while ( iterator.hasNext() )
                {
                    Uri media = iterator.next();
                    if ( currentMedias.containsKey( media ) )
                    {
//                        int id = currentMedias.get(media);
//                        removeMedias.add( id );
                        currentMedias.remove( media );
                        iterator.remove();
                    }
                }

                Integer[] ids = currentMedias.values().toArray( new Integer[0] );
                Uri[] medias = selectedImages.toArray(new Uri[0]);
//                Integer[] ids = removeMedias.toArray(new Integer[0]);

                // image update
                PostItService.PostItResponse posted = postit.updateAd(adId, title, slug, description, cat, subCat, brand, model, body,
                        transmission, type, condition, mileage, years, price, negotiable, sellerName, sellerEmail, sellerPhone,
                        country, state, city, address, userId, ids, medias, progressInformer );

                return () -> {
                    posted.respondToPrimitive(object -> {
                        String msg = object.getAsString();
                        Helper.toast( this, msg );
                        setResult( RESULT_OK, intent );
                        progressInformer.end();
                        endProcessing();
                        finish();
                    });
                };

            }
            else
            {
                Uri[] medias = selectedImages.toArray(new Uri[0]);

                PostItService.PostItResponse posted = postit.postAd(title, slug, description, cat, subCat, brand, model, body,
                        transmission, type, condition, mileage, years, price, negotiable, sellerName, sellerEmail, sellerPhone,
                        country, state, city, address, userId, medias, progressInformer );

                return () -> {
                    posted.respondToObject(object -> {
//                        String msg = object.getAsString();
                        String msg = object.get( "message" ).getAsString();
                        Helper.toast( this, msg );
                        setResult( RESULT_OK, intent );
                        progressInformer.end();
                        endProcessing();
                        finish();
                    });
                };
            }



        });
    }

    private void postAd()
    {
        Log.d( "Post_Activity", "Verifying ads" );
        if  ( !verification() )
        {
            Helper.toast( this, "Please Ensure you provide all the necessary information" );
            endProcessing();
            return;
        }
        Log.d( "Post_Activity", "Checking if user as profile" );

        if  ( !isLogin() )
            return;
        Log.d( "Post_Activity", "Posting ads" );

        postAd( getIntent() );

    }

    private boolean verification() {
        boolean verify = verifyField( titleField, titleLayout, "Your Ad must have a title" );
        verify &= verifyField( regionField, regionLayout, "Where is the region of the Ad" );
        verify &= verifyField( categoryField, categoryLayout, "What is the category of the Ad" );
        verify &= verifyField( descField, descriptionLayout, "Give your Ad a description" );
        verify &= verifyField( priceField, priceLayout, "How much is your Ad" );
        verify &= verifyField( addressField, addressLayout, "Where is the Ad" );
        
        if  ( hasBrand() )
            verify &= verifyField( brandField, brandLayout, "What Brand is the Ad" );
        
        if  ( hasModel() )
            verify &= verifyField( modelField, modelLayout, "What Model is the Ad" );
        
        Log.d( "Post_Activity", "Verifying ads regular " + verify );
        if  ( isVehicle() )
        {
            verify &= verifyField( yearField, yearLayout, "What Year is the Model" );
            verify &= verifyField( bodyStyleField, bodyStyleLayout, "What Body Style is the Model" );
            verify &= verifyField(mileageField, mileageLayout, "What is the Mileage of the Vehicle" );
            Log.d( "Post_Activity", "Verifying vehicle ads regular " + verify );
        }
        Log.d( "Post_Activity", "Verifying after vehicle ads regular " + verify );

        return verify;
    }

    private boolean hasModel() {
        return hasModel;
    }

    private boolean hasBrand() {
        return hasBrand;
    }

    private boolean verifyField( TextInputEditText field, TextInputLayout layout, String error ) {
        if  ( field.getText().toString().isEmpty() )
        {
            layout.setError( error );
            return false;
        }
        return true;
    }

    public boolean isVehicle() {
        return vehicle;
    }

    public void setVehicle(boolean vehicle) {
        this.vehicle = vehicle;
    }

    public void finish(View view) {
        finish();
    }

    public class ProgressInformer
    {
        private final ProgressBar progress;
        private final Context context;

        public ProgressInformer(Context context, ProgressBar progress) {
            this.context = context;
            this.progress = progress;
            progress.setMax( 100 );
        }

        public void begin()
        {
            progress.setIndeterminate( false );
            setValue( 0 );
        }

        public void end()
        {
            setValue( 100 );
        }

        public int getValue()
        {
            return progress.getProgress();
        }

        public void setValue(int value )
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progress.setProgress( value, true );
            }
            else
                progress.setProgress( value );
        }

        public void showMessage( String message )
        {
            Helper.toast( context, message );
        }
    }
}
