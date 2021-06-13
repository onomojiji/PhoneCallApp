package com.gyz.voipdemo_speex.util;

public class Speex {
    private static final int DEFAULT_COMPRESSION = 8;
    // Créer un objet, mode singleton
    private static final Speex speex = new Speex();
    //Bibliothèque de codecs ouverte
    public native int open(int compression);
    //Obtenez la taille du cadre
    public native int getFrameSize();
    //décodage
    public native int decode(byte encoded[], short lin[], int size);
    //codage
    public native int encode(short lin[], int offset, byte encoded[], int size);
    //Fermer la bibliothèque de codecs
    public native void close();

    private Speex() {

    }

    public static Speex getInstance() {
        return speex;
    }

    public void init() {
        load();//Charger le fichier .so
        open(DEFAULT_COMPRESSION);
    }

    private void load() {
        try {
            System.loadLibrary("speex");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
