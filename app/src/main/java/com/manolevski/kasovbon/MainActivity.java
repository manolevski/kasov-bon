package com.manolevski.kasovbon;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.manolevski.kasovbon.AsyncTasks.GetDataTask;
import com.manolevski.kasovbon.Listeners.ResponseListener;
import com.manolevski.kasovbon.AsyncTasks.SendDataTask;
import com.manolevski.kasovbon.AsyncTasks.UserLoginTask;
import com.manolevski.kasovbon.Listeners.ErrorDialogClickListener;
import com.manolevski.kasovbon.Listeners.ReviewDialogListener;
import com.manolevski.kasovbon.Managers.DialogManager;
import com.manolevski.kasovbon.Utils.Constants;
import com.manolevski.kasovbon.Managers.SharedPreferencesManager;
import com.manolevski.kasovbon.Utils.ScannerResult;
import com.manolevski.kasovbon.Utils.User;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "MainActivity";

    private EditText dateEdit;
    private EditText timeEdit;
    private EditText priceEdit;
    private EditText timePosEdit;
    private CheckBox posCheckBox;
    private String cookie;
    private User user;

    private SendDataTask sendDataTask = null;
    private GetDataTask getDataTask = null;
    private UserLoginTask authTask = null;

    private ProgressDialog progressDialog;

    private View dataForm;
    private TextView registeredValues;

    private SharedPreferencesManager preferences;
    private ResponseListener sendDataListener;
    private ResponseListener getDataListener;
    private ResponseListener loginListener;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    static final int SCANNER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        setListeners();

        progressDialog = DialogManager.initProgressDialog(this, getString(R.string.please_wait_message), false);

        preferences = new SharedPreferencesManager(this);
        cookie = preferences.getCookie();
        user = preferences.getUser();

        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        timeFormat = new SimpleDateFormat("HH:mm", Locale.GERMANY);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (cookie.equals("")) {
            logOut();
        } else {
            getData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void initLayout() {
        dateEdit = findViewById(R.id.date_edit);
        timeEdit = findViewById(R.id.time_edit);
        priceEdit = findViewById(R.id.price_edit);
        posCheckBox = findViewById(R.id.pos_check);
        timePosEdit = findViewById(R.id.time_pos_edit);

        dataForm = findViewById(R.id.data_form);
        registeredValues = findViewById(R.id.registered_values);
    }

    private void setListeners() {
        dateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog();
                }
            }
        });

        timeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimePickerDialog();
                }
            }
        });

        posCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                timePosEdit.setEnabled(checked);
                timePosEdit.setText(checked ? timeEdit.getText().toString() : "");
            }
        });

        sendDataListener = new ResponseListener() {
            @Override
            public void onCompleted(String result) {
                sendDataTask = null;
                progressDialog.dismiss();

                if (!preferences.getReviewShown()) {
                    preferences.setRegisteredCount(preferences.getRegisteredCount() + 1);
                    if (preferences.getRegisteredCount() == Constants.REVIEW_THRESHOLD) {
                        preferences.setReviewShown(true);
                        callReviewDialog();
                    }
                }

                if (Boolean.parseBoolean(result)) {
                    if (getDataTask == null) {
                        progressDialog.show();
                        getDataTask = new GetDataTask(cookie, getDataListener);
                        getDataTask.execute((Void) null);
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.send_fail, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCanceled() {
                sendDataTask = null;
                progressDialog.dismiss();
            }
        };

        getDataListener = new ResponseListener() {
            @Override
            public void onCompleted(String result) {
                getDataTask = null;
                if (!result.equals("")) {
                    parseHTML(result);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCanceled() {
                getDataTask = null;
                progressDialog.dismiss();
            }
        };

        loginListener = new ResponseListener() {
            @Override
            public void onCompleted(String result) {
                authTask = null;
                progressDialog.dismiss();

                if (!result.equals("error") && !result.equals("fail")) {
                    preferences.setCookie(result);
                    cookie = result;
                    getData();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.error_common, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCanceled() {
                authTask = null;
                progressDialog.dismiss();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getData() {
        if (getDataTask == null) {
            progressDialog.show();
            getDataTask = new GetDataTask(cookie, getDataListener);
            getDataTask.execute((Void) null);
        }
    }

    private void callReviewDialog() {
        DialogManager.reviewDialog(this, getResources().getString(R.string.review_title), getResources().getString(R.string.review_text), new ReviewDialogListener() {
            @Override
            public void onAccept(DialogInterface dialog) {
                openGooglePlay();
                dialog.dismiss();
            }

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
    }

    private void openGooglePlay() {
        Uri playStoreUri = Uri.parse(Constants.PLAY_STORE_URL);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);

        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(webIntent);
        }

    }

    private void showDatePickerDialog() {
        dateEdit.setError(null);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DialogManager.datePickerDialog(this, year, month, day, this);
    }

    private void showTimePickerDialog() {
        timeEdit.setError(null);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DialogManager.timePickerDialog(this, hour, minute, this);
    }

    public void sendData(View v) {
        // Reset errors.
        timeEdit.setError(null);
        priceEdit.setError(null);
        dateEdit.setError(null);

        String time = timeEdit.getText().toString();
        String posTime = timePosEdit.getText().toString();
        String price = priceEdit.getText().toString();
        String date = dateEdit.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(time)) {
            timeEdit.setError(getString(R.string.error_field_required));
            focusView = timeEdit;
            cancel = true;
        }
        if (TextUtils.isEmpty(date)) {
            dateEdit.setError(getString(R.string.error_field_required));
            focusView = dateEdit;
            cancel = true;
        }
        if (TextUtils.isEmpty(price)) {
            priceEdit.setError(getString(R.string.error_field_required));
            focusView = priceEdit;
            cancel = true;
        }
        if (TextUtils.isEmpty(posTime) && posCheckBox.isChecked()) {
            timePosEdit.setError(getString(R.string.error_field_required));
            focusView = timePosEdit;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (sendDataTask == null) {
                progressDialog.show();
                sendDataTask = new SendDataTask(date, time, price, posTime, cookie, sendDataListener);
                sendDataTask.execute((Void) null);
            }
        }
    }

    public void scan(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivityForResult(intent, SCANNER_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, ScannerActivity.class);
                    startActivityForResult(intent, SCANNER_REQUEST);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.scan_permission), Snackbar.LENGTH_LONG).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_REQUEST) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra(Constants.SCANNER_RESULT);
                String[] resultArray = result.split("\\*");
                ScannerResult scannerResult = null;
                if (resultArray.length == 5) {
                    scannerResult = new ScannerResult(resultArray[0], resultArray[1], parseDate(resultArray[2]), parseTime(resultArray[3]), resultArray[4]);
                }
                if (scannerResult == null || scannerResult.getPrice() == null || scannerResult.getDate() == null || scannerResult.getTime() == null) {
                    showScannerError();
                    return;
                }
                dateEdit.setText(dateFormat.format(scannerResult.getDate()));
                timeEdit.setText(timeFormat.format(scannerResult.getTime()));
                priceEdit.setText(scannerResult.getPrice());
            }
            if (resultCode == Activity.RESULT_CANCELED && data != null) {
                showScannerError();
            }
        }
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

    private void parseHTML(String getDataString) {
        if (getDataString.isEmpty())
            return;
        Document doc = Parser.parse(getDataString, Constants.BASE_URL);
        Elements section = doc.select("section.profile");
        if (section == null || section.size() == 0) {
            if (user.getRememberMe())
                logInAgain();
            else
                logOut();
            return;
        }
        if (section.first() != null) {
            setTitle(section.first().select("strong").first().html());

            //set registered values
            dataForm.setVisibility(View.VISIBLE);
            Elements registeredElements = section.first().select("ul");

            //We expect exactly 3 values: for weekly, monthly and annually prizes
            if (registeredElements.size() != 3) {
                return;
            }

            Integer[] registeredArray = new Integer[3];
            for (int i = 0; i < registeredElements.size(); i++) {
                StringBuilder builder = new StringBuilder();
                for (Element el : registeredElements.get(i).select("li")) {
                    builder.append(el.html());
                }
                registeredArray[i] = Integer.parseInt(builder.toString());
            }

            this.registeredValues.setText(String.format(getString(R.string.registered_values), registeredArray[0], registeredArray[1], registeredArray[2]));
        }
        //error messages
        Elements error = doc.select("div.alert-danger");
        if (error.size() > 0) {
            DialogManager.errorDialog(this, getString(R.string.error), error.first().html(), new ErrorDialogClickListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            return;
        }

        //Success messages
        Elements success = doc.select("div.alert-success");
        if (success.size() > 0 && !success.first().html().equals("")) {
            priceEdit.setText("");
            priceEdit.requestFocus();
            timeEdit.setText("");
            timePosEdit.setText("");
            posCheckBox.setChecked(false);
            Snackbar.make(findViewById(android.R.id.content), success.first().html().replaceAll("<br />", ""), Snackbar.LENGTH_LONG).show();
        }
    }

    private void showScannerError() {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.scan_error), Snackbar.LENGTH_LONG).show();
    }

    private void logInAgain() {
        progressDialog.show();

        if (authTask == null) {
            authTask = new UserLoginTask(user.getUserName(), user.getPassword(), loginListener);
            authTask.execute((Void) null);
        }
    }

    private void logOut() {
        preferences.setCookie("");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        Date selectedDate = calendar.getTime();

        dateEdit.setText(dateFormat.format(selectedDate));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        Date selectedTime = calendar.getTime();
        timeEdit.setText(timeFormat.format(selectedTime));
    }
}
