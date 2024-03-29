package org.bytepoet.shopifysolo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}

}
