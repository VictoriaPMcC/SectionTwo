package com.example.tensorflowtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.example.tensorflowtest.ml.DogModel;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    // One Button
    Button BSelectImage;

    Button btnTriggerModel;

    // One Preview Image
    ImageView IVPreviewImage;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;

    String currentBreed;
    String[] townlist;
//    String[] dogs = new String[1000];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String filename = "sortedLabels.txt";
        String inputString = LoadData(filename);
        townlist = inputString.split("\n");

        BSelectImage = findViewById(R.id.BSelectImage);
        btnTriggerModel = findViewById(R.id.btnNew);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        textView = (TextView) findViewById(R.id.textView);
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

    }

    public void testModel(View view) throws IOException {
        AssetManager assetManager = getAssets();
        IVPreviewImage.invalidate();
        BitmapDrawable drawable = (BitmapDrawable) IVPreviewImage.getDrawable();
        drawable.getBitmap();
        Bitmap bitmap;

        InputStream istr = assetManager.open("rottweiler.jpg");
        bitmap = BitmapFactory.decodeStream(istr);

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        try {

            DogModel model = DogModel.newInstance(this);

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            TensorImage tfImage = TensorImage.createFrom(TensorImage.fromBitmap(resized), DataType.FLOAT32);

            ByteBuffer byteBuffer = tfImage.getBuffer();

            inputFeature0.loadBuffer(byteBuffer);

            DogModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float max = getMax(outputFeature0.getFloatArray());

            textView.setText(townlist[(int) max]);

            model.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getMax(float[] arr) {

        int index = 0;
        float min = 0.0F;

        for (int i = 0; i < townlist.length; i++) {
            {
                if (arr[i] > min) {
                    index = i;
                    min = arr[i];
                }
            }
        }
        return index;
    }

    public String LoadData(String inFile) {
        String tContents = "";

        try {
            InputStream stream = getAssets().open(inFile);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;

    }

    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                }
            }
        }
    }
}