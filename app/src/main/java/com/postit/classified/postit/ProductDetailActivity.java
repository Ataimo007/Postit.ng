package com.postit.classified.postit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.postit.classified.postit.dialog.ShareBottomSheet;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.GsonHelpers;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.service.PostItService;
import com.synnapps.carouselview.CarouselView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

//        implements NavigationView.OnNavigationItemSelectedListener
public class ProductDetailActivity extends AppCompatActivity
{
    private PostItService postit;
    private JsonObject detail;
    private JsonArray medias;
    private Toolbar toolbar;
    private long ads_id;
//    private boolean toolbarVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initStatus();
        setContentView(R.layout.product_detail);

        init();
        setToolbar();

        handleIntent();
    }

    private void initStatus() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void handleIntent() {
        String action = getIntent().getAction();
        if ( action != null && action.equals( Intent.ACTION_VIEW ) )
            fromExternal();
        else
            fromInternal();
        getProductInfo();
    }

    private void fromExternal()
    {
        Uri data = getIntent().getData();
        List<String> paths = data.getPathSegments();
        ads_id = Long.parseLong(paths.get(1));
    }

    private void init()
    {
        postit = PostItService.getInstance();
    }

    private void initViews() {
//        initCollapseToolbar();
        initToolbar();
        initImageViewer();
        initProductInfo();
        initSpecification();
        initDescription();
        initSellersInfo();
    }

    private void initToolbar() {
        String title = GsonHelpers.getString(detail, "title");
        CollapsingToolbarLayout toolbar = findViewById(R.id.product_detail_collapsing_toolbar);
        toolbar.setTitle(title);
    }


    private void setToolbar()
    {
        toolbar = findViewById(R.id.product_detail_toolbar);
        setSupportActionBar(toolbar);
    }

//    private void initCollapseToolbar() {
//        CollapsingToolbarLayout toolbar = findViewById(R.id.product_detail_collapsing_toolbar);
//        AppBarLayout appbar = findViewById(R.id.product_detail_appbar);
//        appbar.addOnOffsetChangedListener((appBarLayout, i) -> {
//            float s = Math.abs( i ) / ( float ) appbar.getTotalScrollRange();
//            if ( s >= 0.9 )
//            {
//
//            }
//        });
//    }

//    private void initToolbar() {
//        TextView title = findViewById(R.id.toolbar_product_title);
//        title.setText( GsonHelpers.getString( detail, "title" ) );
//        TextView price = findViewById(R.id.toolbar_product_price);
//        price.setText( Helper.monetaryRepresentation( GsonHelpers.getString( detail, "price" ) ) );
//        TextView phone = findViewById(R.id.toolbar_seller_phone);
//        phone.setText( GsonHelpers.getString( detail, "seller_phone" ) );
//        ImageView phoneImage = findViewById(R.id.toolbar_seller_image_phone);
//
//        callableViews( phone );
//        callableViews( phoneImage );
//    }

    private void showPhoneNumber()
    {
//        TextView phone = findViewById(R.id.toolbar_seller_phone);
//        phone.setVisibility(View.VISIBLE);
//        ImageView phoneImage = findViewById(R.id.toolbar_seller_image_phone);
//        phoneImage.setVisibility(View.VISIBLE);

        TextView sellerPhone = findViewById(R.id.product_seller_phone);
        sellerPhone.setVisibility( View.VISIBLE );
        ImageView sellerPhoneImage = findViewById(R.id.product_seller_phone_image);
        sellerPhoneImage.setVisibility( View.VISIBLE );
    }

    private void callableViews( View view )
    {
        view.setOnClickListener( this::callSeller );
    }

    private void callSeller()
    {
        String phone = GsonHelpers.getString( detail, "seller_phone" );
        Intent intent = new Intent( Intent.ACTION_DIAL, Uri.fromParts( "tel", phone, null ) );
        startActivity( intent );
    }

    private void fromInternal()
    {
        ads_id = getIntent().getLongExtra("ads_id", -1);
    }

    private void getProductInfo()
    {
        if ( ads_id != -1 )
        {
            Helper.Worker.executeAllTask( () -> {
                PostItService.PostItResponse response = postit.getAds(ads_id);

                return () -> {

                    response.respondToObject( object -> {
                        Log.d( "Populating_Entries", object.toString() );
                        detail = object.getAsJsonObject("ad");
                        medias = object.getAsJsonArray("medias");
                        initViews();
                    }, () -> {});
                };
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        BottomAppBar bottomBar = findViewById(R.id.product_bottom_bar);
//        Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.product_detail_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch ( itemId )
        {
            case R.id.action_favourite:
                addToFavourite();
                break;

            case R.id.action_share:
                shareAd();
                break;

//            case R.id.action_info:
//                showPhoneNumber();
//                break;
//
//            case R.id.action_chat:
//                chatSeller();
//                break;
//
//            case R.id.action_mail:
//                mailSeller();
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareAd() {
        ShareBottomSheet shareSheet = new ShareBottomSheet();
        shareSheet.setIntent( shareIntent() );
        shareSheet.setTitle( String.format( "Share %s Via", GsonHelpers.getString(detail, "title") ) );
        shareSheet.show( getSupportFragmentManager(), "REGION" );
    }

    private Intent shareIntent() {
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("text/plain");

        share.putExtra( "type", "ad" );
        share.putExtra( "ad_id", getIntent().getLongExtra("ads_id", -1) );
        share.putExtra( "title", GsonHelpers.getString(detail, "title") );

        return share;
//        startActivity(Intent.createChooser(share, getResources().getText(R.string.send_to)));
    }

    private void mailSeller() {
//        Intent smsIntent = new Intent( Intent.ACTION_VIEW);
//        smsIntent.setType( "vnd.android-dir/mms-sms" );
//        smsIntent.putExtra("address", GsonHelpers.getString( detail, "seller_phone" ) );
//        smsIntent.putExtra( "sms_body", "Please I Would Like to Buy This Item From You" );
//        startActivity( smsIntent );

//        SmsManager.getDefault().sendTextMessage( GsonHelpers.getString( detail, "seller_phone" ), null,
//                "Please I Would Like to Buy This Item From You", null, null );
    }

    private void chatSeller() {

    }

    private void addToFavourite() {

    }

    private void initSellersInfo()
    {
        TextView sellerName = findViewById(R.id.product_seller_name );
        sellerName.setText( GsonHelpers.getString( detail, "seller_name" ) );
        TextView sellerPhone = findViewById(R.id.product_seller_phone );
        sellerPhone.setText( GsonHelpers.getString( detail, "seller_phone" ) );
        ImageView sellerPhoneImage = findViewById(R.id.product_seller_phone_image);
        TextView sellerEmail = findViewById(R.id.product_seller_email );
        sellerEmail.setText( GsonHelpers.getString( detail, "seller_email" ) );
        TextView sellerLocation = findViewById(R.id.product_seller_location );
        TextView address = findViewById(R.id.product_seller_pickup );
        String location = String.format("%s, %s, %s.",
                GsonHelpers.getString( detail, "city_name" ),
                GsonHelpers.getString( detail, "state_name" ),
                GsonHelpers.getString( detail, "country_name" ) );
        sellerLocation.setText( location );
        address.setText( GsonHelpers.getString( detail, "address" ) );
        ImageView sellerImage = findViewById(R.id.product_seller_image );

        callableViews( sellerPhone );
        callableViews( sellerPhoneImage );
    }

    private void initDescription()
    {
        TextView description = findViewById(R.id.product_description );
        description.setText(GsonHelpers.getString( detail, "description") );
    }

    private void initSpecification()
    {
        TextView condition = findViewById(R.id.product_condition);
        condition.setText( GsonHelpers.getString( detail, "ad_condition" ) );
        otherSpecification();
    }

    private void otherSpecification() {
        addSpecificationWithName( "body_style_name", "Body Style" );
        addSpecificationWithName( "transmission", "Transmission" );
        addSpecificationWithName( "model_name", "Model" );
        addSpecificationWithName( "brand_name", "Make" );
    }

    private void addSpecificationWithName( String name, String title )
    {
        JsonElement valueRaw = detail.get(name);
        if ( !valueRaw.isJsonNull() )
        {
            String value = valueRaw.getAsJsonPrimitive().getAsString();
            if ( value != null && !value.isEmpty() );
                addSpecification( title, value );
        }
    }

    private void addSpecification( String name, String value )
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        TableLayout specs = findViewById(R.id.product_specs_table);
        TableRow property = (TableRow) inflater.inflate(R.layout.specs_item, specs, false);
        TextView propertyName = property.findViewById(R.id.spec_property);
        propertyName.setText( name );
        TextView propertyValue = property.findViewById(R.id.spec_value);
        propertyValue.setText( value );
        specs.addView( property, 0 );
    }

    private void initProductInfo()
    {
        TextView title = findViewById(R.id.product_name);
        title.setText( GsonHelpers.getString( detail, "title" ) );
        TextView locationInfo = findViewById(R.id.product_location_info);
        String location = String.format("%s, %s, %s.",
                GsonHelpers.getString( detail, "city_name" ),
                GsonHelpers.getString( detail, "state_name"),
                GsonHelpers.getString( detail, "country_name" ));
        locationInfo.setText( location );
        TextView timeInfo = findViewById(R.id.product_time_info);
        timeInfo.setText( GsonHelpers.getString( detail, "updated_at" ) );
        TextView price = findViewById(R.id.product_price);
        price.setText( Helper.monetaryRepresentation( GsonHelpers.getString( detail, "price" ) ) );
    }

    private void initImageViewer()
    {
        Uri[] images = getImages();

        CarouselView imageViewer = findViewById(R.id.product_image_viewer);
        imageViewer.setImageListener((position, imageView) -> {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            imageView.setLayoutParams( new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 400 ));

            GlideApp.with( this ).load( images[ position ] )
                    .fitCenter().into( imageView );
        } );
        imageViewer.setPageCount( images.length );

    }

//    private void initImageViewer()
//    {
//        ImageView[] images = getImages();
//        initImageRadios( images.length );
//        initImageViewer( images );
//    }

//    private void initImageRadios(int length) {
//        TabLayout radios = findViewById(R.id.product_image_indicator);
//        for ( int i = 0; i < length; i++ )
//        {
//            TabLayout.Tab radio = radios.newTab();
//            radio.setIcon( R.drawable.image_viewer_selector );
//            radios.addTab( radio );
//        }
//    }

    private Uri[] getImages()
    {
        Uri[] images = new Uri[medias.size()];

        for ( int i = 0; i < medias.size(); ++i )
        {
            JsonObject media = medias.get(i).getAsJsonObject();
            images[ i ] = Uri.parse( GsonHelpers.getString( media, "media_link" ) );
            GlideApp.with(this).load(images[ i ]).preload();
        }

        return images;
    }

    public void finish(View view) {
        finish();
    }

    public void callSeller(View view) {
        showPhoneNumber();
        callSeller();
    }

//    private ImageView[] getImages()
//    {
//        ImageView[] imageViews = new ImageView[medias.size()];
//
//        for ( int i = 0; i < medias.size(); ++i )
//        {
//            ImageView image = new ImageView( this );
//            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            JsonObject media = medias.get(i).getAsJsonObject();
//            Uri mediaUrl = Uri.parse( GsonHelpers.getString( media, "media_link" ) );
//            GlideApp.with( this ).load( mediaUrl ).placeholder( R.drawable.logo2 )
//                    .fitCenter().into( image );
//            imageViews[ i ] = image;
//        }
//
//        return imageViews;
//    }

//    private void initImageViewer( ImageView[] images )
//    {
//        ViewPager viewer = findViewById(R.id.product_image_viewer);
//        TabLayout radios = findViewById(R.id.product_image_indicator);
//
//        ImageViewPagerAdapter imagePager = new ImageViewPagerAdapter(images);
//        viewer.setAdapter( imagePager );
//
//        viewer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(radios));
//        radios.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewer));
//    }

//    private class ImageViewPagerAdapter extends PagerAdapter
//    {
//        private final ImageView[] images;
//
//        public ImageViewPagerAdapter( ImageView[] images )
//        {
//            this.images = images;
//        }
//
//        @NonNull
//        @Override
//        public Object instantiateItem(@NonNull ViewGroup container, int position) {
//            ImageView image = images[ position ];
//            container.addView( image );
//            return image;
//        }
//
//        @Override
//        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//            container.removeView((View) object);
//        }
//
//        @Override
//        public int getCount() {
//            return images.length;
//        }
//
//        @Override
//        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
//            return view == object;
//        }
//    }


}
