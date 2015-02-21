package com.devniel.photo_uploader_softlayer.listeners;
import org.json.JSONObject;

public interface HttpJSONResponseListener {
    public void onResponse(JSONObject result, Integer status);
    public void onError(Exception error);
}