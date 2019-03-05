package com.manolevski.kasovbon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.manolevski.kasovbon.Utils.Constants;
import com.manolevski.kasovbon.Utils.ScannerResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    private static final String TAG = "ScannerActivity";
    private static final int BORDER_RADIUS = 10;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scanner);

        ViewGroup contentFrame = findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomViewFinderView(context);
            }
        };
        contentFrame.addView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent returnIntent = new Intent();

        if (rawResult.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
            String resultText = rawResult.getText();
            if (resultText.contains("*")) {
                String[] resultArray = resultText.split("\\*");
                if (resultArray.length == 5) {
                    ScannerResult scannerResult = new ScannerResult(resultArray[0], resultArray[1], parseDate(resultArray[2]), parseTime(resultArray[3]), resultArray[4]);
                    returnIntent.putExtra(Constants.SCANNER_RESULT, (new Gson()).toJson(scannerResult));
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
            }
        }
        //If something is not right.
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private Date parseDate(String input) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(input);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        return convertedDate;
    }

    private Date parseTime(String input) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
        Date convertedTime = new Date();
        try {
            convertedTime = timeFormat.parse(input);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        return convertedTime;
    }

    private static class CustomViewFinderView extends ViewFinderView {
        public CustomViewFinderView(Context context) {
            super(context);
            init();
        }

        public CustomViewFinderView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            setSquareViewFinder(true);
            setBorderCornerRounded(true);
            setBorderCornerRadius(BORDER_RADIUS);
            setBorderColor(getResources().getColor(R.color.colorAccent));
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
    }
}