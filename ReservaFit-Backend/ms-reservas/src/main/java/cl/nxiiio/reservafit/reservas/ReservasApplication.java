package cl.nxiiio.reservafit.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// @EnableFeignClients creates the HTTP clients declared with @FeignClient (client/GymClient)
@SpringBootApplication
@EnableFeignClients
public class ReservasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservasApplication.class, args);
	}

}
