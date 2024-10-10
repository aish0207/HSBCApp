package com.example.hsbcapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private EditText name, address, mobile;
    private Button addImageButton, submitButton, viewAllButton;
    private ImageView selectedImageView;
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        mobile = findViewById(R.id.mobile);
        addImageButton = findViewById(R.id.add_image_button);
        submitButton = findViewById(R.id.submit_button);
        viewAllButton = findViewById(R.id.view_all_button);
        selectedImageView = findViewById(R.id.selected_image_view);

        // Handle image upload button click
        addImageButton.setOnClickListener(v -> selectImage());

        // Handle submit button click
        submitButton.setOnClickListener(v -> {
            String userName = name.getText().toString();
            String userAddress = address.getText().toString();
            String userMobile = mobile.getText().toString();

            if (validateForm(userName, userAddress, userMobile)) {
                Intent intent = new Intent(ImageUploadActivity.this, TableActivity.class);
                intent.putExtra("name", userName);
                intent.putExtra("address", userAddress);
                intent.putExtra("mobile", userMobile);

                // Convert Bitmap to byte array to pass via Intent
                if (selectedImageBitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent.putExtra("image", byteArray);
                }

                startActivity(intent);
            } else {
                Toast.makeText(ImageUploadActivity.this, "Please complete the form", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle view all button click
        viewAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(ImageUploadActivity.this, TableActivity.class);
            startActivity(intent);
        });
    }

    // Open a dialog to choose between camera or gallery
    private void selectImage() {
        String[] options = {"Select from Device", "Take a Photo"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Request storage permission if not granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                }
            } else {
                // Request camera permission if not granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, TAKE_PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                selectedImageView.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK && data != null) {
            // Instead of using the captured image, inject an image from resources or external source
            selectedImageBitmap = injectCustomImage();
            selectedImageView.setImageBitmap(selectedImageBitmap);
        }
    }

    // Inject a predefined image (custom image injection)
    private Bitmap injectCustomImage() {
        // Here you can replace this with a bitmap from an external source or local storage
        return BitmapFactory.decodeResource(getResources(), R.drawable.predefined_image);  // Assume 'predefined_image' exists in your drawable resources
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to select an image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateForm(String userName, String userAddress, String userMobile) {
        boolean isValid = true;

        if (userName.isEmpty()) {
            name.setError("Name is required");
            isValid = false;
        }
        if (userAddress.isEmpty()) {
            address.setError("Address is required");
            isValid = false;
        }
        if (userMobile.isEmpty() || userMobile.length() != 10) {
            mobile.setError("Mobile number must be 10 digits");
            isValid = false;
        }
        if (selectedImageBitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}
