package edu.stonybrook.aadarshcs.gpsplanner;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
//import com.amazonaws.mobileconnectors.cognito.Dataset;
//import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
//import com.amazonaws.auth.CognitoCachingCredentialsProvider;
//import com.amazonaws.services.s3.*;
//import com.amazonaws.mobileconnectors.s3.transferutility.*;
//import com.amazonaws.regions.Regions;

/**
 * Created by ABHINAV on 10/28/2017.
 */

public class GPSTracker extends Service implements LocationListener {

    BufferedWriter writer;
    private final String delimiter = "@";

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean isGPSTrackingEnabled = false;
    static boolean isLoggingStarted = false;
    static boolean shouldContinue = false;
    static String locationType = "Indoor";
    static String activityType = "None";
    static String modeType = "None";

    Location location;
    double latitude;
    double longitude;

    int geocoderMaxResults = 1;
    protected LocationManager locationManager;
    public String provider_info;

    File root;
    File myDir;
    String fileName;
    File file;

    Thread t;

    public GPSTracker() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;

//        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d("abhinav", "Inside onStartCommand");
        isLoggingStarted = true;
        shouldContinue = true;

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (shouldContinue) {
                    try {
                        getLocation();
                        Log.d("abhinav", "Logging location");
                        writer.write(getLatitude() + delimiter);
                        writer.write(getLongitude() + delimiter);
                        writer.write(getCountryName(getApplicationContext()) + delimiter);
                        writer.write(getLocality(getApplicationContext()) + delimiter);
                        writer.write(getPostalCode(getApplicationContext()) + delimiter);
                        writer.write(getAddressLine(getApplicationContext()) + delimiter);
                        writer.write(locationType + delimiter);
                        writer.write(activityType + delimiter);
                        writer.write(modeType + delimiter);
                        writer.write(System.currentTimeMillis() + "");
                        writer.write("\n");
                    } catch (Exception e) {
                        Log.d("abhinav", "IOException");
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(Utils.LOGGING_FREQUENCY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t.start();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("abhinav", "onCreate");
        root = Environment.getExternalStorageDirectory();
        myDir = new File(root + "/GPS Planner");
        myDir.mkdirs();
        fileName = "log.txt";
        file = new File(myDir, fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);

        try {
            writer = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            Log.d("Abhinav", "Exception");
            e.printStackTrace();
        }

        //Code for AWS server support

        // Initialize the Amazon Cognito credentials provider
//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                getApplicationContext(),
//                "us-east-2:5e383df3-8c61-4847-87c3-463248cf6b64", // Identity pool ID
//                Regions.US_EAST_2 // Region
   //     );

        // Initialize the Cognito Sync client
  /*      CognitoSyncManager syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_2, // Region
                credentialsProvider);
*/
        // Create a record in a dataset and synchronize with the server
        /*Dataset dataset = syncClient.openOrCreateDataset("myDataset");
        dataset.put("myKey", "myValue");
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List newRecords) {
                //Your handler code here
            }
        });*/



        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.gps)
                .setContentTitle("GPS Planner")
                .setContentText("Logging location")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }

    @Override
    public void onDestroy() {
        Log.d("abhinav", "Inside onDestroy");
        super.onDestroy();
        stopUsingGPS();
        isLoggingStarted = false;
        shouldContinue = false;
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Try to get location if you GPS Service is enabled
            if (isGPSEnabled) {
                this.isGPSTrackingEnabled = true;

//                Log.d("abhinav", "Application use GPS Service");

                provider_info = LocationManager.GPS_PROVIDER;

            } else if (isNetworkEnabled) {
                this.isGPSTrackingEnabled = true;

//                Log.d("abhinav", "Application use Network State to get GPS coordinates");

                provider_info = LocationManager.NETWORK_PROVIDER;
            }

            // Application can use GPS or Network Provider
//            Log.d("abhinav", provider_info);
            if (!provider_info.isEmpty()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(
                        provider_info,
                        Utils.LOGGING_FREQUENCY,
                        Utils.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this,
                        Looper.getMainLooper()
                );

                if (locationManager != null) {
//                    Log.d("abhinav", "locationManager != null");
                    location = locationManager.getLastKnownLocation(provider_info);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e("abhinav", "Impossible to connect to LocationManager", e);
        }
    }

    /**
     * GPSTracker latitude getter and setter
     * @return latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * GPSTracker longitude getter and setter
     * @return
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context) {
        if (location != null) {

            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);

            try {
                /**
                 * Geocoder.getFromLocation - Returns an array of Addresses
                 * that are known to describe the area immediately surrounding the given latitude and longitude.
                 */
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, this.geocoderMaxResults);

                return addresses;
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e("abhinav", "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }

    /**
     * Try to get AddressLine
     * @return null or addressLine
     */
    public String getAddressLine(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);

            return addressLine;
        } else {
            return null;
        }
    }

    /**
     * Try to get Locality
     * @return null or locality
     */
    public String getLocality(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String locality = address.getLocality();

            return locality;
        }
        else {
            return null;
        }
    }

    /**
     * Try to get Postal Code
     * @return null or postalCode
     */
    public String getPostalCode(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();

            return postalCode;
        } else {
            return null;
        }
    }

    /**
     * Try to get CountryName
     * @return null or postalCode
     */
    public String getCountryName(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();

            return countryName;
        } else {
            return null;
        }
    }

    /**
     * GPSTracker isGPSTrackingEnabled getter.
     * Check GPS/wifi is enabled
     */
    public boolean getIsGPSTrackingEnabled() {
        return this.isGPSTrackingEnabled;
    }

    /**
     * Stop using GPS listener
     * Calling this method will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
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
}