package com.five.delay;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.five.delay.plan.RedisDelayPlan;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DelayMessageApplication.class)
public class RedisDelayPlanTest {

	@Autowired
	private RedisDelayPlan redisDelay;
	//并发数
	private static final int SERVER_NUM = 10;
	private static final CountDownLatch cdl = new CountDownLatch(SERVER_NUM);
	@Test
	public void product() throws Exception{
		redisDelay.product();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void consumer() throws Exception {
		for (int i = 0; i < SERVER_NUM; i++) {
			new Thread(new serverThread()).start();
			cdl.countDown();//CountDownLatch计数器-1,当数值为0时，释放所有线程执行[redisDelay.consumer();]
		}
		Thread.sleep(60000);
	}
	
	class serverThread implements Runnable{

		public void run() {
			try {
				cdl.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			redisDelay.consumer();
		}
	}
}
