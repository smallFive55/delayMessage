package com.five.delay;

import java.util.concurrent.DelayQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.five.delay.plan.DelayQueuePlan;
import com.five.delay.utils.MyDelayed;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring-servlet.xml"})
@TransactionConfiguration(defaultRollback=true)
public class DelayQueuePlanTest {

	@Test
	public void test() throws Exception{
		DelayQueue<MyDelayed> delayQueue = new DelayQueue<MyDelayed>();
		//生产者生产一个5秒的延时任务
		new Thread(new DelayQueuePlan.ProducerDelay(delayQueue, 5)).start();
		//开启消费者轮询
		new Thread(new DelayQueuePlan.ConsumerDelay(delayQueue)).start();
		
		Thread.sleep(10000);
	}
}
