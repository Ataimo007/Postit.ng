package com.postit.classified.postit.dialog;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.postit.classified.postit.service.PostItService;

public class BrandFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        return postit.getBrands( getArguments().getInt("category_id") );
    }

    @Override
    protected Bundle processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("brand_name").getAsString();
        int cityCount = element.getAsJsonPrimitive("model_count").getAsInt();

        manager.addProperty( id, name );

        Bundle extras = new Bundle();
        extras.putInt( "brand_id", id );
        extras.putInt( "model_count", cityCount );
        extras.putBoolean( "has_next", cityCount > 0 );
        return extras;
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("brand_name").getAsString();
    }

    @Override
    public String getName() {
        return "Brand";
    }
}
