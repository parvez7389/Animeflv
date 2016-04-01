package knf.animeflv.Emision.Section;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import knf.animeflv.Application;
import knf.animeflv.ColorsRes;
import knf.animeflv.Emision.DateCompare;
import knf.animeflv.R;

/**
 * Created by Jordy on 05/03/2016.
 */
public class EmisionActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewPager viewPager;
    SmartTabLayout viewPagerTab;

    public static boolean isXLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int accent = preferences.getInt("accentColor", ColorsRes.Naranja(this));
        if (preferences.getBoolean("is_amoled", false)) {
            if (accent == ColorsRes.Rojo(this)) {
                setTheme(R.style.AppThemeDarkRojo);
            }
            if (accent == ColorsRes.Naranja(this)) {
                setTheme(R.style.AppThemeDarkNaranja);
            }
            if (accent == ColorsRes.Gris(this)) {
                setTheme(R.style.AppThemeDarkGris);
            }
            if (accent == ColorsRes.Verde(this)) {
                setTheme(R.style.AppThemeDarkVerde);
            }
            if (accent == ColorsRes.Rosa(this)) {
                setTheme(R.style.AppThemeDarkRosa);
            }
            if (accent == ColorsRes.Morado(this)) {
                setTheme(R.style.AppThemeDarkMorado);
            }
        } else {
            if (accent == ColorsRes.Rojo(this)) {
                setTheme(R.style.AppThemeRojo);
            }
            if (accent == ColorsRes.Naranja(this)) {
                setTheme(R.style.AppThemeNaranja);
            }
            if (accent == ColorsRes.Gris(this)) {
                setTheme(R.style.AppThemeGris);
            }
            if (accent == ColorsRes.Verde(this)) {
                setTheme(R.style.AppThemeVerde);
            }
            if (accent == ColorsRes.Rosa(this)) {
                setTheme(R.style.AppThemeRosa);
            }
            if (accent == ColorsRes.Morado(this)) {
                setTheme(R.style.AppThemeMorado);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emision);
        initActivity();
        Application application = (Application) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Emision");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        toolbar = (Toolbar) findViewById(R.id.emision_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Emision");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.vp_emision);
        viewPagerTab = (SmartTabLayout) findViewById(R.id.st_Emision);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("is_amoled", false)) {
            toolbar.setBackgroundColor(getResources().getColor(android.R.color.black));
            viewPagerTab.setBackgroundColor(getResources().getColor(android.R.color.black));
            viewPagerTab.setSelectedIndicatorColors(getResources().getColor(R.color.prim));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.negro));
                getWindow().setNavigationBarColor(getResources().getColor(R.color.negro));
            }
        }
        new Loader(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private int getActualDayCode() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int code;
        switch (day) {
            case Calendar.MONDAY:
                code = 1;
                break;
            case Calendar.TUESDAY:
                code = 2;
                break;
            case Calendar.WEDNESDAY:
                code = 3;
                break;
            case Calendar.THURSDAY:
                code = 4;
                break;
            case Calendar.FRIDAY:
                code = 5;
                break;
            case Calendar.SATURDAY:
                code = 6;
                break;
            case Calendar.SUNDAY:
                code = 7;
                break;
            default:
                code = 0;
                break;
        }
        return code;
    }

    private String UTCtoLocal(String utc) {
        String convert = "";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("~hh:mmaa", Locale.ENGLISH);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date myDate = simpleDateFormat.parse(utc);
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            convert = simpleDateFormat.format(myDate);
        } catch (Exception e) {
            e.printStackTrace();
            convert = utc + "-UTC--->" + e.getMessage();
        }
        return convert;
    }

    private void initActivity() {
        if (!isXLargeScreen(getApplicationContext())) { //Portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.dark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.prim));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (!isXLargeScreen(getApplicationContext())) {
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    private class Loader extends AsyncTask<String, String, String> {
        Context context;
        MaterialDialog dialog;

        public Loader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Actualizando lista...")
                    .build();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
            Set<String> ongoing = preferences.getStringSet("ongoingSet", new HashSet<String>());
            ArrayList<String> c1 = new ArrayList<>();
            ArrayList<String> c2 = new ArrayList<>();
            ArrayList<String> c3 = new ArrayList<>();
            ArrayList<String> c4 = new ArrayList<>();
            ArrayList<String> c5 = new ArrayList<>();
            ArrayList<String> c6 = new ArrayList<>();
            ArrayList<String> c7 = new ArrayList<>();
            List<ArrayList<String>> listas = new ArrayList<>();
            listas.add(c1);
            listas.add(c2);
            listas.add(c3);
            listas.add(c4);
            listas.add(c5);
            listas.add(c6);
            listas.add(c7);
            Log.d("Ongoing Size", "" + ongoing.size());
            if (!ongoing.isEmpty()) {
                List<TimeCompareModel> organizar = new ArrayList<>();
                for (String aid : ongoing) {
                    TimeCompareModel compareModel = new TimeCompareModel(aid, context);
                    if (!compareModel.getTime().equals("null"))
                        organizar.add(compareModel);
                }
                Collections.sort(organizar, new DateCompare());
                for (TimeCompareModel compareModel : organizar) {
                    int day = preferences.getInt(compareModel.getAid() + "onday", 0);
                    if (day != 0) {
                        boolean isotherday = compareModel.getTime().contains("AM") && UTCtoLocal(compareModel.getTime()).contains("PM");
                        if (!isotherday) {
                            listas.get(day - 1).add(compareModel.getAid());
                        } else {
                            int daybefore;
                            if (day - 2 <= -1) {
                                daybefore = 7;
                            } else {
                                daybefore = day - 2;
                            }
                            listas.get(daybefore).add(compareModel.getAid());
                        }
                    }
                }
            }
            Bundle code1 = new Bundle();
            code1.putInt("code", 1);
            code1.putStringArrayList("list", c1);
            Bundle code2 = new Bundle();
            code2.putInt("code", 2);
            code2.putStringArrayList("list", c2);
            Bundle code3 = new Bundle();
            code3.putInt("code", 3);
            code3.putStringArrayList("list", c3);
            Bundle code4 = new Bundle();
            code4.putInt("code", 4);
            code4.putStringArrayList("list", c4);
            Bundle code5 = new Bundle();
            code5.putInt("code", 5);
            code5.putStringArrayList("list", c5);
            Bundle code6 = new Bundle();
            code6.putInt("code", 6);
            code6.putStringArrayList("list", c6);
            Bundle code7 = new Bundle();
            code7.putInt("code", 7);
            code7.putStringArrayList("list", c7);
            final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                    getSupportFragmentManager(), FragmentPagerItems.with(context)
                    .add("LUNES", DayFragment.class, code1)
                    .add("MARTES", DayFragment.class, code2)
                    .add("MIERCOLES", DayFragment.class, code3)
                    .add("JUEVES", DayFragment.class, code4)
                    .add("VIERNES", DayFragment.class, code5)
                    .add("SABADO", DayFragment.class, code6)
                    .add("DOMINGO", DayFragment.class, code7)
                    .create());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewPager.setOffscreenPageLimit(7);
                    viewPager.setAdapter(adapter);
                    viewPagerTab.setViewPager(viewPager);
                    viewPager.setCurrentItem(Math.abs(getActualDayCode() - 1), true);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
        }
    }
}
