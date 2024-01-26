package com.sakuram.issuecreator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.SizeF;
import android.widget.RemoteViews;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    // Define the action for tapping the widget
    public static final String ACTION_BUTTON = "com.sakuram.issuecreator.ACTION_WIDGET_BUTTON_TAPPED";
    public static final String ACTION_USER = "com.sakuram.issuecreator.ACTION_WIDGET_USER_TAPPED";
    public static final String ACTION_REPO = "com.sakuram.issuecreator.ACTION_WIDGET_REPO_TAPPED";
    public static final String ACTION_SETTINGS = "com.sakuram.issuecreator.ACTION_WIDGET_SETTINGS_TAPPED";

    private static final String CHANNEL_ID = "com.sakuram.issuecreator.NOTIFICATION_CHANNEL";

    private static String GITHUB_URL = "https://github.com/";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // read shared preferences
        SharedPreferences pref = context.getSharedPreferences("IssueCreator", Context.MODE_PRIVATE);
        String userName = pref.getString("user", "");
        String repoName = pref.getString("repo", "");

        // Construct the RemoteViews object
        // RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        // layout for each size if API level is 31 or higher

        RemoteViews views;
        RemoteViews smallViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_small);
        RemoteViews mediumViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        RemoteViews largeViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_large);

        // Set onClickPendingIntent and TextViewText for each layout
        smallViews.setOnClickPendingIntent(R.id.appwidget_user,getPendingIntent(context, ACTION_REPO));
        smallViews.setOnClickPendingIntent(R.id.appwidget_repo, getPendingIntent(context, ACTION_BUTTON));
        smallViews.setTextViewText(R.id.appwidget_repo, repoName.isEmpty() ? "Set repo" : repoName);

        mediumViews.setOnClickPendingIntent(R.id.appwidget_user,getPendingIntent(context, ACTION_USER));
        mediumViews.setOnClickPendingIntent(R.id.appwidget_repo, getPendingIntent(context, ACTION_REPO));
        mediumViews.setOnClickPendingIntent(R.id.appwidget_button, getPendingIntent(context, ACTION_BUTTON));
        mediumViews.setTextViewText(R.id.appwidget_user, userName.isEmpty() ? "Set user" : userName);
        mediumViews.setTextViewText(R.id.appwidget_repo, repoName.isEmpty() ? "Set repo" : repoName);

        largeViews.setOnClickPendingIntent(R.id.appwidget_user,getPendingIntent(context, ACTION_USER));
        largeViews.setOnClickPendingIntent(R.id.appwidget_repo, getPendingIntent(context, ACTION_REPO));
        largeViews.setOnClickPendingIntent(R.id.appwidget_button, getPendingIntent(context, ACTION_BUTTON));
        largeViews.setOnClickPendingIntent(R.id.appwidget_settings, getPendingIntent(context, ACTION_SETTINGS));
        largeViews.setTextViewText(R.id.appwidget_user, userName.isEmpty() ? "Set user" : userName);
        largeViews.setTextViewText(R.id.appwidget_repo, repoName.isEmpty() ? "Set repo" : repoName);

        // switch widget layout flexibly
        Map<SizeF, RemoteViews> viewMapping = new ArrayMap<>();
        viewMapping.put(new SizeF(210f, 70f), smallViews);
        viewMapping.put(new SizeF(280f, 70f), mediumViews);
        viewMapping.put(new SizeF(350f, 70f), largeViews);

        views = new RemoteViews(viewMapping);

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
            } else if (Objects.equals(intent.getAction(), ACTION_SETTINGS)) {
                openMainActivity(context);
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

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        //update app widget
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }
}