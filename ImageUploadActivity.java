package com.example.hsbcapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageUploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final String TAG = "ImageUploadActivity";

    private EditText name, address, mobile;
    private Button addImageButton, submitButton, viewAllButton;
    private ImageView selectedImageView;
    private Uri photoURI;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        name = findViewById(R.id.namef);
        address = findViewById(R.id.addressf);
        mobile = findViewById(R.id.mobilef);
        addImageButton = findViewById(R.id.add_image_button);
        submitButton = findViewById(R.id.submit_button);
        viewAllButton = findViewById(R.id.view_all_button);
        selectedImageView = findViewById(R.id.selected_image_view);
    }

    private void setupClickListeners() {
        addImageButton.setOnClickListener(v -> selectImage());
        submitButton.setOnClickListener(v -> submitForm());
        viewAllButton.setOnClickListener(v -> openTableActivity());
    }

    private void selectImage() {
        String[] options = {"Select from Gallery", "Take a Photo"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (checkStoragePermission()) {
                    openGallery();
                }
            } else {
                if (checkCameraPermission()) {
                    openCameraOptions();
                }
            }
        });
        builder.show();
    }

    private void openCameraOptions() {
        String[] cameraOptions = {"Camera2 API", "Camera Intent API"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Choose a Camera API");
        builder.setItems(cameraOptions, (dialog, which) -> {
            switch (which) {
                case 0:
                    openCamera2API();
                    break;
                case 1:
                    launchCameraIntent();
                    break;
            }
        });
        builder.show();
    }

    private void openCamera2API() {
        Intent intent = new Intent(this, Camera2Activity.class);
        startActivity(intent);
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void launchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                try {
                    photoURI = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);

                    // Grant URI permissions to all potential camera apps
                    List<ResolveInfo> resInfoList = getPackageManager()
                            .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    // Add these flags to ensure the intent is handled properly
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    // Start the camera activity
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);

                } catch (Exception e) {
                    Log.e(TAG, "Error launching camera: " + e.getMessage(), e);
                    Toast.makeText(this, "Error launching camera: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(this, "No camera available on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    selectedImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading gallery image", e);
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == TAKE_PHOTO_REQUEST) {
                try {
                    if (photoURI != null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                        selectedImageView.setImageBitmap(bitmap);
                    } else {
                        Log.e(TAG, "PhotoURI is null");
                        Toast.makeText(this, "Error: Photo URI is null", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error loading captured image", e);
                    Toast.makeText(this, "Error loading captured image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void submitForm() {
        String nameText = name.getText().toString();
        String addressText = address.getText().toString();
        String mobileText = mobile.getText().toString();
        Toast.makeText(this, "Data submitted: " + nameText + ", " + addressText + ", " +
                mobileText, Toast.LENGTH_SHORT).show();
    }

    private void openTableActivity() {
        Intent intent = new Intent(this, TableActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to select images",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraOptions();
            } else {
                Toast.makeText(this, "Camera permission is required to access the camera",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}