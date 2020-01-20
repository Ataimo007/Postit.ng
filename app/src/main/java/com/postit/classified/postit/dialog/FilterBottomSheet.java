package com.postit.classified.postit.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.postit.classified.postit.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class FilterBottomSheet extends BottomSheetDialogFragment implements ListFilterManager.FilterHandler
{
    private ArrayList< String > queries = new ArrayList<>();
    private View view;
    private ViewPager viewer;
    private TabLayout labels;
    private ListFilterManager filterManger;
    private ChipGroup filterEntries;
    private TextInputEditText search;

    private Consumer< Pair<Integer, String>[] > process;
    private Fragment[] collection;
    private FloatingActionButton filterButton;

    public boolean isShowFilterButton() {
        return showFilterButton;
    }

    public void setShowFilterButton(boolean showFilterButton) {
        this.showFilterButton = showFilterButton;
    }

    private boolean showFilterButton = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initFilter();
    }

    public void finalAction( Pair<Integer, String>[] result )
    {
        process.accept( result );
        dismiss();
    }

    @Override
    public ViewPager getViewer() {
        return viewer;
    }

    @Override
    public TabLayout getLabels() {
        return labels;
    }

    @Override
    public ChipGroup getEntries() {
        return filterEntries;
    }

    public void setProcess(Consumer<Pair<Integer, String>[]> process) {
        this.process = process;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_list_filter, container, false);
        initControls();
        initFilterEntries();
        initFilterView();
        initSearchFilters();
        initSearch();
        initBackPressed();
        return view;
    }

    private void initBackPressed()
    {
        getDialog().setOnKeyListener( (dialog, keyCode, event) -> {
            if  ( KeyEvent.KEYCODE_BACK == keyCode && event.getAction() == KeyEvent.ACTION_UP )
                return handleBackPressed();
            return false;
        });
    }

    public static FilterBottomSheet listFilters(Consumer<Pair<Integer, String>[]> process, Fragment ...fragments)
    {
        FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
        filterBottomSheet.setCollection( fragments );
        filterBottomSheet.setProcess( process );
        return filterBottomSheet;
    }

    public static FilterBottomSheet listFilters(Consumer<Pair<Integer, String>[]> process, Bundle firstArg, Fragment ...fragments)
    {
        FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
        fragments[ 0 ].setArguments( firstArg );
        filterBottomSheet.setCollection( fragments );
        filterBottomSheet.setProcess( process );
        return filterBottomSheet;
    }

    public void setCollection(Fragment[] collection) {
        this.collection = collection;
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
                String query = s.toString().trim();
                updateQuery(query);
                filter( query );
            }
        });
    }

    private void filter( String query )
    {
        int position = viewer.getCurrentItem();
        Fragment item = filterManger.getStaticItem(position);
        if ( item instanceof ListFilterManager.Filterable)
        {
            ListFilterManager.Filterable filterable = (ListFilterManager.Filterable) item;
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

    public Chip getChip(String filter)
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
        filterEntries.removeViews( start, filterEntries.getChildCount() - start );
    }

    private boolean handleBackPressed()
    {
        if ( filterManger.getCount() > 1 )
        {
            removePageFrom( filterManger.getCount() - 1 );
            removeChipsFrom( filterEntries.getChildCount() - 1 );
            return true;
        }
        return false;
    }

    public void initFilterView()
    {
        viewer = view.findViewById(R.id.filter_view);
        labels = view.findViewById(R.id.filter_label);

        filterManger = new ListFilterManager( getChildFragmentManager(), collection, this );
        viewer.setAdapter( filterManger );

        viewer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(labels));
        labels.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewer));
    }

    private void initControls()
    {
        filterButton = view.findViewById(R.id.action_filter);
        filterButton.setOnClickListener( v -> {
            finalAction( filterManger.getProps() );
        });
        if  ( isShowFilterButton() )
            filterButton.show();
    }

    public void removePageFrom( int index )
    {
        TabLayout.Tab[] tabs = new TabLayout.Tab[ labels.getTabCount() - index ];
        for ( int i = 0; i < tabs.length; ++i )
            tabs[ i ] = labels.getTabAt( index + i );
        for ( int i = 0; i < tabs.length; ++i )
            labels.removeTab( tabs[ i ] );

        filterManger.removeFrom( index );
    }

    public void refresh(int position, Bundle extras)
    {
        Fragment item = filterManger.getStaticItem(position);
        if ( item instanceof ListFilterManager.Reloadable)
        {
            ListFilterManager.Reloadable reloadable = (ListFilterManager.Reloadable) item;
            reloadable.reload( extras );
        }
    }

    public void prepareChip()
    {
        int currentView = viewer.getCurrentItem();
        filterEntries.removeViews( currentView, filterEntries.getChildCount() - currentView );
    }

    public void addFilter(Fragment fragment)
    {
        filterManger.add( fragment );
    }

}
