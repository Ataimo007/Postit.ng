package com.postit.classified.postit.service;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.MalformedJsonException;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.postit.classified.postit.PostActivity;
import com.postit.classified.postit.helpers.Helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.core.util.Consumer;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Belal on 9/9/2017.
 */
public class PostItService
{

    private static final String host = "www.postit.ng";
//    private static final String host = "192.168.43.69:8000";
//    private static final String host = "192.168.88.165:8000";

    private static final String rootUrl = String.format( "http://%s/api/", host );
    private static final int MEDIA_UPLOAD = 80;
    private static final int POST_AD = 20;
    private static PostItService service;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType JPEG = MediaType.get("image/jpeg");
    private final OkHttpClient client;
    private static final JsonParser parser = new JsonParser();
    private final Activity context;

    private PostItService( Activity context )
    {
        this.context = context;
        client = getClient();
//        initSession();
    }

    private OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel( HttpLoggingInterceptor.Level.BODY );

        builder.connectTimeout( 5, TimeUnit.MINUTES );
        builder.readTimeout( 5, TimeUnit.MINUTES );
        builder.writeTimeout( 10, TimeUnit.MINUTES );
        builder.addInterceptor( interceptor );
        return builder.build();
    }

//    private void initSession() {
//        CookieManager cookieManager = new CookieManager();
//        CookieHandler.setDefault(cookieManager);
//    }

    public static PostItService getInstance()
    {
        return service;
    }

    public static void initialize( Activity context )
    {
        service = new PostItService( context );
    }

    public static PostItService getInstance( Activity context )
    {
        if ( service == null )
            service = new PostItService( context );
        return service;
    }

    public PostItResponse login(String email )
    {
        String path = "user/login_email";
        JsonObject params = new JsonObject();
        params.addProperty( "email", email );
        return postRequest( path, params );
    }

    public PostItResponse postRequest(String path, JsonObject params )
    {
        String url = rootUrl + path;
        JsonObject result = new JsonObject();
        RequestBody body = RequestBody.create( JSON, params.toString() );
        Request request = new Request.Builder().url(url).post(body).build();
        try ( Response response = client.newCall(request).execute() )
        {
            String resp = response.body().string();
            result.addProperty( "success", true );
//            Log.d( "PostIt_Service", "response is " + resp );
            Helper.log( "PostIt_Service_Response", "response is " + resp, 1000 );
            result.add( "response", parser.parse( resp ).getAsJsonObject() );
        } catch (IOException e) {
            e.printStackTrace();
            result.addProperty( "status", false );
            result.addProperty( "response", e.getMessage() );
        }
        return new PostItResponse( result );
    }

    public PostItResponse postRequest(String path)
    {
        String url = rootUrl + path;
        JsonObject result = new JsonObject();
        RequestBody empty = RequestBody.create(new byte[]{});
        Request request = new Request.Builder().url(url).post( empty ).build();
        try ( Response response = client.newCall(request).execute() )
        {
            String resp = response.body().string();
            result.addProperty( "success", true );
//            Log.d( "PostIt_Service", "response is " + resp );
            Helper.log( "PostIt_Service_Response", "response is " + resp, 1000 );
            result.add( "response", parser.parse( resp ).getAsJsonObject() );
        } catch (IOException e) {
            e.printStackTrace();
            result.addProperty( "status", false );
            result.addProperty( "response", e.getMessage() );
        }
        return new PostItResponse( result );
    }

    public MultipartBody.Builder addFields( JsonObject params, MultipartBody.Builder builder )
    {
        Iterator<Map.Entry<String, JsonElement>> entries = params.entrySet().iterator();
        while (entries.hasNext())
        {
            Map.Entry<String, JsonElement> entry = entries.next();
            JsonElement value = entry.getValue();
            if ( value.isJsonPrimitive() )
                builder.addFormDataPart( entry.getKey(), value.getAsJsonPrimitive().getAsString() );
            if ( value.isJsonNull() )
                builder.addFormDataPart( entry.getKey(), "" );
        }
        return builder;
    }

    public MultipartBody.Builder addFiles(List<File> files, MultipartBody.Builder builder )
    {
        Iterator<File> entries = files.iterator();
        while (entries.hasNext())
        {
            File entry = entries.next();
            builder.addFormDataPart( "file_uploads[]", entry.getName(), RequestBody.create(JPEG, entry) );
        }
        return builder;
    }

    public MultipartBody.Builder addFile(File file, MultipartBody.Builder builder )
    {
        builder.addFormDataPart( "file_uploads", file.getName(), RequestBody.create(JPEG, file) );
        return builder;
    }

    public PostItResponse postRequest(String path, JsonObject params, List< File > files )
    {
        String url = rootUrl + path;
        JsonObject result = new JsonObject();
        RequestBody textBody = RequestBody.create( JSON, params.toString() );
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        addFields(params, builder);
        addFiles( files, builder );
        MultipartBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        try ( Response response = client.newCall(request).execute() )
        {
            String resp = response.body().string();
//            Log.d( "PostIt_Service", "response is " + resp );
            Helper.log( "PostIt_Service_Response", "response is " + resp, 2000 );
            result.addProperty( "success", true );
            result.add( "response", parser.parse( resp ).getAsJsonObject() );
        } catch (IOException e) {
            e.printStackTrace();
            result.addProperty( "status", false );
            result.addProperty( "response", e.getMessage() );
        }
        return new PostItResponse( result );
    }

    public PostItResponse postRequest(String path, JsonObject params, File file )
    {
        String url = rootUrl + path;
        JsonObject result = new JsonObject();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        addFields(params, builder);
        addFile( file, builder );
        MultipartBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        try ( Response response = client.newCall(request).execute() )
        {
            String resp = response.body().string();
//            Log.d( "PostIt_Service", "response is " + resp );
            Helper.log( "PostIt_Service_Response", "response is " + resp, 2000 );
            result.addProperty( "success", true );
            result.add( "response", parser.parse( resp ).getAsJsonObject() );
        } catch (IOException e) {
            e.printStackTrace();
            result.addProperty( "status", false );
            result.addProperty( "response", e.getMessage() );
        }
        return new PostItResponse( result );
    }

    public PostItResponse postRequest(String path, File file )
    {
        String url = rootUrl + path;
        JsonObject result = new JsonObject();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        addFile( file, builder );
        MultipartBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        try ( Response response = client.newCall(request).execute() )
        {
            String resp = response.body().string();
//            Log.d( "PostIt_Service", "response is " + resp );
            Helper.log( "PostIt_Service_Response", "response is " + resp, 2000 );
            result.addProperty( "success", true );
            result.add( "response", parser.parse( resp ).getAsJsonObject() );
        } catch (IOException e) {
            e.printStackTrace();
            result.addProperty( "status", false );
            result.addProperty( "response", e.getMessage() );
        }
        return new PostItResponse( result );
    }

    public PostItResponse getRequest(String path )
    {
        String url = rootUrl + path;
        JsonObject result = new JsonObject();
        Request request = new Request.Builder().url(url).build();
        try ( Response response = client.newCall(request).execute() )
        {
            String resp = response.body().string();
//            Log.d( "PostIt_Service", "response is " + resp );
            Helper.log( "PostIt_Service_Response", "response is " + resp, 2000 );
            result.addProperty( "success", true );
            result.add( "response", parser.parse( resp ).getAsJsonObject() );
        } catch (IOException e ) {
            e.printStackTrace();
            result.addProperty( "success", false );
            result.addProperty( "response", e.getMessage() );
        }
        Log.d( "PostIt_Service", "result is " + result );
        return new PostItResponse( result );
    }

    public BufferedSource getSource(String url)
    {
        JsonObject result = new JsonObject();
        Request request = new Request.Builder().url(url).build();
        try ( Response response = client.newCall(request).execute() )
        {
            return response.body().source();
        } catch (IOException e) {
            Log.d( "PostIt_Service", "source failed" );
        }
        return null;
    }

    public void setImage( String uri )
    {

    }

    public PostItResponse editAds( int id, String title, String slug, String description, int cat, int subCat, int brand, int model,
                                   int body, int transmission, String type, String condition, String mileage, int years, String price,
                                   String negotiable, String sellerName, String sellerEmail, String sellerPhone, int country,
                                   int state, int city, String address, int userId  )
    {
        JsonObject params = postAdParams(title, slug, description, cat, subCat, brand, model, body,
                transmission, type, condition, mileage, years, price, negotiable, sellerName, sellerEmail, sellerPhone,
                country, state, city, address, userId);

        String path = String.format( Locale.ENGLISH, "user/ads/edit/%d", id );
        return postRequest( path, params );
    }

    public JsonObject postAdParams(String title, String slug, String description, int cat, int subCat, int brand, int model,
                                 int body, int transmission, String type, String condition, String mileage, int years, String price,
                                 String negotiable, String sellerName, String sellerEmail, String sellerPhone, int country,
                                 int state, int city, String address, int userId )
    {
        JsonObject params = new JsonObject();

        params.addProperty("title", title );
        params.addProperty("slug", slug );
        params.addProperty("description", description );
        params.addProperty("category_id", cat );
        params.addProperty("sub_category_id", subCat );
        params.addProperty("brand_id", brand );
        params.addProperty("brand_model_id", model );
        params.addProperty("body_style_id", body );
        params.addProperty("transmission_id", transmission );
        params.addProperty("type", type );
        params.addProperty("ad_condition", condition );
        params.addProperty("mileage", mileage );
        params.addProperty("year", years );
        params.addProperty("price", price );
        params.addProperty("is_negotiable", negotiable );
        params.addProperty("seller_name", sellerName );
        params.addProperty("seller_email", sellerEmail );
        params.addProperty("seller_phone", sellerPhone );
        params.addProperty("country_id", country );
        params.addProperty("state_id", state );
        params.addProperty("city_id", city );
        params.addProperty("address", address );
        params.addProperty("user_id", userId );

        return params;
    }

    public PostItResponse postAd(String title, String slug, String description, int cat, int subCat, int brand, int model,
                                 int body, int transmission, String type, String condition, String mileage, int years, String price,
                                 String negotiable, String sellerName, String sellerEmail, String sellerPhone, int country,
                                 int state, int city, String address, int userId, Uri[] medias, PostActivity.ProgressInformer progress )
    {

        JsonObject params = postAdParams(title, slug, description, cat, subCat, brand, model, body,
                transmission, type, condition, mileage, years, price, negotiable, sellerName, sellerEmail, sellerPhone,
                country, state, city, address, userId);

        Log.d( "Post_ads", "post ad " + params );
        Log.d( "Post_ads", "medias of ad " + Arrays.toString(medias));

        return postAdProgress( params, medias, progress );

    }

    public PostItResponse updateAd(long adId, String title, String slug, String description, int cat, int subCat, int brand, int model,
                                   int body, int transmission, String type, String condition, String mileage, int years, String price,
                                   String negotiable, String sellerName, String sellerEmail, String sellerPhone, int country,
                                   int state, int city, String address, int userId, Integer[] removedIds, Uri[] medias, PostActivity.ProgressInformer progress )
    {

        JsonObject params = postAdParams(title, slug, description, cat, subCat, brand, model, body,
                transmission, type, condition, mileage, years, price, negotiable, sellerName, sellerEmail, sellerPhone,
                country, state, city, address, userId);

        Joiner joiner = Joiner.on(',');
        String removed = joiner.join(removedIds);
        params.addProperty( "deleteMedias", removed );

        Log.d( "Post_ads", "post ad " + params );
        Log.d( "Post_ads", "medias of ad " + Arrays.toString(medias));

        return updateAdProgress( adId, params, medias, progress );
    }

    public PostItResponse updateAd(long adId, String title, String slug, String description, int cat, int subCat, int brand, int model,
                                 int body, int transmission, String type, String condition, String mileage, int years, String price,
                                 String negotiable, String sellerName, String sellerEmail, String sellerPhone, int country,
                                 int state, int city, String address, int userId )
    {

        JsonObject params = postAdParams(title, slug, description, cat, subCat, brand, model, body,
                transmission, type, condition, mileage, years, price, negotiable, sellerName, sellerEmail, sellerPhone,
                country, state, city, address, userId);

        Log.d( "Post_ads", "post ad " + params );

        return updateAd( adId, params, null );

    }

    public PostItResponse postAd( JsonObject params, Uri... medias )
    {
        String path = "ads/post";

        if ( medias != null && medias.length > 0 )
        {
            ArrayList<File> files = prepareFileUploads(context, medias);
            return postRequest( path, params, files);
        }
        return postRequest( path, params );
    }

    public PostItResponse postAdProgress(JsonObject params, Uri[] medias, PostActivity.ProgressInformer progress)
    {
        String path = "ads/post";

        progress.begin();

        PostItResponse postItResponse = postRequest(path, params);
        long adId = postItResponse.getResultObject().get("ad_id").getAsLong();

        progress.setValue( POST_AD );

        if ( medias != null && medias.length > 0 )
        {
            uploadAdImage( params.get( "user_id" ).getAsInt(), adId, medias, progress );
        }

        return postItResponse;
    }

    public void uploadAdImage(int userId, long adId, Uri[] medias, PostActivity.ProgressInformer progress)
    {
        ArrayList<File> files = prepareFileUploads(context, medias);
        String uploadPath = String.format(Locale.ENGLISH, "user/ads/upload_image", adId );
        JsonObject param = new JsonObject();
        param.addProperty("user_id", userId );
        param.addProperty("ad_id", adId );
        for ( int i = 0; i < files.size(); ++i )
        {
            File file = files.get( i );
            File compressed;
            try {
                compressed = new Compressor( context ).compressToFile(file);
                PostItResponse uploaded = postRequest(uploadPath, param, compressed);
                JsonObject result = uploaded.getResultObject();
                if ( result != null && result.get("media_id").getAsInt() <= 0 )
                {
                    Log.d( "PostIt_Service_Response", String.format( Locale.ENGLISH, "Image %d didn't upload", i ) );
//                progress.showMessage(String.format( Locale.ENGLISH, "Image %d didn't upload", i ));
                }
                progress.setValue( progress.getValue() + ( MEDIA_UPLOAD / files.size() ) );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public PostItResponse updateAd( long id, JsonObject params, Uri... medias )
    {
        String path = String.format(Locale.ENGLISH, "user/ads/edit/%d", id );

        if ( medias != null && medias.length > 0 )
        {
            ArrayList<File> files = prepareFileUploads(context, medias);
            return postRequest( path, params, files);
        }
        return postRequest( path, params );
    }

    public PostItResponse updateAdProgress( long id, JsonObject params, Uri[] medias, PostActivity.ProgressInformer progress )
    {
        String path = String.format(Locale.ENGLISH, "user/ads/edit/%d", id );

        progress.begin();

        if ( medias != null && medias.length > 0 )
        {
            uploadAdImage( params.get( "user_id" ).getAsInt(), id, medias, progress );
        }
        return postRequest( path, params );
    }

    public PostItResponse refreshAds(long id )
    {
        String path = String.format( Locale.ENGLISH, "user/ads/refresh/%d", id );
        return postRequest( path );
    }

    public PostItResponse deleteAds(long id )
    {
        String path = String.format( Locale.ENGLISH, "user/ads/delete/%d", id );
        return postRequest( path );
    }

    public PostItResponse register(String firstName, String lastName, String email, String password, int countryId, String gender,
                                   String phone, Uri photoPath, Context context )
    {
        String path = "user/register";
        JsonObject params = new JsonObject();
        params.addProperty("first_name", firstName );
        params.addProperty("last_name", lastName );
        params.addProperty("email", email );
        params.addProperty("password", password );
        params.addProperty("country_id", countryId );
        params.addProperty("gender", gender );
        params.addProperty("phone", phone );

        if ( photoPath != null )
        {
            File file = prepareFileUpload(context, photoPath);
            return postRequest( path, params, file );
        }
        return postRequest( path, params );
    }

    private HashMap< String, String > prepareFileUpload(Context context, Uri... files)
    {
        HashMap< String, String > uploads = new HashMap<>();
        for ( Uri file : files )
        {
            File upload = getFile( file, context );
            uploads.put( "uploaded_file", upload.getAbsolutePath() );
        }
        return uploads;
    }

    private ArrayList< File > prepareFileUploads(Context context, Uri... files)
    {
        ArrayList< File > uploads = new ArrayList<>();
        for ( Uri file : files )
        {
            File upload = getFile( file, context );
            uploads.add( upload );
        }
        return uploads;
    }

    private File prepareFileUpload(Context context, Uri file)
    {
        HashMap< String, String > uploads = new HashMap<>();
        File upload = getFile( file, context );
        return upload;
    }

    private File getFile(Uri file, Context context)
    {
        File fileLocal = null;
        try (Cursor cursor = context.getContentResolver().query( file, null, null, null, null, null); )
        {
            if (cursor != null && cursor.moveToFirst())
            {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                try( FileOutputStream fileOutputStream = context.openFileOutput(displayName, MODE_PRIVATE);
                     InputStream inputStream = context.getContentResolver().openInputStream(file) )
                {
                    copy( inputStream, fileOutputStream, 1024 );
                    fileLocal = context.getFileStreamPath(displayName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLocal;
    }

    private void copy( InputStream source, FileOutputStream dest, int bufferSize ) throws IOException {
        bufferSize = Math.min( source.available(), bufferSize );
        byte[] buffer = new byte[ bufferSize ];
        int read = source.read( buffer, 0, bufferSize );
        while   ( read > 0 )
        {
            dest.write( buffer, 0, bufferSize );
            bufferSize = Math.min( source.available(), bufferSize );
            read = source.read( buffer, 0, bufferSize );
        }
    }

    public PostItResponse login(int id, String password )
    {
        String path = "user/login_password";
        JsonObject params = new JsonObject();
        params.addProperty( "id", id );
        params.addProperty( "password", password );
        return postRequest( path, params );
    }

    public PostItResponse getAds()
    {
        String path = "ads/brief";
        return getRequest(path);
    }

    public PostItResponse getAds(int begin, int end )
    {
        String path = String.format( "ads/brief/%d,%d",  begin, end );
        return getRequest(path);
    }

    public PostItResponse getAdsByFilter( JsonObject params )
    {
        String path = "ads/search_brief";
        PostItResponse response = postRequest( path, params );
        Log.d( "Ads_Category", "get ads by category " + response.getResult() );
        return response;
    }

    public PostItResponse getAdsByCategory( int categoryId, int begin, int end )
    {
        String path = String.format( "ads/category_brief/%d,%d,%d", categoryId, begin, end );
        PostItResponse response = getRequest(path);
        Log.d( "Ads_Category", "get ads by category " + response.getResult() );
        return response;
    }

    public PostItResponse getAdsBySubCategory( int categoryId, int begin, int end )
    {
        String path = String.format( "ads/sub_category_brief/%d,%d,%d", categoryId, begin, end );
        PostItResponse response = getRequest(path);
        Log.d( "Ads_Category", "get ads by category " + response.getResult() );
        return response;
    }

    public PostItResponse getAdsByBrand( int brandId, int begin, int end )
    {
        String path = String.format( "ads/brand_brief/%d,%d,%d", brandId, begin, end );
        return getRequest(path);
    }

    public PostItResponse getAdsByModel( int modelId, int begin, int end )
    {
        String path = String.format( "ads/model_brief/%d,%d,%d", modelId, begin, end );
        return getRequest(path);
    }

    public PostItResponse getAds(int user, int begin, int end )
    {
        String path = String.format( "user/ads/brief/%d,%d,%d", user, begin, end );
        return getRequest(path);
    }

    public PostItResponse getAd(long id )
    {
        String path = String.format( "ad/brief/%d", id );
        return getRequest(path);
    }

    public PostItResponse getStatedAds(int user, int state, int begin, int end )
    {
        String path = String.format( "user/statedAds/brief/%d,%d,%d,%d", user, state, begin, end );
        return getRequest(path);
    }

    public PostItResponse getAds(long adsId) {
        String path = String.format( "ads/full/%d", adsId );
        return getRequest(path);
    }

    public PostItResponse getAdsInfos(long adsId) {
        String path = String.format( "ads/fullInfo/%d", adsId );
        return getRequest(path);
    }

    public PostItResponse getCountries() {
        String path = "info/get_countries";
        return getRequest(path);
    }

    public PostItResponse getStates( int countryId ) {
        String path = String.format( "info/get_states/%d", countryId );
        return getRequest(path);
    }

    public PostItResponse getCities( int stateId ) {
        String path = String.format( "info/get_cities/%d", stateId );
        return getRequest(path);
    }

    public PostItResponse getCategories() {
        String path = "info/get_categories";
        return getRequest(path);
    }

    public PostItResponse getBodyStyles() {
        String path = "info/get_body_styles";
        return getRequest(path);
    }

    public PostItResponse getTransmissions() {
        String path = "info/get_transmissions";
        return getRequest(path);
    }

    public PostItResponse getSubCategories(int categoryId) {
        String path = String.format( "info/get_sub_categories/%d", categoryId );
        return getRequest(path);
    }

    public PostItResponse getSubCategory(int categoryId) {
        String path = String.format( "info/get_sub_category/%d", categoryId );
        return getRequest(path);
    }

    public PostItResponse getBrands( int categoryId) {
        String path = String.format( "info/get_brands/%d", categoryId );
        return getRequest(path);
    }

    public PostItResponse hasBrands( int categoryId) {
        String path = String.format( "info/has_brands/%d", categoryId );
        return getRequest(path);
    }

    public PostItResponse hasModels( int categoryId) {
        String path = String.format( "info/has_models/%d", categoryId );
        return getRequest(path);
    }

    public PostItResponse getModels( int brandId) {
        String path = String.format( "info/get_models/%d", brandId );
        return getRequest(path);
    }

    private File profilePicDir()
    {
        return getExternalFilesDir( "images", "profile" );
    }

    private File getExternalFilesDir( String ...names)
    {
        Joiner filenameJoiner = Joiner.on(File.pathSeparatorChar);
        String parentPath = filenameJoiner.join(names);
        File parentFile = new File( context.getExternalFilesDir( null ), parentPath );
        if ( !parentFile.exists() )
            parentFile.mkdirs();
        return parentFile;
    }

    public void saveProfilePic( String url )
    {
        File image = new File( profilePicDir(), String.valueOf( url.hashCode() ) );
        if ( !image.exists() )
            saveFile( url, image );
    }

    public Uri getProfilePic( String url )
    {
        Uri pic = null;
        File image = new File( profilePicDir(), String.valueOf( url.hashCode() ) );
        if ( !image.exists() )
            pic = Uri.fromFile( image );
        return pic;
    }

    public void saveFile( String url, File file ) {
        BufferedSource source = getSource(url);
        try ( BufferedSink sink = Okio.buffer( Okio.sink( file ) ) )
        {
            sink.writeAll( source );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class PostItResponse
    {
        private final JsonObject response;
        private final PostItService postit;

        public PostItResponse(JsonObject response) {
            this.response = response;
            postit = getInstance();
        }

        public JsonObject getResultObject()
        {
            JsonElement result = getResult();
            if ( result != null )
                return result.getAsJsonObject();
            return null;
        }

        public JsonPrimitive getResultPrimitive()
        {
            JsonElement result = getResult();
            if ( result != null )
                return result.getAsJsonPrimitive();
            return null;
        }

        public void respondToObject(Consumer<JsonObject> positive, Action negative)
        {
            JsonObject result = getResultObject();
            if ( result != null )
            {
                positive.accept( result );
            }
            negative.perform();
        }

        public void respondToObject(Consumer<JsonObject> positive)
        {
            JsonObject result = getResultObject();
            if ( result != null )
            {
                positive.accept( result );
            }
        }

        public void respondToPrimitive(Consumer<JsonPrimitive> positive, Action negative)
        {
            JsonPrimitive result = getResultPrimitive();
            if ( result != null )
            {
                positive.accept( result );
            }
            negative.perform();
        }

        public void respondToPrimitive(Consumer<JsonPrimitive> positive)
        {
            JsonPrimitive result = getResultPrimitive();
            if ( result != null )
            {
                positive.accept( result );
            }
        }

        public void respondToArray(Consumer<JsonArray> positive, Action negative)
        {
            JsonArray result = getResultArray();
            if ( result != null )
            {
                positive.accept( result );
            }
            negative.perform();
        }

        public void respondToBoolean(Consumer<Boolean> positive, Action negative)
        {
            Boolean result = getResultBoolean();
            if ( result != null )
            {
                positive.accept( result );
            }
            negative.perform();
        }

        public void respondToBoolean(Consumer<Boolean> positive)
        {
            Boolean result = getResultBoolean();
            if ( result != null )
            {
                positive.accept( result );
            }
        }

        public void respondToArray(Consumer<JsonArray> positive)
        {
            JsonArray result = getResultArray();
            if ( result != null )
            {
                positive.accept( result );
            }
        }

        public JsonElement getResult()
        {
            boolean status = response.getAsJsonPrimitive("success").getAsBoolean();
            if ( status )
            {
                JsonObject result = response.getAsJsonObject( "response" );
                if ( result.getAsJsonPrimitive("success").getAsBoolean() )
                {
                    return result.get( "result" );
                }
                else
                {
                    String msg = result.getAsJsonPrimitive("result").getAsString();
                    Helper.toast( postit.context, msg );
                }
            }
            else
            {
                String msg = "Please Check your internet connection";
                Helper.toast( postit.context, msg );
            }
            return null;
        }

        public JsonArray getResultArray()
        {
            JsonElement result = getResult();
            if ( result != null )
                return result.getAsJsonArray();
            return null;
        }

        public Boolean getResultBoolean()
        {
            JsonElement result = getResult();
            if ( result != null )
                return result.getAsBoolean();
            return null;
        }

        public static interface Action
        {
            void perform();
        }
    }
}