package com.sakuram.issuecreator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "com.sakuram.issuecreator.NOTIFICATION_CHANNEL";

    private TextInputLayout usernameInputLayout;
    private TextInputLayout repoInputLayout;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        setContentView(R.layout.activity_main);

        usernameInputLayout = findViewById(R.id.user_name);
        repoInputLayout = findViewById(R.id.repo_name);

        setViews();
    }

    // set views
    private void setViews() {
        Button button = findViewById(R.id.button);
        button.setOnClickListener(onCLickButton);
    }

    private final View.OnClickListener onCLickButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // read user input
            String user_name = Objects.requireNonNull(((TextInputLayout) findViewById(R.id.user_name)).getEditText()).getText().toString();
            String repo_name = Objects.requireNonNull(((TextInputLayout) findViewById(R.id.repo_name)).getEditText()).getText().toString();

            checkUsernameExists(user_name);

            // show Toast
            Toast.makeText(MainActivity.this, user_name, Toast.LENGTH_SHORT).show();

            // save user input to shared preferences
            getSharedPreferences("user", MODE_PRIVATE).edit().putString("user", user_name).apply();
            getSharedPreferences("repo", MODE_PRIVATE).edit().putString("repo", repo_name).apply();

            // update widget
            Intent intent = new Intent(MainActivity.this, AppWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), AppWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    };

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // check if GitHub user is exist or not
    private void checkUsernameExists(String username) {
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.github.com/users/" + username)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                boolean userExists = response.isSuccessful();
                handler.post(() -> {
                    if (!userExists) {
                        usernameInputLayout.setError("User does not exist. Please try again.");
                    } else {
                        usernameInputLayout.setError(null);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}