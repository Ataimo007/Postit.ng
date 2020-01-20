package com.postit.classified.postit.backup.supplimentries;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.postit.classified.postit.MainActivity;
import com.postit.classified.postit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

public class FilterFragment extends BottomSheetDialogFragment
{
    private ArrayList<Fragment> filters = new ArrayList<>();
    private ArrayList< String > queries = new ArrayList<>();
    private ArrayList< Filter[] > searchFilters = new ArrayList<>();
    private View view;
    private ViewPager viewer;
    private TabLayout labels;
    private FilterManger filterManger;
    public MainActivity.Filter filter = new MainActivity.Filter();
    private MainActivity main;
    private ChipGroup filterEntries;
    private TextInputEditText search;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFilter();
    }

    public void filter()
    {
        Log.d( "Ads_Category", "get ads by category " + filter );
//        main.filter( filter );
        dismiss();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if ( context instanceof MainActivity )
            main = ( MainActivity ) context;
    }

    private void initFilter()
    {
        filters.add( new CategoryFragment() );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_product_filter, container, false);
        initFilterView();
        initControls();
        initFilterEntries();
        initSearchFilters();
        initSearch();
        initBackPressed();
        return view;
    }

    private void initSearchFilters() {
        viewer.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                String query = position < queries.size() ? queries.get( position ) : "";
                setSearchQuery( query );
            }
        });
    }

    public String getSearchQuery()
    {
        return search.getText().toString();
    }

    public void setSearchQuery( String query )
    {
        search.getText().clear();
        search.append( query );
    }

    private void initSearch() {
        search = view.findViewById(R.id.filter_search_field);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateQuery( s.toString() );
                filter( s );
            }
        });
    }

    private void filter( Editable query )
    {
        int position = viewer.getCurrentItem();
//        Fragment item = filterManger.getItem(position);
        Fragment item = filterManger.getStaticItem(position);
        if ( item instanceof Filterable )
        {
            Filterable filterable = ( Filterable ) item;
            for ( Filter filter : filterable.getFilters() )
                filter.filter( query );
        }
    }

    private void updateQuery(String query )
    {
        int position = viewer.getCurrentItem();
        if ( position < queries.size() )
            queries.set(position, query );
        else
            queries.add(query);
    }

    private void initFilterEntries() {
        filterEntries = view.findViewById(R.id.filters_selected);
    }

    private Chip getChip( String filter )
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Chip entry = (Chip) inflater.inflate(R.layout.filter_chip, filterEntries, false);
        entry.setText( filter );
        int pageIndex = viewer.getCurrentItem();
        int chipIndex = filterEntries.getChildCount();

        entry.setOnClickListener(v -> {
            viewer.setCurrentItem( pageIndex );
        });
        entry.setOnCloseIconClickListener( v -> {
            filterEntries.removeViews( chipIndex, filterEntries.getChildCount() - chipIndex );
            removePageFrom( pageIndex + 1 );
        });

        return entry;
    }

    private void removeChipsFrom( int start )
    {
        Log.d( "Filter_Fragment", "The start is " + start );
        Log.d( "Filter_Fragment", "The count from start " + ( filterEntries.getChildCount() - start ) );
        Log.d( "Filter_Fragment", "The count is " + ( filterEntries.getChildCount() ) );
        filterEntries.removeViews( start, filterEntries.getChildCount() - start );
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initBackPressed()
    {
        getDialog().setOnKeyListener( (dialog, keyCode, event) -> {
            if  ( KeyEvent.KEYCODE_BACK == keyCode && event.getAction() == KeyEvent.ACTION_UP )
                return handleBackPressed();
            return false;
        });
    }

    private boolean handleBackPressed()
    {
        if ( filters.size() > 1 )
        {
            removePageFrom( filters.size() - 1 );
            removeChipsFrom( filterEntries.getChildCount() - 1 );
            return true;
        }
        return false;
    }

    private Chip getChip(String filter, int drawable )
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Chip entry = (Chip) inflater.inflate(R.layout.filter_chip, filterEntries, false);
        entry.setText( filter );
        entry.setChipIconResource( drawable );
        return entry;
    }

    public void initFilterView()
    {
        viewer = view.findViewById(R.id.filter_view);
        labels = view.findViewById(R.id.filter_label);

        filterManger = new FilterManger( getChildFragmentManager() );
        viewer.setAdapter( filterManger );

        viewer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(labels));
        labels.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewer));
    }

    private void initControls()
    {
        FloatingActionButton filter = view.findViewById(R.id.action_filter);
        filter.setOnClickListener( v -> {
            filter();
        });
    }

    private void removePageFrom( int index )
    {
        TabLayout.Tab[] tabs = new TabLayout.Tab[ labels.getTabCount() - index ];
        for ( int i = 0; i < tabs.length; ++i )
            tabs[ i ] = labels.getTabAt( index + i );
        for ( int i = 0; i < tabs.length; ++i )
            labels.removeTab( tabs[ i ] );

        ListIterator<Fragment> fragments = filters.listIterator(index);
        while   ( fragments.hasNext() )
        {
            fragments.next();
            fragments.remove();
        }
        filterManger.notifyDataSetChanged();
    }

    public void nextFilter(Bundle extras)
    {
        int currentFilter = viewer.getCurrentItem();
        if ( currentFilter < filters.size() - 1 )
            removePageFrom( currentFilter + 1 );
        addFilter( currentFilter + 1, extras );
    }

//    public void nextFilter(Bundle extras)
//    {
//        int currentFilter = viewer.getCurrentItem();
//        if ( currentFilter < filters.size() - 1 )
//        {
//            removePageFrom( currentFilter + 1 );
//            addFilter( currentFilter + 1, extras, true );
//        }
//        else
//        {
//            addFilter( currentFilter + 1, extras, false );
//        }
//    }


//    public void nextFilter(Bundle extras)
//    {
//        int currentFilter = viewer.getCurrentItem();
//        if ( currentFilter < filters.size() - 1 )
//        {
//            removePageFrom( currentFilter + 2 );
//            refresh( currentFilter + 1, extras );
//        }
//        else
//            addFilter( currentFilter + 1, extras );
//    }


    private void addFilter(int i, Bundle extras) {
        switch ( i )
        {
            case 1:
                addBrand( extras );
                break;

            case 2:
                addModel( extras );
                break;
        }

        if ( filterManger.isStatic( i ) )
            refresh( i, extras );
    }

//    private void addFilter(int i, Bundle extras, boolean reload) {
//        switch ( i )
//        {
//            case 1:
//                addBrand( extras );
//                break;
//
//            case 2:
//                addModel( extras );
//                break;
//        }
//
//        if ( reload )
//            refresh( i, extras );
//    }

    private void refresh(int position, Bundle extras)
    {
        Fragment item = filterManger.getStaticItem(position);
        if ( item instanceof Reloadable )
        {
            Reloadable reloadable = ( Reloadable ) item;
            reloadable.reload( extras );
        }
    }

    private void reload(int i) {
        filterManger.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Fragment fragment = filterManger.getItem( i );
                Fragment original = filterManger.fragments.get(i);
                if ( fragment instanceof ListFragment )
                {
                    ListFragment list = ( ListFragment ) fragment;
                    ListFragment list2 = ( ListFragment ) original;
                }
            }
        });

//        if ( fragment instanceof Reloadable )
//        {
//            Reloadable reloadable = ( Reloadable ) fragment;
//            reloadable.reload();
//        }
    }

    private void addModel(Bundle extras) {
        ModelFragment model = new ModelFragment();
        model.setArguments( extras );
        addFilter( "Models", model );
    }

    private void prepareChip()
    {
        int currentView = viewer.getCurrentItem();
        filterEntries.removeViews( currentView, filterEntries.getChildCount() - currentView );
    }

    public void setCategory( int id, String name )
    {
        prepareChip();

        filter.setCategoryId( id );
        Chip chip = getChip(name);
        filterEntries.addView( chip );
    }

    public void setBrand( int id, String name )
    {
        prepareChip();

        filter.setBrandId( id );
        Chip chip = getChip(name);
        filterEntries.addView( chip );
    }

    public void setModel( int id, String name )
    {
        prepareChip();

        filter.setModelId( id );
        Chip chip = getChip(name);
        filterEntries.addView( chip );
    }

    private void addBrand(Bundle extras) {
        BrandFragment brand = new BrandFragment();
        brand.setArguments( extras );
        addFilter( "Brands", brand );
    }

    public void addFilter(String name, Fragment fragment )
    {
        TabLayout.Tab label = labels.newTab();
        label.setText( name );
        labels.addTab( label );

//        filterManger.addItem( fragment );

        int currentFilter = viewer.getCurrentItem();
        filters.add( fragment );
        filterManger.notifyDataSetChanged();
        viewer.setCurrentItem( currentFilter + 1 );
    }

//    public void addFilters(Filter[] filters) {
//        int position = viewer.getCurrentItem();
//
//        if ( position >= searchFilters.size() )
//            searchFilters.add(position, filters );
//        else
//            searchFilters.set(position, filters );
//
//        if ( position >= queries.size() )
//            queries.add(position, "" );
//    }

    private class FilterManger extends FragmentPagerAdapter
    {
        public ArrayList<Fragment> fragments = new ArrayList<>();

        public FilterManger(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return filters.get(position);
        }

        public Fragment getStaticItem( int position )
        {
            return fragments.get( position );
        }

        public boolean isStatic( int position )
        {
            Fragment item = filters.get( position );
            if ( position < fragments.size() )
                return fragments.get( position ).getClass().equals( item.getClass() );
            return true;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Object itemRaw = super.instantiateItem(container, position);
            Fragment item = ( Fragment ) itemRaw;
            if ( position < fragments.size() )
                fragments.set( position, item );
            else
                fragments.add( item );
            return itemRaw;
        }

        @Override
        public int getCount() {
            return filters.size();
        }
    }

    public static class ArrayListFilter extends Filter
    {
        private final List< ? > list;
        private final Function< Object, String > convert;
        private final RecyclerView.Adapter adapter;
        private final Consumer< List< ? > > replace;

        public ArrayListFilter(RecyclerView.Adapter adapter, List<?> list, Function<Object, String> convert, Consumer<List<?>> replace)
        {
            this.list = list;
            this.convert = convert;
            this.adapter = adapter;
            this.replace = replace;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String query = constraint.toString();
            if ( query.isEmpty() )
                results.values = list;
            else
            {
                List<Object> filtered = new ArrayList<>();
                for ( Object item : list )
                    if ( convert.apply( item ).toLowerCase().contains( query.toLowerCase() ) )
                        filtered.add( item );
                results.values = filtered;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<?> filtered = (List<?>) results.values;
            replace.accept( filtered );
            adapter.notifyDataSetChanged();
        }
    }

    public static class JsonArrayFilter extends Filter
    {
        private final JsonArray list;
        private final Function< JsonElement, String > convert;
        private final RecyclerView.Adapter adapter;
        private final Consumer< JsonArray > replace;

        public JsonArrayFilter(RecyclerView.Adapter adapter, JsonArray list,
                               Function<JsonElement, String> convert, Consumer<JsonArray> replace)
        {
            this.list = list;
            this.convert = convert;
            this.adapter = adapter;
            this.replace = replace;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String query = constraint.toString();
            if ( query.isEmpty() )
                results.values = list;
            else
            {
                JsonArray filtered = new JsonArray();
                for ( JsonElement item : list )
                    if ( convert.apply( item ).toLowerCase().contains( query.toLowerCase() ) )
                        filtered.add( item );
                results.values = filtered;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            JsonArray filtered = (JsonArray) results.values;
            replace.accept( filtered );
            adapter.notifyDataSetChanged();
        }
    }

    public static interface Filterable
    {
        Filter[] getFilters();
    }

    public static interface Reloadable
    {
        void reload( Bundle arguments );
    }

}
