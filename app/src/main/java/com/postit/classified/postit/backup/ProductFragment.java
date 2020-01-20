//package com.postit.classified.postit.home;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.google.android.material.button.MaterialButton;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.postit.classified.postit.helpers.Helper;
//import com.postit.classified.postit.R;
//import com.postit.classified.postit.service.PostItService;
//
//import java.util.ArrayList;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//public class ProductFragment extends Fragment
//{
//
//    private final ArrayList<JsonObject> entities = new ArrayList<>();
//    private EntityAdapter adapter;
//    private LinearLayoutManager layoutManager;
//    protected boolean begin = true;
//    private boolean isPopulating;
//    private PostItService postit;
//
//    /**
//     * Mandatory empty constructor for the fragment manager to instantiate the
//     * fragment (e.g. upon screen orientation changes).
//     */
//    public ProductFragment()
//    {
//        init();
//    }
//
//    protected void init() {
//        postit = PostItService.getInstance();
//    }
//
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
//
//    private void displayDetail(JsonObject entry )
//    {
//        Log.d( "Entry_Detail", "title : " + entry.getAsJsonPrimitive( "title" ).getAsString() );
//    }
//
////    protected abstract void bindHolder( ViewHolder holder, JSONObject data );
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        populateEntries( 10 );
//        begin = false;
//    }
//
//    protected void populateEntries( int size )
//    {
//        populating();
//
//        Helper.Worker.executeAllTask( () -> {
//            JsonObject response = postit.getAds(entities.size(), size);
//
//            return () -> {
//
//                boolean serverSuccess = false;
//                serverSuccess = response.getAsJsonPrimitive("success").getAsBoolean();
//                if ( serverSuccess )
//                {
//                    JsonObject result = response.getAsJsonObject("response");
//                    if ( result.getAsJsonPrimitive("success").getAsBoolean() )
//                    {
//                        JsonArray entries = result.getAsJsonArray("result");
//                        Log.d( "Populating_Entries", entries.toString() );
//                        Log.d( "Scrolling_Listener", "Adding Entry: " + size );
//                        addEntries( entries );
//                        donePopulating();
//                    }
//                    else
//                    {
//
//                    }
//                }
//            };
//        });
//    }
//
//    public synchronized void populating()
//    {
//        isPopulating = true;
//    }
//
//    public synchronized void donePopulating()
//    {
//        isPopulating = false;
//    }
//
//    public synchronized boolean isPopulating()
//    {
//        return isPopulating;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.entity_list, container, false);
//
//        // Set the adapter
//        if (view instanceof RecyclerView)
//        {
//            Log.d( "Setup_Recycler_View", "Setting Up Recyler View" );
//            Context context = view.getContext();
//            RecyclerView recyclerView = (RecyclerView) view;
//            setLayoutManager( new LinearLayoutManager(context) );
//            recyclerView.setLayoutManager( getLayoutManager() );
//            EntityAdapter adapter = new EntityAdapter();
//            recyclerView.setAdapter( adapter );
//            recyclerView.addOnScrollListener( getScrollListener() );
//            this.adapter = adapter;
//        }
//        return view;
//    }
//
//    public LinearLayoutManager getLayoutManager() {
//        return layoutManager;
//    }
//
//    public void setLayoutManager(LinearLayoutManager layoutManager) {
//        this.layoutManager = layoutManager;
//    }
//
//    private RecyclerView.OnScrollListener getScrollListener()
//    {
//        return new EntityScrollListener( 5 );
//    }
//
//    private class EntityScrollListener extends RecyclerView.OnScrollListener
//    {
//        private final int threshold;
//        private volatile int prevTotal;
//
//        public EntityScrollListener( int thresh )
//        {
//            super();
//            threshold = thresh;
//        }
//
//        @Override
//        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//        }
//
//        @Override
//        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//
//            LinearLayoutManager layoutManager = getLayoutManager();
//            int last = layoutManager.findLastVisibleItemPosition();
//            int total = layoutManager.getItemCount();
//            int visibleLeft = ( total - 1 ) - last;
//
////            Log.d( "Scrolling_Listener", "Visible Left: " + visibleLeft );
////            Log.d( "Scrolling_Listener", "Previous Total: " + prevTotal );
//
//            if ( visibleLeft < threshold && !isLoading( total ) && !isPopulating() )
//            {
//                prevTotal = total;
//                Log.d( "Scrolling_Listener", "Is It Loading: " + isLoading( total ) );
//                populateEntries( visibleLeft );
//            }
//        }
//
//        private synchronized boolean isLoading( int currentTotal )
//        {
//            return prevTotal >= currentTotal;
//        }
//    }
//
//    public class EntityAdapter extends RecyclerView.Adapter< ViewHolder >
//    {
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.product_card, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position)
//        {
//            JsonObject entity = entities.get( position );
//            holder.title.setText( entity.getAsJsonPrimitive( "title" ).getAsString() );
////            String title = String.format( "%s ( N%s )", entity.getString("title" ), entity.getString( "price" ) );
////                holder.details.setText( entity.getString("description" ) );
////                String media = entity.getString("media_link");
////                if ( media != null && !media.isEmpty() )
////                    postit.setImage( media );
//        }
//
//        @Override
//        public int getItemCount() {
//            return entities.size();
//        }
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder
//    {
//        public final ImageView image;
//        public final TextView title;
//        public final TextView details;
//        public final MaterialButton first;
//        public final MaterialButton second;
//
//        public ViewHolder(View view) {
//            super(view);
//            image = view.findViewById(R.id.product_image);
//            title = view.findViewById(R.id.product_title);
//            details = view.findViewById(R.id.product_category);
//            first = view.findViewById(R.id.entity_first);
//            second = view.findViewById(R.id.entity_second);
//        }
//
//        public void setImage( Bitmap image )
//        {
//            if ( image != null )
//                this.image.setImageBitmap( image );
//            else
//            {
//                Log.d( "Entry_Default", "setting default image" );
//                int resId = getResources().getIdentifier("sample_holder", "drawable", getContext().getPackageName());
//                this.image.setImageResource( resId );
//            }
//        }
//    }
//
//    public static Fragment getInstance()
//    {
//        ProductFragment product = new ProductFragment();
//        return product;
//    }
//}
