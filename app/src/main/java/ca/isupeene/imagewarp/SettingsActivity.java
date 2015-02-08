package ca.isupeene.imagewarp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;


public class SettingsActivity
        extends ActionBarActivity
        implements SeekBar.OnSeekBarChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        seekBar().setOnSeekBarChangeListener(this);

        int undoLimit = getSharedPreferences(Settings.NAME, MODE_PRIVATE)
                .getInt(Settings.UNDO_LIMIT, Settings.UNDO_LIMIT_DEFAULT);
        seekBar().setProgress(undoLimit);
        seekBarValue().setText(String.valueOf(undoLimit));
    }

    // OnSeekBarChangeListener

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        getSharedPreferences(Settings.NAME, MODE_PRIVATE).edit()
                .putInt(Settings.UNDO_LIMIT, seekBar().getProgress())
                .commit();
        seekBarValue().setText(String.valueOf(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

    // Views

    private TextView seekBarValue() {
        return (TextView) findViewById(R.id.seek_bar_value);
    }

    private SeekBar seekBar() {
        return (SeekBar) findViewById(R.id.seek_bar);
    }
}
