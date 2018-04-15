package es.upm.geo.sensoresgeo;

import android.hardware.Sensor;

import java.util.UUID;

/**
 * Definición de clase para dibujar la lista personalizada
 */
public class SensorCustom {
    private String _id;
    private Sensor _sensor;

    public SensorCustom(Sensor _sensor)
    {
        this._id = UUID.randomUUID().toString();
        this._sensor = _sensor;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Sensor get_sensor() {
        return _sensor;
    }

    public void set_sensor(Sensor _sensor) {
        this._sensor = _sensor;
    }

    @Override
    public String toString() {
        return this._sensor.getName();
    }
}