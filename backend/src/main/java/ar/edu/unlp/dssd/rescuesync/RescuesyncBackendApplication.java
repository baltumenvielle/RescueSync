package ar.edu.unlp.dssd.rescuesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RescuesyncBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RescuesyncBackendApplication.class, args);
	}

}
