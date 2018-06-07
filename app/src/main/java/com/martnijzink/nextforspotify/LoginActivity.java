package com.martnijzink.nextforspotify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.martnijzink.nextforspotify.spotify.objects.User;
import com.martnijzink.nextforspotify.spotify.webapi.WebAPI;
import com.martnijzink.nextforspotify.spotify.webapi.WebAPIException;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class LoginActivity extends AppCompatActivity {

    public static final String ACCESS_TOKEN_KEY = "access_token";

    private static final String LOG_TAG = LoginActivity.class.getName();
    private static final String[] PERMISSIONS = {
            "user-read-currently-playing",
            "user-read-playback-state",
            "user-modify-playback-state"
    };

    private TextView textWelcome;
    private Button loginButton;
    private Button startButton;
    private boolean loggedIn;
    private WebAPI webAPI;
    private String accessToken;
    private boolean appJustStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "activity created");
        setContentView(R.layout.activity_login);

        textWelcome = findViewById(R.id.text_welcome);
        loginButton = findViewById(R.id.button_login);
        startButton = findViewById(R.id.button_start);

        appJustStarted = true;

        startBrowserLogin();
    }

    public void onClickLogin(View view) {
        startBrowserLogin();
    }

    public void onClickStart(View view) {
        if (loggedIn) {
            Intent intent = new Intent(this, ListenActivity.class);
            intent.putExtra(ACCESS_TOKEN_KEY, accessToken);
            startActivity(intent);
        }
    }

    private void startBrowserLogin() {
        Log.d(LOG_TAG, "starting browser login");
        loginButton.setEnabled(false);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(Common.CLIENT_ID, AuthenticationResponse.Type.TOKEN, Common.REDIRECT_URI);
        builder.setScopes(PERMISSIONS);
        builder.setShowDialog(loggedIn);
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginInBrowser(this, request);
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "activity resumed");

        if (!appJustStarted) {
            loginButton.setEnabled(true);
        }

        appJustStarted = false;

        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(LOG_TAG, "new intent");

        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = AuthenticationResponse.fromUri(uri);

            switch (response.getType()) {
                case TOKEN:
                    Log.d(LOG_TAG, "spotify login successfull");

                    loggedIn = true;

                    accessToken = response.getAccessToken();

                    saveAccessToken(accessToken, response.getExpiresIn());

                    webAPI = new WebAPI(this, accessToken);

                    updateLoginAndStartButton();

                    requestUserInfo();
                    break;
                case ERROR:
                    Log.d(LOG_TAG, "spotify login error: " + response.getError());
                    Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
                    break;
                case EMPTY:
                    Log.d(LOG_TAG, "spotify login cancelled");
                    Toast.makeText(this, R.string.login_cancelled, Toast.LENGTH_LONG).show();
                    break;
                default:
                    Log.d(LOG_TAG, "no response type from spotify login");
            }
        }
    }

    private void saveAccessToken(String accessToken, int expiresIn) {
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

        editor.putString("access_token", accessToken);
        editor.putInt("expires_in", expiresIn);
        editor.putLong("timestamp", System.currentTimeMillis());

        editor.apply();
    }

    private void updateLoginAndStartButton() {
        loginButton.setText(loggedIn ? R.string.switch_user : R.string.log_in);
        startButton.setVisibility(View.VISIBLE);
    }

    private void requestUserInfo() {
        textWelcome.setText(R.string.dots);

        webAPI.requestUser(new Consumer<User>() {
            @Override
            public void accept(User user) {
                Log.d(LOG_TAG, "user info retrieved successfully");
                textWelcome.setText(getString(R.string.welcome_user, user.getDisplayName()));
            }
        }, new Consumer<WebAPIException>() {
            @Override
            public void accept(WebAPIException e) {
                Log.d(LOG_TAG, "unable to get user info", e);
                Toast.makeText(LoginActivity.this, R.string.get_user_info_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

}
