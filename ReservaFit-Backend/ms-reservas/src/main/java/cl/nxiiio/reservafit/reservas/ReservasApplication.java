package cl.nxiiio.reservafit.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import cl.nxiiio.reservafit.events.RabbitTopologyConfig;

@SpringBootApplication
// RabbitMQ exchanges, queues and message format, shared with the other services
@Import(RabbitTopologyConfig.class)
public class ReservasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservasApplication.class, args);
	}

}
