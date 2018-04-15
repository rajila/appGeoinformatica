package es.upm.geo.sensoresgeo;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mLeadsList;
    private ArrayAdapter<SensorCustom> mLeadsAdapter;
    private TextView salida;
    private List<SensorCustom> _listSensorCustom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLeadsList = (ListView) findViewById(R.id.listaSensor);

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        //Lista que va a guardar los sensores del dispositivo movil
        _listSensorCustom = new ArrayList<>();

        // Se recorre todos los sensores que se encuentran disponibles en el Movil, y se agrega en la lista
        for(Sensor sensor: sensorManager.getSensorList(Sensor.TYPE_ALL)) _listSensorCustom.add(new SensorCustom(sensor));

        mLeadsAdapter = new ArrayAdapter<SensorCustom>(this, android.R.layout.simple_list_item_1, _listSensorCustom);
        mLeadsList.setAdapter(mLeadsAdapter);

        // Eventos
        mLeadsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SensorCustom currentLead = mLeadsAdapter.getItem(position);
                CharSequence texto = "Seleccionado: " + currentLead.get_sensor().getVendor();
                //Toast.makeText(MainActivity.this,texto,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(view.getContext(),Main3Activity.class);
                //Parametros que se le pasan a la actividad en donde se mostrara el detalle del Sensor
                intent.putExtra("ID",currentLead.get_id());
                intent.putExtra("nombre",currentLead.get_sensor().getName());
                intent.putExtra("poder",currentLead.get_sensor().getPower());
                intent.putExtra("resolucion",currentLead.get_sensor().getResolution());
                intent.putExtra("tipo",currentLead.get_sensor().getType());
                intent.putExtra("vendor",currentLead.get_sensor().getVendor());
                intent.putExtra("version",currentLead.get_sensor().getVersion());
                startActivity(intent);
            }
        });
    }
}