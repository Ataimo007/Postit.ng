package com.postit.classified.postit.dialog;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.postit.classified.postit.service.PostItService;

public class ModelFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        return postit.getModels( getArguments().getInt("brand_id") );
    }

    @Override
    protected Bundle processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("model_name").getAsString();

        manager.addProperty( id, name );

        Bundle extras = new Bundle();
        extras.putInt( "brand_id", id );
        return extras;
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("model_name").getAsString();
    }

    @Override
    public String getName() {
        return "Model";
    }
}
