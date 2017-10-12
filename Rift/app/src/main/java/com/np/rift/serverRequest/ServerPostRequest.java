package com.np.rift.serverRequest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class ServerPostRequest extends AsyncTask<String, Void, String> {

    private final String TAG = getClass().getSimpleName();
    private final String requestCode;
    private Response response = null;
    private StringBuffer resultBuffer;
    private int responseCode;
    private String result;

    public ServerPostRequest(Response response, String requestCode) {
        this.response = response;
        this.requestCode = requestCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        resultBuffer = new StringBuffer(); //StringBuffer is used to append all the lines from the response to the existing string buffer
    }

    @Override
    protected String doInBackground(String... params) {
        String stgUrl = params[0]; //1st parameter passed while creating new Object of ServerPostRequest class, url
        String data = params[1]; //2nd parameter passed while creating new Object of ServerPostRequest class, body parameter

        try {
            //HTTPConnection starts here
            URL url = new URL(stgUrl);
            HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();

            urlconnection.setDoInput(true);
            urlconnection.setDoOutput(true);
            urlconnection.setRequestMethod("POST");
            urlconnection.setUseCaches(false);
            urlconnection.setConnectTimeout(30000);
            urlconnection.setReadTimeout(30000);
            urlconnection.setRequestProperty("Content-Type", "application/json");
            urlconnection.setRequestProperty("Accept", "application/json");

            OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
            out.write(data);
            Log.e(TAG, "Data: " + data);
            out.close();
            responseCode = urlconnection.getResponseCode();
            String httpResultString = urlconnection.getResponseMessage();
            Log.e(TAG, "response code: " + responseCode);
            Log.e(TAG, "response code: " + httpResultString);

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
        }
        Log.e(TAG, "background Post POST: " + resultBuffer.toString());
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e(TAG, "post post response: " + s);
        response.getPostResult(s, requestCode, responseCode); //link the response to the interface method
    }

    //Interface is used to Send the response back to the calling activity
    public interface Response {
        void getPostResult(String response, String requestCode, int responseCode);
    }
}
