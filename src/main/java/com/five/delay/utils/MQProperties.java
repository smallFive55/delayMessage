package com.five.delay.utils;
/**
 * 
 * @author 小五
 * @createTime 2018年10月26日 下午4:40:00
 * 定义MQ中所需要的交换器名、队列名及绑定值
 */
public class MQProperties {
	
	//任务队列名称
	public static final String QUEUE_NAME="order.orderQueue";
	
	//任务交换器名称
	public static final String EXCHANGE_NAME="order.orderExchange";
	
	//任务交换器与任务队列绑定键
	public static final String ROUTE_KEY="order.routeKey";
	
	//死信队列名称
	public static final String DEAD_QUEUE_NAME="order.dead.orderQueue";
	
	//死信交换器名称
	public static final String DEAD_EXCHANGE_NAME="order.dead.orderExchange";
}
