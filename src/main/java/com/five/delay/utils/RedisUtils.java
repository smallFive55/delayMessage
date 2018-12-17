package com.five.delay.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

@Component
public class RedisUtils {

	@Autowired
	private JedisPool jedisPool;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//添加订单任务
	public void addItem(String key, double score, String member){
		//拿到redis客户端
		Jedis jedis = jedisPool.getResource();
		//添加元素
		jedis.zadd(key, score, member); 
		System.out.println(sdf.format(new Date())+" 向redis中添加了一个任务[key:"+key+", score:"+score+", member:"+member+"]");
	}
	
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
}
