package com.postit.classified.postit;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.novoda.merlin.Merlin;
import com.postit.classified.postit.database.AppDatabase;
import com.postit.classified.postit.filter.AdsFilter;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.GsonHelpers;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.dialog.FilterBottomSheet;
import com.postit.classified.postit.dialog.MainCategoryFragment;
import com.postit.classified.postit.dialog.SubCategoryFragment;
import com.postit.classified.postit.user.LoginUser;
import com.postit.classified.postit.service.PostItService;
import com.postit.classified.postit.user.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

//        implements NavigationView.OnNavigationItemSelectedListener
public class MainActivity extends AppCompatActivity
{
    private static final int LOGIN_POST = 100;

    private Menu menu;
    private final static int TAB_COUNT = 2;

    private boolean firstLoad = false;

    private JsonArray entities = new JsonArray();
    private EntityAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private boolean isPopulating;
    private PostItService postit;
    private CircularImageView userInfo;
    private AppDatabase appDatabase;
    private EntityScrollListener scrollListener;

    private Pair<Integer, String>[] categoryPair = new Pair[ 2 ];
    private Pair<Integer, String>[] makePair = new Pair[ 2 ];
    private ArrayList< Pair<Integer, String> > specsPair = new ArrayList();

    private ImageButton searchButton;
    private TextInputLayout searchLayout;
    private ImageView appLogo;
    private ImageButton notification;
    private ImageButton addFilter;
    private ImageButton removeFilter;
    private final Stack<Helper.UIAction> backAction = new Stack<>();
    private JsonObject filter = new JsonObject();
    private TextInputEditText searchField;
    private RecyclerView productRecyclerView;
    private FloatingActionButton toTop;
    private Merlin merlin;
    private TextView initial;
//    private String filterState;

//    private Filter filter = new Filter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initUI();
        initToolbar();
        initUser();
        initUserAction( getIntent() );
        initRecyclerView();
        initRefresher();
        initLoader();
        initSearch();
        initViewExtras();
        initMerlin();
    }

    private void initMerlin() {
        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);

        merlin.registerConnectable( () -> {
            populateEntries( 10 );
        });
    }

    private void initViewExtras() {
        toTop = findViewById( R.id.main_top_button );
    }


    private void initRefresher() {
        SwipeRefreshLayout refresher = findViewById(R.id.main_refresher);
        refresher.setOnRefreshListener( () -> {
            reload();
            refresher.setRefreshing( false );
        });
    }

    public JsonObject getFilter() {
        return filter;
    }

    public void setFilter(JsonObject filter) {
        this.filter = filter;
        showRemoveFilter();
    }

    private void initSearch() {
        searchField.setOnEditorActionListener( (v, actionId, event) ->
        {
            boolean handled = false;
            if ( actionId == EditorInfo.IME_ACTION_SEARCH )
            {
                reload();
                handled = true;
            }
            return handled;
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                reload();
            }
        });
    }

    private void showSearchField()
    {
        userInfo.setVisibility( View.GONE );
        appLogo.setVisibility( View.GONE );
        searchButton.setVisibility( View.GONE );
        notification.setVisibility( View.GONE );

        searchLayout.setVisibility( View.VISIBLE );
        addFilter.setVisibility( View.VISIBLE );

        showRemoveFilter();
    }

    private void showRemoveFilter()
    {
        if ( hasFilter() )
            removeFilter.setVisibility( View.VISIBLE );
        else
            removeFilter.setVisibility( View.GONE );
    }

    private void registerBackAction(Helper.UIAction action)
    {
        backAction.push(action);
    }

    private void endSearch()
    {
        removeFilter();
        clearSearch();
        reload();
        showToolbar();
    }

    private void clearSearch() {
        searchField.getText().clear();
    }

    private void showToolbar()
    {
        userInfo.setVisibility( View.VISIBLE );
        appLogo.setVisibility( View.VISIBLE );
        searchButton.setVisibility( View.VISIBLE );
        notification.setVisibility( View.VISIBLE );

        searchLayout.setVisibility( View.GONE );
        addFilter.setVisibility( View.GONE );
        removeFilter.setVisibility( View.GONE );
    }

    private void initToolbar() {
        searchButton = findViewById(R.id.ads_search_button);
        notification = findViewById(R.id.user_notification);
        addFilter = findViewById(R.id.ads_add_filter);
        removeFilter = findViewById(R.id.ads_remove_filter);

        userInfo = findViewById(R.id.user_info);
        initial = findViewById(R.id.avatar_initial);
        searchLayout = findViewById(R.id.ad_search_layout);
        searchField = findViewById(R.id.ad_search_field);
        appLogo = findViewById(R.id.app_logo);
    }

    private void initLoader()
    {
        ImageView loader = findViewById(R.id.main_loader);
        loader.setVisibility( View.VISIBLE );
        GlideApp.with( this ).load( R.drawable.loader2 ).placeholder( R.drawable.logo2 )
                .into( loader );
    }

    private void initRecyclerView() {
//        setLayoutManager( new LinearLayoutManager(this) );
//        setLayoutManager( stagger );
        StaggeredGridLayoutManager stagger = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);

        productRecyclerView = findViewById( R.id.main_recycler );
//        setLayoutManager( new GridLayoutManager( this, getSpanCount() ) );
        setLayoutManager( stagger );

        productRecyclerView.setLayoutManager( getLayoutManager() );
        EntityAdapter adapter = new EntityAdapter();
        productRecyclerView.setAdapter( adapter );
        productRecyclerView.addOnScrollListener( getScrollListener() );
        this.adapter = adapter;

        productRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 10;
                outRect.bottom = 10;
                outRect.left = 10;
                outRect.right = 10;

                if ( parent.getChildAdapterPosition( view ) == 0 )
                    outRect.top = 20;

                if ( parent.getChildAdapterPosition( view )
                        >= parent.getAdapter().getItemCount() - ( parent.getAdapter().getItemCount() % getSpanCount() ) )
                    outRect.bottom = 150;
            }
        });
    }

    private int getSpanCount() {
        return getResources().getInteger(R.integer.product_span);
//        return 1;
    }

    private void initUI()
    {
        BottomAppBar bottomAppBar = findViewById(R.id.app_bottom_bar);
        setSupportActionBar(bottomAppBar);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setTitle("");

        bottomAppBar.setNavigationOnClickListener( v -> {
            Log.d( "Main_Activity", "Navigation is pressed" );
            FilterBottomSheet category = FilterBottomSheet.listFilters( this::handleCategory, new MainCategoryFragment(), new SubCategoryFragment() );
            category.setShowFilterButton( true );
            category.show( getSupportFragmentManager(), "REGION" );
        });
    }

    private void handleCategory(Pair<Integer, String>[] pairs) {
        filter( pairs );
    }

    @Override
    protected void onStart() {
        super.onStart();
        begin();
    }

    public void begin()
    {
        populateEntries( 10 );
    }

    private void login(Intent intent)
    {
        Helper.Worker.executeTask( () -> {
            appDatabase.userDao().login( intent );
            return () -> {
                changeProfilePic( intent );
            };
        });
        initUserAction( intent );
    }

    private void initUser()
    {
        Intent intent = getIntent();
        Helper.Worker.executeTask( () -> {
            AppDatabase.User user = appDatabase.userDao().getLogin();
            Log.d( "PostIt_Service", "Login user " + user );
            return () -> {
                if ( user != null )
                {
                    user.toIntent(intent);
                    changeProfilePic( intent );
                }
            };
        });

    }

    private void changeProfilePic(Intent intent) {
        String photoUri = intent.getStringExtra("photo_link");
        if ( photoUri != null && !photoUri.isEmpty() )
        {
//            cacheImage( photoUri );
            Uri media = Uri.parse( photoUri );
            changeProfilePic( media );
            initial.setVisibility( View.GONE );
        }
        else
        {
            defaultProfilePic();
        }
    }

    private void cacheImage( String uri )
    {
        Helper.Worker.executeAllTask( () -> {
            postit.saveProfilePic( uri );
            return () -> {};
        });
    }

    private void changeProfilePic(Uri mediaUri) {
        GlideApp.with( this ).load( mediaUri ).placeholder( R.drawable.avatar2 )
                .fitCenter().into( userInfo );
        userInfo.setBorderColor( ContextCompat.getColor( this, R.color.app_sub2 ) );
    }

    private void defaultProfilePic() {
        GlideApp.with( this ).load( R.drawable.avatar2 ).fitCenter().into( userInfo );
        userInfo.setBorderColor(Color.BLACK);
    }

    private void processIntent(Intent intent)
    {
        switch ( intent.getStringExtra( "user_action" ) )
        {
            case "LOGIN":
                login( intent );
                break;

            case "LOGOUT":
                logout( intent );
                break;
        }
    }

    private void logout(Intent intent) {
        changeProfilePic( intent );
        initUserAction( intent );
    }

    private void initUserAction( Intent intent )
    {
        userInfo.setOnClickListener(v -> {
            if ( intent.getIntExtra( "id", -1 ) == -1 )
            {
                Intent userLogin = new Intent( this, LoginUser.class );
                startActivity( userLogin );
            }
            else
            {
                Intent userProfile = new Intent( this, UserProfile.class );
                userProfile.putExtras(intent);
                startActivity( userProfile );
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleSearchables( intent );
        processIntent( intent );
    }

    private void handleSearchables(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }

    private void init()
    {
        postit = PostItService.getInstance( this );
        appDatabase = AppDatabase.getInstance(this);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        this.menu = menu;
//        getMenuInflater().inflate(R.menu.bottom_menu, menu);
//
//        Toolbar appBar = findViewById(R.id.toolbar);
//        Menu appBarMenu = appBar.getMenu();
//        getMenuInflater().inflate(R.menu.main, appBarMenu);
//
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;
        getMenuInflater().inflate(R.menu.bottom_menu, menu);

//        BottomAppBar bottomAppBar = findViewById(R.id.app_bottom_bar);
//        Menu appBarMenu = bottomAppBar.getMenu();
//        getMenuInflater().inflate(R.menu.bottom_menu, appBarMenu);

//        prepareSearchMenu();

        return true;
    }

//    private void prepareSearchMenu() {
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch ( id )
        {
            case R.id.action_notification:
            {
                break;
            }
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_user)
//        {
//            Intent user = new Intent( this, LoginUser.class );
//            startActivity( user );
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

//    protected void addEntries(JsonArray entries ) {
//        int size = entities.size();
//        int addSize = entries.size();
//        for ( int i = 0; i < addSize; ++i )
//        {
//            JsonObject entry = entries.get(i).getAsJsonObject();
//            displayDetail( entry );
//            entities.add( entry );
//        }
//        adapter.notifyItemRangeInserted( size, addSize );
//    }



    protected synchronized void addEntries(JsonArray entries )
    {
        for ( JsonElement entity : entries )
        {
            Uri media = Uri.parse(GsonHelpers.getString( entity.getAsJsonObject(), "media_link" ) );
            GlideApp.with(this).load(media).preload();
        }

        entities.addAll( entries );
        adapter.notifyItemRangeInserted( entities.size(), entries.size() );
    }

    @Override
    public void onBackPressed() {
        Log.d( "Back_Pressed", "Main Activity back pressed" );
        if ( !backAction.isEmpty() )
        {
            backAction.pop().perform();
            return;
        }

        super.onBackPressed();
    }

    private void displayDetail(JsonObject entry )
    {
        Log.d( "Entry_Detail", "title : " + GsonHelpers.getString( entry, "title" ) );
    }

    public void clear()
    {
        entities = new JsonArray();
        adapter.notifyDataSetChanged();
    }

    private boolean isFilterable()
    {
        return hasFilter() || !getSearch().isEmpty();
    }

    private boolean hasFilter()
    {
        return filter.size() != 0;
    }

    private void removeFilter()
    {
        setFilter( new JsonObject() );
        reload();
    }

    private JsonObject prepareFilter(int size )
    {
        JsonObject newFilter = filter.deepCopy();
        newFilter.addProperty( "begin", entities.size() );
        newFilter.addProperty( "count", size );

        String search = getSearch();
        if ( !search.isEmpty() )
            newFilter.addProperty( "search", search);

        if ( categoryPair[ 1 ] != null )
            newFilter.addProperty( "sub_category_id", categoryPair[ 1 ].first);
        if ( categoryPair[ 0 ] != null )
            newFilter.addProperty( "category_id", categoryPair[ 0 ].first);
        return newFilter;
    }

//    private void prepareFilter( int size )
//    {
//        filter.addProperty( "begin", entities.size() );
//        filter.addProperty( "count", size );
//
//        String search = getSearch();
//        if ( !search.isEmpty() )
//            filter.addProperty( "search", search);
//
//        if ( categoryPair[ 1 ] != null )
//            filter.addProperty( "sub_category_id", categoryPair[ 1 ].first);
//        if ( categoryPair[ 0 ] != null )
//            filter.addProperty( "category_id", categoryPair[ 0 ].first);
//    }

    private PostItService.PostItResponse getFilterEntries( int size )
    {
        JsonObject filter = prepareFilter(size);
        return postit.getAdsByFilter(filter);
    }

    private String getSearch()
    {
        return searchField.getText().toString();
    }

    private PostItService.PostItResponse getEntries( int size )
    {
        if ( isFilterable() )
        {
//            Log.d( "Filtering", "Filter the ads search " + getSearch() );
//            Log.d( "Filtering", "Filter the ads search " + getSearch().isEmpty() );
//            Log.d( "Filtering", "Filter the ads filter " + ( filter.size() != 0 ) );
            return getFilterEntries( size );
        }

        Log.d( "Ads_Category", "get ads by category " + categoryPair );
        if ( categoryPair[ 1 ] != null )
            return postit.getAdsBySubCategory( categoryPair[ 1 ].first, entities.size(), size);
        if ( categoryPair[ 0 ] != null && categoryPair[ 0 ].first != -1 )
            return postit.getAdsByCategory( categoryPair[ 0 ].first, entities.size(), size);
        return postit.getAds( entities.size(), size);
    }

    @Override
    protected void onResume() {
        super.onResume();
        merlin.bind();
    }

    @Override
    protected void onPause() {
        merlin.unbind();
        super.onPause();
    }

    protected synchronized void populateEntries( int size )
    {
        populating();

        Helper.Worker.executeAllTask( () -> {
            PostItService.PostItResponse response = getEntries( size );

            return () -> {
                response.respondToArray( jsonElements -> {
                    Log.d( "Populating_Entries", jsonElements.toString() );
                    Log.d( "Scrolling_Listener", "Adding Entry: " + size );
                    addEntries( jsonElements );
                    donePopulating();
                }, () -> {});
            };
        });
    }

    public synchronized void populating()
    {
        isPopulating = true;
    }

    public synchronized void donePopulating()
    {
        isPopulating = false;
        if ( !firstLoad )
        {
            doneLoading();
        }
    }

    private void doneLoading() {
        ImageView loader = findViewById(R.id.main_loader);
        loader.setVisibility( View.GONE );
        firstLoad = true;
    }

    public synchronized boolean isPopulating()
    {
        return isPopulating;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    private EntityScrollListener getScrollListener()
    {
        if ( scrollListener == null )
            scrollListener = new EntityScrollListener(10);
        return scrollListener;
    }

    public void notifications(MenuItem item) {
        Log.d("Main_Activity", "Notification Button Click" );
    }

    private void reload()
    {
        clear();
        initLoader();

        firstLoad = false;
        getScrollListener().reset();

        populateEntries( 10 );
    }

    public void filter(Pair<Integer, String>[] filter) {
        this.categoryPair = filter;
        reload();
    }

    public void postAd(View view) {
        Intent intent = new Intent( this, PostActivity.class );
        if ( isLogin() )
        {
            intent.putExtras( getIntent() );
            startActivity( intent );
        }
        else
        {
            startActivityForResult( intent, LOGIN_POST );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == LOGIN_POST && resultCode == RESULT_OK )
        {
            processIntent( data );
            reload();
        }
    }

    private boolean isLogin()
    {
        int userId = getIntent().getIntExtra("id", -1  );
        return userId != -1;
    }

    public void beginSearch(View view) {
        showSearchField();
        registerBackAction(this::endSearch);
    }

    public void showFilter(View view) {
        Log.d( "Ads_Filter", "This are the ads filter" );
        AdsFilter adsFilter = new AdsFilter();
        adsFilter.setCategory( categoryPair );
        adsFilter.setConsumer( this::handleFilter );
        adsFilter.show( getSupportFragmentManager(), "ADS_FILTER" );
    }

//    public void showFilter(View view) {
//        Log.d( "Ads_Filter", "This are the ads filter" );
//        if ( filterState == null )
//        {
//            AdsFilter adsFilter = new AdsFilter();
//            adsFilter.setCategory( categoryPair );
//            adsFilter.setConsumer( this::handleFilter );
//            adsFilter.setSave( this::filterState );
//            adsFilter.show( getSupportFragmentManager(), "ADS_FILTER" );
//        }
//        else
//        {
//            AdsFilter previous = AdsFilter.getInstance(filterState);
//            previous.show( getSupportFragmentManager(), "ADS_FILTER" );
//        }
//    }

//    private void filterState(String s) {
//        filterState = s;
//    }

    private void handleFilter(JsonObject jsonObject) {
        setFilter( jsonObject );
        showRemoveFilter();
        reload();
    }

    public void removeFilter(View view) {
        removeFilter();
    }

    public void scrollToTop(View view) {
        productRecyclerView.scrollToPosition( 0 );
    }

    private class EntityScrollListener extends RecyclerView.OnScrollListener
    {
        private final int threshold;
        private volatile int prevTotal;

        public void reset()
        {
            prevTotal = 0;
        }

        public EntityScrollListener( int thresh )
        {
            super();
            threshold = thresh;
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            RecyclerView.LayoutManager layoutManager = getLayoutManager();
            int last = findLastVisibleItemPosition();
            int total = layoutManager.getItemCount();
            int visibleLeft = ( total - 1 ) - last;

//            Log.d( "Scrolling_Listener", "Visible Left: " + visibleLeft );
//            Log.d( "Scrolling_Listener", "Previous Total: " + prevTotal );

            if ( visibleLeft < threshold && !isLoading( total ) && !isPopulating() )
            {
                prevTotal = total;
                Log.d( "Scrolling_Listener", "Is It Loading: " + isLoading( total ) );
                populateEntries( visibleLeft );
            }

            showToTop();
        }

        private synchronized boolean isLoading( int currentTotal )
        {
            return prevTotal >= currentTotal;
        }
    }

    private int findLastVisibleItemPosition()
    {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if ( layoutManager instanceof LinearLayoutManager )
        {
            LinearLayoutManager linear = (LinearLayoutManager) layoutManager;
            return linear.findLastVisibleItemPosition();
        }

        if ( layoutManager instanceof StaggeredGridLayoutManager )
        {
            StaggeredGridLayoutManager stagger = (StaggeredGridLayoutManager) layoutManager;
            int[] positions = stagger.findLastVisibleItemPositions( new int[ getSpanCount() ] );
            Arrays.sort( positions );
            return positions[ positions.length - 1 ];
        }
        return 0;
    }

    private int findFirstVisibleItemPosition()
    {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if ( layoutManager instanceof LinearLayoutManager )
        {
            LinearLayoutManager linear = (LinearLayoutManager) layoutManager;
            return linear.findFirstVisibleItemPosition();
        }

        if ( layoutManager instanceof StaggeredGridLayoutManager )
        {
            StaggeredGridLayoutManager stagger = (StaggeredGridLayoutManager) layoutManager;
            int[] positions = stagger.findFirstVisibleItemPositions( new int[ getSpanCount() ] );
            Arrays.sort( positions );
            return positions[ 0 ];
        }
        return 0;
    }

    private void showToTop() {
        int firstVisible = findFirstVisibleItemPosition();
        if  ( firstVisible > 0 )
            toTop.show();
        else
            toTop.hide();
    }

    public class EntityAdapter extends RecyclerView.Adapter< ViewHolder >
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position)
        {
            JsonObject entity = entities.get( position ).getAsJsonObject();
            holder.title.setText( GsonHelpers.getString( entity, "title" ) );
            holder.category.setText( GsonHelpers.getString( entity, "category_name" ) );
            holder.location.setText( GsonHelpers.getString( entity, "state_name" ) );
            holder.price.setText( Helper.monetaryRepresentation( GsonHelpers.getString( entity, "price" ) ) );
            holder.phone.setText( GsonHelpers.getString( entity, "seller_phone" ) );

            Uri media = Uri.parse(GsonHelpers.getString( entity, "media_link" ) );

//            holder.background.setImageDrawable( null );
            holder.image.setImageResource( R.drawable.logo2 );

//            GlideApp.with( MainActivity.this ).load( media )
//                    .apply( RequestOptions.bitmapTransform(new BlurTransformation( 25 ) ) )
//                    .into( holder.background );

//            GlideApp.with( MainActivity.this ).load( media )
//                    .apply( RequestOptions.bitmapTransform(
//                            new RoundedCornersTransformation( 50, 10, RoundedCornersTransformation.CornerType.ALL ) ) )
//                    .placeholder( R.drawable.logo2 )
//                    .into( holder.image );

            GlideApp.with( MainActivity.this ).load( media ).placeholder( R.drawable.logo2 )
                    .apply( RequestOptions.bitmapTransform(
                            new RoundedCornersTransformation( 20, 0, RoundedCornersTransformation.CornerType.ALL ) ) )
                    .into( holder.image );

            holder.itemView.setOnClickListener( v -> {
                Intent detail = new Intent( MainActivity.this, ProductDetailActivity.class );
                detail.putExtra( "ads_id", GsonHelpers.getLong( entity, "id" ) );
                MainActivity.this.startActivity( detail );
            });

            holder.info.setOnClickListener( v -> {
                holder.showNumber();
            });



            holder.favourite.setOnClickListener( v -> {

            });
        }

//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position)
//        {
//            JsonObject entity = entities.get( position ).getAsJsonObject();
//            holder.title.setText( GsonHelpers.getString( entity, "title" ) );
//            holder.category.setText( GsonHelpers.getString( entity, "category_name" ) );
//            holder.location.setText( GsonHelpers.getString( entity, "state_name" ) );
//            holder.price.setText( Helper.monetaryRepresentation( GsonHelpers.getString( entity, "price" ) ) );
//            holder.phone.setText( GsonHelpers.getString( entity, "seller_phone" ) );
//
//            Uri media = Uri.parse(GsonHelpers.getString( entity, "media_link" ) );
//
//            holder.background.setImageDrawable( null );
//            holder.image.setImageResource( R.drawable.logo2 );
//
//            GlideApp.with( MainActivity.this ).load( media )
//                    .apply( RequestOptions.bitmapTransform(new BlurTransformation( 25 ) ) )
//                    .addListener(new RequestListener<Drawable>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                            holder.image.setImageDrawable( null );
//                            GlideApp.with( MainActivity.this ).load( media )
//                                    .apply( RequestOptions.bitmapTransform(
//                                            new RoundedCornersTransformation( 50, 10, RoundedCornersTransformation.CornerType.ALL ) ) )
//                                    .into( holder.image );
//                            return false;
//                        }
//                    })
//                    .into( holder.background );
//
//            holder.itemView.setOnClickListener( v -> {
//                Intent detail = new Intent( MainActivity.this, ProductDetailActivity.class );
//                detail.putExtra( "ads_id", GsonHelpers.getLong( entity, "id" ) );
//                MainActivity.this.startActivity( detail );
//            });
//
//            holder.info.setOnClickListener( v -> {
//                holder.showNumber();
//            });
//
//
//
//            holder.favourite.setOnClickListener( v -> {
//
//            });
//        }

        @Override
        public int getItemCount() {
            return entities.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final ImageView image;
        public final TextView title;
        public final TextView category;
        public final MaterialButton favourite;
        public final MaterialButton info;
        private final TextView location;
        private final TextView phone;
        private final ImageView phoneImage;
        private final TextView price;
//        private final ImageView background;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.product_image);
//            background = view.findViewById(R.id.product_background_image);
            title = view.findViewById(R.id.product_title);
            category = view.findViewById(R.id.product_category);
            location = view.findViewById(R.id.product_location);
            phone = view.findViewById(R.id.product_phone);
            phoneImage = view.findViewById(R.id.product_phone_image);
            price = view.findViewById(R.id.product_price);
            favourite = view.findViewById(R.id.product_favourite);
            info = view.findViewById(R.id.product_info);
        }

        public void setImage( Bitmap image )
        {
            if ( image != null )
                this.image.setImageBitmap( image );
            else
            {
                Log.d( "Entry_Default", "setting default image" );
                int resId = getResources().getIdentifier("sample_holder", "drawable",
                        MainActivity.this.getPackageName());
                this.image.setImageResource( resId );
            }
        }

        public void showNumber()
        {
            phone.setVisibility( View.VISIBLE );
            phoneImage.setVisibility( View.VISIBLE );
            info.setVisibility(View.GONE);

            phone.setOnClickListener( v -> {
                String number = phone.getText().toString();
                Intent intent = new Intent( Intent.ACTION_DIAL, Uri.fromParts( "tel", number, null ) );
                startActivity( intent );
            });

            phoneImage.setOnClickListener( v -> {
                String number = phone.getText().toString();
                Intent intent = new Intent( Intent.ACTION_DIAL, Uri.fromParts( "tel", number, null ) );
                startActivity( intent );
            });
        }
    }

    public static class Filter
    {
        public int categoryId = -1;
        public int brandId = -1;
        public int modelId = -1;

        public int getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(int categoryId) {
            this.categoryId = categoryId;
            brandId = -1;
            modelId = -1;
        }

        public int getBrandId() {
            return brandId;
        }

        public void setBrandId(int brandId) {
            this.brandId = brandId;
            modelId = -1;
        }

        public int getModelId() {
            return modelId;
        }

        public void setModelId(int modelId) {
            this.modelId = modelId;
        }

        @Override
        public String toString() {
            return "Filter{" +
                    "categoryId=" + categoryId +
                    ", brandId=" + brandId +
                    ", modelId=" + modelId +
                    '}';
        }
    }

}
