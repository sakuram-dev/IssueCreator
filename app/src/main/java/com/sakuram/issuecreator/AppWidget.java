package com.sakuram.issuecreator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    // Define the action for tapping the widget
    public static final String ACTION_BUTTON = "com.sakuram.issuecreator.ACTION_WIDGET_BUTTON_TAPPED";
    public static final String ACTION_USER = "com.sakuram.issuecreator.ACTION_WIDGET_USER_TAPPED";
    public static final String ACTION_REPO = "com.sakuram.issuecreator.ACTION_WIDGET_REPO_TAPPED";

    private static final String CHANNEL_ID = "com.sakuram.issuecreator.NOTIFICATION_CHANNEL";

    private static String GITHUB_URL = "https://github.com/";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // read shared preferences
        SharedPreferences pref = context.getSharedPreferences("IssueCreator", Context.MODE_PRIVATE);
        String userName = pref.getString("user", "");
        String repoName = pref.getString("repo", "");

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        // Set onClickPendingIntent for each actions
        views.setOnClickPendingIntent(R.id.appwidget_user,getPendingIntent(context, ACTION_USER));
        views.setOnClickPendingIntent(R.id.appwidget_repo, getPendingIntent(context, ACTION_REPO));
        views.setOnClickPendingIntent(R.id.appwidget_button, getPendingIntent(context, ACTION_BUTTON));

        // setTextViewText
        views.setTextViewText(R.id.appwidget_user, userName.isEmpty() ? "Set user" : userName);
        views.setTextViewText(R.id.appwidget_repo, repoName.isEmpty() ? "Set repo" : repoName);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Helper method to get PendingIntent
    private static PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    // OnReceive is called when the widget is tapped
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // read shared preferences
        SharedPreferences prefs = context.getSharedPreferences("IssueCreator", Context.MODE_PRIVATE);
        String userName = prefs.getString("user", "");
        String repoName = prefs.getString("repo", "");

        // open MainActivity if userName or repoName is empty
        if (userName.isEmpty() || repoName.isEmpty()) {
            openMainActivity(context);
        } else {
            // Handle each action
            if (Objects.equals(intent.getAction(), ACTION_USER)) {
                openBrowser(context, GITHUB_URL + userName);
            } else if (Objects.equals(intent.getAction(), ACTION_REPO)) {
                openBrowser(context, GITHUB_URL + userName + "/" + repoName);
            } else if (Objects.equals(intent.getAction(), ACTION_BUTTON)) {
                openBrowser(context, GITHUB_URL + userName + "/" + repoName + "/issues/new");
            }
        }
    }

    private void openMainActivity(Context context) {
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainActivityIntent);
    }

    private void openBrowser(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    // show notification
    private void showNotification(Context context, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.add_box)
                .setContentTitle("My Notification")
                .setContentText("test")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // grant permission for notification

            return;
        } else {
            notificationManager.notify(0, builder.build());
        }
    }
}