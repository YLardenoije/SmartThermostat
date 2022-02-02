package com.example.thermostatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MyTag";

    final Handler handler = new Handler();
    final int delay = 10*1000; // 1000 milliseconds == 1 second
    protected String DevId = "";
    protected String TemperatureURL;
    protected Runnable runnable;
    protected StringRequest CurrentTempRequest, CurrentDesiredTempRequest, NewDesiredTempRequest;
    protected RequestQueue queue;
    protected TextView CurrentTempVal, CurrentDesiredTempVal;
    protected boolean IsRunning = false;

    @Override
    protected void onStop()
    {
        super.onStop();

        if (IsRunning)
        {
            handler.removeCallbacks(runnable);
        }
        if (queue != null) {
            queue.cancelAll(TAG);
        }
        IsRunning = false;
        TemperatureURL = getString( R.string.TemperatureURL );
    }
  //  @Override
//    protected void onResume()
//    {
//        super.onResume();
//        if (IsRunning)
//        {
//            handler.postDelayed(runnable = new Runnable() {
//                public void run() {
//                    queue.add(CurrentTempRequest);
//                    queue.add(CurrentDesiredTempRequest);
//                    handler.postDelayed(runnable, delay);
//                }
//            }, delay);
//
//        }
//    }
//    @Override
//    protected void onPause()
//    {
//        super.onPause();
////        if (IsRunning)
////        {
////            handler.removeCallbacks(runnable);
////        }
//        if (queue != null) {
//            queue.cancelAll(TAG);
//        }
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TemperatureURL = getString( R.string.TemperatureURL ) ;
        queue = Volley.newRequestQueue(this);
        CurrentTempVal = (TextView) findViewById(R.id.VAL_CurrentTemp);
        CurrentDesiredTempVal = (TextView) findViewById(R.id.VAL_DesiredTemp);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.INTERNET },
                    1);
        }

        Button StartButton = (Button) findViewById(R.id.BTN_Start);
        StartButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (IsRunning)
                {
                    return;
                }
                IsRunning = true;
                DevId =  ((TextInputEditText) findViewById(R.id.VAL_DeviceID)).getText().toString();
                CurrentDesiredTempVal.setText("connecting");
                CurrentTempVal.setText("Connecting");
                TemperatureURL+=DevId+"/";
                StringRequest CurrentTempRequest = new StringRequest(Request.Method.GET, TemperatureURL+"CURRENT",
                        response -> {
                            CurrentTempVal.setText(response);

                            // Display the first 500 characters of the response string.

                        }, error -> CurrentTempVal.setText(error.toString())) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put(getString(R.string.SecretHeaderName),getString(R.string.Secret));
                        return params;
                    }
                };
                StringRequest CurrentDesiredTempRequest = new StringRequest(Request.Method.GET, TemperatureURL+"DESIRED",
                        response -> {
                            CurrentDesiredTempVal.setText(response);
                        }, error -> CurrentDesiredTempVal.setText(error.toString())) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put(getString(R.string.SecretHeaderName),getString(R.string.Secret));
                        return params;
                    }
                };
                CurrentDesiredTempRequest.setTag(TAG);
                CurrentTempRequest.setTag(TAG);

                handler.postDelayed( runnable = new Runnable() {
                    public void run() {
                        queue.add(CurrentTempRequest);
                        queue.add(CurrentDesiredTempRequest);
                       //TODO: GET current temp and get current desired temp
                        handler.postDelayed(runnable, delay);
                    }
                }, 500);

            }
        });
        Button BTN_NewDesiredTemp = findViewById((R.id.BTN_ConfirmNewDesiredValue));
        BTN_NewDesiredTemp.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!IsRunning)
                {
                    return;
                }
                NewDesiredTempRequest  = new StringRequest(Request.Method.POST, TemperatureURL+"DESIRED",
                        response -> {
                           System.out.println(response);
                        }, error -> System.out.println(error.toString())) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put(getString(R.string.SecretHeaderName),getString(R.string.Secret));
                        return params;
                    }
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String value = ((EditText)findViewById(R.id.VAL_NEWDesired)).getText().toString();
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("Temp", value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            return jsonBody.toString() == null ? null : jsonBody.toString().getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            System.out.println(uee.toString());
                            return null;
                        }
                    }
                };

                queue.add(NewDesiredTempRequest);

            }
        });

    }
}