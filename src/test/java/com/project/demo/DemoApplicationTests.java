package com.project.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Deshabilitado para no levantar todo el contexto en unit tests")
@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() { }
}
