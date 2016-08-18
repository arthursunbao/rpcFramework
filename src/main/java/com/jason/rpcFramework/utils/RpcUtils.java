package com.jason.rpcFramework.utils;



import com.jason.rpcFramework.Exception.RpcException;
import com.jason.rpcFramework.Exception.RpcNetExceptionHandler;
import com.jason.rpcFramework.RpcObject;
import javafx.scene.chart.PieChart;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Common Utilties Classes
 * Created by baosun on 7/29/2016.
 */
public class RpcUtils {

    private static Logger logger = Logger.getLogger(RpcUtils.class);
    private static Map<String, Method> methodCache = new HashMap<>();

    public static final int MEM_8KB = 1024 * 8;
    public static final int MEM_16KB = MEM_8KB * 2;
    public static final int MEM_32KB = MEM_16KB * 2;
    public static final int MEM_64KB = MEM_32KB * 2;
    public static final int MEM_128KB = MEM_64KB * 2;
    public static final int MEM_256KB = MEM_128KB * 2;
    public static final int MEM_512KB = MEM_256KB * 2;
    public static final int MEM_1M = MEM_512KB * 2;
    public static final String version = "0.0.1";

    /**
     * Get Local IPV4 Address
     * @return
     */
    public static final List<String> getLocalV4IPs(){
        List<String> ips = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while(interfaces.hasMoreElements()){
                NetworkInterface ni = interfaces.nextElement();
                String name = ni.getDisplayName();
                if(!ni.isLoopback())&&!ni.isVirtual()&&ni.isUp()){
                    if(name==null||!name.contains("Loopback")){
                        Enumeration<InetAddress> addresses = ni.getInetAddresses();
                        while(addresses.hasMoreElements()){
                            InetAddress address = addresses.nextElement();
                            String ip = address.getHostAddress();
                            if(!ip.contains(":")){
                                ips.add(ip);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("localips error",e);
        }
        return ips;
    }

    public static final String chooseIP(List<String> ips){
        Collections.sort(ips);;
        String ip = "127.0.0.1";
        if(ips != null){
            for(String chosenIP : ips){
                if(chosenIP.startsWith("127.")){

                }
                else if(chosenIP.startsWith("192.168")){
                    if(ip.startsWith("127.")){
                        ip = chosenIP;
                    }
                    else if(ip.startsWith("192.168") && ip.endsWith("1.1")){
                        ip = chosenIP;
                    }
                    if(!chosenIP.startsWith("192.168") && chosenIP.endsWith(".1")) {
                        ip = chosenIP;
                    }
                }
                else if(chosenIP.startsWith("10.")){
                    if(ip.startsWith("127.")){
                        ip = chosenIP;
                    }else if(ip.startsWith("10.")&&ip.endsWith(".1")){
                        ip = chosenIP;
                    }
                    if(!chosenIP.startsWith("10.")&&!chosenIP.endsWith(".1")){
                        ip = chosenIP;
                    }
                }else{
                    ip = chosenIP;
                }
            }
        }
        return ip;
    }

    public static void writeRpc(RpcObject rpc, OutputStream outputStream, RpcNetExceptionHandler handler){
        try{
            outputStream.write(rpc.getType().getType());
            outputStream.write(RpcUtils.longToBytes(rpc.getThreadId()));
            outputStream.write(RpcUtils.intToBytes(rpc.getIndex()));
            outputStream.write(RpcUtils.intToBytes(rpc.getLength()));
            if(rpc.getLength() > 0){
                if(rpc.getLength() > MEM_1M){
                    throw new RpcException("The Rpc Message is too long" + rpc.getLength() + " Please consider to compress your message");
                }
                else if(rpc.getLength() < MEM_1M && rpc.getLength() > MEM_512KB){
                    System.out.println("The message is relatively large, please consider to compress the module" + rpc.getLength() + " ");
                    //TO-DO: Adding Compression Module to compress the message
                    outputStream.write(rpc.getData());
                }
                outputStream.write(rpc.getData());
            }
        }
        catch(IOException e){
            handleNetException(e, handler);
        }
    }

    private static void handleNetException(Exception e, RpcNetExceptionHandler handler){
        if(handler != null){
            handler.handleNetException(e);
        }
        else{
            throw new RpcException(e);
        }
    }

    public static String genAddressString(String prefix, InetSocketAddress address){
        StringBuffer sb = new StringBuffer();
        sb.append(prefix);
        sb.append(address.getAddress().getHostAddress());
        sb.append(":");
        sb.append(address.getPort());
        sb.append(";");
        return sb.toString();
    }

    public static void writeDataRpc(RpcObject rpc, DataOutputStream outputStream, RpcNetExceptionHandler handler){
        try{
            outputStream.writeInt(rpc.getType().getType());
            outputStream.writeLong(rpc.getThreadId());
            outputStream.writeInt(rpc.getIndex());
            outputStream.writeInt(rpc.getLength());
            if(rpc.getLength() > 0){
                if(rpc.getLength() > MEM_1M){
                    throw new RpcException("The Rpc Message is too long" + rpc.getLength() + " Please consider to compress your message");
                }
                else if(rpc.getLength() < MEM_1M && rpc.getLength() > MEM_512KB){
                    System.out.println("The message is relatively large, please consider to compress the module" + rpc.getLength() + " ");
                    //TO-DO: Adding Compression Module to compress the message
                    outputStream.write(rpc.getData());
                }
                outputStream.write(rpc.getData());
            }
            outputStream.flush();
        }
        catch(IOException e){
            handleNetException(e, handler);
        }
    }

    public static RpcObject readRpc(InputStream inputstream, byte[] buffer, RpcNetExceptionHandler handler){
        try{
            RpcObject rpc = new RpcObject();
            int type = inputstream.read();
            rpc.setType(RpcType.getByType(type));
            byte[] eightBytes = new byte[8];
            inputstream.read(eightBytes);
            rpc.setThreadId(RpcUtils.bytesToLong(eightBytes));
            byte[] indexBytes = new byte[4];
            inputstream.read(indexBytes);
            rpc.setIndex(RpcUtils.bytesToInt(indexBytes));
            byte[] lenBytes = new byte[4];
            inputstream.read(lenBytes);
            rpc.setLength(RpcUtils.bytesToInt(lenBytes));
            if (rpc.getLength() > 0) {
                if (rpc.getLength() > MEM_1M) {
                    throw new RpcException("rpc data too long "	+ rpc.getLength());
                }
                byte[] buf = new byte[rpc.getLength()];
                inputstream.read(buf);
                rpc.setData(buf);
            }
            return rpc;
        }
        catch (IOException e){
            handleNetException(e, handler);
            return null;
        }
    }

    public static RpcObject readDataRpc(DataInputStream inputStream, RpcNetExceptionHandler handler){
        try{
            RpcObject rpc = new RpcObject();
            rpc.setType(RpcType.getByType(inputStream.readInt()));
            rpc.setThreadId(inputStream.readLong());
            rpc.setIndex(inputStream.readInt());
            rpc.setLength(inputStream.readInt());
            if (rpc.getLength() > 0) {
                if (rpc.getLength() > MEM_1M) {
                    throw new RpcException("rpc data too long "+ rpc.getLength());
                }
                byte[] buf = new byte[rpc.getLength()];
                inputStream.read(buf);
                rpc.setData(buf);
            }
            return rpc;
        }
        catch(IOException e){
            handleNetException(e, handler);
            return null;
        }
    }

    public static void close(DataInputStream inputStream, DataOutputStream outputStream){
        try{
            inputStream.close();
            outputStream.close();
        }
        catch(IOException e){

        }
    }





    public enum RpcType{
        ONEWAY(1), INVOKE(2), SUC(3), FAIL(4);
        private int type;

        RpcType(int type){
            this.type = type;
        }

        public int getType(){
            return type;
        }

        public static RpcType getByType(int type){
            RpcType[] values = RpcType.values();
            for(RpcType v : values){
                if(v.type == type){
                    return v;
                }
            }
            return ONEWAY;



        }

    }

}
