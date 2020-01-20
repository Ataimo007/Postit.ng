package com.postit.classified.postit.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.tabs.TabLayout;
import com.postit.classified.postit.MainActivity;
import com.postit.classified.postit.PostActivity;
import com.postit.classified.postit.R;
import com.postit.classified.postit.database.AppDatabase;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.service.PostItService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

//        implements NavigationView.OnNavigationItemSelectedListener
public class UserProfile extends AppCompatActivity
{
    public enum ProductState{
        PUBLISHED( 1 ), PENDING( 0), BLOCKED( 2 ),
        CLOSED( 3 ); // or SOLD $ad->is_sold = '1';

        int status;

        ProductState( int status )
        {
            this.status = status;
        }

    }

//    private JsonObject detail;
//    private JsonArray medias;

    private PostItService postit;
    private Toolbar toolbar;
    private AppDatabase appDatabase;

//    private boolean toolbarVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initStatus();
        setContentView(R.layout.user_profile);

        init();
        initToolbar();
        initSellersInfo();
        initPages();
    }

    private void initStatus() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void initPages() {
        TabLayout tabs = findViewById(R.id.user_tabs);
        ViewPager pages = findViewById(R.id.user_viewpager);

        ProductPagerAdapter pagerAdapter = new ProductPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pages.setAdapter( pagerAdapter );

        tabs.setupWithViewPager( pages );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init()
    {
        postit = PostItService.getInstance();
        appDatabase = AppDatabase.getInstance(this);
    }

    private void initToolbar()
    {
//        toolbar = findViewById(R.id.product_bottom_bar);
        toolbar = findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout layout = findViewById( R.id.user_collapsing_toolbar );
        Intent intent = getIntent();
        String name = String.format("%s %s", intent.getStringExtra("first_name"), intent.getStringExtra("last_name"));
        layout.setTitle( name );

        initProfilePic();
    }

    private void initProfilePic()
    {
        Intent intent = getIntent();
        String link = intent.getStringExtra("photo_link");
        Log.d( "Photo_Link", "link is " + link );
        if ( link != null && !link.isEmpty() )
            initPic();
        else
            initInitials();
    }

    private void initInitials()
    {
        Intent intent = getIntent();
        TextView initial = findViewById(R.id.user_initials);
        initial.setText( String.valueOf( intent.getStringExtra("first_name").charAt( 0 ) ) );
    }

    private void initPic() {
        TextView initial = findViewById(R.id.user_initials);
        initial.setVisibility(View.GONE);

        ImageView dp = findViewById(R.id.profile_pic);
        Intent intent = getIntent();
        GlideApp.with( this ).load( intent.getStringExtra("photo_link") ).placeholder( R.drawable.avatar )
                .fitCenter().into( dp );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        BottomAppBar bottomBar = findViewById(R.id.user_bottom_bar);
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initSellersInfo()
    {
        Intent intent = getIntent();
        TextView userName = findViewById(R.id.user_name );
        String name = String.format("%s %s", intent.getStringExtra("first_name"), intent.getStringExtra("last_name"));
        userName.setText( name );
        TextView email = findViewById( R.id.user_email );
        email.setText( intent.getStringExtra( "email" ) );
        TextView phone = findViewById( R.id.user_phone_number );
        phone.setText( intent.getStringExtra( "phone" ));
    }

    public void logout(MenuItem item) {

        Helper.Worker.executeTask( () -> {
            appDatabase.userDao().logout();
            return this::logout;
        });
    }

    private void logout()
    {
        Intent intent = new Intent( this, MainActivity.class );
        intent.putExtra( "user_action", "LOGOUT" );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
    }

    public void postAd(View view) {
        Intent intent = new Intent( this, PostActivity.class );
        intent.putExtras( getIntent() );
        startActivity( intent );
    }

    private class ProductPagerAdapter extends FragmentStatePagerAdapter
    {
        private UserProductList[] pages;

        public ProductPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
            pages = new UserProductList[]{ getProductInstance( ProductState.PUBLISHED ), getProductInstance( ProductState.PENDING ),
                    getProductInstance( ProductState.CLOSED ) };
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return pages[ position ];
        }

        @Override
        public int getCount() {
            return pages.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return pages[ position ].getState().name();
        }

        private UserProductList getProductInstance(ProductState state )
        {
            UserProductList userProductList = new UserProductList();
            Bundle bundle = new Bundle();
            bundle.putInt( "id", getIntent().getIntExtra( "id", -1 ) );
            userProductList.setState( state );
            userProductList.setArguments( bundle );
            return userProductList;
        }
    }

}
