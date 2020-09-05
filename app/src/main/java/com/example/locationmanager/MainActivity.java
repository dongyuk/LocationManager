package com.example.locationmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private List<String> listProviders;
    private TextView tvGpsEnable, tvNetworkEnable, tvPassiveEnable, tvGpsLatitude, tvGpsLongitude, tvOutput;
    private TextView tvNetworkLatitude, tvNetworkLongitude, tvPassiveLatitude, tvPassivekLongitude;
    private EditText etAddress, etPort, etRouter, etUserId;
    private String TAG = "LocationProvider";
    private Button btnShowLocation;
    private RequestHttpURLConnection requestHttpURLConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGpsEnable = (TextView)findViewById(R.id.tvGpsEnable);
        tvNetworkEnable = (TextView)findViewById(R.id.tvNetworkEnable);
        tvPassiveEnable = (TextView)findViewById(R.id.tvPassiveEnable);
        tvNetworkLatitude = (TextView)findViewById(R.id.tvNetworkLatitude);
        tvNetworkLongitude = (TextView)findViewById(R.id.tvNetworkLongitude);

        etAddress = (EditText)findViewById(R.id.etAddress);
        etPort = (EditText)findViewById(R.id.etPort);
        etRouter = (EditText)findViewById(R.id.etRouter);
        etUserId = (EditText)findViewById(R.id.etUserId);

        btnShowLocation = (Button) findViewById(R.id.btn_start);

        tvOutput = (TextView)findViewById(R.id.tvOutput);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        requestHttpURLConnection = new RequestHttpURLConnection();

        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // URL 설정.
                String url = "http://" + etAddress.getText().toString() + ":" + etPort.getText().toString() + '/' + etRouter.getText().toString();
//                        "http://172.30.1.59:3000/infos/";

                // AsyncTask를 통해 HttpURLConnection 수행.
                NetworkTask networkTask = new NetworkTask(url, null);
                networkTask.execute();
            }
        });

    }

    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        /*
        double latitude = 0.0;
        double longitude = 0.0;

        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            tvGpsLatitude.setText(": " + Double.toString(latitude ));
            tvGpsLongitude.setText((": " + Double.toString(longitude)));
            Log.d(TAG + " GPS : ", Double.toString(latitude )+ '/' + Double.toString(longitude));
        }

        if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            tvNetworkLatitude.setText(": " + Double.toString(latitude ));
            tvNetworkLongitude.setText((": " + Double.toString(longitude)));
            Log.d(TAG + " NETWORK : ", Double.toString(latitude )+ '/' + Double.toString(longitude));
        }

        if(location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            tvPassiveLatitude.setText(": " + Double.toString(latitude ));
            tvPassivekLongitude.setText((": " + Double.toString(longitude)));
            Log.d(TAG + " PASSIVE : ", Double.toString(latitude )+ '/' + Double.toString(longitude));
        }

         */

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없을 경우 최초 권한 요청 또는 사용자에 의한 재요청 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // 권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    // http ========================================================================================
    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;
        private double latitude, longitude;
        private boolean[] isEnable;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            //권한 체크
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
                /*
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    double lng = lastKnownLocation.getLongitude();
                    double lat = lastKnownLocation.getLatitude();
                    Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
                    tvGpsLatitude.setText(":: " + Double.toString(lat ));
                    tvGpsLongitude.setText((":: " + Double.toString(lng)));
                }

                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (lastKnownLocation != null) {
                    double lng = lastKnownLocation.getLongitude();
                    double lat = lastKnownLocation.getLatitude();
                    Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
                    tvPassiveLatitude.setText(":: " + Double.toString(lat ));
                    tvPassivekLongitude.setText((":: " + Double.toString(lng)));
                }
                 */

            latitude = 0.0;
            longitude = 0.0;

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation != null) {
                longitude = lastKnownLocation.getLongitude();
                latitude = lastKnownLocation.getLatitude();

                Log.d(TAG, "longtitude=" + longitude + ", latitude=" + latitude);
//                tvNetworkLatitude.setText(":: " + Double.toString(latitude));
//                tvNetworkLongitude.setText((":: " + Double.toString(longitude)));
            }

            listProviders = locationManager.getAllProviders();
            isEnable = new boolean[3];
            for (int i = 0; i < listProviders.size(); i++) {
                if (listProviders.get(i).equals(LocationManager.GPS_PROVIDER)) {
                    isEnable[0] = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                    tvGpsEnable.setText(": " + String.valueOf(isEnable[0]));

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MainActivity.this);
                } else if (listProviders.get(i).equals(LocationManager.NETWORK_PROVIDER)) {
                    isEnable[1] = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//                    tvNetworkEnable.setText(": " + String.valueOf(isEnable[1]));

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, MainActivity.this);
                } else if (listProviders.get(i).equals(LocationManager.PASSIVE_PROVIDER)) {
                    isEnable[2] = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
//                    tvPassiveEnable.setText(": " + String.valueOf(isEnable[2]));

                    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, MainActivity.this);
                }
            }

            Log.d(TAG, listProviders.get(0) + '/' + String.valueOf(isEnable[0]));
            Log.d(TAG, listProviders.get(1) + '/' + String.valueOf(isEnable[1]));
            Log.d(TAG, listProviders.get(2) + '/' + String.valueOf(isEnable[2]));


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("user_id", etUserId.getText().toString());
                jsonObject.accumulate("latitude", latitude);
                jsonObject.accumulate("longitude", longitude);

            } catch (Exception e) {
                e.printStackTrace();
            }

            String result =""; // 요청 결과를 저장할 변수.
//            result = requestHttpURLConnection.request(url, jsonObject); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            tvGpsEnable.setText(": " + String.valueOf(isEnable[0]));
            tvNetworkEnable.setText(": " + String.valueOf(isEnable[1]));
            tvPassiveEnable.setText(": " + String.valueOf(isEnable[2]));

            tvNetworkLatitude.setText(":: " + Double.toString(latitude));
            tvNetworkLongitude.setText((":: " + Double.toString(longitude)));

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            tvOutput.setText(s);
        }
    }
    // -----========================================================================================



}