package com.postit.classified.postit.dialog;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.postit.classified.postit.service.PostItService;

public class CountryFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        return postit.getCountries();
    }

    @Override
    protected Bundle processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("country_name").getAsString();
        int stateCount = element.getAsJsonPrimitive("state_count").getAsInt();

        manager.addProperty( id, name );

        Bundle extras = new Bundle();
        extras.putInt( "country_id", id );
        extras.putInt( "state_count", stateCount );
        extras.putBoolean( "has_next", stateCount > 0 );
        return extras;
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("country_name").getAsString();
    }

    @Override
    public String getName() {
        return "Country";
    }
}
