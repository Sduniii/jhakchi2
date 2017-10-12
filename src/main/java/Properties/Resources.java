package Properties;

import javafx.scene.image.Image;

import java.util.ResourceBundle;

public class Resources {
    private static final Resources instance = new Resources();

    public static Resources getInstnance(){
        return instance;
    }

    private ResourceBundle bundle = ResourceBundle.getBundle("lang");

    public ResourceBundle getBundle(){
        return this.bundle;
    }

    private static Resources res = new Resources();
    public static Image blank_nes = getImage("/images/blank_nes.png");
    public static Image blank_jp = getImage("/images/blank_nes.png");
    public static Image blank_fds = getImage("/images/blank_nes.png");
    public static Image blank_snes_us = getImage("/images/blank_nes.png");
    public static Image blank_app = getImage("/images/blank_nes.png");
    public static Image blank_n64;
    public static Image blank_sms;
    public static Image blank_genesis;
    public static Image blank_32x;
    public static Image blank_gb;
    public static Image blank_gbc;
    public static Image blank_gba;
    public static Image blank_pce;
    public static Image blank_gg;
    public static Image blank_2600;
    public static Image blank_arcade;
    public static String PatchQ = getInstnance().getBundle().getString("PatchQ");
    public static String Ignore = getInstnance().getBundle().getString("Ignore");
    public static String MapperNotSupported = getInstnance().getBundle().getString("MapperNotSupported");
    public static String FourScreenNotSupported = getInstnance().getBundle().getString("FourScreenNotSupported");

    private static Image getImage(String s) {
        if (!s.startsWith("/")) s = "/" + s;
        //return null;
        return new Image(res.getClass().getResourceAsStream(s));
    }
}
