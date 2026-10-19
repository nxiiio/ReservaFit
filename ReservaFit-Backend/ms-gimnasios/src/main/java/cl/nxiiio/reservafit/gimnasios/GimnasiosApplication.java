package cl.nxiiio.reservafit.gimnasios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import cl.nxiiio.reservafit.events.RabbitTopologyConfig;

@SpringBootApplication
// RabbitMQ exchanges, queues and message format, shared with the other services
@Import(RabbitTopologyConfig.class)
public class GimnasiosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GimnasiosApplication.class, args);
	}

}
