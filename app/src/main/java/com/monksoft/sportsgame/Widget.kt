package com.monksoft.sportsgame

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.monksoft.sportsgame.MainActivity.Companion.chronoWidget
import com.monksoft.sportsgame.MainActivity.Companion.distanceWidget

class Widget : AppWidgetProvider() {

    override fun onUpdate(context: Context?,appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        if (appWidgetIds != null && appWidgetManager !=null && context != null ){
            for (appWidgetId in appWidgetIds){

                val views = RemoteViews(context.packageName, R.layout.widget)

                views.setTextViewText(R.id.tvWidgetChrono, chronoWidget)
                views.setTextViewText(R.id.tvWidgetDistance, distanceWidget)

                val iLogin : PendingIntent = Intent(context, LoginActivity::class.java).let{ intent->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }
                views.apply{ setOnClickPendingIntent(R.id.ivUser, iLogin)}

                val iMain : PendingIntent = Intent(context,MainActivity::class.java).let { intent ->
                    PendingIntent.getActivity(context,0, intent, 0)  }
                views.apply{ setOnClickPendingIntent(R.id.ivRun,iMain)}

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}