package com.proposeme.seven.phonecall.net;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Describe: Utilisez le framework Netty pour créer un client de transmission audio
 * Ce proxy écoute uniquement le port local, puis doit spécifier l'adresse IP et le port de l'autre partie lors de l'envoi de données
 */
public class NettyClient {

    private NettyReceiverHandler handler;
    private int port = BaseData.PORT;  // port d'ecoute

    private EventLoopGroup group;

    private static NettyClient sClient;


    private NettyClient() {
        init();
    }

    /**
     * Obtenir un objet client singleton
     * @return NettyClient
     */
    public static NettyClient getClient(){
        if (sClient == null){
            sClient = new NettyClient();
        }

        return sClient;
    }

    /**
     *  Enregistrer le rappel
     * @param callback Variable de rappel。
     */
    public void setFrameResultedCallback(NettyReceiverHandler.FrameResultedCallback callback) {
        if (handler != null){
            handler.setOnFrameCallback(callback);
        }
    }

    /**
     * Initialiser l'objet Netty。
     */
    private void init() {
        //Initialiser receiverHandler.
        handler = new NettyReceiverHandler();

        //Démarrez le client pour envoyer des données
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                group = new NioEventLoopGroup();
                try {
                    //Définir les propriétés de connexion de netty。
                    b.group(group)
                            .channel(NioDatagramChannel.class) //Connexion UDP asynchrone
                            .option(ChannelOption.SO_BROADCAST, true)
                            .option(ChannelOption.SO_RCVBUF, 1024 * 1024)//Tampon de 2 m dans la zone de réception
                            .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))//Avec cela, il y a la longueur maximale de réception et d'envoi
                            .handler(handler); //Configurer le processeur de données

                    b.bind(port).sync().channel().closeFuture().await();
                    //Construisez un proxy netty qui écoute le port local
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    group.shutdownGracefully();
                }
            }
        }).start();
    }


    /**
     *  Envoyer des données à l'adresse IP spécifiée，
     * @param targetIp IP cible
     * @param data Les données
     * @param msgType type de données
     */
    public void UserIPSendData(String targetIp, Object data, String msgType) {
        //C'est l'appel de la méthode NettyReceiverHandler.sendData (), c'est-à-dire qu'elle peut être dans NettyReceiverHandler
        //Autrement dit, le processus de réception des données traite également et envoie des données.
        handler.sendData(targetIp, port, data, msgType);
    }

    /**
     * Déconnecter
     * @return true Déconnexion réussie ou false Déconnexion échouée
     */
    public boolean DisConnect(){
        return  handler.DisConnect();
    }

    /**
     * Fermez gracieusement l'objet Netty。
     */
    public void shutDownBootstrap(){
        group.shutdownGracefully();
    }
}
