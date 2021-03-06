package knf.animeflv.Utils;

import android.os.Environment;

import java.io.File;


public class Keys {
    public static class Dirs {
        public static final File LOGS = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/logs");
        public static final File SOUNDS = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/.sounds");
        public static final File CACHE = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache");
        public static final File CACHE_MINI = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/mini");
        public static final File CACHE_PORTADA = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/portada");
        public static final File CACHE_THUMBS = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/thumbs");
        public static final File DOWNLOADS = new File(Environment.getExternalStorageDirectory() + "/Animeflv/downloads");
        public static final File UPDATE = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache", "Animeflv_Nver.apk");
        public static final File SOUNDS_NOMEDIA = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/.sounds/.nomedia");
    }

    public static class Url {
        public static final String VERSION_INT = "https://raw.githubusercontent.com/jordyamc/Animeflv/master/app/version.html";
        public static final String UPDATE = "https://github.com/jordyamc/Animeflv/blob/master/app/app-release.apk?raw=true";
        public static final String SOUNDS = "https://raw.githubusercontent.com/jordyamc/Animeflv/master/app/sounds/";
        public static final String SOUNDS_JSON = "https://raw.githubusercontent.com/jordyamc/Animeflv/master/app/sounds.json";
        public static final String SOUNDS_JSON_BETA = "https://raw.githubusercontent.com/jordyamc/Animeflv/master/app/sounds-beta.json";
        public static final String ADMINS = "https://raw.githubusercontent.com/jordyamc/Animeflv/master/app/admins.json";
    }

    public static class Conf {
        public static final String SOUNDS = "sonido";
        public static final String INDICADOR_SONIDOS = "ind_sounds";
        public static final String RECHARGE_SOUNDS = "r_sounds";
    }

    public static class Login {
        public static final String EMAIL_NORMAL = "login_email";
        public static final String EMAIL_CODED = "login_email_coded";
        public static final String PASS_CODED = "login_pass_coded";
    }

    public static class Extra {
        public static final String JSON_ADMINS = "json_admins";
    }
}
