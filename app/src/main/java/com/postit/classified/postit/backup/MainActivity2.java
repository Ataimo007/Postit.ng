//package com.postit.classified.postit.backup;
//
//import android.app.SearchManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//
//import com.google.android.material.tabs.TabLayout;
//import com.postit.classified.postit.R;
//import com.postit.classified.postit.home.ProductFragment;
//import com.postit.classified.postit.service.PostItService;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.SearchView;
//import androidx.appcompat.widget.Toolbar;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentPagerAdapter;
//import androidx.viewpager.widget.ViewPager;
//
////        implements NavigationView.OnNavigationItemSelectedListener
//public class MainActivity2 extends AppCompatActivity
//{
//    private PostItService handler;
//    private Menu menu;
//    private ViewPager mViewPager;
//    private TabLayout tabLayout;
//    private final static int TAB_COUNT = 2;
//    private SectionsPagerAdapter pagerAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main_app);
//
//        initUI();
//        init( this );
//    }
//
//    private void initTabViewPager()
//    {
//        pagerAdapter = new SectionsPagerAdapter();
//        mViewPager = findViewById(R.id.app_viewpager);
//        tabLayout = findViewById( R.id.app_tabs );
//
//        mViewPager.setAdapter(pagerAdapter);
//
//        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
//    }
//
//    private void initUI()
//    {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("");
//
//        initTabViewPager();
//    }
//
//    private void initUI2()
//    {
//
////        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
////        fab.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
////            }
////        });
//
////        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
////        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
////                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
////        drawer.addDrawerListener(toggle);
////        toggle.syncState();
////
////        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
////        navigationView.setNavigationItemSelectedListener(this);
////
////        navigationView.setCheckedItem( R.id.nav_home );
//    }
//
//    private void initUser()
//    {
//        Intent intent = getIntent();
//        if ( intent.getIntExtra( "id", -1 ) != -1 )
//        {
////            initNav();
////            initMenu();
//        }
//    }
//
////    private void initMenu()
////    {
////        getMenuInflater().inflate( R.menu.main_login, menu );
////        prepareSearchMenu();
////    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//
//        initUser();
//    }
//
//    private void init(Context context)
//    {
//        handler = PostItService.getInstance();
//    }
//
////    @Override
////    public void onBackPressed() {
////        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
////        if (drawer.isDrawerOpen(GravityCompat.START)) {
////            drawer.closeDrawer(GravityCompat.START);
////        } else {
////            super.onBackPressed();
////        }
//////        TextInputEditText e = new TextInputEditText(this);
////
////    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        this.menu = menu;
//        getMenuInflater().inflate(R.menu.main, menu);
////        prepareSearchMenu();
////        return super.onCreateOptionsMenu(menu);
//        return true;
//    }
//
//    private void prepareSearchMenu() {
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//
//        SearchManager searchManager = (SearchManager) MainActivity2.this.getSystemService(Context.SEARCH_SERVICE);
//
//        SearchView searchView = null;
//        if (searchItem != null) {
//            searchView = (SearchView) searchItem.getActionView();
//        }
//        if (searchView != null) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity2.this.getComponentName()));
//        }
//    }
//
////    @Override
////    public boolean onOptionsItemSelected(MenuItem item)
////    {
////        // Handle action bar item clicks here. The action bar will
////        // automatically handle clicks on the Home/Up button, so long
////        // as you specify a parent activity in AndroidManifest.xml.
////        int id = item.getItemId();
////
////        //noinspection SimplifiableIfStatement
////        if (id == R.id.action_user)
////        {
////            Intent user = new Intent( this, LoginUser.class );
////            startActivity( user );
////            return true;
////        }
////
////        return super.onOptionsItemSelected(item);
////    }
//
//    public class SectionsPagerAdapter extends FragmentPagerAdapter
//    {
//        Fragment frags[] = new Fragment[ TAB_COUNT ];
//
//        public SectionsPagerAdapter() {
//            super(MainActivity2.this.getSupportFragmentManager());
//            frags[ 0 ] = ProductFragment.getInstance();
//            frags[ 1 ] = new Fragment();
//        }
//
//        @Override
//        public Fragment getItem(int position)
//        {
//            return frags[ position ];
//        }
//
//
//        @Override
//        public int getCount()
//        {
//            return frags.length;
//        }
//    }
//
//}
