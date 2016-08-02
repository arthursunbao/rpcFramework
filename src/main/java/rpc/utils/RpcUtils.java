package main.java.rpc.utils;

/**
 * Created by baosun on 7/29/2016.
 */
public class RpcUtils {


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
