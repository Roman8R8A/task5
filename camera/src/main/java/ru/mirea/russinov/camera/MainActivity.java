package ru.mirea.russinov.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import ru.mirea.russinov.camera.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult> {
    private static final int REQUEST_CODE_PERMISSION = 200;
    private boolean is_permissions_granted = false;
    private ActivityMainBinding binding = null;

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = null;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this);
        MakePermissionsRequest();
    }


    private void MakePermissionsRequest() {
        final boolean camera_enabled = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        if(camera_enabled) {
            is_permissions_granted = true;
        } else {
            is_permissions_granted = false;
            ActivityCompat.requestPermissions(this,
                    new	String[] { android.Manifest.permission.CAMERA
                    },	REQUEST_CODE_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("", "onRequestPermissionsResult: " + String.valueOf(requestCode));
        if(requestCode == REQUEST_CODE_PERMISSION) {
            is_permissions_granted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }



    public void OnUpdatePhotoButtonClicked(View v) {
        Log.i("", "OnUpdatePhotoButtonClicked");
        if(!is_permissions_granted) {
            Log.i("", "\tHas no permissions...");
            MakePermissionsRequest();
            return;
        }

        try {
            File file = createImageFile();
            String authorities = "ru.mirea.russinov.camera.fileprovider";//getApplicationContext().getPackageName() + ".fileprovider";
            Log.i("", "\tGet authorities: " + authorities);
            imageUri = FileProvider.getUriForFile(this, authorities, file);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraActivityResultLauncher.launch(cameraIntent);
        } catch (IOException e) {
            Log.d("", e.toString());
        }
    }
    @Override
    public void onActivityResult(ActivityResult o) {
        Log.i("", "onActivityResult: " + String.valueOf(o.getResultCode()));
        if(o.getResultCode() != Activity.RESULT_OK)
            return;

        Intent data = o.getData();
        binding.showPhotoView.setImageURI(imageUri);
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss", Locale.ENGLISH).format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDirectory);
    }
}