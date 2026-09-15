package cl.nxiiio.ReservaFit_Backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

// Checks the "activa = NULL when canceled" trick against the real H2 schema Hibernate generates
@SpringBootTest
@Transactional
class SlotUniqueConstraintTest {

    @Autowired
    private JdbcTemplate jdbc;

    private Long userId;

    @BeforeEach
    void createUser() {
        jdbc.update("insert into usuario (microsoft_oid, nombre) values ('oid-unique-test', 'Test')");
        userId = jdbc.queryForObject(
                "select id from usuario where microsoft_oid = 'oid-unique-test'", Long.class);
    }

    private void insertBooking(String estado, Boolean activa) {
        jdbc.update("insert into reserva (usuario_id, gimnasio_id, horario_id, fecha, estado, activa) "
                + "values (?, 1, 1, DATE '2026-10-01', ?, ?)", userId, estado, activa);
    }

    @Test
    void canceledRowsCoexistWithOneConfirmedRow() {
        insertBooking("CANCELADA", null);
        insertBooking("CANCELADA", null);
        insertBooking("CONFIRMADA", true);

        Integer rows = jdbc.queryForObject(
                "select count(*) from reserva where horario_id = 1 and fecha = DATE '2026-10-01'", Integer.class);
        assertEquals(3, rows);
    }

    @Test
    void twoConfirmedRowsForTheSameSlotViolateTheConstraint() {
        insertBooking("CONFIRMADA", true);

        DataIntegrityViolationException e = assertThrows(DataIntegrityViolationException.class,
                () -> insertBooking("CONFIRMADA", true));
        // BookingService relies on the constraint name to map this to a 409
        assertTrue(e.getMostSpecificCause().getMessage().toLowerCase(Locale.ROOT).contains("uq_reserva_slot"));
    }
}
