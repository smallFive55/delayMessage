# delayMessage

## 延迟任务处理方案

### 方案一：数据库轮询

小型项目常用方式，通过一个线程去扫描数据库或数据库定时任务，通过订单时间判断超时的订单，进行更新状态或其他操作。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/task-db.png)

### 方案二：JDK延迟队列

DelayQueue是一个无界阻塞队列，只有在延迟期满时才能从中获取元素，放入DelayQueue中的对象需要实现Delayed接口。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/delayQueue.png)

#### 实现

`com.five.delay.plan.DelayQueuePlan`

```java
public class DelayQueuePlan {

	public static void main(String[] args) {
		DelayQueue<MyDelayed> delayQueue = new DelayQueue<MyDelayed>();
		//生产者生产一个5秒的延时任务
		new Thread(new ProducerDelay(delayQueue, 5)).start();
		//开启消费者轮询
		new Thread(new ConsumerDelay(delayQueue)).start();
	}
	
	/**
	 * 延时任务生产者 
	 **/
	public static class ProducerDelay implements Runnable{
		DelayQueue<MyDelayed> delayQueue;
		int delaySecond;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		public ProducerDelay(DelayQueue<MyDelayed> delayQueue, int delaySecond){
			this.delayQueue = delayQueue;
			this.delaySecond = delaySecond;
		}
		
		public void run() {
			String orderId = "1010101";
			for (int i = 0; i < 10; i++) {
				//定义一个Delay, 放入到DelayQueue队列中
				MyDelayed delay = new MyDelayed(this.delaySecond, orderId+i);
				delayQueue.add(delay);//向队列中插入一个元素（延时任务）
				System.out.println(sdf.format(new Date())+ " Thread "+Thread.currentThread()+" 添加了一个delay. orderId:"+orderId+i);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	/**
	 * 延时任务消费者
	 **/
	public static class ConsumerDelay implements Runnable{
		
		DelayQueue<MyDelayed> delayQueue;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		public ConsumerDelay(DelayQueue<MyDelayed> delayQueue){
			this.delayQueue = delayQueue;
		}
		public void run() {
			//轮询获取DelayQueue队列中当前超时的Delay元素
			while(true){
				MyDelayed delayed=null;
				try {
					delayed = delayQueue.take();
				} catch (Exception e) {
					e.printStackTrace();
				}
				//如果Delay元素存在,则任务到达超时时间
				if(delayed!=null){
					//处理任务
					System.out.println(sdf.format(new Date())+" Thread "+Thread.currentThread()+" 消费了一个delay. orderId:"+delayed.getOrderId());
				}else{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("....");
				}
			}
		}
	}
}
```

#### 结果

```tiki wiki
2018-05-11 16:20:48 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101010
2018-05-11 16:20:48 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101011
2018-05-11 16:20:48 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101012
2018-05-11 16:20:48 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101013
2018-05-11 16:20:48 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101014
2018-05-11 16:20:49 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101015
2018-05-11 16:20:49 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101016
2018-05-11 16:20:49 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101017
2018-05-11 16:20:49 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101018
2018-05-11 16:20:49 Thread Thread[Thread-4,5,main] 添加了一个delay. orderId:10101019
2018-05-11 16:20:53 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101010
2018-05-11 16:20:53 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101011
2018-05-11 16:20:53 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101012
2018-05-11 16:20:53 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101013
2018-05-11 16:20:53 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101014
2018-05-11 16:20:54 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101015
2018-05-11 16:20:54 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101016
2018-05-11 16:20:54 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101017
2018-05-11 16:20:54 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101018
2018-05-11 16:20:54 Thread Thread[Thread-5,5,main] 消费了一个delay. orderId:10101019
```



### 方案三：Redis 有序集合

将订单超时时间戳与订单号分别设置为score与member，系统扫描第一个元素判断是否超时。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/redis.png)

#### 实现

`com.five.delay.utils.RedisUtils`

```java
//扫描redis 判断订单是否超时需要处理
public void dofind(String key){
    //拿到redis客户端
    Jedis jedis = jedisPool.getResource();
    while(true){
        Set<Tuple> zrangeWithScores = jedis.zrangeWithScores(key, 0, 0);
        //判断元素是否超时  根据超时时间戳
        if(zrangeWithScores !=null && !zrangeWithScores.isEmpty()){
            //score  ===  订单的超时时间戳       与当前时间戳对比 判断是否超时
            double score = ((Tuple)(zrangeWithScores.toArray()[0])).getScore();//订单的超时时间戳
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis>=score){
                //订单超时
                String element = ((Tuple)(zrangeWithScores.toArray()[0])).getElement();//订单ID
                //删除元素
                Long zrem = jedis.zrem(key, element); //关键点：redis单线程机制解决并发场景安全问题。
                if(zrem!=null && zrem>0){
                    //处理超时订单
                    System.out.println(sdf.format(new Date())+"["+Thread.currentThread()+"] 从redis中拿到一个超时任务[key:"+key+", score:"+score+", member:"+element+"]");
                }else{
                    //						System.out.println(sdf.format(new Date())+"["+Thread.currentThread()+"] 任务被其他服务消费了");
                }
            }else{
                //					System.out.println(sdf.format(new Date())+"["+Thread.currentThread()+"] 当前没有超时的订单");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            //				System.out.println("当前redis中没有可以操作的数据");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```



#### 结果

##### 生产者

```tiki wiki
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152621E12, member:OIDNO100000]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152646E12, member:OIDNO100001]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152648E12, member:OIDNO100002]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152649E12, member:OIDNO100003]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.52602615265E12, member:OIDNO100004]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152651E12, member:OIDNO100005]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152652E12, member:OIDNO100006]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152652E12, member:OIDNO100007]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152653E12, member:OIDNO100008]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152654E12, member:OIDNO100009]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152656E12, member:OIDNO100010]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152658E12, member:OIDNO100011]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152659E12, member:OIDNO100012]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.52602615266E12, member:OIDNO100013]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152661E12, member:OIDNO100014]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152661E12, member:OIDNO100015]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152662E12, member:OIDNO100016]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152663E12, member:OIDNO100017]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152664E12, member:OIDNO100018]
2018-05-11 16:08:12 向redis中添加了一个任务[key:ORDER_KEY, score:1.526026152664E12, member:OIDNO100019]
```

##### 消费者

```tiki wiki
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152648E12, member:OIDNO100002]
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152621E12, member:OIDNO100000]
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152646E12, member:OIDNO100001]
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152649E12, member:OIDNO100003]
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.52602615265E12, member:OIDNO100004]
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152651E12, member:OIDNO100005]
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152652E12, member:OIDNO100006]
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152652E12, member:OIDNO100007]
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152653E12, member:OIDNO100008]
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152654E12, member:OIDNO100009]
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152656E12, member:OIDNO100010]
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152658E12, member:OIDNO100011]
2018-05-11 16:09:12[Thread[Thread-6,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152659E12, member:OIDNO100012]
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.52602615266E12, member:OIDNO100013]
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152661E12, member:OIDNO100014]
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-6,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152661E12, member:OIDNO100015]
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-6,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152662E12, member:OIDNO100016]
2018-05-11 16:09:12[Thread[Thread-8,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-12,5,main]] 任务被其他服务消费了
当前redis中没有可以操作的数据
2018-05-11 16:09:12[Thread[Thread-9,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152664E12, member:OIDNO100019]
2018-05-11 16:09:12[Thread[Thread-8,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152664E12, member:OIDNO100018]
当前redis中没有可以操作的数据
2018-05-11 16:09:12[Thread[Thread-4,5,main]] 任务被其他服务消费了
当前redis中没有可以操作的数据
2018-05-11 16:09:12[Thread[Thread-6,5,main]] 任务被其他服务消费了
当前redis中没有可以操作的数据
2018-05-11 16:09:12[Thread[Thread-13,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-11,5,main]] 任务被其他服务消费了
2018-05-11 16:09:12[Thread[Thread-7,5,main]] 从redis中拿到一个超时任务[key:ORDER_KEY, score:1.526026152663E12, member:OIDNO100017]
2018-05-11 16:09:12[Thread[Thread-10,5,main]] 任务被其他服务消费了
当前redis中没有可以操作的数据
当前redis中没有可以操作的数据
```



### 方案四：RabbitMQ TTL+DLX

RabbitMQ可设置消息过期时间（TTL），当消息过期后可以将该消息投递到队列上设置的死信交换器（DLX）上，再次投递到死信队列中，重新消费。
 ![image](https://github.com/smallFive55/delayMessage/raw/master/pic/rabbitMQ.png)

#### 实现

##### 生产者

`com.five.delay.plan.MQDelayPlan`

```java
public void product() {
    String orderId = "1010101";
    for (int i = 0; i < 10; i++) {
        //创建订单
        amqpTemplate.convertAndSend(MQProperties.EXCHANGE_NAME, MQProperties.ROUTE_KEY, orderId+i);

        System.out.println(CalendarUtils.getCurrentTimeByStr(0)+" 生成了一个订单，订单ID："+orderId+i);
        if(i%3==0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

##### 消费者

`com.five.delay.utils.MQBusiness`

```java
@Component
public class MQBusiness {
	
	@RabbitListener(queues=MQProperties.DEAD_QUEUE_NAME)
	public void process(String message) throws IOException{
		System.out.println(CalendarUtils.getCurrentTimeByStr(0)+" 消费了一个超时订单，订单ID："+message);
		
//		basicAck() //确认消费成功，并删除RabbitMQ中对应数据
	}
}
```

#### 结果

```tiki wiki
16:49:51 生成了一个订单，订单ID：10101010
16:49:52 生成了一个订单，订单ID：10101011
16:49:52 生成了一个订单，订单ID：10101012
16:49:52 生成了一个订单，订单ID：10101013
16:49:53 生成了一个订单，订单ID：10101014
16:49:53 生成了一个订单，订单ID：10101015
16:49:53 生成了一个订单，订单ID：10101016
16:49:54 生成了一个订单，订单ID：10101017
16:49:54 生成了一个订单，订单ID：10101018
16:49:54 生成了一个订单，订单ID：10101019
16:50:1 消费了一个超时订单，订单ID：10101010
16:50:2 消费了一个超时订单，订单ID：10101011
16:50:2 消费了一个超时订单，订单ID：10101012
16:50:2 消费了一个超时订单，订单ID：10101013
16:50:3 消费了一个超时订单，订单ID：10101014
16:50:3 消费了一个超时订单，订单ID：10101015
16:50:3 消费了一个超时订单，订单ID：10101016
16:50:4 消费了一个超时订单，订单ID：10101017
16:50:4 消费了一个超时订单，订单ID：10101018
16:50:4 消费了一个超时订单，订单ID：10101019
```



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
增加Redis维护、占用宽带、代码逻辑稍显复杂、轮询增加Redis压力； （备注：针对缺点，最近在思考的一个项目[输入链接说明](https://gitee.com/smallfive55/redis-delay-handler)，欢迎感兴趣的小伙伴一起参与）

### RabbitMQ

**优点：**
解耦、异常恢复、扩展性强、支持分布式/集群环境；  
**缺点：**
增加RabbitMQ维护、占用宽带、单个队列中任务的延迟时间必须相同；

