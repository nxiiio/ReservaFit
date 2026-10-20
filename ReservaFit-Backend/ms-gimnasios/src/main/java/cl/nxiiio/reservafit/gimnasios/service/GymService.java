package cl.nxiiio.reservafit.gimnasios.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.nxiiio.reservafit.gimnasios.exception.GymNotFoundException;
import cl.nxiiio.reservafit.gimnasios.exception.ScheduleNotFoundException;
import cl.nxiiio.reservafit.gimnasios.model.Gimnasio;
import cl.nxiiio.reservafit.gimnasios.model.Horario;
import cl.nxiiio.reservafit.gimnasios.repository.GymRepository;
import cl.nxiiio.reservafit.gimnasios.repository.ScheduleRepository;

@Service
@Transactional(readOnly = true)
public class GymService {

    private final GymRepository gymRepository;
    private final ScheduleRepository scheduleRepository;

    public GymService(GymRepository gymRepository, ScheduleRepository scheduleRepository) {
        this.gymRepository = gymRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // A blank comuna means "no filter"
    public List<Gimnasio> findAll(String comuna) {
        if (comuna == null || comuna.isBlank()) {
            return gymRepository.findAllByOrderByNombreAsc();
        }
        return gymRepository.findByComunaIgnoreCaseOrderByNombreAsc(comuna.trim());
    }

    public Gimnasio findById(Long id) {
        return gymRepository.findById(id).orElseThrow(() -> new GymNotFoundException(id));
    }

    // 404 for an unknown gym instead of an empty list
    public List<Horario> findSchedules(Long gymId) {
        if (!gymRepository.existsById(gymId)) {
            throw new GymNotFoundException(gymId);
        }
        return scheduleRepository.findByGimnasioIdOrderByHoraInicioAsc(gymId);
    }

    public Horario findSchedule(Long scheduleId) {
        return scheduleRepository.findByIdWithGym(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }
}
