package com.example.diagnostic;

import android.graphics.Bitmap;

public class DicomDataModel {
    public DicomDataModel (byte[]  imageByteArray, String patientName) {
        this.imageByteArray = imageByteArray;
        this.patientName = patientName;
    }
    private byte[] imageByteArray;
    private String patientName;

    public String getPatientName() {
        return patientName;
    }


    public byte[] getImageByteArray() {
        return imageByteArray;
    }
}
