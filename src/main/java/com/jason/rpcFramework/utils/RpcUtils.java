package com.jason.rpcFramework.utils;



import com.jason.rpcFramework.Exception.RpcException;
import com.jason.rpcFramework.Exception.RpcExceptionHandler;
import com.jason.rpcFramework.Exception.RpcNetExceptionHandler;
import com.jason.rpcFramework.RemoteCall;
import com.jason.rpcFramework.RpcObject;
import com.jason.rpcFramework.network.AbstractRpcConnector;
import com.jason.rpcFramework.nio.AbstractRpcNioSelector;
import com.jason.rpcFramework.nio.RpcNioAcceptor;
import com.jason.rpcFramework.nio.SimpleRpcNioSelector;
import com.jason.rpcFramework.oio.AbstractRpcOioWriter;
import javafx.scene.chart.PieChart;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public static List<Field> getFields(Class classz){
        Field[] fields = classz.getDeclaredFields();
        ArrayList<Field> fs = new ArrayList<Field>();
        for(Field field : fields){
            fs.add(field);
        }
        Class superclass = classz.getSuperclass();
        if(superclass != null && superclass != Object.class){
            fs.addAll(getFields(superclass));
        }
        return fs;
    }

    public static Object invokeMethod(Object obj, String methodName, Object[] args, RpcExceptionHandler exceptionHandler){
        Class<? extends Object> classz = obj.getClass();
        String key = classz.getCanonicalName() + "." + methodName;
        Method method = methodCache.get(key);
        if(method == null){
            method = RpcUtils.findMethod(classz, methodName, args);
            if(method == null){
                throw new RpcException("method does not exist" + methodName);
            }
            methodCache.put(key, method);
        }
        return RpcUtils.invokeMethod(method, obj, args, exceptionHandler);
    }

    public static Object invoke(Method method, Object obj, Object[] args, RpcExceptionHandler exceptionHandler){
        try{
            return method.invoke(obj, args);
        }
        catch(IllegalAccessException e){
            throw new RpcException("Illegal Access Request Exception");
        }
        catch(IllegalArgumentException e){
            throw new RpcException("Illegal Argument Exception");
        }
        catch(InvocationTargetException e){
            if(e.getCause()!=null){
                exceptionHandler.handleException(null, null, e.getCause());
            }else{
                exceptionHandler.handleException(null, null, e);
            }
            throw new RpcException("rpc invoke target error");
        }
    }

    public static void handleException(RpcExceptionHandler rpcExceptionHandler, RpcObject rpc, RemoteCall call, Exception e){
        if(rpcExceptionHandler != null){
            rpcExceptionHandler.handleException(rpc, call, e);
        }
        else{
            logger.error("exceptionHandler null exception message" + e.getMessage());
        }
    }

    public static long getNowInmilliseconds() {
        return new Date().getTime();
    }

    public static byte[] intToBytes(int iSource) {
        byte[] bLocalArr = new byte[4];
        for (int i=0;i<bLocalArr.length; i++) {
            bLocalArr[i] = (byte) (iSource >> 8*(3-i) & 0xFF);
        }
        return bLocalArr;
    }

    public static int bytesToInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;
        for (int i=0; i<bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * (3-i));
        }
        return iOutcome;
    }

    public static byte[] longToBytes(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 7; i>-1; i--) {
            b[i] = new Long(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return b;
    }

    public static long bytesToLong(byte[] b) {
        long s = 0;
        long s0 = b[0] & 0xff;
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff;
        s6 <<= 8;
        s5 <<= 16;
        s4 <<= 24;
        s3 <<= 8 * 4;
        s2 <<= 8 * 5;
        s1 <<= 8 * 6;
        s0 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    public static AbstractRpcConnector createRpcConnector(AbstractRpcNioSelector nioSelector,
                                                          AbstractRpcOioWriter writer, Class<? extends AbstractRpcConnector> connectorClass) {
        try {
            if (connectorClass == RpcNioAcceptor.class) {
                Constructor<? extends AbstractRpcConnector> constructor = connectorClass.getConstructor(AbstractRpcNioSelector.class);
                return constructor.newInstance(nioSelector);
            }
            else if (connectorClass == RpcOioConnector.class) {
                Constructor<? extends AbstractRpcConnector> constructor = connectorClass.getConstructor(AbstractRpcOioWriter.class);
                return constructor.newInstance(writer);
            }
            else {
                return connectorClass.newInstance();
            }
        }
        catch(Exception e){
            throw new RpcException(e);
        }
    }




    public static AbstractRpcConnector createConnector(Class connectorClass){
        SimpleRpcNioSelector nioSelector = new SimpleRpcNioSelector();
        SimpleRpcOioWriter writer = new SimpleRpcOioWriter();
        if(connectorClass == null){
            connectorClass = RpcNioAcceptor.class;
        }
        return RpcUtils.createRpcConnector(nioSelector,writer,connectorClass);
    }

    public static Method findMethod(Class clazz, String name, Object[] args) {
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    public static long getNowMinute(){
        return getMinute(new Date());
    }

    public static long getMinute(Date date){
        GregorianCalendar calendar = new GregorianCalendar(1900+date.getYear(),date.getMonth(),date.getDay(),date.getHours(),date.getMinutes());
        return calendar.getTimeInMillis();
    }

    public static final long MINUTE = 60*1000;

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
