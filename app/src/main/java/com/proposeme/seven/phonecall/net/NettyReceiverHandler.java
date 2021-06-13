package com.proposeme.seven.phonecall.net;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Describe: Artistes qui envoient et reçoivent des données vocales。
 */
public class NettyReceiverHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private ChannelHandlerContext channelHandlerContext;
    //Enregistrement de l'interface de retour de données
    private FrameResultedCallback frameCallback;

    public void setOnFrameCallback(FrameResultedCallback callback) {
        this.frameCallback = callback;
    }

    //Démarrer le déclencheur lorsque les données sont reçues
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception {
        //Le serveur pousse l'IP et le PORT du pair
        ByteBuf buf = (ByteBuf) packet.copy().content(); //Tampon d'octets
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String str = new String(req, "UTF-8");
        Message message = JSON.parseObject(str,Message.class);  //Pas besoin de guider le colis dans la même catégorie

        //L'adresse IP de l'autre partie ne sera renvoyée que lors de l'envoi d'un SMS。
        //Correspondant à leurs rappels respectifs。
        if (message.getMsgtype().equals(Message.MES_TYPE_NORMAL)){
            if (frameCallback !=null){
                frameCallback.onTextMessage(message.getMsgBody());
                frameCallback.onGetRemoteIP(message.getMsgIp());
            }
        }else if (message.getMsgtype().equals(Message.MES_TYPE_AUDIO)){
            if (frameCallback !=null){
                frameCallback.onAudioData(message.getFrame());
            }
        }
    }

    //Déclenchement lorsque le canal est activé,
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channelHandlerContext = ctx;
        Log.e("ccc", "nettyReceiver démarre");
    }

    //Appelé lorsqu'une exception se produit
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.e("ccc", "La même manière se ferme anormalement ");
    }

    //Envoyez les données en fonction de l'adresse IP et du port transmis.
    public void sendData(String ip, int port, Object data, String type) {
        Message message = null;
        if (data instanceof byte[]) {
            message = new Message();
            message.setFrame((byte[]) data);
            message.setMsgtype(type);
            message.setTimestamp(System.currentTimeMillis());
        }else if (data instanceof String){
            //Lors de l'envoi de texte, l'adresse IP locale doit également être envoyée.
            message = new Message();
            message.setMsgBody((String) data);
            message.setMsgtype(type);
            message.setTimestamp(System.currentTimeMillis());
            message.setMsgIp(BaseData.LOCALHOST);
        }

        //Envoyer des données
        if (channelHandlerContext != null) {
            channelHandlerContext.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(JSON.toJSONString(message).getBytes()),
                    new InetSocketAddress(ip, port)));
        }
    }

    //Déconnectez-vous.
    public boolean DisConnect(){
        ChannelFuture disconnect = channelHandlerContext.disconnect();
        return disconnect.isDone();
    }


    // Rappel de données。
    public interface FrameResultedCallback {
        void onTextMessage(String msg); //Renvoyer les informations du texte
        void onAudioData(byte[] data);  //Renvoyer les informations audio
        void onGetRemoteIP(String ip);  //Renvoie l'adresse IP de l'autre partie uniquement reçue lors de l'envoi d'un message texte
    }
}
