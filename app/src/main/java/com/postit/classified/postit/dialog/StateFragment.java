package com.postit.classified.postit.dialog;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.postit.classified.postit.service.PostItService;

public class StateFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        return postit.getStates( getArguments().getInt("country_id") );
    }

    @Override
    protected Bundle processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("state_name").getAsString();
        int cityCount = element.getAsJsonPrimitive("city_count").getAsInt();

        manager.addProperty( id, name );

        Bundle extras = new Bundle();
        extras.putInt( "state_id", id );
        extras.putInt( "city_count", cityCount );
        extras.putBoolean( "has_next", cityCount > 0 );
        return extras;
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("state_name").getAsString();
    }

    @Override
    public String getName() {
        return "State";
    }
}
