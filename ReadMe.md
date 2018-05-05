## 项目结构
1. hollyvoc-data-topology
    数据流式处理核心模块，kafka读取数据，保存到hbase，然后进行索引和新词发现
    - 本地运行执行app包中的类主方法即可
    - 集群运行
    打包：mvn clean package -pl hollyvoc-data-topology -am -DskipTests
    上传jar：hollyvoc-data-topology.jar
    执行方法：
    普通拓扑：java -jar hollyvoc-data-topology.jar com.hollycrm.hollyvoc.app.Application
    batch 拓扑：java -jar hollyvoc-data-topology.jar com.hollycrm.hollyvoc.app.BatchApplication
    

2. hollyvoc-kafka
    对kafka数据的操作：发送和读取数据.kafka需要有有生产者提供数据，消费者处理数据。

3. hollyvoc-kafka 
    kafka消费者、提供者，主要对kafka进行发数据和读取数据操作的模块。
4. hollyvoc-util 
    工具类模块
5. hollyvoc-constant
    常量模块
6. hollyvoc-helper
    数据库连接、redis
