package es.upm.geo.practicageopackage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.regex.Pattern;

public class MainActivityPrincipal extends AppCompatActivity {

    private final String TAG = MainActivityPrincipal.class.getSimpleName();
    private boolean _disButton = false;

    private ImageButton _btnBuscar;
    private TextView _txtLabelNameFile;

    private Button _btnTexto;
    private Button _btnMapa;

    private String _pathFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_principal);

        // Verifica el acceso a los archivos del dispositivo
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1001);
        }

        _btnBuscar = (ImageButton) findViewById(R.id.btnBuscar);
        _txtLabelNameFile = (TextView) findViewById(R.id.txtLabelNameFile);

        _btnTexto = (Button) findViewById(R.id.btnTexto);
        _btnTexto.setEnabled(_disButton);

        _btnMapa = (Button) findViewById(R.id.btnMapa);
        _btnMapa.setEnabled(_disButton);

        _btnBuscar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Para buscar el archivo geopackage en el explorador de archivos
                new MaterialFilePicker()
                        .withActivity(MainActivityPrincipal.this)
                        .withRequestCode(1)
                        .withFilter(Pattern.compile(".*\\.gpkg$")) // Filtering files and directories by file name using regexp
                        //.withFilterDirectories(true) // Set directories filterable (false by default)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });
    }

    /**
     * Obtiene la respuesta del Explorador de archivos
     * https://github.com/nbsp-team/MaterialFilePicker
     * @param requestCode
     * @param resultCode
     * @param resultData
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 1 && resultCode == MainActivityPrincipal.RESULT_OK) {
            if (resultData != null) {
                String filePath = resultData.getStringExtra(FilePickerActivity.RESULT_FILE_PATH); // Obtenemos la url del archivo seleccioando
                _pathFile = filePath;
                Log.i(TAG, "file path --> : " + filePath);
                String [] _path = resultData.getStringExtra(FilePickerActivity.RESULT_FILE_PATH).split(File.separator);
                Log.i(TAG, "file path --> : " +_path[_path.length - 1]);
                _txtLabelNameFile.setText(_path[_path.length - 1]);
                _disButton = true;
                _btnTexto.setEnabled(_disButton);
                _btnMapa.setEnabled(_disButton);
            }
        }
    }

    /**
     * Evento del boton Ver Mapa
     * @param view
     */
    public void verMapaButtonHandler(View view)
    {
        Intent intent = new Intent(view.getContext(),MainActivity.class);
        //Parametros que se le pasan a la actividad en donde se mostrara el detalle del Sensor
        intent.putExtra("pathFile",_pathFile);
        startActivity(intent);
    }

    /**
     * Evento del boton Ver Texto
     * @param view
     */
    public void verTextoButtonHandler(View view)
    {
        Intent intent = new Intent(view.getContext(),MainActivityTexto.class);
        //Parametros que se le pasan a la actividad en donde se mostrara el detalle del Sensor
        intent.putExtra("pathFile",_pathFile);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1001:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permisos Concedidos",Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(this,R.string.error_read_storage,Toast.LENGTH_SHORT).show();
                    //finish();
                    showSnackbar(R.string.error_read_storage,
                            android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED){
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1001);
                                    }
                                }
                            });
                }
            }
        }
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
}