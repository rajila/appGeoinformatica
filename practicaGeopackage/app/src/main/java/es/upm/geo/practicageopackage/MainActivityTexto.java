package es.upm.geo.practicageopackage;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.File;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.map.geom.GoogleMapShape;
import mil.nga.geopackage.map.geom.GoogleMapShapeConverter;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.wkb.geom.Geometry;

public class MainActivityTexto extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private final int INFOMAX = 50;

    File geoPackageFile;

    private TextView _txtDataGP;

    GeoPackage geoPackage;
    String _pathFile;

    private ProgressDialog pd = null;

    String _cadena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_texto);

        _txtDataGP = (TextView) findViewById(R.id.txtDataGP);

        Bundle bundle = getIntent().getExtras();
        _pathFile = bundle.get("pathFile").toString();
        _cadena = "";

        // Ventana de espera hasta cargar toda la información del archivo geopackage
        pd = ProgressDialog.show(this, "Procesando", "Espere unos segundos...", true, false);

        // Se comienza la nueva Thread que descargará los datos necesarios
        new DownloadGeoPackage().execute("Parametros que necesite el DownloadTask");

    }

    /**
     * Guardar la información del geopackage en una cadena de texto.
     * @param _featureRow
     */
    private void getInfoGeoPackage(FeatureRow _featureRow)
    {
        String [] _nameColumna = _featureRow.getColumnNames();
        for (int i = 0; _nameColumna.length > i; i++) {
            Log.d(TAG, _nameColumna[i] + ": " + _featureRow.getValue(_nameColumna[i]).toString());
            if(_nameColumna[i].compareTo("geom") != 0 && _nameColumna[i].compareTo("Shape") != 0  )
                _cadena = _cadena + _nameColumna[i] + ": " + _featureRow.getValue(_nameColumna[i]).toString()+"\n";
        }
        _cadena = _cadena + "\n";
    }

    /**
     * Subclase privada que crea un hilo aparte para realizar
     * las acciones que deseemos.
     * http://www.tutorialandroid.com/medio/como-programar-una-rueda-de-espera-progressdialog/
     */
    private class DownloadGeoPackage extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Log.i("Mi app", "Empezando hilo en segundo plano");

            geoPackageFile = new File(_pathFile);

            Log.d(TAG, "Path --> "+_pathFile);

            GeoPackageManager manager = GeoPackageFactory.getManager(MainActivityTexto.this);

            List<String> databases = manager.databases();
            manager.deleteAll(); // Borra todas las bases y vuelve a crear
            boolean imported = manager.importGeoPackage(geoPackageFile);

            if (imported == false){
                Log.d("myTag", "Database not imported!");
                return null;
            }

            Log.d(TAG, "Error DB --> "+databases.size());
            // Open database
            geoPackage = manager.open(databases.get(0));

            // GeoPackage Table DAOs

            // Feature and tile tables
            List<String> features = geoPackage.getFeatureTables();
            List<String> tiles = geoPackage.getTileTables();

            // Query Features
            //validaciones para informacion raster o vector
            if (features.size()>0) {
                String featureTable = features.get(0);
                FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
                GoogleMapShapeConverter converter = new GoogleMapShapeConverter(featureDao.getProjection());
                FeatureCursor featureCursor = featureDao.queryForAll();
                int _cont = 0;
                try {
                    while (featureCursor.moveToNext()) {
                        FeatureRow featureRow = featureCursor.getRow();
                        GeoPackageGeometryData geometryData = featureRow.getGeometry();

                        if (geometryData != null) {
                            getInfoGeoPackage(featureRow);
                            _cont++;
                        }
                        if(_cont > INFOMAX) break;
                    }
                } finally {
                    featureCursor.close();
                }
            }

            return "Datos ya procesados (resultado)";
        }

        protected void onPostExecute(Object result) {
            // Pasamos el resultado de los datos a la Acitvity principal
            if (MainActivityTexto.this.pd != null) {
                MainActivityTexto.this.pd.dismiss(); // Esconde la barra de espera
                _txtDataGP.setText(_cadena);
            }
        }
    }
}