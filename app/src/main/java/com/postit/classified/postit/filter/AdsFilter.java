package com.postit.classified.postit.filter;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.postit.classified.postit.R;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.dialog.BrandFragment;
import com.postit.classified.postit.dialog.CategoryFragment;
import com.postit.classified.postit.dialog.CityFragment;
import com.postit.classified.postit.dialog.CountryFragment;
import com.postit.classified.postit.dialog.FilterFragment;
import com.postit.classified.postit.dialog.ModelFragment;
import com.postit.classified.postit.dialog.StateFragment;
import com.postit.classified.postit.dialog.SubCategoryFragment;
import com.postit.classified.postit.service.PostItService;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdsFilter extends BottomSheetDialogFragment
{

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.nav_menu_make:
                showMake();
                return true;

            case R.id.nav_menu_spec:
                showSpecs();
                return true;

            case R.id.nav_menu_region:
                showRegion();
                return true;
        }
        return false;
    };

    private SpecsFragment specs;
    private FilterFragment make;
    private FilterFragment cat;
    private Pair<Integer, String>[] category;
    private View view;
    private BottomNavigationView nav;
    private FilterFragment region;
    private Consumer<JsonObject> filter;
    private PostItService postit;
//    private static final Gson gson = new Gson();
//    private Consumer<String> save;

//    public static AdsFilter getInstance(String filterState) {
//        return fromGson( filterState );
//    }

    private void showCategory() {
        cat = FilterFragment.listFilters( new CategoryFragment(), new SubCategoryFragment() );
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace( R.id.filter_container, cat, "MAKE_FILTER" );
        transaction.commitNow();
    }

    private void showSpecs() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace( R.id.filter_container, specs, "SPEC_FILTER" );
        transaction.commitNow();
    }

    private void showMake()
    {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace( R.id.filter_container, make, "MAKE_FILTER" );
        transaction.commitNow();
    }

    private void showMakeMenu()
    {
        nav.getMenu().findItem( R.id.nav_menu_make ).setVisible(true);
        Bundle makeParams = new Bundle();
        makeParams.putInt( "category_id", category[ 1 ].first );
        make = FilterFragment.listFilters( makeParams, new BrandFragment(), new ModelFragment());
    }

    private void hideMakeMenu()
    {
        nav.getMenu().findItem( R.id.nav_menu_make ).setVisible(false);
    }

    private void initMake()
    {
        if ( category == null || category[ 0 ] == null || category[ 1 ] == null )
        {
            hideMakeMenu();
            return;
        }

        Helper.Worker.executeTask(() -> {
            PostItService.PostItResponse subcategory = postit.getSubCategory( category[ 1 ].first );
            return () -> {
                subcategory.respondToObject( jsonObject -> {
                    Log.d( "Post_Activity", "subcategory is " + jsonObject );
                    int brandCount = jsonObject.getAsJsonPrimitive("brand_count").getAsInt();

                    boolean hasTrans = jsonObject.getAsJsonPrimitive("has_trans").getAsInt() == 1;
                    boolean hasBodyStyle = jsonObject.getAsJsonPrimitive("has_body_style").getAsInt() == 1;

                    Log.d( "Post_Activity", "has transmission " + hasTrans );
                    Log.d( "Post_Activity", "has body style " + hasBodyStyle );

                    if ( brandCount > 0 )
                        showMakeMenu();
                    else
                        hideMakeMenu();

                });
            };
        });
    }

    private void showRegion()
    {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace( R.id.filter_container, region, "MAKE_FILTER" );
        transaction.commitNow();
    }

    private void filter()
    {
//        JsonObject params = new JsonObject();
//        addParams( params, specs.getProperties() );
        JsonObject params = specs.getProperties();
        if ( make != null )
            addParams( params, make.getProperty(), "brand_id", "brand_model_id" );
        addParams( params, region.getProperty(), "country_id", "state_id", "city_id" );
        filter.accept( params );
//        save.accept( toGson() );
        dismiss();
    }

//    private String toGson()
//    {
//        return gson.toJson( this );
//    }
//
//    private static AdsFilter fromGson(String state)
//    {
//        return gson.fromJson( state, AdsFilter.class );
//    }

    private void addParams(JsonObject params, JsonObject properties) {
        for (Map.Entry<String, JsonElement> elementEntry : properties.entrySet()) {
            params.add( elementEntry.getKey(), elementEntry.getValue() );
        }
    }

    private void addParams(JsonObject params, Pair<Integer, String>[] property, String... names )
    {
        if ( property != null )
        {
            for ( int i = 0; i < property.length; ++i )
            {
                if ( property[ i ] != null )
                {
                    params.addProperty( names[ i ], property[ i ].first );
                }
            }
        }
    }

//    private void initBackPressed()
//    {
//        getDialog().setOnKeyListener( (dialog, keyCode, event) -> {
//            if  ( KeyEvent.KEYCODE_BACK == keyCode && event.getAction() == KeyEvent.ACTION_UP )
//                return specs.handleBackPressed();
//            return false;
//        });
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        postit = PostItService.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ads_filter, container, false);
        initNavBar();
        initNavs();
        initControls();
        return view;
    }

    private void initControls() {
        FloatingActionButton filter = view.findViewById(R.id.filter_confirm);
        filter.setOnClickListener( v -> {
            filter();
        });
    }

    private void initNavs() {
        initMake();
        initSpecs();
        initRegion();
    }

    private void initRegion() {
        region = FilterFragment.listFilters( new CountryFragment(), new StateFragment(), new CityFragment() );
    }

    private void initSpecs() {
        specs = new SpecsFragment();
    }

    private void initNavBar() {
        nav = view.findViewById(R.id.filter_nav_bar);
        nav.setOnNavigationItemSelectedListener( mOnNavigationItemSelectedListener );
    }

    @Override
    public void onStart() {
        super.onStart();
        nav.setSelectedItemId( R.id.nav_menu_spec);
    }

    public void setCategory(Pair<Integer, String>[] category) {
        this.category = category;
    }

    public Pair<Integer, String>[] getCategory() {
        return category;
    }

    public void setConsumer( Consumer< JsonObject > filter ) {
        this.filter = filter;
    }

//    public void setSave(Consumer<String> save) {
//        this.save = save;
//    }
//
//    public Consumer<String> getSave() {
//        return save;
//    }
}
