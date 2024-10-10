package com.example.hsbcapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TableActivity extends AppCompatActivity {

    private TextView nameTextView, addressTextView, mobileTextView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table); // Ensure this is the correct layout file

        // Initialize views
        nameTextView = findViewById(R.id.name_text_view);
        addressTextView = findViewById(R.id.address_text_view);
        mobileTextView = findViewById(R.id.mobile_text_view);
        imageView = findViewById(R.id.image_view);

        // Get the data passed from ImageUploadActivity
        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        String mobile = getIntent().getStringExtra("mobile");
        byte[] byteArray = getIntent().getByteArrayExtra("image");

        // Set the received data to the TextViews
        nameTextView.setText(name);
        addressTextView.setText(address);
        mobileTextView.setText(mobile);

        // Convert byte array to Bitmap and set it to the ImageView
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imageView.setImageBitmap(bitmap);
        }
    }
}
