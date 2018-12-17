package com.five.delay.utils;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 
 * @author 小五
 * @createTime 2018年10月26日 下午4:36:38
 * 
 */
@Component
public class MQBusiness {
	
	@RabbitListener(queues=MQProperties.DEAD_QUEUE_NAME)
	public void process(String message) throws IOException{
		System.out.println(CalendarUtils.getCurrentTimeByStr(0)+" 消费了一个超时订单，订单ID："+message);
		
//		basicAck() //确认消费成功，并删除RabbitMQ中对应数据
	}
}
