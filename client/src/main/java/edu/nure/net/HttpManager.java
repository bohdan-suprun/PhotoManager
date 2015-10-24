package edu.nure.net;

import edu.nure.db.entity.Image;
import edu.nure.db.entity.Transmittable;
import edu.nure.listener.Action;
import edu.nure.listener.ResponseListener;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.entity.NByteArrayEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bod on 01.10.15.
 */
public class HttpManager {
    private final int THREAD_COUNT = 8;
    public static final String HOST = "https://93.76.213.31";
    private final String PATH = "/admin/";
    private HttpAsyncWorker worker;
    private HttpClient uploader, fileUploader;
    private XMLParser[] parsers;
    private static HttpManager self;

    private HttpManager() {
        BasicCookieStore store = new BasicCookieStore();
        worker = new HttpAsyncWorker(store);
        uploader = new HttpClient(store);
        fileUploader = new HttpClient(store);
        worker.start();
        uploader.start();
        fileUploader.start();
        parsers = new XMLParser[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            parsers[i] = new XMLParser();
            parsers[i].start();
        }
    }

    public  static HttpManager getManager(){
        return (self == null)? self = new HttpManager(): self;
    }

    private String escapeUrl(String uri){
        try {
            String[] params = uri.split("&[A-z]*=");
            params = Arrays.copyOfRange(params, 1, params.length);
            for(String value: params){
                String encValue = URLEncoder.encode(value, "utf-8");
                uri = uri.replace("="+value, "="+encValue);
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unsupported encoding " + e.getMessage());
        }
        return uri;
    }

    public void sendGet(String uri, Priority pr){
        sendGet(uri, pr, null);
    }

    public void sendGet(String uri, Priority pri, ResponseListener performer){
        uri = escapeUrl(HOST+PATH+uri);
        HttpGet httpGet = new HttpGet(uri);
        SimpleRequest request = new SimpleRequest(httpGet, pri, performer);
        worker.put(request);
    }

    public void sendGet(String url, String uri, Priority pri, ResponseListener performer){
        uri = escapeUrl(url+uri);
        HttpGet httpGet = new HttpGet(uri);
        SimpleRequest request = new SimpleRequest(httpGet, pri, performer);
        worker.put(request);
    }

    public void sendGet(Transmittable entity, int actionId, Priority pr, ResponseListener performer){
        sendGet(HOST,PATH+"?action="+actionId+"&"+entity.toQuery(), pr, performer);
    }

    public void getImage(Transmittable entity, ResponseListener performer){
        getImage(entity.toQuery(), performer);
    }

    public void getImage(String uri, ResponseListener performer){
        uri = escapeUrl("/image/?action="+ Action.GET_IMAGE+"&"+uri);
        sendGet(HOST,uri,new Priority(4), performer);
    }

    public void sendGet(Transmittable entity, int actionId, Priority pr){
        sendGet(HOST,PATH+"?action="+actionId+"&"+entity.toQuery(), pr, null);
    }

    public void sendGet(String uri) {
        sendGet(uri, new Priority());
    }

    public void sendGet(Transmittable entity, int actionId){
        sendGet(HOST+PATH+"?action="+actionId+entity.toQuery());
    }

    public void login(String phone, String pass, ResponseListener listener){
        String uri = HOST+PATH;
        HttpPost req = new HttpPost(uri);
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("phone", phone));
        formParams.add(new BasicNameValuePair("password", pass));
        formParams.add(new BasicNameValuePair("action", String.valueOf(1)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        req.setEntity(entity);
        SimpleRequest request = new SimpleRequest(req, new Priority(), listener);
        worker.put(request);
    }

    public void sendFile(Image imageDesc, ResponseListener l){
        HttpPost httpPost = new HttpPost(HOST + "/image/?action");
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", new ByteArrayInputStream(imageDesc.getImage()),
                        ContentType.create("image/jpeg"), "image.jpg")
                .addTextBody("album", String.valueOf(imageDesc.getAlbum()))
                .addTextBody("action", String.valueOf(Action.INSERT_IMAGE))
                .addTextBody("hash", String.valueOf(imageDesc.getHash()))
                .setBoundary("------gc0p4Jq0M2Yt08jU534c0p")
                .build();
        try {
            ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
            httpEntity.writeTo(baoStream);
            ContentType ct = ContentType.create("multipart/form-data; boundary=------gc0p4Jq0M2Yt08jU534c0p");
            HttpEntity nByteEntity = new NByteArrayEntity(baoStream.toByteArray(), ct);
            httpPost.setEntity(nByteEntity);
            SimpleRequest request = new SimpleRequest(httpPost, new Priority(Priority.MIN), l);
            fileUploader.put(request);
        } catch (IOException ex){
            ex.printStackTrace();
        }


    }

    public void loadPreview(int imId, int albumId,ResponseListener li){
        String uri = "?action="+Action.GET_IMAGE+"&id="+imId+"&albumId="+albumId+"&preview=";
        String url = escapeUrl(HOST+"/image/"+uri);
        HttpGet httpGet = new HttpGet(url);
        SimpleRequest request = new SimpleRequest(httpGet, new Priority(Priority.MIN), li);
        uploader.put(request);
    }

    public void addResponseListener(ResponseListener listener){
        XMLParser.addListener(listener);
    }

    public String getSessionId(){
        for(Cookie c: worker.getCookieStore().getCookies()){
            if(c.getName().equals("JSESSIONID"))
                return c.getValue();
        }
        return null;
    }
}
