-- Fictional gyms for local development (H2 is in-memory, so this runs on every boot)
-- Images are hotlinked from Unsplash (free license)
INSERT INTO gimnasio (nombre, comuna, direccion, descripcion, imagen_url) VALUES
    ('Fuerza Barrio Gym', 'Ñuñoa', 'Av. Irarrázaval 3450',
     'Gimnasio de barrio enfocado en musculación y entrenamiento funcional, con máquinas de peso libre y zona de cardio.',
     'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&q=80'),
    ('Cordillera Fitness', 'Providencia', 'Av. Manuel Montt 1120',
     'Espacio con clases grupales de spinning, yoga y HIIT, además de sala de musculación.',
     'https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=1200&q=80'),
    ('Titán Box', 'Maipú', 'Av. Pajaritos 2870',
     'Box de entrenamiento cruzado y levantamiento olímpico, con coaches certificados y grupos reducidos.',
     'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=1200&q=80');

-- Daily one-hour slots per gym (Horario has no weekday, so each slot repeats every day)
INSERT INTO horario (gimnasio_id, hora_inicio, hora_fin) VALUES
    (1, '07:00', '08:00'),
    (1, '08:00', '09:00'),
    (1, '18:00', '19:00'),
    (1, '19:00', '20:00'),
    (1, '20:00', '21:00'),
    (2, '09:00', '10:00'),
    (2, '12:00', '13:00'),
    (2, '18:30', '19:30'),
    (2, '19:30', '20:30'),
    (3, '06:30', '07:30'),
    (3, '07:30', '08:30'),
    (3, '19:00', '20:00'),
    (3, '20:00', '21:00');
