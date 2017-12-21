package edu.stonybrook.aadarshcs.gpsplanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationSource,
        LocationListener,
        GoogleMap.OnMyLocationChangeListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GPSTracker gpsTracker;
    int loggingFrequency;
    private Spinner loggingFreq;
    private Button startLogging;
    private Button IndoorSetting;
    private Spinner outdoorAct;
    private Spinner transportMode;

    private ArrayList<LatLng> points; //added
    Polyline line; //added


    ArrayAdapter<CharSequence> adapter, outdooradapter, transportadaptor;

    private LatLngBounds.Builder builder = new LatLngBounds.Builder();

    private LocationSource.OnLocationChangedListener mapLocationListener = null;
    private LocationManager locMgr = null;
    private Criteria crit = new Criteria();
    private boolean needsInit = false;
    private GoogleMap map = null;

    public GoogleApiClient mApiClient;

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = new Intent(this, GPSTracker.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        points = new ArrayList<LatLng>(); //added

        loggingFreq = (Spinner) findViewById(R.id.loggingFreq);
        outdoorAct = (Spinner) findViewById(R.id.Outdoor);
        transportMode = (Spinner) findViewById(R.id.Transport);
        startLogging = (Button) findViewById(R.id.startLogging);
        IndoorSetting = (Button) findViewById(R.id.Indoor);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.sampling_frequencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loggingFreq.setAdapter(adapter);

        outdooradapter = ArrayAdapter.createFromResource(this,
                R.array.outdoor_activities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outdoorAct.setAdapter(outdooradapter);

        transportadaptor = ArrayAdapter.createFromResource(this,
                R.array.mode_of_transport, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportMode.setAdapter(transportadaptor);

        if (gpsTracker.isLoggingStarted) {
            loggingFreq.setEnabled(false);
            startLogging.setText("Stop Logging");
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        startLogging.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (loggingFreq.isEnabled()) {
                    startLogging.setText("Stop Logging");
                    loggingFreq.setEnabled(false);
                    loggingFrequency = loggingFreq.getSelectedItemPosition() + 1;
                    Utils.LOGGING_FREQUENCY = 1000 * 60 * loggingFrequency;
                    startService(intent);
                } else {
                    stopService(intent);
                    startLogging.setText("Start Logging");
                    loggingFreq.setEnabled(true);
                }
            }
        });

        IndoorSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPSTracker.locationType = "Indoor";
                GPSTracker.activityType = "None";
            }
        });

        outdoorAct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    GPSTracker.locationType = "Outdoor";
                    GPSTracker.activityType = parent.getItemAtPosition(position).toString();
                }

            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        transportMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    //GPSTracker.locationType = "Outdoor";
                    GPSTracker.modeType = parent.getItemAtPosition(position).toString();
                }

            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        MapFragment mapFrag =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        if (savedInstanceState == null) {
            needsInit = true;
        }

        mapFrag.getMapAsync(this);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (map != null)
            map.setLocationSource(null);
        locMgr.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        String compareValue = GPSTracker.modeType;
        if (!compareValue.equals("None")) {
            int spinnerPosition = transportadaptor.getPosition(compareValue);
            transportMode.setSelection(spinnerPosition);
        }

        super.onResume();
        compareValue = GPSTracker.activityType;
        if (!compareValue.equals("None")) {
            int spinnerPosition = outdooradapter.getPosition(compareValue);
            outdoorAct.setSelection(spinnerPosition);
        }

        super.onResume();
        if (locMgr != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            locMgr.requestLocationUpdates(0L, 0.0f, crit, this, null);
        }
        if (map!=null) { map.setLocationSource(this);
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;

        addMarker(map, 40.90734989338772, -73.11037693853976,
                R.string.un, R.string.Chapin_apartments);
        addMarker(map, 40.91303763175558, -73.12215837043951,
                R.string.javits_lecture,
                R.string.javits_center);
        addMarker(map, 40.913951460529674, -73.12372004936702,
                R.string.sac_center, R.string.sac_2);
        addMarker(map, 40.70686417491799, -74.01572942733765,
                R.string.downtown_club, R.string.heisman_trophy);

        if (needsInit) {
            findViewById(android.R.id.content).post(new Runnable() {
                @Override
                public void run() {
                    CameraUpdate allTheThings =
                            CameraUpdateFactory.newLatLngBounds(builder.build(), 32);
                    map.moveCamera(allTheThings);
                }
            });
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

//        map.setMyLocationEnabled(true);
//        map.setOnMyLocationChangeListener(this);

        locMgr = (LocationManager)getSystemService(LOCATION_SERVICE);
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        locMgr.requestLocationUpdates(0L, 0.0f, crit, this, null);

        map.setLocationSource(this);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void addMarker(GoogleMap map, double lat, double lon, int title, int snippet) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .title(getString(title))
                .snippet(getString(snippet)));

        builder.include(marker.getPosition());
    }


    @Override
    public void activate(OnLocationChangedListener listener) {
        this.mapLocationListener = listener;
    }

    @Override
    public void deactivate() {
        this.mapLocationListener=null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mapLocationListener != null) {
            mapLocationListener.onLocationChanged(location);
            LatLng latlng=
                    new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cu = CameraUpdateFactory.newLatLng(latlng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);


            points.add(latlng); //added
            redrawLine(); //added

            map.moveCamera(cu);
            map.animateCamera(zoom, 500, null);
        }
    }

    private void redrawLine(){

        map.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
       // addMarker(); //add Marker in current position
        line = map.addPolyline(options); //add Polyline
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMyLocationChange(Location lastKnownLocation) {
        Log.d(getClass().getSimpleName(),
                String.format("%f:%f", lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude()));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000,
                pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
