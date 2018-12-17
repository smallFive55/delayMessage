package com.five.delay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.five.delay.plan.MQDelayPlan;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DelayMessageApplication.class)
public class MQDelayPlanTest {

	@Autowired
	private MQDelayPlan mqDelay;
	
	@Test
	public void product() throws Exception{
		mqDelay.product();
		
		Thread.sleep(60000);
	}
}
