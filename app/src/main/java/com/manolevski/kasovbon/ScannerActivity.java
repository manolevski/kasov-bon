package com.manolevski.kasovbon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.manolevski.kasovbon.Utils.Constants;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;
    private boolean flashEnabled = false;
    private ImageView flashButton;

    private static final int BORDER_RADIUS = 10;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scanner);

        flashButton = findViewById(R.id.flash_button);

        ViewGroup contentFrame = findViewById(R.id.content_frame);
        scannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomViewFinderView(context);
            }
        };
        contentFrame.addView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
        flashButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent returnIntent = new Intent();

        if (rawResult.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
            String resultText = rawResult.getText();
            if (resultText.contains("*")) {
                returnIntent.putExtra(Constants.SCANNER_RESULT, resultText);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
        //If something is not right.
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void toggleFlash(View v) {
        flashEnabled = !flashEnabled;
        flashButton.setImageDrawable(getResources().getDrawable(flashEnabled ? R.drawable.flash_on : R.drawable.flash_off));
        scannerView.setFlash(flashEnabled);
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