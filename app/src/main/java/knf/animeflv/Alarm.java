package knf.animeflv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        String url = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("dir_inicio", "http://animeflvapps.x10.mx/getHtml.php");//new Parser().getInicioUrl(TaskType.NORMAL, context);
        Boolean not= PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notificaciones",true);
        if (not) {
            int corePoolSize = 60;
            int maximumPoolSize = 80;
            int keepAliveTime = 10;
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
            Executor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
            Log.d("Service", "Servicio Iniciado");
            new RequestsBackground(context, TaskType.NOT).executeOnExecutor(threadPoolExecutor);
            new RequestsBackground(context,TaskType.VERSION).executeOnExecutor(threadPoolExecutor);
        }else {Log.d("Service", "Servicio Desactivado");}
        wl.release();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("knf.animeflv.START_ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        int time=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("tiempo", "60000"));
        Log.d("Timer",Integer.toString(time));
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pi); //1000*60*10
    }
    public void SetAlarm(Context context,int tiempo)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("knf.animeflv.START_ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        Log.d("Timer",Integer.toString(tiempo));
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), tiempo, pi); //1000*60*10
    }

    public void CancelAlarm(Context context)
    {
        Log.d("Alarma","Cancelado");
        Intent intent = new Intent("knf.animeflv.START_ALARM");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
