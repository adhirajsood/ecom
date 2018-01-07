package com.heady.headyecom;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.heady.headyecom.models.Categories;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by adhirajsood on 07/01/18.
 */

public class APIManager implements IAsyncCallback{

    private int timeout = 15000;
    private static APIManager manager;
    String baseUrl;
    private APIManager(){}


    public static APIManager getInstance(){
        if (manager == null){
            manager=new APIManager();
        }
        return manager;
    }

    public void sendAsyncCall(String requestMethod, IAsyncCallback callback){

        AsyncCallHandler handler = new AsyncCallHandler(requestMethod,callback);
        handler.execute();
    }

    @Override
    public void onSuccessResponse(String successResponse) {


    }

    @Override
    public void onErrorResponse(int errorCode, String errorResponse) {

    }


    class AsyncCallHandler extends AsyncTask<Void,Void,String> {
        private String requestMethod;
        private IAsyncCallback callback;
        private boolean initialized = false;
        private String reply = null;


        private int responseCode = 0;

        public AsyncCallHandler(String requestMethod, IAsyncCallback callback){

            this.requestMethod = requestMethod;
            this.callback = callback;
            initialized = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            if(!initialized){
                Log.e("API manager", "doInBackground : Request not initialized. Cancelling ");
                return null;
            }
            try {
                sendJSONRequest();
            } catch (Exception e) {
                Log.e("API manager", "doInBackground : Failed to process " + " " + requestMethod, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            callback.onSuccessResponse(reply);

        }


        private void sendJSONRequest() throws Exception {



            baseUrl = "https://stark-spire-93433.herokuapp.com/json";

            URL obj = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            //add request header
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod(requestMethod);

            connection.connect();

            responseCode = connection.getResponseCode();

            BufferedReader bufferedReader = null;

            InputStream errorStream = connection.getErrorStream();

            if(errorStream==null){
                InputStream inputStream = connection.getInputStream();
                bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));
            }else{
                bufferedReader = new BufferedReader(
                        new InputStreamReader(errorStream));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();

            if(response!=null){
                reply = response.toString();
            }
            return;

        }

    }
}
