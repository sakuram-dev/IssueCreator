package com.sakuram.issuecreator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout usernameInputLayout;
    private TextInputLayout repoInputLayout;
    private Button submitButton;

    private AdView mAdView;
    private FrameLayout adContainerView;
    private boolean initialLayoutComplete = false;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        hideActionBar();
        initializeMobileAds();
        setupAdView();
        loadFullScreenAd();
    }

    private void hideActionBar() {
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    private void initializeMobileAds() {
        MobileAds.initialize(this, initializationStatus -> {});
    }

    private void setupAdView() {
        adContainerView = findViewById(R.id.id_container);
        mAdView = new AdView(this);
        adContainerView.addView(mAdView);
        adContainerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!initialLayoutComplete) {
                            initialLayoutComplete = true;
                            loadBanner();
                        }
                    }
                }
        );
    }

    private void loadFullScreenAd() {
        AdRequest fullScreenAdRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.admob_fullscreen_unit_id), fullScreenAdRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });
    }

    private void loadBanner() {
        mAdView.setAdUnitId(getString(R.string.admob_unit_id));
        mAdView.setAdSize(getAdSize());
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        float adWidthPixels = getAdWidthPixels();
        int adWidth = convertPixelsToDp(adWidthPixels);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private float getAdWidthPixels() {
        float adWidthPixels = adContainerView.getWidth();
        if (adWidthPixels == 0f) {
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();
            adWidthPixels = bounds.width();
        }
        return adWidthPixels;
    }

    private int convertPixelsToDp(float pixels) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (pixels / density);
    }

    private void initializeViews() {
        usernameInputLayout = findViewById(R.id.user_name);
        repoInputLayout = findViewById(R.id.repo_name);
        submitButton = findViewById(R.id.button);
        submitButton.setOnClickListener(onCLickButton);
    }

    private final View.OnClickListener onCLickButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // read user input
            String user_name = getTextInput(usernameInputLayout);
            String repo_name = getTextInput(repoInputLayout);

            // validate user input
            if (!validateInput(user_name, repo_name)) {
                return;
            }

            checkUsernameExists(user_name, repo_name);

            // show Toast
            showToast("User: " + user_name + "\nRepo: " + repo_name);
        }
    };

    // validate user input
    private boolean validateInput(String user_name, String repo_name) {
        if (user_name.isEmpty()) {
            usernameInputLayout.setError("Username cannot be empty");
            return false;
        } else {
            usernameInputLayout.setError(null);
        }

        if (repo_name.isEmpty()) {
            repoInputLayout.setError("Repository name cannot be empty");
            return false;
        } else {
            repoInputLayout.setError(null);
        }

        return true;
    }

    private String getTextInput(TextInputLayout inputLayout) {
        return Objects.requireNonNull(inputLayout.getEditText()).getText().toString();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveToPreferences(String key, String value) {
        getSharedPreferences("IssueCreator", MODE_PRIVATE).edit().putString(key, value).apply();
    }

    private void updateWidget() {
        Intent intent = new Intent(this, AppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), AppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // check if GitHub user is exist or not
    private void checkUsernameExists(String user_name, String repo_name) {
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.github.com/users/" + user_name)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                boolean userExists = response.isSuccessful();
                handler.post(() -> {
                    if (!userExists) {
                        usernameInputLayout.setError("User does not exist. Please try again.");
                    } else {
                        usernameInputLayout.setError(null);

                        // Save user input to shared preferences
                        saveToPreferences("user", user_name);
                        saveToPreferences("repo", repo_name);

                        // Update widget
                        updateWidget();

                        // show full screen ad
                        if (mInterstitialAd != null) {
                            mInterstitialAd.show(MainActivity.this);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}