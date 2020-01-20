package com.postit.classified.postit.backup;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.postit.classified.postit.R;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.service.PostItService;

import java.util.ArrayList;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BrandFragment extends Fragment
{

    private PostItService postit;
    private ArrayList< JsonObject > listElements = new ArrayList<>();
//    private JsonArray listElements;
    private RecyclerView list;
    private ListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void initCategories() {
        Helper.Worker.executeTask( () -> {
            int categoryId = getArguments().getInt("category_id");
            PostItService.PostItResponse categories = postit.getBrands( categoryId );
            return () -> {
                Log.d( "Category_Fragment", "The categories are " + categories.getResultArray() );
                categories.respondToArray(this::prepareElements);
                initList( list );
            };
        });
    }

    private void prepareElements( JsonArray elements )
    {
//        listElements = elements;

        listElements.clear();
        Iterator<JsonElement> list = elements.iterator();
        while   ( list.hasNext() )
        {
            JsonElement listElement = list.next();
            listElements.add( listElement.getAsJsonObject() );
        }
        Log.d( "Category_Fragment", "Elements are " + listElements );
    }

    private void init() {
        postit = PostItService.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
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
                    outRect.top = 10;

                if ( parent.getChildAdapterPosition( view ) == parent.getAdapter().getItemCount() - 1 )
                    outRect.bottom = 10;
            }
        });
    }

    private class ListAdapter extends RecyclerView.Adapter< ListHolder >
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
            holder.name.setText( jsonObject.getAsJsonPrimitive("brand_name").getAsString());

            if ( position % 2 == 0 )
                holder.container.setBackgroundColor(ContextCompat.getColor( getContext(), R.color.list_normal ));
            else
                holder.container.setBackgroundColor(ContextCompat.getColor( getContext(), R.color.list_invert ));
        }

        @Override
        public int getItemCount() {
            return listElements.size();
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
