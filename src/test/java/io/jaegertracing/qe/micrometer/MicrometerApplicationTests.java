package io.jaegertracing.qe.micrometer;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Category(io.jaegertracing.qe.micrometer.UnitTest.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class MicrometerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
