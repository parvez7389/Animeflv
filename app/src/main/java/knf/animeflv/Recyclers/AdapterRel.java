package knf.animeflv.Recyclers;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import knf.animeflv.ColorsRes;
import knf.animeflv.Parser;
import knf.animeflv.R;
import knf.animeflv.Utils.CacheManager;
import knf.animeflv.info.Helper.InfoHelper;
import knf.animeflv.info.InfoNewMaterial;

/**
 * Created by Jordy on 17/08/2015.
 */
public class AdapterRel extends RecyclerView.Adapter<AdapterRel.ViewHolder> {

    List<String> titulosCard;
    List<String> tiposCard;
    String[] url;
    String[] aids;
    private Activity context;
    public AdapterRel(Activity context, List<String> titulos, List<String> tipos, String[] urls, String[] aid) {
        this.context = context;
        this.titulosCard = titulos;
        this.tiposCard = tipos;
        this.url = urls;
        this.aids=aid;
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

    @Override
    public AdapterRel.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.item_anime_rel, parent, false);
        return new AdapterRel.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AdapterRel.ViewHolder holder, final int position) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_amoled", false)) {
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.prim));
            holder.tv_tit.setTextColor(context.getResources().getColor(R.color.blanco));
        }
        holder.tv_tipo.setTextColor(getColor());
        //PicassoCache.getPicassoInstance(context).load(new Parser().getBaseUrl(TaskType.NORMAL, context) + "imagen.php?certificate=" + getCertificateSHA1Fingerprint() + "&thumb=" + url[position]).error(R.drawable.ic_block_r).into(holder.iv_rel);
        new CacheManager().mini(context,aids[position],holder.iv_rel);
        holder.tv_tit.setText(titulosCard.get(position));
        holder.tv_tipo.setText(tiposCard.get(position));
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String file = Environment.getExternalStorageDirectory() + "/Animeflv/cache/directorio.txt";
                String json = getStringFromFile(file);
                String link = new Parser().getUrlFavs(json, aids[position]);
                InfoHelper.open(
                        ((InfoNewMaterial) context),
                        new InfoHelper.SharedItem(holder.iv_rel, "img"),
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        new InfoHelper.BundleItem("aid", aids[position]),
                        new InfoHelper.BundleItem("link", link),
                        new InfoHelper.BundleItem("title", titulosCard.get(position))
                );
            }
        });
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
    public int getItemCount() {
        return titulosCard.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_rel;
        public TextView tv_tit;
        public TextView tv_tipo;
        public CardView card;

        public ViewHolder(View itemView) {
            super(itemView);
            this.iv_rel = (ImageView) itemView.findViewById(R.id.imgCardInfoRel);
            this.tv_tit = (TextView) itemView.findViewById(R.id.tv_info_rel_tit);
            this.tv_tipo = (TextView) itemView.findViewById(R.id.tv_info_rel_tipo);
            this.card = (CardView) itemView.findViewById(R.id.cardRel);
        }
    }

}