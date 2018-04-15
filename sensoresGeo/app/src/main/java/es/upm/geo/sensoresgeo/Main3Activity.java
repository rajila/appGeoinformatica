package es.upm.geo.sensoresgeo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Main3Activity extends AppCompatActivity implements SensorEventListener {

    private TextView txtNombre;
    private TextView txtTipo;
    private TextView txtVendor;
    private TextView txtResolucion;
    private TextView txtPoder;
    private TextView txtVersion;
    private TextView txtMedicionSensor;

    private SensorManager _mSensorManager;
    private Sensor _dataSensor;

    String tipo = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        //Obtenemos los paramatros del sensor que son enviados desde la actividad principal
        Bundle bundle = getIntent().getExtras();
        String id = bundle.get("ID").toString();
        String nombre = bundle.get("nombre").toString();
        tipo = bundle.get("tipo").toString();
        String vendor = bundle.get("vendor").toString();
        String resolucion = bundle.get("resolucion").toString();
        String poder = bundle.get("poder").toString();
        String version = bundle.get("version").toString();


        txtNombre = (TextView) findViewById(R.id.txtNombre);
        txtNombre.setText(nombre);

        txtTipo = (TextView) findViewById(R.id.txtTipoSensor);
        txtTipo.setText(tipo);

        txtVendor = (TextView) findViewById(R.id.txtVendor);
        txtVendor.setText(vendor);

        txtResolucion = (TextView) findViewById(R.id.txtResolucion);
        txtResolucion.setText(resolucion);

        txtPoder = (TextView) findViewById(R.id.txtPoder);
        txtPoder.setText(poder);

        txtVersion = (TextView) findViewById(R.id.txtVersion);
        txtVersion.setText(version);

        txtMedicionSensor = (TextView) findViewById(R.id.txtInfoSensor);

        _mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        // En base al tipo de sensor seleccionado se crea una instancia
        switch ( Integer.parseInt(tipo) )
        {
            case Sensor.TYPE_LIGHT:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                break;

            case Sensor.TYPE_PRESSURE:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
                break;

            case Sensor.TYPE_PROXIMITY:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                break;

            case Sensor.TYPE_ACCELEROMETER:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                break;

            case Sensor.TYPE_GYROSCOPE:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                break;

            case Sensor.TYPE_GRAVITY:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                break;

            case Sensor.TYPE_ORIENTATION:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                break;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                break;

            case Sensor.TYPE_STEP_COUNTER:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                break;

            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
                break;

            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
                break;

            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                _dataSensor = _mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registar el evento Listener del sensor
        _mSensorManager.registerListener(this, _dataSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Mata el evento del sensor
        _mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float _valor = 0;
        //En base al tipo de sensor obtenemos los respectivos valores de medición del sensor.
        switch ( Integer.parseInt(tipo) )
        {
            case Sensor.TYPE_LIGHT:
                _valor = event.values[0];
                txtMedicionSensor.setText(_valor+" lux");
                break;

            case Sensor.TYPE_PRESSURE:
                _valor = event.values[0];
                txtMedicionSensor.setText(_valor+" hPa");
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                _valor = event.values[0];
                txtMedicionSensor.setText(_valor+" ºC");
                break;

            case Sensor.TYPE_PROXIMITY:
                _valor = event.values[0];
                txtMedicionSensor.setText(_valor+" cm");
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                _valor = event.values[0];
                txtMedicionSensor.setText(_valor+" %");
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_ACCELEROMETER:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_GYROSCOPE:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_GRAVITY:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_ORIENTATION:
                txtMedicionSensor.setText("X: " + event.values[SensorManager.DATA_X] + "\nY: " + event.values[SensorManager.DATA_Y] + "\nZ: " + event.values[SensorManager.DATA_Z]);
                break;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_STEP_COUNTER:
                _valor = event.values[0];
                txtMedicionSensor.setText(_valor+" Steps");
                break;

            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                txtMedicionSensor.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Nada
    }
}
