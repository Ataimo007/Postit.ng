package com.postit.classified.postit.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Filter;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

public class ListFilterManager extends FragmentPagerAdapter
{
    private ViewPager viewer;
    private TabLayout labels;
    private ChipGroup filterEntries;

    private final Fragment[] collection;
    private final Pair<Integer, String>[] props;
    private final FilterHandler handler;


    private ArrayList<Fragment> filters = new ArrayList<>();

    public ArrayList<Fragment> staticFragments = new ArrayList<>();

    public ListFilterManager(@NonNull FragmentManager fm, Fragment[] collection, FilterHandler handler )
    {
        super(fm);
        this.collection = collection;
        props = new Pair[ collection.length ];
        this.handler = handler;
        init();
        setupFilters();
    }

    private void init() {
        viewer = handler.getViewer();
        labels = handler.getLabels();
        filterEntries = handler.getEntries();
    }

    public Pair<Integer, String>[] getProps() {
        return props;
    }

    public void addProperty(int id, String name )
    {
        handler.prepareChip();
        Chip chip = handler.getChip(name);
        setProperty( id, name );
        filterEntries.addView( chip );
    }

    private void setProperty(int id, String name) {
        int currentItem = viewer.getCurrentItem();
        Pair<Integer, String> prop = Pair.create(id, name);
        props[ currentItem ] = prop;

        int size = viewer.getChildCount();
        if ( currentItem < size - 1 )
            Arrays.fill( props, currentItem + 1, size, null );
    }

    private void setupFilters()
    {
        Fragment fragment = collection[0];
        registerManager( fragment );
        filters.add( fragment );
        addLabel( fragment );
    }

    private void registerManager( Fragment fragment )
    {
        Manageble manageble = ( Manageble ) fragment;
        manageble.setManager( this );
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return filters.get(position);
    }

    public Fragment getStaticItem( int position )
    {
        return staticFragments.get( position );
    }

    public boolean isStatic( int position )
    {
        Fragment item = filters.get( position );
        if ( position < staticFragments.size() )
            return staticFragments.get( position ).getClass().equals( item.getClass() );
        return false;
    }

    public void nextFilter(Bundle extras)
    {
        Log.d( "Filter_Fragment", "moving to next filter with bundle " + extras );
        int currentFilter = viewer.getCurrentItem();
        if ( currentFilter + 1 >= collection.length )
        {
            finish();
            return;
        }

        boolean hasNext = extras.getBoolean("has_next", false);
        Log.d( "Filter_Fragment", "The bundle is " + extras );
        Log.d( "Filter_Fragment", "does it has next " + hasNext );
        if ( hasNext )
        {
            if ( currentFilter < getCount() - 1 )
                handler.removePageFrom( currentFilter + 1 );
            addFilter( currentFilter + 1, extras );
        }
        else
            finish();
    }

    private void finish()
    {
        handler.finalAction( props );
    }

    private void addFilter(int i, Bundle extras) {
        Fragment fragment = collection[i];
        fragment.setArguments( extras );
        handler.addFilter( fragment );

        if ( isStatic( i ) )
            handler.refresh( i, extras );
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Object itemRaw = super.instantiateItem(container, position);
        Fragment item = ( Fragment ) itemRaw;
        if ( position < staticFragments.size() )
            staticFragments.set( position, item );
        else
            staticFragments.add( item );
        return itemRaw;
    }

    @Override
    public int getCount() {
        return filters.size();
    }

    public void add( Fragment fragment ) {
        addLabel( fragment );
        addPage( fragment );
    }

    private void addPage( Fragment fragment )
    {
        registerManager( fragment );
        filters.add( fragment );
        notifyDataSetChanged();
        int currentFilter = viewer.getCurrentItem();
        viewer.setCurrentItem( currentFilter + 1 );
    }

    private void addLabel( Fragment fragment )
    {
        Manageble manageble = ( Manageble ) fragment;
        TabLayout.Tab label = labels.newTab();
        label.setText( manageble.getName() );
        labels.addTab( label );
    }

    public void removeFrom(int index) {
        ListIterator<Fragment> fragments = filters.listIterator(index);
        while   ( fragments.hasNext() )
        {
            fragments.next();
            fragments.remove();
        }
        notifyDataSetChanged();
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
            query = query.trim();
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
            query = query.trim();
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

    public static interface Filterable
    {
        Filter[] getFilters();
    }

    public static interface Reloadable
    {
        void reload(Bundle arguments);
    }

    public static interface Manageble
    {
        void setManager(ListFilterManager manager);
        String getName();
    }

    public static interface FilterHandler
    {
        void prepareChip();

        Chip getChip(String name);

        void removePageFrom(int i);

        void refresh(int i, Bundle extras);

        void addFilter(Fragment fragment);

        void finalAction(Pair<Integer, String>[] props);

        ViewPager getViewer();

        TabLayout getLabels();

        ChipGroup getEntries();
    }
}
