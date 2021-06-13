package com.proposeme.seven.phonecall.net;

/**
 * Describe: Enregistrez la signalisation interactive après avoir passé un appel
 */
public class BaseData {

    // control text
    public static final Integer PHONE_MAKE_CALL = 100; //composer le numéro
    public static final Integer PHONE_ANSWER_CALL = 200; //repond au telephone
    public static final Integer PHONE_CALL_END = 300; //appel terminé

    // localhost
    public static String LOCALHOST = "127.0.0.1"; // L'adresse IP de cette machine doit être envoyée lors de l'envoi de données texte.

    // port
    public static final int PORT = 7777; // Port d'écoute。

    // isFromService
    public static final String IFS = "IFS";
}
