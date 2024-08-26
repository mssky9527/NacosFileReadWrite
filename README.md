#### Nacos JRaft 任意文件读写

一些操作信息记录

```java
com.alibaba.nacos.core.storage.kv.FileKvStorage实现了com.alibaba.nacos.core.storage.kv.KvStorage，这是任意文件读写位置

com.alibaba.nacos.naming.consistency.persistent.impl.BasePersistentServiceProcessor#onApply任意文件读写调用点


com.alibaba.nacos.naming.consistency.persistent.impl.BasePersistentServiceProcessor#group返回了naming_persistent_service表示调用的group


读文件方法：
com.alibaba.nacos.core.storage.kv.FileKvStorage#get

写文件方法：
com.alibaba.nacos.core.storage.kv.FileKvStorage#put
```

#### onApply调用堆栈

```
onApply:119, NacosStateMachine (com.alibaba.nacos.core.distributed.raft)
doApplyTasks:541, FSMCallerImpl (com.alipay.sofa.jraft.core)
doCommitted:510, FSMCallerImpl (com.alipay.sofa.jraft.core)
runApplyTask:442, FSMCallerImpl (com.alipay.sofa.jraft.core)
access$100:73, FSMCallerImpl (com.alipay.sofa.jraft.core)
onEvent:148, FSMCallerImpl$ApplyTaskHandler (com.alipay.sofa.jraft.core)
onEvent:142, FSMCallerImpl$ApplyTaskHandler (com.alipay.sofa.jraft.core)
run:137, BatchEventProcessor (com.lmax.disruptor)
run:745, Thread (java.lang)
```

#### 注册group类

```java
com.alibaba.nacos.core.distributed.raft.JRaftServer#createMultiRaftGroup
```



<br />注意：工具仅供学习使用，请勿滥用，否则后果自负！

<br />**食用方式 **<br />
<br />

```shell
java -jar NacosRce.jar http://192.168.90.1:8848/  7848 
```

<br />[https://exp.ci/2023/06/14/Nacos-JRaft-Hessian-反序列化分析/](https://exp.ci/2023/06/14/Nacos-JRaft-Hessian-%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E5%88%86%E6%9E%90/)
<br />[https://github.com/c0olw/NacosRce](https://github.com/c0olw/NacosRce)

