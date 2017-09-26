package com.np.rift.serverRequest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerGetRequest extends AsyncTask<String, Void, String> {

    private final String TAG = getClass().getSimpleName();
    private final String requestCode;
    private Response response = null;
    private String httpResultString;
    private int responseCode;
    private StringBuffer resultBuffer;

    public ServerGetRequest(Response response, String requestCode) {
        this.response = response;
        this.requestCode = requestCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "GET onPreExecute()");
        resultBuffer = new StringBuffer();
    }

    @Override
    protected String doInBackground(String... params) {
        Log.e(TAG, "GET doInBackground()");

        String stgUrl = params[0];

        Log.e(TAG, "url: " + stgUrl);
        String result;
        try {
            URL url = new URL(stgUrl);

            HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();

            urlconnection.setRequestMethod("GET");
            urlconnection.setUseCaches(false);
            urlconnection.setConnectTimeout(15000);
            urlconnection.setReadTimeout(15000);

            responseCode = urlconnection.getResponseCode();
            httpResultString = urlconnection.getResponseMessage();
            Log.e(TAG, "response code: " + responseCode);
            Log.e(TAG, "response message: " + httpResultString);

            if (responseCode != 200) {
                JSONObject object = new JSONObject();
                object.put("message", httpResultString);
                result = object.toString();
            } else {
                InputStream inputStream = urlconnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String resultString;
                while ((resultString = bufferedReader.readLine()) != null) {
                    resultBuffer.append(resultString);
                }
                result = resultBuffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return httpResultString;
        }
        Log.e(TAG, "background GET response : " + resultBuffer.toString());
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e(TAG, "GET onPostExecute()");
        Log.e(TAG, "post response: " + s);
        response.getGetResult(s, requestCode, responseCode); //link the response to the interface method
    }

    //Interface is used to Send the response back to the calling activity
    public interface Response {
        void getGetResult(String response, String requestCode, int responseCode);
    }
}
