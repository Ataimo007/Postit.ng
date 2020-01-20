package com.postit.classified.postit.dialog;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.postit.classified.postit.R;
import com.postit.classified.postit.helpers.GlideApp;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.service.PostItService;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class GridIconFragment extends Fragment implements ListFilterManager.Filterable, ListFilterManager.Reloadable,
        ListFilterManager.Manageble
{
    public PostItService postit;
    protected ArrayList< GridElement > gridElements;
    private ArrayList<GridElement> gridCopy;
    private RecyclerView list;
    private GridAdapter adapter;
    protected ListFilterManager manager;
//    private String query;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void initCategories() {
        Helper.Worker.executeTask( () -> {
            PostItService.PostItResponse models = getElements();
            return () -> {
                models.respondToArray(this::prepareElements);
                saveCopy();
                initList( list );
//                setupContext();
            };
        });
    }

    private void refresh()
    {
        Helper.Worker.executeTask( () -> {
            PostItService.PostItResponse models = getElements();
            return () -> {
                models.respondToArray(this::prepareElements);
                saveCopy();
                adapter.notifyDataSetChanged();
//                setupContext();
            };
        });
    }

    @Override
    public void reload( Bundle arguments ) {
        setArguments( arguments );
        refresh();
    }

    protected abstract PostItService.PostItResponse getElements();

    protected abstract Bundle processElements(  GridElement element );

    @Override
    public void setManager(ListFilterManager manager) {
        this.manager = manager;
    }

    protected void prepareElements(JsonArray elements )
    {
        gridElements = new ArrayList<>( elements.size() );
        for ( JsonElement element : elements )
            gridElements.add( convert( element) );
    }

    private void saveCopy()
    {
        gridCopy = gridElements;
    }

    protected abstract GridElement convert(JsonElement element);

    @Override
    public Filter[] getFilters() {
        if ( adapter == null )
            return new Filter[ 0 ];
        return new Filter[]{ adapter.getFilter() };
    }

    private void init() {
        postit = PostItService.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_list, container, false);
        list = view.findViewById(R.id.list_category);
        initCategories();
        return view;
    }

    public void initList(RecyclerView list)
    {
        adapter = new GridAdapter();
        LinearLayoutManager layout = new GridLayoutManager(getContext(), getSpanCount() );
        list.setLayoutManager( layout );
        list.setAdapter(adapter);

        list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 30;
                outRect.bottom = 30;
                outRect.left = 20;
                outRect.right = 20;

                if ( parent.getChildAdapterPosition( view ) == 0 )
                    outRect.top = 0;

                if ( parent.getChildAdapterPosition( view )
                        >= parent.getAdapter().getItemCount() - ( parent.getAdapter().getItemCount() % getSpanCount() ) )
                    outRect.bottom = 200;
            }
        });
    }

    protected abstract int getSpanCount();

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
                Bundle bundle = processElements(gridElement);
                manager.nextFilter( bundle );
            });

        }

        @Override
        public int getItemCount() {
            return gridElements.size();
        }


        @Override
        public Filter getFilter()
        {
            return new ListFilterManager.ArrayListFilter(this, gridCopy, input -> {
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

    class GridElement
    {
        final int id;
        final String name;
        final int drawable;
        final int count;

        public GridElement(int id, String name, int count, int drawable) {
            this.id = id;
            this.name = name;
            this.drawable = drawable;
            this.count = count;
        }
    }
}
