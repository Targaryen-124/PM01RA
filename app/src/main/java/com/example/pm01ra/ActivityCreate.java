package com.example.pm01ra;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pm01ra.Config.RAMethods;
import com.example.pm01ra.Models.Personas;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityCreate extends AppCompatActivity {

    private RequestQueue requestQueue;

    Button btnfoto, btnsave;
    EditText nombres, apellidos, direccion, telefono;
    ImageView imageView;
    static final int REQUEST_IMAGE = 101;
    static final int ACCESS_CAMERA = 201;
    String currentPhotoPath;
    CharSequence[] opciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        opciones = new String[1];
        opciones[0] = "OK";

        nombres = (EditText) findViewById(R.id.txtNombres);
        apellidos = (EditText) findViewById(R.id.txtApellidos);
        direccion = (EditText) findViewById(R.id.txtDireccion);
        telefono = (EditText) findViewById(R.id.txtTelefono);
        imageView = (ImageView) findViewById(R.id.imageView);
        //botones
        btnfoto = (Button) findViewById(R.id.btntake);
        btnsave = (Button) findViewById(R.id.btnsavec);

        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObtenerFoto();
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendDataCreate();
            }
        });
    }

    private void ObtenerFoto(){
        // Metodo para obtener los permisos requeridos de la aplicacion
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},ACCESS_CAMERA);
        }else{
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ACCESS_CAMERA){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else{
                Toast.makeText(getApplicationContext(), "se necesita el permiso de la camara",Toast.LENGTH_LONG).show();
            }
        }
    }

    private File createImageFile() throws IOException {
    // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pm01ra.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE){
            try {
                File foto = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(foto));
            }catch (Exception ex){
                ex.toString();
            }
        }
    }

    private void SendDataCreate(){
        requestQueue = Volley.newRequestQueue(this);

        Personas person = new Personas();

        person.setNombres(nombres.getText().toString());
        person.setApellidos(apellidos.getText().toString());
        person.setDireccion(direccion.getText().toString());
        person.setTelefono(telefono.getText().toString());
        person.setFoto(ConvertImageBase64(currentPhotoPath));

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("nombres",person.getNombres());
            jsonObject.put("apellidos",person.getApellidos());
            jsonObject.put("direccion",person.getDireccion());
            jsonObject.put("telefono",person.getTelefono());
            jsonObject.put("foto",person.getFoto());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, RAMethods.EndPointPOST, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean isSuccess = response.getBoolean(getResources().getString(R.string.issuccess));
                    String message = response.getString(getResources().getString(R.string.message));
                    if(isSuccess) {
                        Log.i(TAG, "Exito" + message);

                        final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityCreate.this);
                        alert.setTitle("Ingresado con exito");
                        alert.setIcon(R.mipmap.ic_launcher);
                        alert.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(opciones[i].equals("OK")){

                                }
                            }
                        });
                        alert.show();

                        Snackbar.make(ActivityCreate.this.getWindow().getDecorView().getRootView(),"Exito: " + message, Snackbar.LENGTH_LONG).show();
                    }else{
                        Log.i(TAG, "Fallo: " + message);
                        Snackbar.make(ActivityCreate.this.getWindow().getDecorView().getRootView(), "Fallo: " + message, Snackbar.LENGTH_LONG).show();
                    }
                    Log.i(TAG, "onRespose" + response.toString());
                }
                catch (Exception ex) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(stringRequest);
    }

    private void SendData(){
        requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, RAMethods.EndPointGET, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    String msg = s.toString();
                    Log.i(TAG,"onResponse" + msg);
                }
                catch (Exception ex) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(stringRequest);
    }

    private String ConvertImageBase64(String path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imagearray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imagearray, Base64.DEFAULT);

    }
}
