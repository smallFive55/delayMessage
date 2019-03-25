# delayMessage
## 延迟任务处理方案

### 方案一：数据库轮询
小型项目常用方式，通过一个线程去扫描数据库或数据库定时任务，通过订单时间判断超时的订单，进行更新状态或其他操作。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/task-db.png)
 
### 方案二：JDK延迟队列
DelayQueue是一个无界阻塞队列，只有在延迟期满时才能从中获取元素，放入DelayQueue中的对象需要实现Delayed接口。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/delayQueue.png)
 
### 方案三：Redis 有序集合
将订单超时时间戳与订单号分别设置为score与member，系统扫描第一个元素判断是否超时。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/redis.png)
 
### 方案四：RabbitMQ TTL+DLX
RabbitMQ可设置消息过期时间（TTL），当消息过期后可以将该消息投递到队列上设置的死信交换器（DLX）上，再次投递到死信队列中，重新消费。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/rabbitMQ.png)
 
## 各方案总结：
### DB轮询
**优点：**
实现简单、无技术难点、异常恢复、支持分布式/集群环境；
**缺点：**
影响数据库性能；

### DelayedQueue
**优点：**
实现简单、性能较好；  
**缺点：**
异常恢复困难、只适用于单机环境，分布式/集群实现困难；

### Redis
**优点：**
解耦、异常恢复、支持分布式/集群环境；  
**缺点：**
增加Redis维护、占用宽带、增加异常处理；

### RabbitMQ
**优点：**
解耦、异常恢复、扩展性强、支持分布式/集群环境；  
**缺点：**
增加RabbitMQ维护、占用宽带；