package es.upm.geo.practicageopackage;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ArchivoCustom {

    private final String TAG = ArchivoCustom.class.getSimpleName();
    private static ArchivoCustom ourInstance;

    private Uri _uri = null;
    private String _name = null;
    private InputStream _streamFile = null;
    private AppCompatActivity _actividadPrincipal = null;

    public static ArchivoCustom getInstance() {
        if (ourInstance == null) ourInstance = new ArchivoCustom();
        return ourInstance;
    }

    private ArchivoCustom() {}

    public Uri get_uri() {
        return _uri;
    }

    public void set_uri(Uri _uri) {
        this._uri = _uri;
    }

    public void set_actividadPrincipal(AppCompatActivity _actividadPrincipal) {
        this._actividadPrincipal = _actividadPrincipal;
    }

    public String get_name() {
        return _name;
    }

    public InputStream get_streamFile() {
        return _streamFile;
    }

    public void initData(){
        if( _uri == null || _actividadPrincipal == null ) return;
        this._name = getNameFile();
        try {
            this._streamFile = getStreamFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNameFile() {
        String _nameFile = null;
        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = this._actividadPrincipal.getContentResolver()
                .query(_uri, null, null, null, null, null);
        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {
                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                _nameFile = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + _nameFile);
            }
        } finally {
            cursor.close();
        }
        return _nameFile;
    }

    private InputStream getStreamFile() throws IOException {
        InputStream inputStream = this._actividadPrincipal.getContentResolver().openInputStream(_uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        Log.i(TAG, "Contenido Archivo: " + stringBuilder.toString());
        return inputStream;
    }
}