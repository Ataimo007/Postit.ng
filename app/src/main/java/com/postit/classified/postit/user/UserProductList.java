package com.postit.classified.postit.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.postit.classified.postit.PostActivity;
import com.postit.classified.postit.ProductDetailActivity;
import com.postit.classified.postit.R;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.GsonHelpers;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.service.PostItService;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class UserProductList extends Fragment
{
    private LinearLayoutManager layoutManager;
    private final ArrayList<JsonElement> entities = new ArrayList<>();
    private ProductAdapter adapter;
    private boolean isPopulating;
    private boolean firstLoad = false;
    private boolean begin = true;
    private ProductScrollListener scrollListener;
    private RecyclerView list;

    private UserProfile.ProductState state;
    private PostItService postit;

    private final int EDIT = 101;
    private final int DELETE = 102;
    private final int REFRESH = 103;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        postit = PostItService.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        list = (RecyclerView) inflater.inflate(R.layout.user_products, container, false);
        initRecyclerView();
//        initRefresher();
        return list;
    }

    public void setState(UserProfile.ProductState state) {
        this.state = state;
    }

    public UserProfile.ProductState getState() {
        return state;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateEntries( 5 );
    }

    private void displayDetail(JsonObject entry )
    {
        Log.d( "Entry_Detail", "title : " + GsonHelpers.getString( entry, "title" ) );
    }

    protected synchronized void addEntries(JsonArray entries ) {
        int size = entities.size();
        int addSize = entries.size();
        if ( begin && addSize == 0 )
        {
            entities.add( JsonNull.INSTANCE );
            addSize = 1;
        }
        else
        {
            for ( int i = 0; i < addSize; ++i )
            {
                JsonObject entry = entries.get(i).getAsJsonObject();
                displayDetail( entry );
                entities.add( entry );
            }
        }

        begin = false;
        adapter.notifyItemRangeInserted( size, addSize );
    }

    private PostItService.PostItResponse getAds(int size )
    {
        return postit.getStatedAds( getArguments().getInt( "id" ), state.status, entities.size(), size );
    }

    protected synchronized void populateEntries( int size )
    {
        populating();

        Helper.Worker.executeAllTask( () -> {
            PostItService.PostItResponse response = getAds( size );

            if ( response != null )
            {
                return () -> {

                    response.respondToArray( jsonElements -> {
                        Log.d( "Populating_Entries", jsonElements.toString() );
                        Log.d( "Scrolling_Listener", "Adding Entry: " + size );
                        addEntries( jsonElements );
                        donePopulating();
                        Log.d( "User_Profile", "Entities are " + entities );
                    }, () -> {});
                };
            }

            return () -> {};
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
        firstLoad = true;
    }

    public synchronized boolean isPopulating()
    {
        return isPopulating;
    }

    private ProductScrollListener getScrollListener()
    {
        if ( scrollListener == null )
            scrollListener = new ProductScrollListener( 5 );
        return scrollListener;
    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager( layoutManager );
        adapter = new ProductAdapter();
        list.setAdapter( adapter );
        list.addOnScrollListener( getScrollListener() );

        list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 10;
                outRect.bottom = 10;
                outRect.left = 10;
                outRect.right = 10;

                if ( parent.getChildAdapterPosition( view ) == 0 )
                    outRect.top = 20;

                if ( parent.getChildAdapterPosition( view ) == parent.getAdapter().getItemCount() - 1 )
                    outRect.bottom = 200;
            }
        });
    }

//    private void initRefresher() {
//        SwipeRefreshLayout refresher = findViewById(R.id.main_refresher);
//        refresher.setOnRefreshListener( () -> {
//            reload();
//            refresher.setRefreshing( false );
//        });
//    }

    public void clear()
    {
        entities.clear();
        adapter.notifyDataSetChanged();
    }

    private void reload() {
        clear();
        begin = true;
        firstLoad = false;
        getScrollListener().reset();

        populateEntries( 5 );
    }

    private class ProductScrollListener extends RecyclerView.OnScrollListener
    {
        private final int threshold;
        private volatile int prevTotal;

        public void reset()
        {
            prevTotal = 0;
        }

        public ProductScrollListener(int thresh )
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

            int last = layoutManager.findLastVisibleItemPosition();
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
        }

        private synchronized boolean isLoading( int currentTotal )
        {
            return prevTotal >= currentTotal;
        }
    }

    public class ProductAdapter extends RecyclerView.Adapter< ViewHolder >
    {
        private static final int EMPTY_VIEW = 0;
        private static final int PRODUCT_VIEW = 1;

        @Override
        public int getItemViewType(int position)
        {
//            if ( inside )
            Log.d( "User_Profile", "adapter Entities are " + entities );
            JsonElement entry = entities.get(position);
            if ( entry.isJsonNull() )
            {
                Log.d( "User_Profile", "Entry " + entry );
                return EMPTY_VIEW;
            }
            else
                return PRODUCT_VIEW;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if ( viewType == PRODUCT_VIEW )
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.product_card, parent, false);
                return new ViewHolder(view,false);
            }
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.empty_user_product, parent, false);
            return new ViewHolder(view,true);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position)
        {
            if  ( !holder.isNull )
            {
                JsonObject entity = entities.get( position ).getAsJsonObject();
                long id = GsonHelpers.getLong(entity, "id");
                holder.title.setText( GsonHelpers.getString( entity, "title" ) );
                holder.category.setText( GsonHelpers.getString( entity, "category_name" ) );
                holder.location.setText( GsonHelpers.getString( entity, "state_name" ) );
                holder.price.setText( Helper.monetaryRepresentation( GsonHelpers.getString( entity, "price" ) ) );

                Uri media = Uri.parse(GsonHelpers.getString( entity, "media_link" ) );
                GlideApp.with( getContext() ).load( media ).placeholder( R.drawable.logo2 )
                        .fitCenter().into( holder.image );

                holder.itemView.setOnClickListener( v -> {
                    Intent detail = new Intent( getContext(), ProductDetailActivity.class );
                    detail.putExtra( "ads_id", id);
                    UserProductList.this.startActivity( detail );
                });

                holder.edit.setOnClickListener( v -> {
                    editAd( id, position );
                });

                holder.refresh.setOnClickListener( v -> {
                    refreshAd( id, position );
                });

                holder.delete.setOnClickListener( v -> {
                    deleteAd( id, position );
                });
            }
        }

        private void editAd(long id, int position) {
            Intent edit = new Intent(UserProductList.this.getContext(), PostActivity.class);
            edit.setAction( "edit" );
            edit.putExtra("ad_id", id );
            edit.putExtra( "user_id", getArguments().getInt( "id" ) );
            edit.putExtra("card_index", position );
            startActivityForResult( edit, EDIT );
        }

        @Override
        public int getItemCount() {
            return entities.size();
        }
    }

    private void refreshAd(long id, int index) {
        Helper.Worker.executeAllTask( () -> {
            PostItService.PostItResponse response = postit.refreshAds( id );

            if ( response != null )
            {
                return () -> {

                    response.respondToPrimitive(object -> {
                        String msg = object.getAsString();
                        Helper.toast( UserProductList.this.getContext(), msg );
                        refreshAd( index );
                    });
                };
            }

            return () -> {};
        });
    }

    private void deleteAd(long id, int index)
    {
        Helper.Worker.executeAllTask( () -> {
            PostItService.PostItResponse response = postit.deleteAds(id);

            if ( response != null )
            {
                return () -> {

                    response.respondToPrimitive(object -> {
                        String msg = object.getAsString();
                        Helper.toast( UserProductList.this.getContext(), msg );
                        deleteAd( index );
                    });
                };
            }

            return () -> {};
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK )
        {
            switch ( requestCode )
            {
                case EDIT:
                    long id = data.getLongExtra( "ad_id", -1 );
                    int index = data.getIntExtra( "card_index", -1 );
                    updateAd( id, index );
                    break;
            }
        }
    }

    private void refreshAd(int index) {
        JsonElement ad = entities.remove(index);
        entities.add( 0, ad );
        adapter.notifyItemMoved( index, 0 );
    }

    private void deleteAd(int index)
    {
        entities.remove( index );
        adapter.notifyItemRemoved( index );
    }

    private void updateAd( long id, int index )
    {
        Log.d( "Updating_Index", String.format("updating index %d, id %d", index, id ) );
        Helper.Worker.executeAllTask( () -> {
            PostItService.PostItResponse response = postit.getAd( id );

            if ( response != null )
            {
                return () -> {

                    response.respondToObject( jsonElement -> {
                        entities.remove( index );
                        entities.add( index, jsonElement );
                        adapter.notifyItemChanged( index );
                    } );
                };
            }

            return () -> {};
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView image;

        public TextView title;
        public TextView category;
        private TextView location;
        private TextView price;
        private boolean isNull;

        public MaterialButton edit;
        public MaterialButton delete;
        public MaterialButton refresh;

        public ViewHolder(View view, boolean isNull ) {
            super(view);
            this.isNull = isNull;
            if ( !isNull )
            {
                image = view.findViewById(R.id.product_image);
                title = view.findViewById(R.id.product_title);
                category = view.findViewById(R.id.product_category);
                location = view.findViewById(R.id.product_location);
                price = view.findViewById(R.id.product_price);

                edit = view.findViewById(R.id.product_edit);
                delete = view.findViewById(R.id.product_delete);
                refresh = view.findViewById(R.id.product_refresh);

                view.findViewById(R.id.user_info).setVisibility( View.GONE );
                view.findViewById(R.id.owners_option).setVisibility( View.VISIBLE );
            }

        }

        public void setImage( Bitmap image )
        {
            if ( image != null )
                this.image.setImageBitmap( image );
            else
            {
                Log.d( "Entry_Default", "setting default image" );
                int resId = getResources().getIdentifier("sample_holder", "drawable",
                        UserProductList.this.getContext().getPackageName() );
                this.image.setImageResource( resId );
            }
        }
    }
}