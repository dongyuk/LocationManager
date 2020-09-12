package com.example.locationmanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private LocationManager locationManager;
    private List<String> listProviders;
    private TextView tvGpsEnable, tvNetworkEnable, tvPassiveEnable, tvGpsLatitude, tvGpsLongitude, tvOutput;
    private TextView tvNetworkLatitude, tvNetworkLongitude, tvPassiveLatitude, tvPassivekLongitude, tvAzimuth, tvGeoCoder, tvLongLocal, tvStrLocal, tvLongUTC, tvStrUTC;
    private EditText etAddress, etPort, etRouter, etUserId;
    private String TAG = "LocationProvider";
    private Button btnShowLocation;
    private RequestHttpURLConnection requestHttpURLConnection;
    private SensorManager sensorManager;
    private Sensor sensorAccel,sensorMag;
    private Geocoder geocoder;

    float[] rotation;
    float[] result_data;
    float[] mag_data; //센서데이터를 저장할 배열 생성
    float[] acc_data; //가속도데이터값이 들어갈 배열. 각도를 뽑으려면 가속도와 지자계의 값이 있어야함.
    float azimuth;

    private String geoStr;
    private long longLocal, longUTC;
    private String strLocal, strUTC;
    private Date date;

    private  Exception error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGpsEnable = (TextView)findViewById(R.id.tvGpsEnable);
        tvNetworkEnable = (TextView)findViewById(R.id.tvNetworkEnable);
        tvPassiveEnable = (TextView)findViewById(R.id.tvPassiveEnable);
        tvNetworkLatitude = (TextView)findViewById(R.id.tvNetworkLatitude);
        tvNetworkLongitude = (TextView)findViewById(R.id.tvNetworkLongitude);
        tvGeoCoder = (TextView)findViewById(R.id.tvGeoCoder);
        tvAzimuth = (TextView)findViewById(R.id.tvAzimuth);
        tvLongLocal = (TextView)findViewById(R.id.tvLongLocal);
        tvStrLocal = (TextView)findViewById(R.id.tvStrLocal);
        tvLongUTC = (TextView)findViewById(R.id.tvLongUTC);
        tvStrUTC = (TextView)findViewById(R.id.tvStrUTC);

        etAddress = (EditText)findViewById(R.id.etAddress);
        etPort = (EditText)findViewById(R.id.etPort);
        etRouter = (EditText)findViewById(R.id.etRouter);
        etUserId = (EditText)findViewById(R.id.etUserId);

        btnShowLocation = (Button) findViewById(R.id.btn_start);

        tvOutput = (TextView)findViewById(R.id.tvOutput);

        geocoder = new Geocoder(this, Locale.KOREA);

        rotation = new float[9];
        result_data = new float[3];
        mag_data = new float[3]; //센서데이터를 저장할 배열 생성
        acc_data = new float[3]; //가속도데이터값이 들어갈 배열. 각도를 뽑으려면 가속도와 지자계의 값이 있어야함.

        //센서값에 접근하려면 SensorManager과 SensorEventListener을 사용해야한다.
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        // 가속도 센서
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 자기장 센서
        sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

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

    private String convertAddr(double lat, double lng){
        List<Address> address;
        String getAddr = null;

        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);

                if(address != null && address.size() > 0){
                    getAddr = address.get(0).getAddressLine(0).toString();
                }
            }
//            locationText.setText(getAddr);
        }catch (IOException e){
            e.printStackTrace();
        }

        return getAddr;
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

        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        sensorManager.registerListener(this, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {  //센서가 읽어들인 값이 마그네틱필드일때
            mag_data = event.values.clone();    //데이터를 모두 mag_data 배열에 저장
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {// 가속도센서값일때
            acc_data = event.values.clone();  //마찬가지
        }

        if (mag_data != null && acc_data != null) { //널체크
            SensorManager.getRotationMatrix(rotation, null, acc_data, mag_data); //회전메트릭스 연산
            SensorManager.getOrientation(rotation, result_data); //연산값으로 방향값 산출
            result_data[0] = (float)Math.toDegrees(result_data[0]); // 방향값을 각도로 변환
            if(result_data[0] < 0) azimuth = result_data[0] + 360; //0보다 작을경우 360을더해줌
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public String getCurrentTimeStamp(long timestamp){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String currentDateTime = dateFormat.format(new Date(timestamp)); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    // http ========================================================================================
    public class NetworkTask extends AsyncTask<Void, Void, Boolean> {

        private String url;
        private ContentValues values;
        private double latitude, longitude;
        private boolean[] isEnable;
        private String result;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //권한 체크
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
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

            geoStr = convertAddr(latitude, longitude);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("user_id", etUserId.getText().toString());
                jsonObject.accumulate("latitude", latitude);
                jsonObject.accumulate("longitude", longitude);
                jsonObject.accumulate("azimuth", azimuth);
                jsonObject.accumulate("addr", geoStr);

                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
                Calendar localCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
                longLocal = localCalendar.getTimeInMillis();
                strLocal = getCurrentTimeStamp(longLocal);

                TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                Calendar UTCcalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                longUTC = UTCcalendar.getTimeInMillis();
                strUTC = getCurrentTimeStamp(longUTC);

                jsonObject.accumulate("timestamps", longUTC);

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                result = requestHttpURLConnection.request(url, jsonObject); // 해당 URL로 부터 결과물을 얻어온다.
            } catch(Exception e) {
                error = e;
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean Result) {
//            super.onPostExecute(s);

            tvGpsEnable.setText(": " + String.valueOf(isEnable[0]));
            tvNetworkEnable.setText(": " + String.valueOf(isEnable[1]));
            tvPassiveEnable.setText(": " + String.valueOf(isEnable[2]));

            tvNetworkLatitude.setText(":: " + Double.toString(latitude));
            tvNetworkLongitude.setText((":: " + Double.toString(longitude)));
            tvGeoCoder.setText(": " + geoStr);
            tvAzimuth.setText((":: " + Float.toString(azimuth)));
            tvLongLocal.setText(": " + Long.toString(longLocal));
            tvStrLocal.setText(": " + strLocal);
            tvLongUTC.setText(": " + Long.toString(longUTC));
            tvStrUTC.setText(": " + strUTC);

            if (Result) {
                //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
                tvOutput.setText(result);
            } else {
                tvOutput.setText("conn error");
            }
        }
    }
    // -----========================================================================================



}