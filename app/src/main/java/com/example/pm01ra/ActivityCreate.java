package com.example.pm01ra;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.PixelCopy;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pm01ra.Config.RAMethods;
import com.example.pm01ra.Models.Personas;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class ActivityCreate extends AppCompatActivity {

    private RequestQueue requestQueue;

    Button btnfoto, btnsave;
    EditText nombres, apellidos, direccion, telefono, foto;

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

        nombres = (EditText) findViewById(R.id.txtNombres);
        apellidos = (EditText) findViewById(R.id.txtApellidos);
        direccion = (EditText) findViewById(R.id.txtDireccion);
        telefono = (EditText) findViewById(R.id.txtTelefono);
        foto = (EditText) findViewById(R.id.txtNombres);
    }

    private void SendDataCreate(){
        requestQueue = Volley.newRequestQueue(this);

        Personas person = new Personas();

        person.setNombres(nombres.getText().toString());
        person.setApellidos(apellidos.getText().toString());
        person.setDireccion(direccion.getText().toString());
        person.setTelefono(telefono.getText().toString());
        person.setFoto(ConvertImageBase64(""));

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("nombres",person.getNombres());
            jsonObject.put("apellidos",person.getApellidos());
            jsonObject.put("direccion",person.getDireccion());
            jsonObject.put("telefono",person.getTelefono());
            jsonObject.put("foto",person.getFoto());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, RAMethods.EndPointPOST, new Response.Listener<String>() {
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