package com.example.luca.usephonecamerademo.feature;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainStartInternalCameraActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    TextView lastPictureNameView;
    String lastPicturePath;
    File storageDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastPicturePath = savedInstanceState.getString("lastPicturePath");
            Toast.makeText(this, "lastPicturePath: " + lastPicturePath, Toast.LENGTH_SHORT).show();
        }

        setContentView(R.layout.activity_main_start_internal_camera);

        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        lastPictureNameView = findViewById(R.id.newest_file_name);

        final Button openCameraBtn = findViewById(R.id.open_camera_btn);
        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        final PackageManager pm = this.getPackageManager();
        final Button checkHasCameraBtn = findViewById(R.id.check_camera_btn);
        checkHasCameraBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainStartInternalCameraActivity.this, Boolean.toString(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)), Toast.LENGTH_SHORT).show();
            }
        });

        final Button deleteAllBtn = findViewById(R.id.delete_all);
        deleteAllBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean allDeleted = deleteAllPics();
                Toast.makeText(MainStartInternalCameraActivity.this, "All pictures deleted: " + Boolean.toString(allDeleted), Toast.LENGTH_SHORT).show();
            }
        });

        final Button deleteLastBtn = findViewById(R.id.delete_last_pic);
        deleteLastBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean allDeleted = deleteLastPictureFile(lastPicturePath);
                lastPictureNameView.setText(getString(R.string.last_pic_name));
                Toast.makeText(MainStartInternalCameraActivity.this, "Last picture deleted: " + Boolean.toString(allDeleted), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (lastPicturePath != null) {
            outState.putString("lastPicturePath", lastPicturePath);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String newText = getString(R.string.last_pic_name) + lastPicturePath;
        lastPictureNameView.setText(newText);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle extras = data.getExtras();
                Toast.makeText(this, "onActivityResult called with non empty data. Pic should have not been saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "onActivityResult called with empty data. Pic should have been saved", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();

                lastPicturePath = photoFile.getPath();

            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.luca.usephonecamerademo.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private boolean deleteAllPics() {
        boolean deleted = true;

        for(File tempFile : storageDir.listFiles()) {
            // Remains false after first fail.
            deleted = deleted && tempFile.delete();
        }
        return deleted;
    }

    private boolean deleteLastPictureFile(String path) {
        File lastPictureFile = new File(path);
        boolean deleted = false;

        if (lastPictureFile.isFile() && !lastPictureFile.isDirectory()) {
            deleted = lastPictureFile.delete();
        }
        return deleted;
    }
}