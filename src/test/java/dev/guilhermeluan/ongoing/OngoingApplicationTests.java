package dev.guilhermeluan.ongoing;

import dev.guilhermeluan.ongoing.config.TestcontainersConfigurations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@SpringBootTest
@ImportTestcontainers(TestcontainersConfigurations.class)
class OngoingApplicationTests {

	@Test
	void contextLoads() {
	}

}
