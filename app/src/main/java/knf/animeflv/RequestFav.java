package knf.animeflv;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Jordy on 22/08/2015.
 */
public class RequestFav extends AsyncTask<String,String,String> {
    InputStream is;
    String _response;
    callback call;
    TaskType taskType;
    Parser parser=new Parser();
    HttpURLConnection c = null;
    URL u;
    Activity context;
    MaterialDialog dialog;
    Boolean running;
    int prog = 0;
    List<String> aids;
    public RequestFav(Activity con, TaskType taskType, MaterialDialog d, List<String> aids) {
        call=(callback) con;
        this.context = con;
        this.taskType=taskType;
        this.dialog = d;
        this.aids=aids;
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

    public static String getStringFromFile(String filePath) {
        String ret = "";
        try {
            File fl = new File(filePath);
            FileInputStream fin = new FileInputStream(fl);
            ret = convertStreamToString(fin);
            fin.close();
        } catch (IOException e) {
        } catch (Exception e) {
        }
        return ret;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setContent("Actualizando Favoritos\n"+"("+prog+"/"+aids.size()+")");
            }
        });
        running = true;
    }

    @Override
    protected String doInBackground(String... params) {
        final List<String> list = new ArrayList<String>();
        for (final String i : aids) {
            final File file = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/" + i + ".txt");
            if (!file.exists() || !isJSONValid(getStringFromFile(file.getPath()))) {
                Log.d("Link", new Parser().getInicioUrl(TaskType.NORMAL, context) + "?url=" + parser.getUrlAnimeCached(i) + "&certificate=" + getCertificateSHA1Fingerprint());
                new SyncHttpClient().get(new Parser().getInicioUrl(TaskType.NORMAL, context) + "?url=" + parser.getUrlAnimeCached(i) + "&certificate=" + getCertificateSHA1Fingerprint(), null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        writeToFile(response.toString(), file);
                        list.add(parser.getTit(response.toString()));
                        updateDialog();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        File file1 = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/" + i + ".txt");
                        String file_loc = Environment.getExternalStorageDirectory() + "/Animeflv/cache/" + i + ".txt";
                        if (file1.exists()) {
                            list.add(parser.getTit(getStringFromFile(file_loc)));
                        }
                        updateDialog();
                    }
                });
            }else {
                Log.d("Link", "Loaded "+i);
                String file_loc = Environment.getExternalStorageDirectory() + "/Animeflv/cache/" + i + ".txt";
                if (file.exists()) {
                    list.add(parser.getTit(getStringFromFile(file_loc)));
                }
                updateDialog();
            }
        }
        String[] favoritos=new String[list.size()];
        list.toArray(favoritos);
        StringBuilder builder = new StringBuilder();
        for(String i : favoritos) {
            builder.append(":::" + i);
        }
        _response=builder.toString();
        return _response;
    }

    private void updateDialog(){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                prog++;
                dialog.setContent("Actualizando Favoritos\n"+"("+prog+"/"+aids.size()+")");
            }
        });
    }

    public  void writeToFile(String body,File file){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(body.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        call.favCall(s, taskType);
    }

    public interface callback {
        void favCall(String data, TaskType taskType);
    }
}
