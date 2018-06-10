package es.upm.geo.practicacartogeo;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.carto.datasources.MBTilesTileDataSource;
import com.carto.geometry.GeoJSONGeometryReader;
import com.carto.geometry.LineGeometry;
import com.carto.geometry.MultiGeometry;
import com.carto.geometry.MultiPolygonGeometry;
import com.carto.geometry.PolygonGeometry;
import com.carto.layers.VectorTileLayer;
import com.carto.styles.AnimationStyleBuilder;
import com.carto.styles.AnimationType;
import com.carto.styles.CartoCSSStyleSet;
import com.carto.styles.GeometryCollectionStyleBuilder;
import com.carto.styles.LineStyleBuilder;
import com.carto.styles.PolygonStyleBuilder;
import com.carto.vectorelements.GeometryCollection;
import com.carto.vectorelements.Point;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.carto.core.BinaryData;
import com.carto.core.MapPos;
import com.carto.core.MapRange;
import com.carto.core.Variant;
import com.carto.datasources.LocalVectorDataSource;
import com.carto.geometry.FeatureCollection;
import com.carto.geometry.PointGeometry;
import com.carto.graphics.Bitmap;
import com.carto.graphics.Color;
import com.carto.layers.CartoBaseMapStyle;
import com.carto.layers.CartoOnlineVectorTileLayer;
import com.carto.layers.Layer;
import com.carto.layers.VectorLayer;
import com.carto.projections.Projection;
import com.carto.services.CartoSQLService;
import com.carto.services.CartoVisBuilder;
import com.carto.services.CartoVisLoader;
import com.carto.styles.BalloonPopupMargins;
import com.carto.styles.BalloonPopupStyleBuilder;
import com.carto.styles.BillboardOrientation;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.styles.PointStyleBuilder;
import com.carto.styles.TextStyleBuilder;
import com.carto.ui.MapView;
import com.carto.utils.AssetUtils;
import com.carto.utils.BitmapUtils;
import com.carto.utils.ZippedAssetPackage;
import com.carto.vectorelements.BalloonPopup;
import com.carto.vectorelements.Marker;
import com.carto.vectorelements.NMLModel;
import com.carto.vectorelements.Polygon;
import com.carto.vectorelements.Text;
import com.carto.vectorelements.VectorElementVector;
import com.carto.vectortiles.MBVectorTileDecoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.support.design.widget.Snackbar;

public class MainActivity extends AppCompatActivity {

    final String LICENSE = "XTUMwQ0ZRRERrVkk0Rm9HSTdtcDRJcE83R0FYd2w5dlJjQUlVSUV2dzRha3pSckRCc1FwNVpFbFJRSE1TbURNPQoKYXBwVG9rZW49MTIxNjNjMzAtYjhkMC00ZTA2LWJlODQtNDMyMTNhMmMyNzNiCnBhY2thZ2VOYW1lPWVzLnVwbS5nZW8ucHJhY3RpY2FjYXJ0b2dlbwpvbmxpbmVMaWNlbnNlPTEKcHJvZHVjdHM9c2RrLWFuZHJvaWQtNC4qCndhdGVybWFyaz1jYXJ0b2RiCg==";
    final int ALPHARGB = 255;
    final int GENRED = 102;
    final int GENGREEN = 102;
    final int GENBLUE = 102;

    private MapView _mapView;
    LocalVectorDataSource vectorDataSource1;
    LocalVectorDataSource _source;
    Projection _projection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapView.registerLicense(LICENSE, this);
        _mapView = (MapView) this.findViewById(R.id.mapView);

        // Create basemap layer with bundled style
        CartoOnlineVectorTileLayer baseLayer = new CartoOnlineVectorTileLayer(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_VOYAGER);

        _mapView.getLayers().add(baseLayer);

        _projection = _mapView.getOptions().getBaseProjection();

        // 1. Initialize an local vector data source
        vectorDataSource1 = new LocalVectorDataSource(_projection);

        // 2. Initialize a vector layer with the previous data source
        VectorLayer vectorLayer1 = new VectorLayer(vectorDataSource1);

        // 3. Add the previous vector layer to the map
        _mapView.getLayers().add(vectorLayer1);

        // 4. Set limited visible zoom range for the vector layer (optional)
        vectorLayer1.setVisibleZoomRange(new MapRange(10, 24));

        if(!verificarInternet()) return;

        loadViaVisGeoJson();
    }

    private void addMarker (){
        // 1. Create marker style
        MarkerStyleBuilder markerStyleBuilder = new MarkerStyleBuilder();
        markerStyleBuilder.setSize(30);
        // Green colour as ARGB
        markerStyleBuilder.setColor(new Color(0xFF00FF00));

        MarkerStyle sharedMarkerStyle = markerStyleBuilder.buildStyle();

        // 2. Add marker
        MapPos madrid = _projection.fromWgs84(new MapPos(-3.70, 40.41));

        Marker marker1 = new Marker(madrid, sharedMarkerStyle);
        marker1.setMetaDataElement("ClickText", new Variant("Marker nr 1"));
        vectorDataSource1.add(marker1);

        // 3. Animate map to the marker
        _mapView.setFocusPos(madrid, 1);
        _mapView.setZoom(12, 1);
    }

    private void addPoint (){
        MapPos madrid = _projection.fromWgs84(new MapPos(-3.70, 40.41));
        // 2. Create style and position for the Point
        PointStyleBuilder pointStyleBuilder = new PointStyleBuilder();
        pointStyleBuilder.setColor(new Color(0xFF00FF00));
        pointStyleBuilder.setSize(16);

        // 3. Create Point, add to datasource with metadata
        Point point1 = new Point(madrid, pointStyleBuilder.buildStyle());
        point1.setMetaDataElement("ClickText", new Variant("Point nr 1"));

        vectorDataSource1.add(point1);

        // 4. Animate map to the point
        _mapView.setFocusPos(madrid, 1);
        _mapView.setZoom(12, 1);

    }

    private void addText(){
        // 1. Create text style
        TextStyleBuilder textStyleBuilder = new TextStyleBuilder();
        textStyleBuilder.setColor(new Color(0xFFFF0000));
        textStyleBuilder.setOrientationMode(BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA);

        // This enables higher resolution texts for retina devices, but consumes more memory and is slower
        textStyleBuilder.setScaleWithDPI(false);

        // 2. Add text
        MapPos position = _projection.fromWgs84(new MapPos(-3.70, 40.41));
        Text textpopup1 = new Text(position, textStyleBuilder.buildStyle(), "Ubicación 1");
        textpopup1.setMetaDataElement("ClickText", new Variant("Text nr 1"));
        vectorDataSource1.add(textpopup1);

        // 3. Animate zoom to position
        _mapView.setFocusPos(position, 1);
        _mapView.setZoom(13, 1);
    }

    private void addOverlay (){

        MapPos madrid = _projection.fromWgs84(new MapPos(-3.70, 40.41));
        // 1. Load bitmaps to show on the label
        //   Bitmap infoImage = BitmapFactory.decodeResource(getResources(), R.drawable.info);
        //   Bitmap arrowImage = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);

        // 2. Add popup
        BalloonPopupStyleBuilder builder = new BalloonPopupStyleBuilder();
        builder.setCornerRadius(20);
        builder.setLeftMargins(new BalloonPopupMargins(6, 6, 6, 6));
        //   builder.setLeftImage(BitmapUtils.createBitmapFromAndroidBitmap(infoImage));
        //  builder.setRightImage(BitmapUtils.createBitmapFromAndroidBitmap(arrowImage));
        builder.setRightMargins(new BalloonPopupMargins(2, 6, 12, 6));
        builder.setPlacementPriority(1);

        BalloonPopup popup = new BalloonPopup(madrid, builder.buildStyle(), "Popup with pos", "Images, round");
        popup.setMetaDataElement("ClickText", new Variant("Popup caption nr 1"));

        vectorDataSource1.add(popup);

        _mapView.setFocusPos(madrid, 1);
        _mapView.setZoom(13, 1);
    }

    private void print3D (){

        // 1. Load NML model from a file
        BinaryData modelFile = AssetUtils.loadAsset("milktruck.nml");

        // 2. Set location for model, and create NMLModel object with this

        MapPos madrid = _projection.fromWgs84(new MapPos(-3.70, 40.41));
        NMLModel model = new NMLModel(madrid, modelFile);

        // 3. Adjust the size- oversize it by 20*, just to make it more visible (optional)
        model.setScale(20);

        // 4. Add metadata for click handling (optional)
        model.setMetaDataElement("ClickText", new Variant("Single model"));

        // 5. Add it to normal datasource
        vectorDataSource1.add(model);

        _mapView.setFocusPos(madrid, 1);
        _mapView.setZoom(15, 1);

    }

    public void loadViaSQL (){
        final String query = "SELECT * FROM cities15000 WHERE population > 100000";

        final CartoSQLService service = new CartoSQLService();
        service.setUsername("nutiteq");

        final LocalVectorDataSource source = new LocalVectorDataSource(_projection);
        // Be sure to make network queries on another thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final FeatureCollection features = service.queryFeatures(query, _projection);

                    for (int i = 0; i < features.getFeatureCount(); i++) {

                        Log.i("myTag", "Feature: "+features.getFeature(i).toString());
                        // This data set features point geometry,
                        // however, it can also be LineGeometry or PolygonGeometry

                        // 2. Create style and position for the Point
                        PointStyleBuilder pointStyleBuilder = new PointStyleBuilder();
                        pointStyleBuilder.setColor(new Color(0xFF00FF00));
                        pointStyleBuilder.setSize(16);

                        PointGeometry geometry = (PointGeometry)features.getFeature(i).getGeometry();

                        //Esto hay que cambiarlo, ya que no funciona:

                        source.add(new Point(geometry, pointStyleBuilder.buildStyle()));
                    }

                    // VectorDataSource vectorDataSource1 = new LocalVectorDataSource(proj);

                    // 2. Initialize a vector layer with the previous data source
                    VectorLayer vectorLayer1 = new VectorLayer(source);

                    // 3. Add the previous vector layer to the map
                    _mapView.getLayers().add(vectorLayer1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void loadViaVisJson () {
        //final String visJSONURL = "https://ramonalcarria.carto.com/api/v2/viz/180ca0c6-3a1a-11e7-be25-0e3ff518bd15/viz.json";
        //final String visJSONURL = "http://documentation.carto.com/api/v2/viz/2b13c956-e7c1-11e2-806b-5404a6a683d5/viz.json";
        //final String visJSONURL = "https://mapgeo.carto.com/u/mapgeoprod/api/v2/viz/724465d2-9931-11e5-ab0f-42010a14800c/viz.json";
        final String visJSONURL = "https://team.cartodb.com/u/cartotraining/api/v2/viz/36d25ff0-2189-11e6-b39e-0e787de82d45/viz.json";

        Thread thread = new Thread(new Runnable() {
            String cartoCss =
                    "#offlinepackages {\n" +
                            "  polygon-fill: #374C70;\n" +
                            "  polygon-opacity: 0.9;\n" +
                            "  polygon-gamma: 0.5;\n" +
                            " ::outline {" +
                            " line-color: #FFF;\n" +
                            "}" +
                            "#offlinepackages::labels {\n" +
                            "  text-name: [package_id];\n" +
                            "  text-face-name: 'DejaVu Sans Book';\n" +
                            "  text-size: 10;\n" +
                            "  text-fill: #130505;\n" +
                            "  text-label-position-tolerance: 0;\n" +
                            "  text-halo-radius: 1;\n" +
                            "  text-halo-fill: #dee3e7;\n" +
                            "  text-dy: -10;\n" +
                            "  text-allow-overlap: false;\n" +
                            "  text-placement: nutibillboard;\n" +
                            "  text-placement-type: dummy;\n" +
                            "}";
            @Override
            public void run() {
                _mapView.getLayers().clear();

                // Create overlay layer for popups
                Projection proj = _mapView.getOptions().getBaseProjection();
                LocalVectorDataSource dataSource = new LocalVectorDataSource(proj);
                VectorLayer vectorLayer = new VectorLayer(dataSource);

                // Create VIS loader
                CartoVisLoader loader = new CartoVisLoader();
                loader.setDefaultVectorLayerMode(true);

                BinaryData fontData = AssetUtils.loadAsset("carto-fonts.zip");
                loader.setVectorTileAssetPackage(new ZippedAssetPackage(fontData));

                MyCartoVisBuilder visBuilder = new MyCartoVisBuilder(_mapView, vectorLayer);

                try {
                    loader.loadVis(visBuilder, visJSONURL);
                }
                catch (IOException e) {
                    Log.e("EXCEPTION", "Exception: " + e);
                }

                // Add the created popup overlay layer on top of all visJSON layers
                _mapView.getLayers().add(vectorLayer);
            }
        });
        thread.start(); // TODO: should serialize execution
    }

    /**
     * Función que procesa un archivo GeoJson y en base a la geometria de la base de datos dibujamos en el mapa
     * con ayuda de las librerias de Carto
     */
    public void loadViaVisGeoJson()
    {
        MapPos _ubicacionMadrid = _projection.fromWgs84(new MapPos(-3.70, 40.41));
        // Initialize a local vector data source
        _projection = _mapView.getOptions().getBaseProjection();
        _source = new LocalVectorDataSource(_projection);

        VectorLayer layer = new VectorLayer(_source);

        _mapView.getLayers().add(layer);
        _mapView.setFocusPos(_ubicacionMadrid, 3);
        _mapView.setZoom(10, 3);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                // Read GeoJSON, parse it using SDK GeoJSON parser
                GeoJSONGeometryReader reader = new GeoJSONGeometryReader();

                // Set target projection to base (mercator)
                reader.setTargetProjection(_projection);

                String fileName = "distritos.geojson";
                String json;

                try {
                    InputStream is = getAssets().open(fileName);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();

                    json = new String(buffer, "UTF-8");

                } catch (IOException ex) {
                    return;
                }

                // Read features from local asset
                FeatureCollection features = reader.readFeatureCollection(json);

                LineStyleBuilder lineBuilder = new LineStyleBuilder();
                lineBuilder.setColor(new Color(android.graphics.Color.argb(250, 225, 225, 225)));
                lineBuilder.setWidth(1.0f);

                PointStyleBuilder pointStyleBuilder = new PointStyleBuilder();
                pointStyleBuilder.setColor(new Color(android.graphics.Color.argb(250, 225, 225, 225)));
                pointStyleBuilder.setSize(10);

                //AnimationStyleBuilder animationBuilder = new AnimationStyleBuilder();
                //animationBuilder.setRelativeSpeed(2.0f);
                //animationBuilder.setFadeAnimationType(AnimationType.ANIMATION_TYPE_SMOOTHSTEP);

                //BalloonPopupStyleBuilder builder = new BalloonPopupStyleBuilder();
                //builder.setLeftMargins(new BalloonPopupMargins(0, 0, 0, 0));
                //builder.setRightMargins(new BalloonPopupMargins(6, 3, 6, 3));
                //builder.setCornerRadius(5);
                //builder.setAnimationStyle(animationBuilder.buildStyle());
                // Make sure this label is shown on top of all other labels
                //builder.setPlacementPriority(10);

                TextStyleBuilder textStyleBuilder = new TextStyleBuilder();
                textStyleBuilder.setColor(new Color(android.graphics.Color.argb(250, 111, 128, 141)));
                textStyleBuilder.setOrientationMode(BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA);
                textStyleBuilder.setFontSize(8);

                // This enables higher resolution texts for retina devices, but consumes more memory and is slower
                textStyleBuilder.setScaleWithDPI(false);

                MapPos position = null;
                for (int i = 0; i < features.getFeatureCount(); i++) {
                    if (features.getFeature(i).getGeometry() instanceof PointGeometry) {
                        Log.i("PointGeometry", "PointGeometry: "+features.getFeature(i).toString());
                    } else if (features.getFeature(i).getGeometry() instanceof LineGeometry) {
                        Log.i("LineGeometry", "LineGeometry: "+features.getFeature(i).toString());
                    }  else if (features.getFeature(i).getGeometry() instanceof PolygonGeometry) {
                        Log.i("PolygonGeometry", "PolygonGeometry: "+features.getFeature(i).toString());
                    } else if (features.getFeature(i).getGeometry() instanceof MultiGeometry) {
                        Variant _pro = features.getFeature(i).getProperties();
                        PolygonStyleBuilder polygonBuilder = new PolygonStyleBuilder();
                        asignarColorDistrito(polygonBuilder, Integer.parseInt(_pro.getObjectElement("coddistrit").toString().replace("\"","")));
                        polygonBuilder.setLineStyle(lineBuilder.buildStyle());

                        MultiGeometry geometry = (MultiGeometry)features.getFeature(i).getGeometry();
                        GeometryCollectionStyleBuilder collectionBuilder = new GeometryCollectionStyleBuilder();
                        collectionBuilder.setPointStyle(pointStyleBuilder.buildStyle());
                        collectionBuilder.setLineStyle(lineBuilder.buildStyle());
                        collectionBuilder.setPolygonStyle(polygonBuilder.buildStyle());

                        _source.add(new GeometryCollection(geometry, collectionBuilder.buildStyle()));

                        position = geometry.getCenterPos();

                        Text textpopup1 = new Text(position, textStyleBuilder.buildStyle(), _pro.getObjectElement("coddistrit").toString().replace("\"",""));
                        _source.add(textpopup1);
                    }
                }
            }
        });
        thread.start(); // TODO: should serialize execution
    }

    /**
     * Función que asigna los estilos a cada distrito. Para esto hay que conocer el código de cada distrito de madrid
     * Para este ejemplo se ha definido un archivo geojson ya conocido
     * @param poligonoStyle
     * @param codigoDistrito
     */
    public void asignarColorDistrito(PolygonStyleBuilder poligonoStyle, int codigoDistrito)
    {
        switch (codigoDistrito) {
            case 1: // Centro
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 15, 133, 84)));
                break;
            case 2: // Arganzuela
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 95, 70, 144)));
                break;
            case 3: // Retiro
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 4: // Salamanca
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 5: // Chamartín
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 115, 175, 72)));
                break;
            case 6: // Tetuán
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 7: // Chamberí
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 237, 173, 8)));
                break;
            case 8: // Fuencarral - El Pardo
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 204, 80, 62)));
                break;
            case 9: // Moncloa - Aravaca
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 10: // Latina
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 111, 64, 112)));
                break;
            case 11: // Carabanchel
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 56, 166, 165)));
                break;
            case 12: // Usera
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 13: // Puente de Vallecas
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 14: // Moratalaz
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 15: // Ciudad Lineal
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 225, 124, 5)));
                break;
            case 16: // Hortaleza
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 148, 52, 110)));
                break;
            case 17: // Villaverde
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 18: // Villa de Vallecas
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 19: // Vicálvaro
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 20: // San Blas - Canillejas
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
            case 21: // Barajas
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, 29, 105, 150)));
                break;
            default:
                poligonoStyle.setColor(new Color(android.graphics.Color.argb(ALPHARGB, GENRED, GENGREEN, GENBLUE)));
                break;
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

    private class MyCartoVisBuilder extends CartoVisBuilder {

        private VectorLayer vectorLayer; // vector layer for popups
        private MapView mapView;

        public MyCartoVisBuilder(MapView mapView, VectorLayer vectorLayer) {
            this.mapView = mapView;
            this.vectorLayer = vectorLayer;
        }

        @Override
        public void setCenter(MapPos mapPos) {
            mapView.setFocusPos(mapView.getOptions().getBaseProjection().fromWgs84(mapPos), 1.0f);
        }

        @Override
        public void setZoom(float zoom) {
            mapView.setZoom(zoom, 1.0f);
        }

        @Override
        public void addLayer(Layer layer, Variant attributes) {

            // Add the layer to the map view
            mapView.getLayers().add(layer);

        }
    }
}
