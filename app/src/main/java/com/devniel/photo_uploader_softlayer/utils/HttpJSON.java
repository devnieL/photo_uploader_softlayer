package com.devniel.photo_uploader_softlayer.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.devniel.photo_uploader_softlayer.listeners.HttpJSONResponseListener;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class HttpJSON extends AsyncTask<Object, Void, Object>{
	
	static final String TAG = HttpJSON.class.getSimpleName();
	
	public String result;
	public HttpJSONResponseListener responseListener;
	public LinearLayout progressBar;

	public String url;
	public String method;
	private ArrayList <NameValuePair> headers;
    private ArrayList<NameValuePair> postParams;
    
    private String authToken;
    
    public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public HttpJSON(String url) {
        this.url = url;
        this.headers = new ArrayList<NameValuePair>();
        this.postParams = new ArrayList<NameValuePair>();
    }
    
    public HttpJSON() {
        this.headers = new ArrayList<NameValuePair>();
        this.postParams = new ArrayList<NameValuePair>();
    }
	
	public void setHeadersParameters(HttpUriRequest request)
    {
        for(NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }
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
		// TODO Auto-generated method stub
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
			JSONObject data = new JSONObject();
			
			try {
				status = response.getInt("status");
				data = response.getJSONObject("data");
			} catch (JSONException e) {
				e.printStackTrace();
				status = 400;
				data = new JSONObject();
			} catch (NullPointerException e) {
				e.printStackTrace();
				status = 400;
				data = new JSONObject();
			}
			
			if(responseListener != null)
				responseListener.onResponse(data,status);
		}else{
			
			Exception response = (Exception) res;
			
			responseListener.onError(response);
		}
	}
	
	public Object request()  {
		
		HttpURLConnection connection = null;
		URL to_url = null;
		
		try {
			to_url = new URL(this.getUrl());
			connection = (HttpURLConnection) to_url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			
			Log.d(TAG, "URL : " + this.getUrl());
			
			if (this.getMethod().equals("POST")){
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
			}else{
				connection.setRequestMethod("GET");
			}
						
			if(this.getAuthToken() != null)
				connection.setRequestProperty("Authorization","Token " + this.getAuthToken());
	        
	        // Data
	        if(this.getMethod().equals("POST") && this.getData() != null){	        	
				byte[] outputBytes = this.getData().toString().getBytes("UTF-8");
				OutputStream os = connection.getOutputStream();
				os.write(outputBytes);
				os.flush();
				os.close();
	        }else if(this.getMethod().equals("POST") && this.getData() == null) {
	        	OutputStream os = connection.getOutputStream();
				os.write(null);
				os.flush();
				os.close();
	        }
						
			String result = null;

			// Response
			try {
				result = convertStreamToString(connection.getInputStream());
				Log.d(TAG, "RESULT : " + result);
			}catch(IOException exp){
				// Error Response
				int statusCode = connection.getResponseCode();
				String message = connection.getResponseMessage();
				result = convertStreamToString(connection.getErrorStream());
				exp.printStackTrace();
			}
												
			JSONObject response = new JSONObject();
			response.put("status", connection.getResponseCode());
			
			if(result.length() == 0){
				response.put("data", new JSONObject("{}"));
			}else{
				response.put("data", new JSONObject(result));
			}
			
			return response;
		
		} catch (ConnectException e){
			Log.d(TAG,"CONNECT EXCEPTION");
			e.printStackTrace();
			return e;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e;
        } catch (JSONException e){
        	e.printStackTrace();
        	return e;
        } catch (SocketTimeoutException e){
        	e.printStackTrace();
        	Log.d(TAG, " SOCKET TIMEOUT EXCEPTION ");
        	return e;
        } catch (IOException e){
        	e.printStackTrace();
        	return e;
        }
	}
	
	
	
	/*
	 * Convert Stream to String
	 */
	
	private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        int c = 0;
        
	    try {
	    	//if(reader.ready()){
		        while ((c = reader.read()) != -1) {
		            sb.append((char)c);
		        }
		       		        
		        reader.close();
	    	//}
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

        return sb.toString();
    }
	
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

	public void setMethod(String method) {	
		if(!method.toUpperCase().equals("POST") && !method.toUpperCase().equals("GET")){
			throw new InvalidParameterException("El m�todo " + method + " es inv�lido.");
		}
		
		this.method = method;
	}

	public HttpJSONResponseListener getResponseListener() {
		return responseListener;
	}

	public void setResponseListener(HttpJSONResponseListener responseListener) {
		this.responseListener = responseListener;
	}	
}