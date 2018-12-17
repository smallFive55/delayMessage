package com.five.delay;

import java.util.concurrent.DelayQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.five.delay.plan.DelayQueuePlan;
import com.five.delay.utils.MyDelayed;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DelayMessageApplication.class)
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
