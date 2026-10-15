-- Test-only seed: in the running system these copies arrive from ms-gimnasios.
-- Mirrors gym 1 ("Fuerza Barrio Gym") of ms-gimnasios' data.sql.
INSERT INTO horario_replica (id, gimnasio_id, gimnasio_nombre, gimnasio_comuna, gimnasio_imagen_url, hora_inicio, hora_fin) VALUES
    (1, 1, 'Fuerza Barrio Gym', 'Ñuñoa', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&q=80', '07:00', '08:00'),
    (2, 1, 'Fuerza Barrio Gym', 'Ñuñoa', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&q=80', '08:00', '09:00'),
    (3, 1, 'Fuerza Barrio Gym', 'Ñuñoa', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&q=80', '18:00', '19:00'),
    (4, 1, 'Fuerza Barrio Gym', 'Ñuñoa', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&q=80', '19:00', '20:00'),
    (5, 1, 'Fuerza Barrio Gym', 'Ñuñoa', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&q=80', '20:00', '21:00');
