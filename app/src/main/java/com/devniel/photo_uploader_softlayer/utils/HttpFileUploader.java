package com.devniel.photo_uploader_softlayer.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.devniel.photo_uploader_softlayer.listeners.HttpResponseListener;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class HttpFileUploader extends AsyncTask<Object, Void, Object>{

    static final String TAG = HttpJSON.class.getSimpleName();

    public String result;
    public HttpResponseListener responseListener;
    public LinearLayout progressBar;

    public String url;
    public String method;
    private ArrayList <NameValuePair> headers;
    private ArrayList<NameValuePair> postParams;
    private int timeout = 30000;

    private String filePath;
    private String fileType;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }




    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private String authToken;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public HttpFileUploader(String url) {
        this.url = url;
        this.headers = new ArrayList<NameValuePair>();
        this.postParams = new ArrayList<NameValuePair>();
    }

    public HttpFileUploader() {
        this.headers = new ArrayList<NameValuePair>();
        this.postParams = new ArrayList<NameValuePair>();
    }

    public void addHeader(NameValuePair header)
    {
        headers.add(header);
    }

    public void addPostParam(NameValuePair param) {
        postParams.add(param);
    }

    public JSONObject getData() {
        JSONObject jsonObj = new JSONObject();

        for(NameValuePair p : postParams) {
            try {
                jsonObj.put(p.getName(), p.getValue());
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: " + e);
            }
        }
        return jsonObj;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(progressBar != null){
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
        }
    }

    @Override
    protected void onPostExecute(Object res){

        if(progressBar != null)
            progressBar.setVisibility(View.GONE);

        if(res instanceof JSONObject){

            JSONObject response = (JSONObject) res;

            Integer status = 400;
            String data = null;

            try {
                status = response.getInt("status");
            } catch (JSONException e) {
                e.printStackTrace();
                status = 400;
            } catch (NullPointerException e) {
                e.printStackTrace();
                status = 400;
            }

            if(responseListener != null)
                responseListener.onResponse(data,status);
        }else{

            Exception response = (Exception) res;

            responseListener.onError(response);
        }
    }

    public Object request()  {

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPut put = new HttpPut(this.getUrl());  //-X PUT

            for(NameValuePair header : headers)
                put.addHeader(header.getName(), header.getValue());

            put.setEntity(new FileEntity(new File(getFilePath()), "image/jpeg"));  //@ - absolute path
            HttpResponse res = httpClient.execute(put);

            JSONObject response = new JSONObject();
            response.put("status", res.getStatusLine().getStatusCode());

            return response;

        } catch (ConnectException e){
            e.printStackTrace();
            return e;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return e;
        }  catch (SocketTimeoutException e){
            e.printStackTrace();
            return e;
        } catch (IOException e){
            e.printStackTrace();
            return e;
        } catch (JSONException e) {
            e.printStackTrace();
            return e;
        }

    };

    public LinearLayout getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(LinearLayout progressBar) {
        this.progressBar = progressBar;
    }

    protected Object doInBackground(Object... params) {
        return request();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public HttpResponseListener getResponseListener() {
        return responseListener;
    }

    public void setResponseListener(HttpResponseListener responseListener) {
        this.responseListener = responseListener;
    }
}