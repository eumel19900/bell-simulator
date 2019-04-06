package de.rw.bell;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelo;
    private long lastBing;
    float[] gravity = new float[3];
    boolean currentDirection[] = new boolean[3];
    float[] oldAcceleration = new float[3];
    private int mediaPlayerCount = 5;
    private int currentMediaPlayer = 0;
    private MediaPlayer[] mediaPlayer = new MediaPlayer[mediaPlayerCount];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelo = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelo == null){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("There is no accelerometer on this device!",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
            alertDialogBuilder.create().show();
        } else {
            for(int i = 0; i < mediaPlayerCount; i++) {
                mediaPlayer[i] = MediaPlayer.create(getApplicationContext(), R.raw.bell);
            }
        }

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        final float alpha = 0.8f;
        float combinedAcceleration = 0;
        float[] currentAcceleration = new float[3];

        for(int i = 0; i < 3; i++) {
            float value = event.values[i];
            gravity[i] = alpha * gravity[i] + (1 - alpha) * value;
            float acceleration = value - gravity[i];
            combinedAcceleration += acceleration;
            currentAcceleration[i] = acceleration;
            currentDirection[i] = combinedAcceleration > 0;
        }

        boolean accelerationDirectionChanged = false;
        for(int i = 0; i < 3; i++) {
            float acceloChange = Math.abs(Math.abs(currentAcceleration[i]) - Math.abs(oldAcceleration[i]));
            if(acceloChange > 10.0f) {
                accelerationDirectionChanged = true;
                for(int j = 0; j < 3; j++) {
                    oldAcceleration[j] = currentAcceleration[j];
                }
                break;
            }
        }

        float threshold = 5.0f;
        float absAccelo = Math.abs(combinedAcceleration);
        if(accelerationDirectionChanged && (absAccelo > threshold) && System.currentTimeMillis() - lastBing > 100) {
            lastBing = System.currentTimeMillis();

            if(currentMediaPlayer++ >= mediaPlayerCount - 1) {
                currentMediaPlayer = 0;
            }
            if(mediaPlayer[currentMediaPlayer].isPlaying()) {
                mediaPlayer[currentMediaPlayer].seekTo(0);
            }
            float volume = absAccelo > 10.0f ? 1.0f : 0.15f;
            mediaPlayer[currentMediaPlayer].setVolume(volume, volume);
            mediaPlayer[currentMediaPlayer].start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            DialogFragment dialog = new AboutDialogFragment();
            dialog.show(getSupportFragmentManager(), "DialogFragmentTag");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(accelo != null) {
            sensorManager.registerListener(this, accelo, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(accelo != null) {
            sensorManager.unregisterListener(this);
        }
    }

}