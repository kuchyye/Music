package com.example.musicplayer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import android.content.Context;

public class PropretyBean {
    public static String[] THEMES;
    private static String DEFAULT_THEME; 
    private Context context;
    private String theme;
    
    public PropretyBean(Context context) {
		this.context = context;
		THEMES = context.getResources().getStringArray(R.array.theme);
		DEFAULT_THEME = THEMES[0];
		loadTheme();
	}
    
    private void loadTheme() {
		Properties properties = new Properties();
		try {
			FileInputStream  stream = context.openFileInput("configuration.cfg");
			properties.load(stream);
			theme = properties.getProperty("theme").toString();
		} catch (Exception e) {
			// TODO: handle exception
			saveTheme(DEFAULT_THEME);
		}
	}
    
    private boolean saveTheme(String theme){
    	Properties properties = new Properties();
    	properties.put("theme", theme);
    	try {
			FileOutputStream stream = context.openFileOutput("configuration.cfg", Context.MODE_WORLD_WRITEABLE);
			properties.store(stream, "");
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
    }
    
    public String getTheme(){
    	return theme;
    }
    
    public void  setAndSaveTheme(String theme) {
		this.theme = theme;
		saveTheme(theme);
	}
}
