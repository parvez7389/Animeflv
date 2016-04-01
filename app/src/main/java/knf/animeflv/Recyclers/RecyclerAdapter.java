package knf.animeflv.Recyclers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import knf.animeflv.ColorsRes;
import knf.animeflv.DownloadManager.CookieConstructor;
import knf.animeflv.DownloadManager.ManageDownload;
import knf.animeflv.LoginServer;
import knf.animeflv.Parser;
import knf.animeflv.R;
import knf.animeflv.StreamManager.StreamManager;
import knf.animeflv.TaskType;
import knf.animeflv.Utils.FileUtil;

/**
 * Created by Jordy on 08/08/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    List<String> capitulo;
    String id;
    List<String> eids;
    String ext_storage_state = Environment.getExternalStorageState();
    Parser parser = new Parser();
    MaterialDialog dialog;
    MaterialDialog d;
    Boolean streaming = false;
    int posT;
    private Context context;

    public RecyclerAdapter(Context context, List<String> capitulos, String aid, List<String> eid) {
        this.capitulo = capitulos;
        this.context = context;
        this.id = aid;
        this.eids = eid;
    }

    public static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }

    public String getTit() {
        return context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("titInfo", "");
    }

    public String getNum(int position) {
        return capitulo.get(position).substring(capitulo.get(position).lastIndexOf(" ") + 1).trim();
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.item_anime_descarga, parent, false);
        return new RecyclerAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
        SetUpWeb(holder.web, holder);

        final String item = capitulo.get(position).replace("Capitulo ", "").trim();
        final File file = new File(Environment.getExternalStorageDirectory() + "/Animeflv/download/" + id + "/" + id + "_" + item + ".mp4");
        final File sd = new File(FileUtil.getSDPath() + "/Animeflv/download/" + id + "/" + id + "_" + item + ".mp4");
        if (file.exists() || sd.exists()) {
            holder.ib_des.setImageResource(R.drawable.ic_borrar_r);
        } else {
            holder.ib_ver.setImageResource(R.drawable.ic_ver_no);
            holder.ib_ver.setEnabled(false);
        }
        holder.tv_capitulo.setText(capitulo.get(position));
        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
        holder.tv_capitulo.setTextColor(context.getResources().getColor(R.color.black));
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_amoled", false)) {
            holder.card.setCardBackgroundColor(Color.parseColor("#212121"));
            holder.tv_capitulo.setTextColor(context.getResources().getColor(R.color.blanco));
            holder.ib_ver.setColorFilter(Color.argb(255, 255, 255, 255));
            holder.ib_des.setColorFilter(Color.argb(255, 255, 255, 255));
        }
        if (vistos) {
            holder.tv_capitulo.setTextColor(getColor());
        }
        holder.ib_des.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!file.exists() && !sd.exists()) {
                    if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("streaming", false)) {
                        new CheckDown(holder.web, holder.ib_des, holder.ib_ver, holder.tv_capitulo, holder.getAdapterPosition()).execute(new Parser().getUrlCached(id, item));
                    } else {
                        dialog = new MaterialDialog.Builder(context)
                                .title("Descargar?")
                                .titleGravity(GravityEnum.CENTER)
                                .content("Desea descargar el capitulo?")
                                .autoDismiss(false)
                                .cancelable(true)
                                .positiveText("DESCARGAR")
                                .negativeText("STREAMING")
                                .neutralText("ATRAS")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        materialDialog.dismiss();
                                        new CheckDown(holder.web, holder.ib_des, holder.ib_ver, holder.tv_capitulo, holder.getAdapterPosition()).execute(new Parser().getUrlCached(id, item));
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        materialDialog.dismiss();
                                        new CheckStream(holder.web, holder.tv_capitulo, holder.getAdapterPosition(), holder).execute(new Parser().getUrlCached(id, item));
                                    }
                                })
                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .build();
                        dialog.show();
                    }
                } else {
                    final String item = capitulo.get(holder.getAdapterPosition()).replace("Capitulo ", "").trim();
                    MaterialDialog borrar = new MaterialDialog.Builder(context)
                            .title("Eliminar")
                            .titleGravity(GravityEnum.CENTER)
                            .content("Desea eliminar el capitulo " + item + "?")
                            .positiveText("Eliminar")
                            .negativeText("Cancelar")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (file.delete()) {
                                        holder.ib_des.setImageResource(R.drawable.ic_get_r);
                                        holder.ib_ver.setImageResource(R.drawable.ic_ver_no);
                                        ManageDownload.cancel(context, eids.get(holder.getAdapterPosition()));
                                        Toast.makeText(context, "Archivo Eliminado", Toast.LENGTH_SHORT).show();
                                    }
                                    if (sd.delete()) {
                                        holder.ib_des.setImageResource(R.drawable.ic_get_r);
                                        holder.ib_ver.setImageResource(R.drawable.ic_ver_no);
                                        ManageDownload.cancel(context, eids.get(holder.getAdapterPosition()));
                                        Toast.makeText(context, "Archivo Eliminado", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    materialDialog.dismiss();
                                }
                            })
                            .build();
                    borrar.show();
                }
            }
        });
        holder.ib_ver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("t_video", "0"));
                if (file.exists()) {
                    if (type == 0) {
                        PlayIntbySrc(file, holder.getAdapterPosition(), holder);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
                        intent.setDataAndType(Uri.fromFile(file), "video/mp4");
                        context.startActivity(intent);
                    }
                } else {
                    if (sd.exists()) {
                        if (type == 0) {
                            PlayIntbySrc(file, holder.getAdapterPosition(), holder);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
                            intent.setDataAndType(Uri.fromFile(file), "video/mp4");
                            context.startActivity(intent);
                        }
                    } else {
                        Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = capitulo.get(holder.getAdapterPosition()).replace("Capitulo ", "").trim();
                Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("cambio", true).apply();
                if (!vistos) {
                    context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("visto" + id + "_" + item, true).apply();
                    holder.tv_capitulo.setTextColor(getColor());
                } else {
                    context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("visto" + id + "_" + item, false).apply();
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_amoled", false)) {
                        holder.tv_capitulo.setTextColor(context.getResources().getColor(R.color.blanco));
                    } else {
                        holder.tv_capitulo.setTextColor(context.getResources().getColor(R.color.black));
                    }
                }
            }
        });
    }

    public String getSD1() {
        String sSDpath = null;
        File fileCur = null;
        for (String sPathCur : Arrays.asList("MicroSD", "external_SD", "sdcard1", "ext_card", "external_sd", "ext_sd", "external", "extSdCard", "externalSdCard")) {
            fileCur = new File("/mnt/", sPathCur);
            if (fileCur.isDirectory() && fileCur.canWrite()) {
                sSDpath = fileCur.getAbsolutePath();
                break;
            }
            if (sSDpath == null) {
                fileCur = new File("/storage/", sPathCur);
                if (fileCur.isDirectory() && fileCur.canWrite()) {
                    sSDpath = fileCur.getAbsolutePath();
                    break;
                }
            }
            if (sSDpath == null) {
                fileCur = new File("/storage/emulated", sPathCur);
                if (fileCur.isDirectory() && fileCur.canWrite()) {
                    sSDpath = fileCur.getAbsolutePath();
                    Log.e("path", sSDpath);
                    break;
                }
            }
        }
        return sSDpath;
    }

    @Override
    public int getItemCount() {
        return capitulo.size();
    }

    public void DescargarSD(String url, ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position) {
        ManageDownload.external(context).startDownload(eids.get(position), url);
        ib_des.setImageResource(R.drawable.ic_borrar_r);
        ib_ver.setImageResource(R.drawable.ic_rep_r);
        ib_ver.setEnabled(true);
        String item = capitulo.get(position).replace("Capitulo ", "").trim();
        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
        if (!vistos) {
            tv_capitulo.setTextColor(getColor());
        }
    }

    public void DescargarSD(String url, ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position, CookieConstructor constructor) {
        ManageDownload.external(context).startDownload(eids.get(position), url, constructor);
        ib_des.setImageResource(R.drawable.ic_borrar_r);
        ib_ver.setImageResource(R.drawable.ic_rep_r);
        ib_ver.setEnabled(true);
        String item = capitulo.get(position).replace("Capitulo ", "").trim();
        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
        if (!vistos) {
            tv_capitulo.setTextColor(getColor());
        }
    }

    public void DownloadByUrl(String url, ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position) {
        ManageDownload.internal(context).startDownload(eids.get(position), url);
        ib_des.setImageResource(R.drawable.ic_borrar_r);
        ib_ver.setImageResource(R.drawable.ic_rep_r);
        ib_ver.setEnabled(true);
        String item = capitulo.get(position).replace("Capitulo ", "").trim();
        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
        if (!vistos) {
            tv_capitulo.setTextColor(getColor());
        }
    }

    public void DownloadByUrl(String url, ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position, CookieConstructor constructor) {
        ManageDownload.internal(context).startDownload(eids.get(position), url, constructor);
        ib_des.setImageResource(R.drawable.ic_borrar_r);
        ib_ver.setImageResource(R.drawable.ic_rep_r);
        ib_ver.setEnabled(true);
        String item = capitulo.get(position).replace("Capitulo ", "").trim();
        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
        if (!vistos) {
            tv_capitulo.setTextColor(getColor());
        }
    }

    private int getColor() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int accent = preferences.getInt("accentColor", ColorsRes.Naranja(context));
        int color = ColorsRes.Naranja(context);
        if (accent == ColorsRes.Rojo(context)) {
            color = ColorsRes.Rojo(context);
        }
        if (accent == ColorsRes.Naranja(context)) {
            color = ColorsRes.Naranja(context);
        }
        if (accent == ColorsRes.Gris(context)) {
            color = ColorsRes.Gris(context);
        }
        if (accent == ColorsRes.Verde(context)) {
            color = ColorsRes.Verde(context);
        }
        if (accent == ColorsRes.Rosa(context)) {
            color = ColorsRes.Rosa(context);
        }
        if (accent == ColorsRes.Morado(context)) {
            color = ColorsRes.Morado(context);
        }
        return color;
    }

    public void chooseDownDir(String url, ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position) {
        Boolean inSD = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("sd_down", false);
        if (inSD) {
            DescargarSD(url, ib_des, ib_ver, tv_capitulo, position);
        } else {
            DownloadByUrl(url, ib_des, ib_ver, tv_capitulo, position);
        }
    }

    public void chooseDownDir(String url, ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position, CookieConstructor constructor) {
        Boolean inSD = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("sd_down", false);
        if (inSD) {
            DescargarSD(url, ib_des, ib_ver, tv_capitulo, position, constructor);
        } else {
            DownloadByUrl(url, ib_des, ib_ver, tv_capitulo, position, constructor);
        }
    }

    public void SetUpWeb(final WebView web, final RecyclerAdapter.ViewHolder holder) {
        web.getSettings().setJavaScriptEnabled(true);
        CookieSyncManager.createInstance(context);
        CookieSyncManager.getInstance().startSync();
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("zippyshare.com") || url.contains("blank")) {
                    context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("urlD", url).apply();
                    web.loadUrl("javascript:("
                            + "function(){var l=document.getElementById('dlbutton');" + "var f=document.createEvent('HTMLEvents');" + "f.initEvent('click',true,true);" + "l.dispatchEvent(f);}"
                            + ")()");
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        web.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                web.loadUrl("about:blank");
                if (!streaming) {
                    File Dstorage = new File(Environment.getExternalStorageDirectory() + "/Animeflv/download/" + url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("_")));
                    if (ext_storage_state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
                        if (!Dstorage.exists()) {
                            Dstorage.mkdirs();
                        }
                    }
                    File archivo = new File(Environment.getExternalStorageDirectory() + "/Animeflv/download/" + url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("_")) + "/" + fileName);
                    if (!archivo.exists()) {
                        String item = capitulo.get(holder.getAdapterPosition()).replace("Capitulo ", "").trim();
                        String urlD = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("urlD", null);
                        CookieManager cookieManager = CookieManager.getInstance();
                        String cookie = cookieManager.getCookie(url.substring(0, url.indexOf("/", 8)));
                        CookieConstructor constructor = new CookieConstructor(cookie, web.getSettings().getUserAgentString(), urlD);
                        chooseDownDir(url, holder.ib_des, holder.ib_ver, holder.tv_capitulo, holder.getAdapterPosition(), constructor);
                        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
                        if (!vistos) {
                            holder.tv_capitulo.setTextColor(getColor());
                        }
                    } else {
                        Toast.makeText(context, "El archivo ya existe", Toast.LENGTH_SHORT).show();
                    }
                    d.dismiss();

                } else {
                    int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("t_streaming", "0"));
                    String urlD = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("urlD", null);
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookie = cookieManager.getCookie(url.substring(0, url.indexOf("/", 8)));
                    streaming = false;
                    web.loadUrl("about:blank");
                    CookieConstructor constructor = new CookieConstructor(cookie, web.getSettings().getUserAgentString(), urlD);
                    if (type == 1) {
                        StreamManager.mx(context).Stream(eids.get(holder.getAdapterPosition()), url, constructor);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            StreamManager.internal(context).Stream(eids.get(holder.getAdapterPosition()), url, constructor);
                        } else {
                            if (isMXinstalled()) {
                                toast("Version de android por debajo de lo requerido, reproduciendo en MXPlayer");
                                StreamManager.mx(context).Stream(eids.get(holder.getAdapterPosition()), url, constructor);
                            } else {
                                toast("No hay reproductor adecuado disponible");
                            }
                        }
                    }
                }
            }
        });
    }

    public boolean isMXinstalled() {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        String pack = "null";
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.mxtech.videoplayer.pro")) {
                pack = "com.mxtech.videoplayer.pro";
                break;
            }
            if (packageInfo.packageName.equals("com.mxtech.videoplayer.ad")) {
                pack = "com.mxtech.videoplayer.ad";
                break;
            }
        }
        return !pack.equals("null");
    }

    private String getCertificateSHA1Fingerprint() {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        InputStream input = new ByteArrayInputStream(cert);
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        String hexString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return hexString;
    }

    public void toast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public void StreamInbyURL(int position, String url, RecyclerAdapter.ViewHolder holder) {
        if (isNetworkAvailable()) {
            int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("t_streaming", "0"));
            Log.d("Streaming", PreferenceManager.getDefaultSharedPreferences(context).getString("t_streaming", "0"));
            switch (type) {
                case 0:
                    StreamingIntbyUrl(position, url, holder);
                    break;
                case 1:
                    StreamingExtbyURL(position, url);
                    break;
            }
        } else {
            toast("No hay conexion a internet");
        }
    }

    public void StreamingExtbyURL(int position, String url) {
        Intent i = (new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setType("application/mp4"));
        PackageManager pm = context.getPackageManager();
        final ResolveInfo mInfo = pm.resolveActivity(i, 0);
        if (mInfo != null) {
            String id = mInfo.activityInfo.applicationInfo.processName;
            if (id.startsWith("com.mxtech.videoplayer")) {
                StreamMXbyURL(position, url);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public void StreamingIntbyUrl(int position, String url, RecyclerAdapter.ViewHolder holder) {
        Intent interno = parser.getPrefIntPlayer(context);
        interno.putExtra("url", url);
        interno.putExtra("title", getTit() + " " + getNum(position));
        interno.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(interno);
        final String email_coded = PreferenceManager.getDefaultSharedPreferences(context).getString("login_email_coded", "null");
        final String pass_coded = PreferenceManager.getDefaultSharedPreferences(context).getString("login_pass_coded", "null");
        String item = capitulo.get(holder.getAdapterPosition()).substring(capitulo.get(holder.getAdapterPosition()).lastIndexOf(" ") + 1).trim();
        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
        if (!vistos) {
            context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("visto" + id + "_" + item, true).apply();
            String Svistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("vistos", "");
            Svistos = Svistos + ";;;" + "visto" + id + "_" + item;
            context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("vistos", Svistos).apply();
            String favoritos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("favoritos", "");
            if (!email_coded.equals("null") && !email_coded.equals("null")) {
                new LoginServer(context, TaskType.GET_FAV_SL, null, null, null, null).execute(parser.getBaseUrl(TaskType.NORMAL, context) + "fav-server.php?tipo=refresh&email_coded=" + email_coded + "&pass_coded=" + pass_coded + "&new_favs=" + favoritos + ":;:" + Svistos);
            }
            holder.tv_capitulo.setTextColor(getColor());
        }
    }

    public void PlayIntbySrc(File file, int position, RecyclerAdapter.ViewHolder holder) {
        Intent interno = parser.getPrefIntPlayer(context);
        interno.putExtra("file", file.getAbsolutePath());
        interno.putExtra("title", getTit() + " " + getNum(position));
        interno.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(interno);
        final String email_coded = PreferenceManager.getDefaultSharedPreferences(context).getString("login_email_coded", "null");
        final String pass_coded = PreferenceManager.getDefaultSharedPreferences(context).getString("login_pass_coded", "null");
        String item = capitulo.get(holder.getAdapterPosition()).substring(capitulo.get(holder.getAdapterPosition()).lastIndexOf(" ") + 1).trim();
        Boolean vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("visto" + id + "_" + item, false);
        if (!vistos) {
            context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("visto" + id + "_" + item, true).apply();
            String Svistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("vistos", "");
            Svistos = Svistos + ";;;" + "visto" + id + "_" + item;
            context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("vistos", Svistos).apply();
            String favoritos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("favoritos", "");
            if (!email_coded.equals("null") && !email_coded.equals("null")) {
                new LoginServer(context, TaskType.GET_FAV_SL, null, null, null, null).execute(parser.getBaseUrl(TaskType.NORMAL, context) + "fav-server.php?tipo=refresh&email_coded=" + email_coded + "&pass_coded=" + pass_coded + "&new_favs=" + favoritos + ":;:" + Svistos);
            }
            holder.tv_capitulo.setTextColor(getColor());
        }
    }

    public void StreamMXbyURL(int position, String url) {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        String pack = "null";
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.mxtech.videoplayer.pro")) {
                pack = "com.mxtech.videoplayer.pro";
                break;
            }
            if (packageInfo.packageName.equals("com.mxtech.videoplayer.ad")) {
                pack = "com.mxtech.videoplayer.ad";
                break;
            }
        }
        switch (pack) {
            case "com.mxtech.videoplayer.pro":
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri videoUri = Uri.parse(url);
                intent.setDataAndType(videoUri, "application/mp4");
                intent.setPackage("com.mxtech.videoplayer.pro");
                intent.putExtra("title", getTit() + " " + getNum(position));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("visto" + id + "_" + getNum(position), true).apply();
                String vistos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("vistos", "");
                if (!vistos.contains(eids.get(position).trim())) {
                    vistos = vistos + eids.get(position).trim() + ":::";
                    context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("vistos", vistos).apply();
                }
                break;
            case "com.mxtech.videoplayer.ad":
                Intent intentad = new Intent(Intent.ACTION_VIEW);
                Uri videoUriad = Uri.parse(url);
                intentad.setDataAndType(videoUriad, "application/mp4");
                intentad.setPackage("com.mxtech.videoplayer.ad");
                intentad.putExtra("title", getTit() + " " + getNum(position));
                intentad.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentad);
                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putBoolean("visto" + id + "_" + getNum(position), true).apply();
                String vistosad = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("vistos", "");
                if (!vistosad.contains(eids.get(position).trim())) {
                    vistosad = vistosad + eids.get(position).trim() + ":::";
                    context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("vistos", vistosad).apply();
                }
                break;
            default:
                toast("MX player no instalado");
                break;
        }
    }

    private boolean isNetworkAvailable() {
        Boolean net = false;
        int Tcon = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("t_conexion", "0"));
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        switch (Tcon) {
            case 0:
                NetworkInfo Wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                net = Wifi.isConnected();
                break;
            case 1:
                NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                net = mobile.isConnected();
                break;
            case 2:
                NetworkInfo WifiA = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mobileA = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                net = WifiA.isConnected() || mobileA.isConnected();
                break;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && net;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_capitulo;
        public ImageButton ib_ver;
        public ImageButton ib_des;
        public CardView card;
        public RecyclerView recyclerView;
        public WebView web;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_capitulo = (TextView) itemView.findViewById(R.id.tv_cardD_capitulo);
            this.ib_ver = (ImageButton) itemView.findViewById(R.id.ib_ver_rv);
            this.ib_des = (ImageButton) itemView.findViewById(R.id.ib_descargar_rv);
            this.card = (CardView) itemView.findViewById(R.id.card_descargas_info);
            this.web = (WebView) itemView.findViewById(R.id.wv_anime_zippy);
        }
    }

    public class CheckDown extends AsyncTask<String, String, String> {
        ImageButton des;
        ImageButton ver;
        WebView web;
        TextView cap;
        int pos;
        String _response;
        Spinner sp;

        public CheckDown(WebView w, ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position) {
            this.des = ib_des;
            this.ver = ib_ver;
            this.cap = tv_capitulo;
            this.pos = position;
            this.web = w;
        }

        @Override
        protected String doInBackground(String... params) {
            new SyncHttpClient().get(new Parser().getInicioUrl(TaskType.NORMAL, context) + "?certificate=" + getCertificateSHA1Fingerprint() + "&url=" + params[0], null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    _response = response.toString();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    super.onSuccess(statusCode, headers, responseString);
                    _response = responseString;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    _response = "error";
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    _response = "error";
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    _response = "error";
                }
            });
            return _response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("error")) {
                dialog.dismiss();
                Toast.makeText(context, "Error en servidor", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s.trim());
                    JSONArray jsonArray = jsonObject.getJSONArray("downloads");
                    final List<String> nombres = new ArrayList<>();
                    final List<String> urls = new ArrayList<>();
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String u = object.getString("url");
                            if (!u.trim().equals("null")) {
                                nombres.add(object.getString("name"));
                                urls.add(u);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    d = new MaterialDialog.Builder(context)
                            .title("Opciones")
                            .titleGravity(GravityEnum.CENTER)
                            .customView(R.layout.dialog_down, false)
                            .cancelable(true)
                            .autoDismiss(false)
                            .positiveText("Descargar")
                            .negativeText("Cancelar")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
                                    String desc = nombres.get(sp.getSelectedItemPosition());
                                    final String ur = urls.get(sp.getSelectedItemPosition());
                                    Log.d("Descargar", "URL -> " + ur);
                                    switch (desc.toLowerCase()) {
                                        case "izanagi":
                                            new Izanagi(des, ver, cap, pos).execute(ur);
                                            d.dismiss();
                                            break;
                                        case "zippyshare":
                                            web.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    web.loadUrl(ur);
                                                }
                                            });
                                            d.dismiss();
                                            break;
                                        case "mega":
                                            d.dismiss();
                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ur)));
                                            break;
                                        default:
                                            chooseDownDir(ur, des, ver, cap, pos);
                                            d.dismiss();
                                            break;
                                    }
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    materialDialog.dismiss();
                                }
                            })
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    d.dismiss();
                                }
                            })
                            .build();
                    sp = (Spinner) d.getCustomView().findViewById(R.id.spinner_down);
                    sp.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nombres));
                    d.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error en JSON", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class CheckStream extends AsyncTask<String, String, String> {
        WebView web;
        TextView cap;
        int pos;
        String _response;
        Spinner sp;
        RecyclerAdapter.ViewHolder holder;
        public CheckStream(WebView w, TextView tv_capitulo, int position, RecyclerAdapter.ViewHolder holder) {
            this.cap = tv_capitulo;
            this.pos = position;
            this.web = w;
            this.holder = holder;
        }

        @Override
        protected String doInBackground(String... params) {
            new SyncHttpClient().get(new Parser().getInicioUrl(TaskType.NORMAL, context) + "?certificate=" + getCertificateSHA1Fingerprint() + "&url=" + params[0], null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    _response = response.toString();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    super.onSuccess(statusCode, headers, responseString);
                    _response = responseString;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    _response = "error";
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    _response = "error";
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    _response = "error";
                }
            });
            return _response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("error")) {
                dialog.dismiss();
                Toast.makeText(context, "Error en servidor", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s.trim());
                    JSONArray jsonArray = jsonObject.getJSONArray("downloads");
                    final List<String> nombres = new ArrayList<>();
                    final List<String> urls = new ArrayList<>();
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String u = object.getString("url");
                            if (!u.trim().equals("null")) {
                                nombres.add(object.getString("name"));
                                urls.add(u);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    d = new MaterialDialog.Builder(context)
                            .title("Opciones")
                            .titleGravity(GravityEnum.CENTER)
                            .customView(R.layout.dialog_down, false)
                            .cancelable(true)
                            .autoDismiss(false)
                            .positiveText("Reproducir")
                            .negativeText("Cancelar")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
                                    String desc = nombres.get(sp.getSelectedItemPosition());
                                    final String ur = urls.get(sp.getSelectedItemPosition());
                                    Log.d("Streaming", "URL -> " + ur);
                                    switch (desc.toLowerCase()) {
                                        case "izanagi":
                                            new IzanagiStream(cap, pos, holder).execute(ur);
                                            d.dismiss();
                                            break;
                                        case "zippyshare":
                                            streaming = true;
                                            posT = pos;
                                            web.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    web.loadUrl(ur);
                                                }
                                            });
                                            d.dismiss();
                                            break;
                                        case "mega":
                                            d.dismiss();
                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ur)));
                                            break;
                                        default:
                                            StreamInbyURL(pos, ur, holder);
                                            d.dismiss();
                                            break;
                                    }
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    materialDialog.dismiss();
                                }
                            })
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    d.dismiss();
                                }
                            })
                            .build();
                    sp = (Spinner) d.getCustomView().findViewById(R.id.spinner_down);
                    sp.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nombres));
                    d.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error en JSON", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class Izanagi extends AsyncTask<String, String, String> {
        ImageButton des;
        ImageButton ver;
        TextView cap;
        int pos;
        String _response;
        public Izanagi(ImageButton ib_des, ImageButton ib_ver, TextView tv_capitulo, int position) {
            this.des = ib_des;
            this.ver = ib_ver;
            this.cap = tv_capitulo;
            this.pos = position;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection c = null;
            try {
                URL u = new URL(params[0]);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestProperty("Content-length", "0");
                c.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                c.setRequestProperty("Accept", "*/*");
                c.setInstanceFollowRedirects(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setAllowUserInteraction(false);
                c.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                //c.disconnect();
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                _response = sb.toString();
                //String fullPage = page.asXml();
            } catch (Exception e) {
                Log.e("Requests", "Error in http connection " + e.toString());
                _response = "error";
            }
            return _response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String furl = s.substring(s.indexOf("URL=") + 4, s.lastIndexOf("\">"));
            chooseDownDir(furl, des, ver, cap, pos);
        }
    }

    public class IzanagiStream extends AsyncTask<String, String, String> {
        ImageButton des;
        ImageButton ver;
        TextView cap;
        int pos;
        String _response;
        RecyclerAdapter.ViewHolder holder;

        public IzanagiStream(TextView tv_capitulo, int position, RecyclerAdapter.ViewHolder holder) {
            this.cap = tv_capitulo;
            this.pos = position;
            this.holder = holder;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection c = null;
            try {
                URL u = new URL(params[0]);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestProperty("Content-length", "0");
                c.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                c.setRequestProperty("Accept", "*/*");
                c.setInstanceFollowRedirects(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setAllowUserInteraction(false);
                c.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                //c.disconnect();
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                _response = sb.toString();
                //String fullPage = page.asXml();
            } catch (Exception e) {
                Log.e("Requests", "Error in http connection " + e.toString());
                _response = "error";
            }
            return _response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String furl = s.substring(s.indexOf("URL=") + 4, s.lastIndexOf("\">"));
            StreamInbyURL(pos, furl, holder);
        }
    }
}
