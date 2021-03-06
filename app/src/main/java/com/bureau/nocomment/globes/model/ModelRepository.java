package com.bureau.nocomment.globes.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.common.PictoCache;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.io.InputStream;

public class ModelRepository {
    private static final ModelRepository mInstance = new ModelRepository();
    private ItemLibrary mItemLibrary;
    private PictoCache mPictoCache;

    public static ModelRepository getInstance() {
        return mInstance;
    }

    private ModelRepository() {
        mPictoCache = new PictoCache();
    }

    public ItemLibrary getItemLibrary() {
        return mItemLibrary;
    }

    public void loadProjectPictograms(Context context) {
        mPictoCache.loadProjects(mItemLibrary.getProjects(), context);
    }

    public Bitmap pictogramForProject(Project p) {
        return mPictoCache.bitmapForProject(p);
    }

    public void loadItemLibrary(Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JsonFactory jsonFactory = new JsonFactory();

        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.item_library);
            JsonParser jsonParser = jsonFactory.createParser(inputStream);
            mItemLibrary = objectMapper.readValue(jsonParser, ItemLibrary.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
