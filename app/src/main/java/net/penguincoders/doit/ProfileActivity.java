package net.penguincoders.doit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sp;//will save the changes in the user profile for next times
    EditText etFirstName, etLastName, etEmail;
    Button btnSave, btnTakePicture, btnTakePictureFromGallery;
    ImageView ivPicture;
    Bitmap bitmap;//for the picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        sp = getSharedPreferences("profile", 0);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        ivPicture = findViewById(R.id.image);
        btnSave = findViewById(R.id.btnSave);
        btnTakePictureFromGallery = findViewById(R.id.btnTakePictureFromGallery);
        btnTakePicture = findViewById(R.id.btnTakePicture);
        btnSave.setOnClickListener(this);
        btnTakePicture.setOnClickListener(this);
        btnTakePictureFromGallery.setOnClickListener(this);

        String firstName = sp.getString("firstName", null);
        String lastName = sp.getString("lastName", null);
        String email = sp.getString("email", null);

        //get string bitmap and encoded to bitmap
        String encoded = sp.getString("picture", null);
        if(encoded != null) {
            byte[] imageAsBytes = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            ivPicture.setImageBitmap(bitmap);
        }
        etFirstName.setText(firstName);
        etLastName.setText(lastName);
        etEmail.setText(email);

        //Bitmap bitmap = ((BitmapDrawable)ivPicture.getDrawable()).getBitmap();
    }

    @Override
    public void onClick(View view) {
        if(view == btnSave){
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("firstName", etFirstName.getText().toString());
            editor.putString("lastName", etLastName.getText().toString());
            editor.putString("email", etEmail.getText().toString());

            //this code save the bitmap. the SharedPreferences cant get complex object like bitmap
            // so i convert this object to string
            if(bitmap != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                String encoded = Base64.encodeToString(b, Base64.DEFAULT);
                editor.putString("picture", encoded);
            }
            editor.apply();
            Toast.makeText(this, "The data is saved", Toast.LENGTH_SHORT).show();
            endActivity();
        }
        else if(view == btnTakePicture){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 5);
        }
        else if(view == btnTakePictureFromGallery){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 4);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 5){
            if(resultCode == RESULT_OK){
                bitmap = (Bitmap) data.getExtras().get("data");
                ivPicture.setImageBitmap(bitmap);
            }
            else{
                Toast.makeText(this, "Something Failed", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 4){
            if(resultCode == RESULT_OK){
                ContentResolver resolver = getContentResolver();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, data.getData());
                    ivPicture.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(this, "Something Failed", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.returnHomePage){
            endActivity();
        }
        return true;
    }
    public void endActivity(){
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}