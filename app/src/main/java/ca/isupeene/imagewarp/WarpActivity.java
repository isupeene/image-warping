package ca.isupeene.imagewarp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import ca.isupeene.imagewarp.Multitouch.WarpActionListener;
import ca.isupeene.imagewarp.Multitouch.WarpTouchAdapter;
import ca.isupeene.imagewarp.Warp.FisheyeTask;
import ca.isupeene.imagewarp.Warp.NarrowTask;
import ca.isupeene.imagewarp.Warp.RenderScriptSingleton;
import ca.isupeene.imagewarp.Warp.SwirlTask;
import ca.isupeene.imagewarp.Warp.WarpTaskListener;


public class WarpActivity
        extends ActionBarActivity
        implements WarpActionListener, WarpTaskListener
{
    private static final String TAG = "WarpActivity";

    Deque<Bitmap> _bitmaps = new ArrayDeque<>();
    Menu _menu;

    // Whether the original image has yet to be saved.  We must save
    // before we warp the image, but we should avoid saving
    // again if we undo to the original image and warp again.
    boolean _savedOriginal;

    // Whether the current image has yet to be saved.  We don't want to show
    // the menu item if we've already saved the image.
    boolean _savedCurrent;

    // Flag to avoid starting a transformation while one is in progress.
    boolean _transformationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warp);

        RenderScriptSingleton.setContext(this);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnTouchListener(new WarpTouchAdapter(this));

        switch (getIntent().getAction()) {
            case Intent.ACTION_VIEW:
            case Intent.ACTION_EDIT:
                setNewImageFromIntent(getIntent());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_warp, menu);

        setMenuItemVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_undo:
                undo();
                return true;
            case R.id.action_take_picture:
                takePicture();
                return true;
            case R.id.action_discard:
                discardPicture();
                return true;
            case R.id.action_save:
                savePicture();
                return true;
            case R.id.action_load_picture:
                loadPicture();
                return true;
            case R.id.action_swirl:
                onSwirl();
                return true;
            case R.id.action_narrow:
                onNarrow();
                return true;
            case R.id.action_fisheye:
                onFisheye();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private static final int REQUEST_LOAD_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                setNewImage((Bitmap) data.getExtras().get("data"));
                break;
            case REQUEST_LOAD_IMAGE:
                setNewImageFromUri(data.getData());
                break;
        }
    }

    private void openSettings()
    {
        Intent toOpenSettings = new Intent(this, SettingsActivity.class);
        startActivity(toOpenSettings);
    }

    private void undo()
    {
        Debug.Assert(_bitmaps.size() > 1);

        _bitmaps.removeLast();
        imageView().setImageBitmap(_bitmaps.getLast());

        _savedCurrent = false;
        setMenuItemVisibility();
    }

    private void setNewImage(Bitmap bitmap)
    {
        _bitmaps.clear();
        _bitmaps.add(bitmap);

        imageView().setImageBitmap(bitmap);

        setMenuItemVisibility();
        _savedOriginal = false;
    }

    private void setNewImageFromUri(Uri uri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(uri);
            Bitmap image = BitmapFactory.decodeStream(imageStream);

            setNewImage(image);
        }
        catch (FileNotFoundException ex) {
            Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening file.", ex);
        }
    }

    private void setNewImageFromIntent(Intent intent) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        setNewImageFromUri(uri);
    }

    private void setWarpedImage(Bitmap bitmap)
    {
        _bitmaps.addLast(bitmap);
        imageView().setImageBitmap(bitmap);

        int undoLimit = getSharedPreferences(Settings.NAME, MODE_PRIVATE)
                .getInt(Settings.UNDO_LIMIT, Settings.UNDO_LIMIT_DEFAULT);

        Log.d(TAG, "Undo Limit: " + undoLimit);

        while (_bitmaps.size() > undoLimit + 1)
        {
            _bitmaps.removeFirst();
        }

        _savedCurrent = false;
        setMenuItemVisibility();
    }

    private void takePicture() {
        Intent toTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(toTakePicture, REQUEST_IMAGE_CAPTURE);
    }

    private void discardPicture()
    {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Discard Image?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _bitmaps.clear();
                        imageView().setImageBitmap(null);
                        setMenuItemVisibility();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void savePicture()
    {
        Debug.Assert(_bitmaps.size() > 0);

        FileOutputStream file = null;

        try {
            file = new FileOutputStream(getExternalFilesDir(null) + "/image_" + System.currentTimeMillis() + ".jpg");
            _bitmaps.getLast().compress(Bitmap.CompressFormat.PNG, 100, file);
            file.close();
        }
        catch (IOException ex) {
            Toast.makeText(this, "Error saving file.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error saving file.", ex);
        }
        finally {
            try {
                if (file != null) {
                    file.close();
                }
            }
            catch (IOException ex) {
                Toast.makeText(this, "Error saving file.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error closing file.", ex);
            }
        }

        if (_bitmaps.size() == 1) {
            _savedOriginal = true;
        }
        else {
            _savedCurrent = true;
        }

        setMenuItemVisibility();
    }

    private void loadPicture() {
        Intent toLoadPicture = new Intent(Intent.ACTION_PICK);
        toLoadPicture.setType("image/*");
        startActivityForResult(toLoadPicture, REQUEST_LOAD_IMAGE);
    }

    private void setMenuItemVisibility() {
        if (_menu != null) {
            _menu.findItem(R.id.action_discard).setVisible(_bitmaps.size() > 0);
            _menu.findItem(R.id.action_undo).setVisible(_bitmaps.size() > 1);
            _menu.findItem(R.id.action_save).setVisible(
                    (!_savedCurrent && _bitmaps.size() > 1) ||
                            (!_savedOriginal && _bitmaps.size() == 1)
            );
        }
    }

    // WarpActionListener

    @Override
    public void onSwirl() {
        if (!_bitmaps.isEmpty() && !_transformationInProgress) {
            if (!_savedOriginal) {
                savePicture();
            }

            Toast.makeText(this, "Swirl", Toast.LENGTH_SHORT).show();
            new SwirlTask(this).execute(_bitmaps.getLast());
        }
    }

    @Override
    public void onFisheye() {
        if (!_bitmaps.isEmpty() && !_transformationInProgress) {
            if (!_savedOriginal) {
                savePicture();
            }

            Toast.makeText(this, "Fisheye", Toast.LENGTH_SHORT).show();
            new FisheyeTask(this).execute(_bitmaps.getLast());
        }
    }

    @Override
    public void onNarrow() {
        if (!_bitmaps.isEmpty() && !_transformationInProgress) {
            if (!_savedOriginal) {
                savePicture();
            }

            Toast.makeText(this, "Narrow", Toast.LENGTH_SHORT).show();
            new NarrowTask(this).execute(_bitmaps.getLast());
        }
    }

    // WarpTaskListener

    @Override
    public void onPostExecute(Bitmap result) {
        setWarpedImage(result);
        _transformationInProgress = false;
    }

    // Views

    private ImageView imageView() { return (ImageView) findViewById(R.id.imageView); }
}
