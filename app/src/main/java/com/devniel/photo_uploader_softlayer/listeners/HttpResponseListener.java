package com.devniel.photo_uploader_softlayer.listeners;

public interface HttpResponseListener {
	public void onResponse(String result, Integer status);
    public void onError(Exception error);
}
