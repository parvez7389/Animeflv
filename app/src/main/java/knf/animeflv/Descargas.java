package knf.animeflv;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nullwire.trace.ExceptionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import knf.animeflv.Recyclers.DownloadAdapterNew;


/**
 * Created by Jordy on 12/10/2015.
 */
public class Descargas extends AppCompatActivity implements RequestDownload.callback{
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<String> LepIDS;
    List<String> numeros;
    List<Long> Dids;
    List<String> Leids;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpAnimations();
        setContentView(R.layout.descargas);
        context = Descargas.this;
        ExceptionHandler.register(this, new Parser().getBaseUrl(TaskType.NORMAL, context) + "/errors/server.php");
        if (!isXLargeScreen(getApplicationContext())) { //set phones to portrait;
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
        toolbar=(Toolbar) findViewById(R.id.des_toolbar);
        recyclerView=(RecyclerView) findViewById(R.id.rv_descargas);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DESCARGAS");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_r);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String eids=getSharedPreferences("data", MODE_PRIVATE).getString("eids_descarga","");
                String epID=getSharedPreferences("data", MODE_PRIVATE).getString("epIDS_descarga", "");
                Leids= Arrays.asList(eids.split(":::"));
                LepIDS=Arrays.asList(epID.split(":::"));
                List<String> aids=new ArrayList<>();
                numeros=new ArrayList<>();
                Dids=new ArrayList<>();
                if (LepIDS.size()!=0&&!epID.trim().equals("")) {
                    for (String ep : LepIDS) {
                        String[] array = ep.split("_");
                        aids.add(array[0]);
                        numeros.add(array[1]);
                    }
                }else {
                    numeros.add("");
                }
                for (String lon:Leids){
                    try {
                        String id=getSharedPreferences("data", MODE_PRIVATE).getString(lon,"");
                        if (!id.equals(""))
                            Dids.add(Long.parseLong(id));
                    }catch (ClassCastException e){

                    }
                }
                String[] getTit=new String[aids.size()];
                aids.toArray(getTit);
                new RequestDownload(context,TaskType.CONTAR).execute(getTit);
            }
        }, 0, 1500);*/
        String eids = getSharedPreferences("data", MODE_PRIVATE).getString("eids_descarga", "");
        String epID = getSharedPreferences("data", MODE_PRIVATE).getString("epIDS_descarga", "");
        Leids = Arrays.asList(eids.split(":::"));
        LepIDS = Arrays.asList(epID.split(":::"));
        List<String> aids = new ArrayList<>();
        numeros = new ArrayList<>();
        Dids = new ArrayList<>();
        if (LepIDS.size() != 0 && !epID.trim().equals("")) {
            for (String ep : LepIDS) {
                String[] array = ep.split("_");
                aids.add(array[0]);
                numeros.add(array[1]);
            }
        } else {
            numeros.add("");
        }
        for (String lon : Leids) {
            try {
                String id = getSharedPreferences("data", MODE_PRIVATE).getString(lon, "");
                if (!id.equals(""))
                    Dids.add(Long.parseLong(id));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        String[] getTit = new String[aids.size()];
        aids.toArray(getTit);
        new RequestDownload(context, TaskType.CONTAR).execute(getTit);
    }
    public static boolean isXLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @TargetApi(21)
    public void setUpAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setEnterTransition(fade);

            Slide slide = new Slide();
            slide.setDuration(1000);
            getWindow().setReturnTransition(slide);
        }
    }
    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (!isXLargeScreen(getApplicationContext()) ) {
            return;
        }
    }

    @Override
    public void favCall(String data, TaskType taskType) {
        if (data.contains(":::")) {
            String[] tits = data.split(":::");
            final List<String> titulos = new ArrayList<>();
            for (String tit : tits) {
                if (!tit.equals("")) {
                    titulos.add(tit);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DownloadAdapterNew adapter = new DownloadAdapterNew(context, titulos, numeros, Dids, LepIDS, Leids);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }
            });
        }else {
            final List<String> titulosOff = new ArrayList<>();
            titulosOff.add("Sin Descargas");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DownloadAdapterNew adapter = new DownloadAdapterNew(context, titulosOff, numeros, Dids, LepIDS, Leids);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }
            });
        }
    }
}
