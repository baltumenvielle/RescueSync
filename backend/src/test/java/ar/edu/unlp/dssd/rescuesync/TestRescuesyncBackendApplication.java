package ar.edu.unlp.dssd.rescuesync;

import org.springframework.boot.SpringApplication;

public class TestRescuesyncBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(RescuesyncBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
