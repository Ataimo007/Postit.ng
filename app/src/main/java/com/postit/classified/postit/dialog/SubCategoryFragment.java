package com.postit.classified.postit.dialog;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.postit.classified.postit.service.PostItService;

public class SubCategoryFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        return postit.getSubCategories( getArguments().getInt("category_id") );
    }

    @Override
    protected Bundle processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("category_name").getAsString();
        int stateCount = element.getAsJsonPrimitive("brand_count").getAsInt();

        manager.addProperty( id, name );

        Bundle extras = new Bundle();
        extras.putInt( "sub_category_id", id );
        extras.putInt( "brand_count", stateCount );
        return extras;
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("category_name").getAsString();
    }

    @Override
    public String getName() {
        return "Sub Category";
    }
}
