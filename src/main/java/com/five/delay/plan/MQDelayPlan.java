package com.five.delay.plan;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.five.delay.utils.CalendarUtils;
import com.five.delay.utils.MQProperties;

/**
 * 
 * @author 小五
 * @createTime 2018年11月11日 下午5:35:58
 * 
 */
@Service
public class MQDelayPlan {

	@Autowired
	private AmqpTemplate amqpTemplate;
	
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
}
