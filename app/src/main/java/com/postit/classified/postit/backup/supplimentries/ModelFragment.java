package com.postit.classified.postit.backup.supplimentries;

import com.google.gson.JsonObject;
import com.postit.classified.postit.service.PostItService;

public class ModelFragment extends ListFragment
{

    @Override
    protected PostItService.PostItResponse getElements() {
        int brandId = getArguments().getInt("brand_id");
        return postit.getModels( brandId );
    }

    @Override
    protected void processElements(JsonObject element) {
        int id = element.getAsJsonPrimitive("id").getAsInt();
        String name = element.getAsJsonPrimitive("model_name").getAsString();
        filters.setModel( id, name );
        filters.filter();
    }

    @Override
    protected String getElementName(JsonObject element) {
        return element.getAsJsonPrimitive("model_name").getAsString();
    }
}
