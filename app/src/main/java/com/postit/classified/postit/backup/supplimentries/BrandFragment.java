package com.postit.classified.postit.backup.supplimentries;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonObject;
import com.postit.classified.postit.helpers.Helper;
import com.postit.classified.postit.service.PostItService;

public class BrandFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        int categoryId = getArguments().getInt("category_id");
        Log.d( "Add_Category", "category id " + categoryId );
        return postit.getBrands( categoryId );
    }

    @Override
    protected void processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("brand_name").getAsString();

        filters.setBrand( id, name );

        Helper.Worker.executeTask( () -> {
            PostItService.PostItResponse postItResponse = postit.hasModels(id);
            return () -> {
                postItResponse.respondToBoolean( aBoolean -> {
                    if ( aBoolean )
                    {
                        Bundle extras = new Bundle();
                        extras.putInt( "brand_id", id);
                        filters.nextFilter( extras );
                    }
                    else
                        filters.filter();
                });
            };
        });
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("brand_name").getAsString();
    }
}
