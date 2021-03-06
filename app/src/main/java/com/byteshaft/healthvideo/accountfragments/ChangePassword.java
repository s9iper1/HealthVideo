package com.byteshaft.healthvideo.accountfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.byteshaft.healthvideo.AppGlobals;
import com.byteshaft.healthvideo.R;
import com.byteshaft.healthvideo.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by s9iper1 on 6/16/17.
 */

public class ChangePassword extends AppCompatActivity implements View.OnClickListener, HttpRequest.OnErrorListener,
        HttpRequest.OnReadyStateChangeListener {

    private EditText oldPassword;
    private EditText newPassword;
    private Button mRecoverButton;
    private String mPasswordString;
    private String mNewPasswordString;
    private HttpRequest request;
    private TextView forgetPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);
        getSupportActionBar()
                .setTitle(getResources().getString(R.string.forgot_password));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        forgetPassword = (TextView) findViewById(R.id.text_change_password);
        oldPassword = (EditText) findViewById(R.id.old_password);
        newPassword = (EditText) findViewById(R.id.new_password);
        mRecoverButton = (Button) findViewById(R.id.change_password);
        mRecoverButton.setOnClickListener(this);
        oldPassword.setTypeface(AppGlobals.normalTypeFace);
        newPassword.setTypeface(AppGlobals.normalTypeFace);
        mRecoverButton.setTypeface(AppGlobals.normalTypeFace);
        forgetPassword.setTypeface(AppGlobals.normalTypeFace);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }


    public boolean validate() {
        boolean valid = true;
        mPasswordString = oldPassword.getText().toString();
        mNewPasswordString = newPassword.getText().toString();
        if (mPasswordString.length() < 6 || mNewPasswordString.length() < 6) {
            Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.password_error));
            valid = false;
        }
        if (mPasswordString.trim().isEmpty() || mNewPasswordString.trim().isEmpty()) {
            oldPassword.setError("please provide a valid email");
            valid = false;
        } else {
            oldPassword.setError(null);
        }
        return valid;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_password:
                if (validate()) {
                    recoverUserPassword(mPasswordString, mNewPasswordString);
                }
                break;
        }
    }

    private void recoverUserPassword(String oldPass, String newPass) {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%schangePassword?oldPassword=%s&newPassword=%s",
                AppGlobals.BASE_URL, oldPass, newPass));
        request.setRequestHeader("authorization",
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
        Helpers.showProgressDialog(this, "Processing...");
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();
        switch (readyState) {
            case HttpRequest.ERROR_CONNECTION_TIMED_OUT:
                Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.connection_time_out));
                break;
            case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                Helpers.showSnackBar(findViewById(android.R.id.content), exception.getLocalizedMessage());
                break;
            case HttpRequest.ERROR_SSL_CERTIFICATE_INVALID:
                Helpers.showSnackBar(findViewById(android.R.id.content), getResources().getString(R.string.hand_shake_error));
        }

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        System.out.println(request.getResponseText());
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(request.getResponseText());
                            if (jsonObject.isNull("error") && jsonObject.getBoolean("success")) {
                                Helpers.showSnackBar(findViewById(android.R.id.content),
                                        getResources().getString(R.string.password_changed));
                                new android.os.Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 1000);

                            } else if (!jsonObject.isNull("error") && !jsonObject.getBoolean("success")) {
                                if (jsonObject.getString("error").equals("BAD_CREDENTIALS")) {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            getResources().getString(R.string.check_credentials), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                }
        }

    }
}