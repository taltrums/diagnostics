package com.example.diagnostic;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.imebra.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "channelDicomFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // load the Imebra library
        System.loadLibrary("imebra_lib");

        super.onCreate(savedInstanceState);

    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            // Note: this method is invoked on the main thread.
                            if (call.method.equals("getFileData")) {
                                byte[] fileBytes = call.argument("fileBytes");
                                if (fileBytes != null) {
                                    DicomDataModel dicomFileData = readDicomFile(fileBytes);
                                    if(dicomFileData != null) {
                                        HashMap<String, Object> returnFileData =
                                                new HashMap<String, Object>();
                                        returnFileData.put("patientName", dicomFileData.getPatientName());
                                        returnFileData.put("imageByteArray", dicomFileData.getImageByteArray());

                                        result.success(returnFileData);
                                    }
                                } else {
                                    result.error("UNAVAILABLE", "Dicom file could not be read.", null);
                                }
                            } else {
                                result.notImplemented();
                            }

                        }

                );
    }


    private DicomDataModel readDicomFile( byte[] fileBytes) {
        try {

            CodecFactory.setMaximumImageSize(8000, 8000);
            InputStream stream = new ByteArrayInputStream(fileBytes);
            PipeStream imebraPipe = new PipeStream(32000);
            Thread pushThread = new Thread(new PushToImebraPipe(imebraPipe, stream));
            pushThread.start();
            DataSet loadDataSet = CodecFactory.load(new StreamReader(imebraPipe.getStreamInput()));
            Image dicomFile = loadDataSet.getImageApplyModalityTransform(0);
            TransformsChain chain = new TransformsChain();

            if(ColorTransformsFactory.isMonochrome(dicomFile.getColorSpace()))
            {
                VOILUT voilut = new VOILUT(VOILUT.getOptimalVOI(dicomFile, 0, 0, dicomFile.getWidth(), dicomFile.getHeight()));
                chain.addTransform(voilut);
            }
            DrawBitmap drawBitmap = new DrawBitmap(chain);
            Memory memory = drawBitmap.getBitmap(dicomFile, drawBitmapType_t.drawBitmapRGBA, 4);

        
            Bitmap renderBitmap = Bitmap.createBitmap((int)dicomFile.getWidth(), (int)dicomFile.getHeight(), Bitmap.Config.ARGB_8888);
            byte[] memoryByte = new byte[(int)memory.size()];
            memory.data(memoryByte);
            ByteBuffer byteBuffer = ByteBuffer.wrap(memoryByte);
            renderBitmap.copyPixelsFromBuffer(byteBuffer);



            String patientName = loadDataSet
                    .getPatientName(new TagId(0x10,0x10), 0, new PatientName("Undefined", "", ""))
                    .getAlphabeticRepresentation();

            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            renderBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            renderBitmap.recycle();

            DicomDataModel dicomFileData = new DicomDataModel(byteArray, patientName);

            return dicomFileData;
        }
        catch(Exception e) {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage(e.getMessage());
            dlgAlert.setTitle("Error");
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } } );
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
            String test = "Test";
        }
        return null;
    }
}