package cl.nxiiio.reservafit.reservas.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import cl.nxiiio.reservafit.reservas.exception.GymNotFoundException;
import cl.nxiiio.reservafit.reservas.exception.GymServiceUnavailableException;
import cl.nxiiio.reservafit.reservas.exception.ScheduleNotFoundException;

// Asks ms-gimnasios for its schedules over HTTP. It calls the service directly
// (app.gimnasios-url), not through the gateway. Both endpoints are public, so no token is sent.
@Component
public class GymClient {

    private final RestClient restClient;

    public GymClient(@Value("${app.gimnasios-url}") String gimnasiosUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(gimnasiosUrl)
                .build();
    }

    // The schedules of one gym, ordered by start time
    public List<ScheduleInfo> findSchedulesOfGym(Long gymId) {
        try {
            ScheduleInfo[] schedules = restClient.get()
                    .uri("/api/gimnasios/{id}/horarios", gymId)
                    .retrieve()
                    .body(ScheduleInfo[].class);

            return List.of(schedules);
        } catch (HttpClientErrorException.NotFound e) {
            throw new GymNotFoundException(gymId);
        } catch (RestClientException e) {
            // ms-gimnasios is down or answered with an error
            throw new GymServiceUnavailableException(e);
        }
    }

    // One schedule, with its gym's data
    public ScheduleInfo findSchedule(Long scheduleId) {
        try {
            return restClient.get()
                    .uri("/api/gimnasios/horarios/{id}", scheduleId)
                    .retrieve()
                    .body(ScheduleInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ScheduleNotFoundException(scheduleId);
        } catch (RestClientException e) {
            throw new GymServiceUnavailableException(e);
        }
    }
}
