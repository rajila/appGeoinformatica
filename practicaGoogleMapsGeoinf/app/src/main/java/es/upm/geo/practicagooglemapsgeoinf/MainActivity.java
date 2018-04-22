package es.upm.geo.practicagooglemapsgeoinf;

        import android.content.Context;
        import android.content.Intent;
        import android.location.Location;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.webkit.WebView;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;

        import com.google.android.gms.maps.model.LatLng;

        import android.annotation.SuppressLint;
        import android.content.IntentSender;
        import android.content.pm.PackageManager;

        import android.os.Looper;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.view.View;

        import android.location.Address;
        import android.location.Geocoder;

        import android.util.Log;
        import android.widget.Toast;

        import com.google.android.gms.common.api.ApiException;
        import com.google.android.gms.common.api.ResolvableApiException;
        import com.google.android.gms.location.FusedLocationProviderClient;
        import com.google.android.gms.location.LocationCallback;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationResult;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.location.LocationSettingsRequest;
        import com.google.android.gms.location.LocationSettingsResponse;
        import com.google.android.gms.location.LocationSettingsStatusCodes;
        import com.google.android.gms.location.SettingsClient;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.gms.tasks.Task;

        import android.support.design.widget.Snackbar;

        import java.io.IOException;
        import java.util.List;
        import java.util.Locale;

        import android.Manifest;

        import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    // UI Widgets.
    private Button _btnCalcularDistancia;
    private Button _btnVerMapa;

    private TextView _txtValueOrigen;
    private EditText _txtValueDestino;
    private TextView _txtValueDistancia;
    private TextView _txtValueConsultaD;

    private String _nameOrigen = "";

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btnCalcularDistancia = (Button) findViewById(R.id.btnCalcularDistancia);
        _btnVerMapa = (Button) findViewById(R.id.btnVerMapa);
        _txtValueOrigen = (TextView) findViewById(R.id.txtValueOrigen);
        _txtValueDestino = (EditText) findViewById(R.id.txtValueDestino);
        _txtValueDistancia = (TextView) findViewById(R.id.txtValueDistancia);
        _txtValueConsultaD = (TextView) findViewById(R.id.txtValueConsultaD);
        mRequestingLocationUpdates = false;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if( checkPermissions() ){
            startLocationUpdates();
            //mapFragment.getMapAsync(this);
        } else if (!checkPermissions()) {
            Log.i(TAG, "R -> 0000222");
            requestPermissionsCustom();
        }

        Log.i(TAG, "R -> GENERALLLLLL TODOS");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void requestPermissionsCustom() {
        // Verifica si el usuario ha denegado el acceso a la localización
        // True --> Da denegado el acceso a la localización
        // False --> No ha contestado
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        Log.i(TAG, "RESPUESTA USUARIO -> "+shouldProvideRationale);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "F0001 -> Mensaje: El usuario no ha permitido el acceso a la localización.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "F0002 -> Mensaje: Pregunta al usuario para el uso de localización");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                if( mCurrentLocation != null ){
                    Log.i(TAG, "L0002 -> Mensaje: Longitud: "+mCurrentLocation.getLongitude());
                    actualizarUbiciacionLocal();
                    stopLocationUpdates();
                }
                //updateLocationUI();
                // Se agrego una nueva linea de codigo para el log
                //getAddressFromLocation(mCurrentLocation,getApplicationContext());
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.i(TAG, "INGRESSOOOOOOOOOOO.");
                            return;
                        }
                        mRequestingLocationUpdates = true;
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                        //updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        //updateUI();
                    }
                });
    }

    private void actualizarUbiciacionLocal()
    {
        _txtValueOrigen.setText(getAddressFromLocation(mCurrentLocation,getApplicationContext()));
    }

    public String getAddressFromLocation(final Location location, final Context context)
    {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String result = null;
        try {
            List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                // recuperamos la primera línea de la dirección y el lugar
                result = address.getAddressLine(0);
                _nameOrigen = result;
            }
        } catch (IOException e) {
            Log.e("myTag", "La conexión con el Geocoder no tuvo éxito", e);
        } finally {
            if (result != null) Log.i("Localización Actual", result);
            else Log.e("myTag", "No hay resultado");
        }
        return result;
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    public void calcularDistanciaButtonHandler(View view)
    {
        LatLng seattle;
        //seattle coordinates
        if(checkPermissions()){
            if(!verificarInternet()) return;
            if( mCurrentLocation != null ) seattle = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            else seattle = new LatLng(47.6062095, -122.3320708);
            Geocoder geo = new Geocoder(MainActivity.this);
            int maxResultados = 1;
            List<Address> adress = null;
            try {
                adress = geo.getFromLocationName(_txtValueDestino.getText().toString(), maxResultados);
                if(adress.isEmpty()) {
                    _txtValueDistancia.setText("0.00 Km");
                    showSnackbar(R.string.error_consulta_destino,
                            android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {}
                            });
                    return;
                }
                Log.i(TAG, "Adresss -> "+ adress.toString());
                LatLng latLng = new LatLng(adress.get(0).getLatitude(), adress.get(0).getLongitude());

                Location location = new Location("localizacion 1");
                location.setLatitude(seattle.latitude);  //latitud
                location.setLongitude(seattle.longitude); //longitud
                Location location2 = new Location("localizacion 2");
                location2.setLatitude(latLng.latitude);  //latitud
                location2.setLongitude(latLng.longitude); //longitud
                double distance = location.distanceTo(location2);
                Log.i(TAG, "Distancia --> " + (distance/1000.00) + " m");
                _txtValueDistancia.setText((distance/1000.00)+" Km");
                _txtValueConsultaD.setText(adress.get(0).getAddressLine(0));
                //stopLocationUpdates();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void verMapaButtonHandler(View view)
    {
        LatLng _origen;
        //seattle coordinates
        if(checkPermissions()){
            if(!verificarInternet()) return;
            if( mCurrentLocation != null ) _origen = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            else _origen = new LatLng(47.6062095, -122.3320708);
            Geocoder geo = new Geocoder(MainActivity.this);
            int maxResultados = 1;
            List<Address> adress = null;
            try {
                adress = geo.getFromLocationName(_txtValueDestino.getText().toString(), maxResultados);
                if(adress.isEmpty()) {
                    _txtValueDistancia.setText("0.00 Km");
                    showSnackbar(R.string.error_consulta_destino,
                            android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {}
                            });
                    return;
                }
                LatLng _destino = new LatLng(adress.get(0).getLatitude(), adress.get(0).getLongitude());

                Location location = new Location("Origen");
                location.setLatitude(_origen.latitude);  //latitud
                location.setLongitude(_origen.longitude); //longitud
                Location location2 = new Location("Destino");
                location2.setLatitude(_destino.latitude);  //latitud
                location2.setLongitude(_destino.longitude); //longitud
                double _distancia = location.distanceTo(location2);
                Log.i(TAG, "Distancia --> " + (_distancia/1000.00) + " Km");
                _txtValueDistancia.setText((_distancia/1000.00)+" Km");
                _txtValueConsultaD.setText(adress.get(0).getAddressLine(0));
                stopLocationUpdates();

                Intent intent = new Intent(view.getContext(),MainActivityMapa.class);
                //Parametros que se le pasan a la actividad en donde se mostrara el detalle del Sensor
                intent.putExtra("direccionOrigen",_nameOrigen);
                intent.putExtra("latitudOrigen",_origen.latitude);
                intent.putExtra("longitudOrigen",_origen.longitude);
                intent.putExtra("direccionDestino",adress.get(0).getAddressLine(0));
                intent.putExtra("latitudDestino",_destino.latitude);
                intent.putExtra("longitudDestino",_destino.longitude);
                intent.putExtra("distancia",(_distancia/1000.00)+" Km");
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public Boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public boolean verificarInternet()
    {
        boolean _r = true;
        if(!(isNetDisponible() && isOnlineNet()))
        {
            _r = false;
            showSnackbar(R.string.error_internet,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {}
                    });
        }
        return _r;
    }
}