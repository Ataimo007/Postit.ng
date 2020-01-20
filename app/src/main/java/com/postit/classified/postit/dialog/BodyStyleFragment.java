package com.postit.classified.postit.dialog;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.postit.classified.postit.service.PostItService;

public class BodyStyleFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        return postit.getBodyStyles();
    }

    @Override
    protected Bundle processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("body_style_name").getAsString();

        manager.addProperty( id, name );

        Bundle extras = new Bundle();
        extras.putInt( "body_style_id", id );
        return extras;
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("body_style_name").getAsString();
    }

    @Override
    public String getName() {
        return "Body Style";
    }
}
