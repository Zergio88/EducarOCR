package ar.educarocr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button button_capture, button_copy, button_procesar;
    TextView textview_data;
    Bitmap bitmap;
    private static final int REQUEST_CAMERA_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_capture = findViewById(R.id.button_capture);
        button_copy = findViewById(R.id.button_copy);
        button_procesar = findViewById(R.id.button_procesar);
        textview_data = findViewById(R.id.text_data);

        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ android.Manifest.permission.CAMERA }, REQUEST_CAMERA_CODE);
        }

        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });

        button_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scanned_text = textview_data.getText().toString();
                copyToClipBoard(scanned_text);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, scanned_text);
                sendIntent.setType("text/plain");
                //sendIntent.setPackage("com.whatsapp");
                if (sendIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "WhatsApp no está instalado", Toast.LENGTH_SHORT).show();
                }

            }
        });

        button_procesar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String scanned_text = textview_data.getText().toString();
                textProcess(scanned_text);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    getTextFromImage(bitmap);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void getTextFromImage(Bitmap bitmap){
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if(!recognizer.isOperational()){
            Toast.makeText(MainActivity.this,"Error Ocurred!!!",Toast.LENGTH_SHORT).show();
        }else{
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0;i<textBlockSparseArray.size();i++){
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }
            textview_data.setText(stringBuilder.toString());
            button_capture.setText("Retake");
            button_copy.setVisibility(View.VISIBLE);
            button_procesar.setVisibility(View.VISIBLE);
        }
    }

    private void copyToClipBoard(String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied data",text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this,"Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void textProcess(String text){
        String[] lines = text.split("\n");
        String searchStringHWID = "Hardware ID", searchStringBT = "Boot Tick";
        String hardwareID="", boottick="", boottickOK="", resultado="";


        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(searchStringHWID)) {
                hardwareID = lines[i].toString().replace("Hardware ID:","");
                hardwareID = hardwareID.replaceAll("\\s","");
                hardwareID = hardwareID.replaceAll("-","");
            }
            if (lines[i].contains(searchStringBT)) {
                boottick = lines[i].toString().replace("Boot Tick:","");
                boottick = boottick.replaceAll("\\s","");
                boottick = boottick.replaceAll("-","");
                boottickOK = boottick.substring(boottick.length() - 2);
            }
        }
        resultado = hardwareID + "\n" + boottickOK;
        textview_data.setText(resultado);
    }
}