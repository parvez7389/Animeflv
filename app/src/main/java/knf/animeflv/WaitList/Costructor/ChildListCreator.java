package knf.animeflv.WaitList.Costructor;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jordy on 31/03/2016.
 */
public class ChildListCreator {
    public static List<Float> getList(Context context, String aid) {
        String key = aid + "waiting";
        Set<String> set = context.getSharedPreferences("data", Context.MODE_PRIVATE).getStringSet(key, new HashSet<String>());
        List<Float> list = new ArrayList<>();
        for (String i : set) {
            try {
                list.add(Float.parseFloat(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(list);
        return list;
    }
}
