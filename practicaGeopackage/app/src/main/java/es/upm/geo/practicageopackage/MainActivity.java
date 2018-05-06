package es.upm.geo.practicageopackage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = MainActivity.class.getSimpleName();

    File geoPackageFile;

    private GoogleMap mMap;

    SupportMapFragment mapFragment;

    GeoPackage geoPackage;

    String _pathFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        _pathFile = bundle.get("pathFile").toString();

        geoPackageFile = new File(_pathFile);

        Log.d(TAG, "Path --> "+_pathFile);

        GeoPackageManager manager = GeoPackageFactory.getManager(MainActivity.this);

        List<String> databases = manager.databases();
        manager.deleteAll(); // Borra todas las bases y vuelve a crear
        boolean imported = manager.importGeoPackage(geoPackageFile);

        if (imported == false){
            Log.d("myTag", "Database not imported!");
            return;
        }

        // Open database
        geoPackage = manager.open(databases.get(0));
        mapFragment.getMapAsync(MainActivity.this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMaxZoomPreference(20);

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

            try {
                while (featureCursor.moveToNext()) {
                    FeatureRow featureRow = featureCursor.getRow();
                    GeoPackageGeometryData geometryData = featureRow.getGeometry();

                    if (geometryData != null) {
                        Geometry geometry = geometryData.getGeometry();
                        GoogleMapShape shape = converter.toShape(geometry);
                        GoogleMapShape mapShape = GoogleMapShapeConverter.addShapeToMap(mMap, shape); // Dibuja la geometria en el mapa.
                    }
                }
            } finally {
                featureCursor.close();
            }
        }
    }
}