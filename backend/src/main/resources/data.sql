-- ============================================================
-- FleetTracking seed data
-- Runs after Hibernate creates the tables (idempotent).
-- Re-running is safe: existing rows are kept (ON CONFLICT DO NOTHING).
-- NOTE: if you previously seeded with the OLD schema, run
--   docker compose down -v
-- to wipe the volume so existing rows match the new columns.
-- ============================================================

-- Admin account (used to log in as administrator)
INSERT INTO admin_user (id, nom, login, password) VALUES
  ('a1', 'Admin Système', 'admin', 'admin2026')
ON CONFLICT (id) DO NOTHING;

-- Chauffeurs (login / password are used for driver authentication).
-- A chauffeur is NOT permanently tied to a vehicle anymore: the link
-- lives on the vehicule row (conducteur_id) and only for "En mission" cars.
INSERT INTO chauffeur (id, nom, telephone, email, permis, login, password, statut, photo) VALUES
  ('c1', 'Ahmed Benali',    '0612345678', 'ahmed.benali@fleet.ma',    'AB-2018-4471', 'ahmed.benali',    'ahmed@2026',   'Actif',   NULL),
  ('c2', 'Youssef Karim',   '0698765432', 'youssef.karim@fleet.ma',   'YK-2017-1185', 'youssef.karim',   'youssef@2026', 'Actif',   NULL),
  ('c3', 'Mohamed Idrissi', '0655667788', 'mohamed.idrissi@fleet.ma', 'MI-2019-9920', 'mohamed.idrissi', 'mohamed@2026', 'Actif',   NULL),
  ('c4', 'Said Amine',      '0611223344', 'said.amine@fleet.ma',      'SA-2016-7732', 'said.amine',      'said@2026',    'Inactif', NULL)
ON CONFLICT (id) DO NOTHING;

-- Vehicules.
-- Statut is one of: Disponible / Indisponible / En mission.
-- Only "En mission" vehicles carry a conducteur_id; the others are NULL.
INSERT INTO vehicule (id, marque, modele, immatriculation, annee, kilometrage, statut, carburant_pct, consommation, photo, prochaine_vidange, controle_technique, conducteur_id, lat, lng, vitesse) VALUES
  ('v1', 'Renault', 'Master',  'AA-123-BB', 2021, 145320, 'Disponible',   65, 8.2, NULL, 'Dans 2 500 km', '12/11/2026', NULL, 33.5731, -7.5898,  0),
  ('v2', 'Dacia',   'Dokker',  'CC-456-DD', 2020,  98750, 'En mission',   40, 7.6, NULL, 'Dans 1 200 km', '03/09/2026', 'c2', 33.5950, -7.6190, 48),
  ('v3', 'Peugeot', 'Boxer',   'EE-789-FF', 2022,  45780, 'Indisponible', 80, 9.0, NULL, 'Dans 4 000 km', '20/01/2027', NULL, 33.5600, -7.6100,  0),
  ('v4', 'Iveco',   'Daily',   'GG-012-HH', 2019, 187200, 'Disponible',   55, 9.1, NULL, 'Dans 800 km',   '15/07/2026', NULL, 33.5820, -7.6300,  0),
  ('v5', 'Ford',    'Transit', 'II-345-JJ', 2021, 112400, 'En mission',   25, 8.8, NULL, 'Dans 3 100 km', '28/10/2026', 'c1', 33.5500, -7.5950, 72)
ON CONFLICT (id) DO NOTHING;

-- Incidents (declared by chauffeurs about the vehicle they were driving).
INSERT INTO incident (id, chauffeur_id, chauffeur_nom, vehicule_id, vehicule_nom, immatriculation, type, description, event_date, statut) VALUES
  ('i1', 'c2', 'Youssef Karim', 'v2', 'Dacia Dokker', 'CC-456-DD', 'Accident / Collision', 'Collision arrière au feu rouge.',              '30/05/2026', 'En cours'),
  ('i2', 'c1', 'Ahmed Benali',  'v5', 'Ford Transit', 'II-345-JJ', 'Pneu crevé',           'Pneu avant droit crevé sur autoroute.',        '29/05/2026', 'En cours'),
  ('i3', 'c2', 'Youssef Karim', 'v2', 'Dacia Dokker', 'CC-456-DD', 'Bris de glace',        'Rayure côté droit, pare-brise intact.',        '28/05/2026', 'Résolu'),
  ('i4', 'c1', 'Ahmed Benali',  'v5', 'Ford Transit', 'II-345-JJ', 'Panne mécanique',      'Le moteur fait un bruit anormal au démarrage.','26/05/2026', 'Résolu')
ON CONFLICT (id) DO NOTHING;

-- Entretiens
INSERT INTO entretien (id, type, vehicule_nom, immatriculation, entretien_date, echeance, a_venir) VALUES
  ('e1', 'Vidange',           'Renault Master', 'AA-123-BB', '05/06/2026', 'Dans 6 jours',  true),
  ('e2', 'Changement pneus',  'Iveco Daily',    'GG-012-HH', '01/06/2026', 'Dans 2 jours',  true),
  ('e3', 'Contrôle technique','Dacia Dokker',   'CC-456-DD', '15/06/2026', 'Dans 16 jours', true),
  ('e4', 'Révision générale', 'Peugeot Boxer',  'EE-789-FF', '20/06/2026', 'Dans 21 jours', true),
  ('e5', 'Vidange',           'Ford Transit',   'II-345-JJ', '10/05/2026', 'Effectué',      false),
  ('e6', 'Changement freins', 'Renault Master', 'AA-123-BB', '02/05/2026', 'Effectué',      false)
ON CONFLICT (id) DO NOTHING;

-- Trajets (completed trips; en_cours = false). Tied to the chauffeur + vehicle.
INSERT INTO trajet (id, trajet_date, chauffeur_id, vehicule_id, vehicule_nom, depart, arrivee, heure_depart, heure_arrivee, distance_km, duree, vitesse_moyenne, consommation, en_cours, depart_lat, depart_lng, arrivee_lat, arrivee_lng) VALUES
  ('t1', '30/05/2026', 'c1', 'v5', 'Ford Transit', 'Rabat - Siège',      'Casablanca - Client',  '08:30', '12:15', 120, '03h 45', 68, 8.2, false, 34.0209, -6.8416, 33.5731, -7.5898),
  ('t2', '29/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Casablanca - Dépôt', 'Mohammedia - Client',  '09:00', '10:30',  85, '01h 30', 56, 7.6, false, 33.5950, -7.6190, 33.6861, -7.3828),
  ('t3', '28/05/2026', 'c1', 'v5', 'Ford Transit', 'Casablanca - Siège', 'Marrakech - Chantier', '06:00', '09:40', 150, '03h 40', 62, 9.1, false, 33.5731, -7.5898, 31.6295, -7.9811),
  ('t4', '27/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Rabat - Siège',      'Kénitra - Client',     '14:00', '15:50', 110, '01h 50', 60, 7.9, false, 34.0209, -6.8416, 34.2610, -6.5802)
ON CONFLICT (id) DO NOTHING;
