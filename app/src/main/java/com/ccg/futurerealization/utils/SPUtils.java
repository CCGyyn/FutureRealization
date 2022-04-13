package com.ccg.futurerealization.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author：cgaopeng on 2021/10/14 21:51
 */
public class SPUtils {

    public static String getString(Context ctx, String key){
        SharedPreferences sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static int getInt(Context ctx, String key){
        SharedPreferences sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }

    public static boolean getBoolean(Context ctx, String key){
        SharedPreferences sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    public static long getLong(Context ctx, String key){
        SharedPreferences sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getLong(key, 0);
    }

    public static void put(Context ctx, String key, Object value){
        SharedPreferences sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        if(value instanceof String){
            edit.putString(key, (String) value);
        }else if( value instanceof Boolean){
            edit.putBoolean(key, (Boolean) value);
        }else if( value instanceof Integer){
            edit.putInt(key, (Integer) value);
        }
        edit.commit();
    }

    public static void remove(Context ctx, String key) {
        SharedPreferences sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(key);
        edit.commit();
    }

    public static void clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
