package com.postit.classified.postit.backup;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.postit.classified.postit.R;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.dialog.ListFilterManager;
import com.postit.classified.postit.service.PostItService;
import com.postit.classified.postit.backup.supplimentries.FilterFragment;

import java.util.ArrayList;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryFragment extends Fragment implements ListFilterManager.Filterable, ListFilterManager.Reloadable, ListFilterManager.Manageble
{

    private PostItService postit;
    private ArrayList< GridElement > gridElements = new ArrayList<>();
    private ArrayList<GridElement> gridCopy;
    private JsonArray listElements = new JsonArray();
    private RecyclerView grid;
    private RecyclerView list;
    private GridAdapter gridAdapter;
    private ListAdapter listAdapter;
    private JsonArray listCopy;
    private ListFilterManager manager;
//    private String query = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
        initGridElements();
    }

    private void initGridElements() {
        gridElements.add( new GridElement( 27, "Cars", R.drawable.ic_car ) );
        gridElements.add( new GridElement( 2, "Smart Phone", R.drawable.ic_phone ) );
        gridElements.add( new GridElement( 5, "Laptops & Computers", R.drawable.ic_laptop ) );
        gridElements.add( new GridElement( 6, "Home & Garden", R.drawable.ic_home ) );
        gridElements.add( new GridElement( 12, "Food & Agriculture", R.drawable.ic_tractor ) );
        gridElements.add( new GridElement( 21, "Services", R.drawable.ic_service ) );

        gridCopy = gridElements;
    }

    private void initCategories() {
        Helper.Worker.executeTask( () -> {
            PostItService.PostItResponse categories = postit.getCategories();
            return () -> {
                Log.d( "Category_Fragment", "The categories are " + categories.getResultArray() );
                categories.respondToArray(this::prepareElements);
                initGrid();
                initList();
//                setupContext();
            };
        });
    }

    private void prepareElements( JsonArray elements )
    {
        Iterator<GridElement> grid = gridElements.iterator();
        while   ( grid.hasNext() )
        {
            GridElement element = grid.next();
            Iterator<JsonElement> list = elements.iterator();
            while   ( list.hasNext() )
            {
                JsonElement listElement = list.next();
                int id = listElement.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
                if ( element.id == id )
                {
                    int brandCount = listElement.getAsJsonObject().getAsJsonPrimitive("brand_count").getAsInt();
                    element.setCount( brandCount );
                    list.remove();
                    break;
                }
            }
        }

        listElements = elements;
        listCopy = elements;
    }

    private void init(Bundle savedInstanceState) {
        postit = PostItService.getInstance();
//        if ( savedInstanceState != null )
//            query = savedInstanceState.getString( "query", "" );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.view_grid_list, container, false);
        grid = view.findViewById(R.id.grid_category);
        list = view.findViewById(R.id.list_category);
        initCategories();
        return view;
    }

    public void initGrid()
    {
        gridAdapter = new GridAdapter();
        GridLayoutManager layout = new GridLayoutManager(getContext(), 3);
        grid.setLayoutManager( layout );
        grid.setAdapter(gridAdapter);

        grid.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 10;
                outRect.bottom = 10;
                outRect.left = 10;
                outRect.right = 10;

                if ( parent.getChildAdapterPosition( view ) <= 2 )
                    outRect.top = 10;

                if ( parent.getChildAdapterPosition( view ) >= parent.getAdapter().getItemCount() - 3 )
                    outRect.bottom = 10;
            }
        });
    }

    public void initList()
    {
        listAdapter = new ListAdapter();
        LinearLayoutManager layout = new LinearLayoutManager( getContext() );
        list.setLayoutManager( layout );
        list.setAdapter(listAdapter);

        list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 30;
                outRect.bottom = 30;
                outRect.left = 20;
                outRect.right = 20;

                if ( parent.getChildAdapterPosition( view ) == 0 )
                    outRect.top = 10;

                if ( parent.getChildAdapterPosition( view ) == parent.getAdapter().getItemCount() - 1 )
                    outRect.bottom = 10;
            }
        });
    }

    @Override
    public Filter[] getFilters() {
        if ( gridAdapter == null || listAdapter == null )
            return new Filter[ 0 ];
        return new Filter[]{ gridAdapter.getFilter(), listAdapter.getFilter() };
    }

    @Override
    public void reload( Bundle arguments ) {
        setArguments( arguments );
        Log.d( "Add_Category", "reloading for " + getArguments() );
        refresh();
    }

    private void refresh()
    {
        Helper.Worker.executeTask( () -> {
            PostItService.PostItResponse categories = postit.getCategories();
            return () -> {
                Log.d( "Category_Fragment", "The categories are " + categories.getResultArray() );
                categories.respondToArray(this::prepareElements);
                gridAdapter.notifyDataSetChanged();
                listAdapter.notifyDataSetChanged();
//                setupContext();
            };
        });
    }

    @Override
    public void setManager(ListFilterManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "Category";
    }

//    @Override
//    public void reload() {
//        getArguments();
//    }

    private class GridAdapter extends RecyclerView.Adapter< GridHolder > implements Filterable
    {
        @NonNull
        @Override
        public GridHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item, parent, false);
            return new GridHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GridHolder holder, int position) {
            GridElement gridElement = gridElements.get(position);
            holder.name.setText( gridElement.name );
            GlideApp.with( getContext() ).load( gridElement.drawable ).fitCenter().into( holder.image );

            holder.itemView.setOnClickListener( v ->
            {
                Log.d( "Filter_Fragment", "handling category fragment" );
                manager.addProperty( gridElement.id, gridElement.name );

                Bundle extras = new Bundle();
                extras.putInt( "category_id", gridElement.id );
                extras.putInt( "brand_count", gridElement.getCount() );
                extras.putBoolean( "has_next", gridElement.getCount() > 0 );
                manager.nextFilter( extras );

            });

        }

        @Override
        public int getItemCount() {
            return gridElements.size();
        }


        @Override
        public Filter getFilter()
        {
            return new FilterFragment.ArrayListFilter(this, gridCopy, input -> {
                GridElement gridElement = (GridElement) input;
                return gridElement.name;
            }, objects -> gridElements = (ArrayList<GridElement>) objects);
        }
    }

    public class GridHolder extends RecyclerView.ViewHolder
    {
        private final ImageView image;
        private final TextView name;

        public GridHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById( R.id.item_image );
            name = itemView.findViewById( R.id.item_name );
        }
    }

    private class ListAdapter extends RecyclerView.Adapter< ListHolder > implements Filterable
    {

        @NonNull
        @Override
        public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ListHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ListHolder holder, int position) {
            JsonObject jsonObject = listElements.get(position).getAsJsonObject();
            String name = jsonObject.getAsJsonPrimitive("category_name").getAsString();
            holder.name.setText(name);

            if ( position % 2 == 0 )
                holder.container.setBackgroundColor(ContextCompat.getColor( getContext(), R.color.list_normal ));
            else
                holder.container.setBackgroundColor(ContextCompat.getColor( getContext(), R.color.list_invert ));

            holder.itemView.setOnClickListener( v ->
            {
                Log.d( "Filter_Fragment", "handling category fragment" );
                int id = jsonObject.getAsJsonPrimitive("id").getAsInt();
                int brandCount = jsonObject.getAsJsonPrimitive("brand_count").getAsInt();

                manager.addProperty( id, name );

                Bundle extras = new Bundle();
                extras.putInt( "category_id", id );
                extras.putInt( "brand_count", brandCount );
                extras.putBoolean( "has_next", brandCount > 0 );
                manager.nextFilter( extras );
            });
        }

        @Override
        public int getItemCount() {
            return listElements.size();
        }

        @Override
        public Filter getFilter()
        {
            return new ListFilterManager.JsonArrayFilter(this, listCopy,
                    input -> input.getAsJsonObject().getAsJsonPrimitive("category_name").getAsString(),
                    jsonElements -> listElements = jsonElements);
        }
    }

    public class ListHolder extends RecyclerView.ViewHolder
    {
        private final TextView name;
        private final ConstraintLayout container;

        public ListHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            container = itemView.findViewById(R.id.item_container);
        }
    }

    private class GridElement
    {
        private final int id;
        private final String name;
        private final int drawable;

        private int count;

        public GridElement(int id, String name, int drawable) {
            this.id = id;
            this.name = name;
            this.drawable = drawable;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
