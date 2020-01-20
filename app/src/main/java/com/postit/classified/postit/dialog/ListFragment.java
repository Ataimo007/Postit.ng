package com.postit.classified.postit.dialog;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.postit.classified.postit.R;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.service.PostItService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ListFragment extends Fragment implements ListFilterManager.Filterable, ListFilterManager.Reloadable,
        ListFilterManager.Manageble
{
    public PostItService postit;
//    private ArrayList< JsonObject > listElements = new ArrayList<>();
    private JsonArray listElements;
    private RecyclerView list;
    private ListAdapter adapter;
    private JsonArray listCopy;
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

    protected abstract Bundle processElements(  JsonObject element );

    protected abstract String getElementName( JsonObject element );

    @Override
    public void setManager(ListFilterManager manager) {
        this.manager = manager;
    }

    private void prepareElements(JsonArray elements )
    {
        listElements = elements;
        listCopy = elements;
    }

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
        adapter = new ListAdapter();
        LinearLayoutManager layout = new LinearLayoutManager( getContext() );
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

                if ( parent.getChildAdapterPosition( view ) == parent.getAdapter().getItemCount() - 1 )
                    outRect.bottom = 200;
            }
        });
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
            JsonObject jsonObject = listElements.get( position ).getAsJsonObject();
            holder.name.setText( getElementName( jsonObject ) );

            if ( position % 2 == 0 )
                holder.container.setBackgroundColor(ContextCompat.getColor( getContext(), R.color.list_normal ));
            else
                holder.container.setBackgroundColor(ContextCompat.getColor( getContext(), R.color.list_invert ));

            holder.itemView.setOnClickListener( v -> {
                Bundle bundle = processElements(jsonObject);
                manager.nextFilter( bundle );
            });
        }

        @Override
        public int getItemCount() {
            return listElements.size();
        }

        @Override
        public Filter getFilter() {
            return new ListFilterManager.JsonArrayFilter(this, listCopy,
                    input -> getElementName(input.getAsJsonObject()), jsonElements -> listElements = jsonElements);
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
}
