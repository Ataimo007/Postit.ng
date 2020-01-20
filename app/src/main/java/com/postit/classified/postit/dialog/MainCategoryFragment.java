package com.postit.classified.postit.dialog;

import android.os.Bundle;
import android.util.SparseIntArray;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.postit.classified.postit.R;
import com.postit.classified.postit.service.PostItService;

public class MainCategoryFragment extends GridIconFragment
{
    private final SparseIntArray drawables = new SparseIntArray();

    {
        drawables.put( 11, R.drawable.ic_industry );
        drawables.put( 7, R.drawable.ic_car );
        drawables.put( 10, R.drawable.ic_education );
        drawables.put( 1, R.drawable.ic_electronics );
        drawables.put( 18, R.drawable.ic_fashion );
        drawables.put( 12, R.drawable.ic_tractor );
        drawables.put( 19, R.drawable.ic_hearth );
        drawables.put( 6, R.drawable.ic_home );
        drawables.put( 115, R.drawable.ic_jobs );
        drawables.put( 46, R.drawable.ic_medicals );
        drawables.put( 20, R.drawable.ic_others );
        drawables.put( 9, R.drawable.ic_pet );
        drawables.put( 3, R.drawable.ic_property );
        drawables.put( -1, R.drawable.ic_recommended );
    }

    @Override
    protected void prepareElements(JsonArray elements) {
        super.prepareElements(elements);
        gridElements.add( 0, new GridElement( -1, "Recommended", 0, getDrawable( -1 ) ) );
    }

    @Override
    protected PostItService.PostItResponse getElements() {
        return postit.getCategories();
    }

    @Override
    protected Bundle processElements(GridElement element) {

        manager.addProperty( element.id, element.name );

        Bundle extras = new Bundle();
        extras.putInt( "category_id", element.id );
        extras.putInt( "sub_category_count", element.count );
        extras.putBoolean( "has_next", element.count > 0 );
        return extras;
    }

    @Override
    protected GridElement convert(JsonElement element) {
        int id = element.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
        int count = element.getAsJsonObject().getAsJsonPrimitive("sub_category_count").getAsInt();
        String name = element.getAsJsonObject().getAsJsonPrimitive("category_name").getAsString();
        return new GridElement( id, name, count, getDrawable( id ) );
    }

    private int getDrawable(int id) {
        return drawables.get( id, R.drawable.ic_unknown );
    }

    @Override
    protected int getSpanCount() {
        return 3;
    }

    @Override
    public String getName() {
        return "Category";
    }
}
