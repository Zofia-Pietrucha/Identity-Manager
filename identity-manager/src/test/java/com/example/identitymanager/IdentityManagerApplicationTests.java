package com.example.identitymanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IdentityManagerApplicationTests {

	@Test
	void contextLoads() {
		// Verifies that Spring context loads correctly
	}

	@Test
	void mainMethodStartsApplication() {
		// This test covers the main() method
		// We use empty args as we don't need to actually start the full server
		IdentityManagerApplication.main(new String[]{});
	}
}