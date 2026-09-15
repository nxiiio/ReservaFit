package cl.nxiiio.ReservaFit_Backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.nxiiio.ReservaFit_Backend.exception.GymNotFoundException;
import cl.nxiiio.ReservaFit_Backend.model.Gimnasio;
import cl.nxiiio.ReservaFit_Backend.model.Horario;
import cl.nxiiio.ReservaFit_Backend.repository.GymRepository;
import cl.nxiiio.ReservaFit_Backend.repository.ScheduleRepository;

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
}
