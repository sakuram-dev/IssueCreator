package com.sakuram.issuecreator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    public static final String ACTION_BUTTON = "com.sakuram.issuecreator.ACTION_WIDGET_BUTTON_TAPPED";
    public static final String ACTION_USER = "com.sakuram.issuecreator.ACTION_WIDGET_USER_TAPPED";
    public static final String ACTION_REPO = "com.sakuram.issuecreator.ACTION_WIDGET_REPO_TAPPED";
    private static final String CHANNEL_ID = "com.sakuram.issuecreator.NOTIFICATION_CHANNEL";

    private static String GITHUB_URL = "https://github.com/";

    private static String userName;
    private static String repoName;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent_button = new Intent(context, AppWidget.class);
        intent_button.setAction(ACTION_BUTTON);
        PendingIntent pendingIntent_button = PendingIntent.getBroadcast(context, 0, intent_button, PendingIntent.FLAG_IMMUTABLE);

        Intent intent_user = new Intent(context, AppWidget.class);
        intent_user.setAction(ACTION_USER);
        PendingIntent pendingIntent_user = PendingIntent.getBroadcast(context, 0, intent_user, PendingIntent.FLAG_IMMUTABLE);

        Intent intent_repo = new Intent(context, AppWidget.class);
        intent_repo.setAction(ACTION_REPO);
        PendingIntent pendingIntent_repo = PendingIntent.getBroadcast(context, 0, intent_repo, PendingIntent.FLAG_IMMUTABLE);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent_button);
        views.setOnClickPendingIntent(R.id.appwidget_user, pendingIntent_user);
        views.setOnClickPendingIntent(R.id.appwidget_repo, pendingIntent_repo);


        // read shared preferences
        userName = context.getSharedPreferences("IssueCreator", Context.MODE_PRIVATE).getString("user", "user");
        repoName = context.getSharedPreferences("IssueCreator", Context.MODE_PRIVATE).getString("repo", "repo");

        // setTextViewText
        views.setTextViewText(R.id.appwidget_user, userName);
        views.setTextViewText(R.id.appwidget_repo, repoName);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
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
        userName = context.getSharedPreferences("IssueCreator", Context.MODE_PRIVATE).getString("user", "user");
        repoName = context.getSharedPreferences("IssueCreator", Context.MODE_PRIVATE).getString("repo", "repo");

        if (Objects.equals(intent.getAction(), ACTION_BUTTON)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL + userName + "/" + repoName + "/issues/new"));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, browserIntent, PendingIntent.FLAG_IMMUTABLE);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(intent.getAction(), ACTION_USER)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL + userName));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, browserIntent, PendingIntent.FLAG_IMMUTABLE);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(intent.getAction(), ACTION_REPO)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL + userName + "/" + repoName));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, browserIntent, PendingIntent.FLAG_IMMUTABLE);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    // show notification
    private void showNotification(Context context, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
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