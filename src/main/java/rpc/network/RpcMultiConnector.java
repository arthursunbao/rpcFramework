package main.java.rpc.network;

import main.java.rpc.utils.RpcUtils;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by baosun on 7/29/2016.
 */
public class RpcMultiConnector extends RpcNetBase implements Service {
    private static final int DEFAULT_CONNECTION_COUNT = 5;
    private int connectionCount = DEFAULT_CONNECTION_COUNT;
    private static final Class<? extends AbstractRpcConnector> DEFAULT_CONNECTOR_CLASS = RpcNioConnector.class;
    private Class<? extends AbstractRpcConnector> connectorClass = XmlJavaTypeAdapter.DEFAULT;
    private CopyOnWriteArrayList<AbstractRpcConnector> connectors = new CopyOnWriteArrayList<>();
    private AtomicInteger loadBalance = new AtomicInteger(0);

    public int getConnectionCount(){return connectionCount;}

    public void setConnectionCount(int connectionCount){this.connectionCount = connectionCount;}

    public Class<? extends AbstractRpcConnector> getConnectorClass(){
        return connectorClass;
    }

    public void setConnectorClass(Class<? extends AbstractRpcConnector> connectorClass){
        this.connectorClass = connectorClass;
    }

    /*
    Round-Robin Algorithm to get the resource
     */
    private AbstractRpcConnector getResource(int max){
        if(max < 1){
            return null;
        }
        int load = loadBalance.getAndIncrement();
        int size = connectors.size();
        if(size > 0){
            int index = load%size;
            if(size > index){
                AbstractRpcConnector connector = connectors.get(index);
                if(connector.isStop()){
                    checkConnectors();
                    return this.getResource(max - 1);
                }
                else{
                    return connector;
                }
            }
        }
        return null;
    }

    public AbstractRpcConnector getResource(){
        return this.getResource(10);
    }

    private void checkConnectors(){
        for(AbstractRpcConnector connector : connectors){
            if(connector.isStop()){
                connectors.remove(connector);
            }
        }
    }

    @Override
    public void handleNetException(Exception e){

    }

    private void initConnector(AbstractRpcConnector connector){
        connector.setHost(this.getHost());
        connector.setPort(this.getPort());
        connector.setExecutorService(this.getExecutorService());
        connector.setExecutorSharable(true);
    }

    private List<AbstractRpcConnector> createInstance(int count){
        LinkedList<AbstractRpcConnector> list = new LinkedList<>();
        SimpleRpcNioSelector nioSelector = NEW SimpleRpcNioSelector();
        SimpleRpcOioSelector oioSelector = new SimpleRpcOioSelector();
        for(int i = 0; i < count; i++){
            AbstractRpcConnector connector = RpcUtils.createRpcConnector(nioSelector, writer, connectorClass);
            this.initConnector(connector);
            list.add(connector);
        }
        return list;
    }

    @Override
    public void startService() {
        super.startService();
        List<AbstractRpcConnector> createdConnectors = this.createInstance(connectionCount);
        connectors.addAll(createdConnectors);
        this.startConnectors();

    }

    private void startConnectors(){
        for(AbstractRpcConnector connector:connectors){
            for(RpcCallListener listener:callListeners){
                connector.addRpcCallListener(listener);
            }
            connector.startService();
        }
    }

    @Override
    public void stopService() {
        super.stopService();
        for(AbstractRpcConnector connector:connectors){
            connector.stopService();
        }
    }
}
