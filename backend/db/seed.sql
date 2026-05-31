-- ============================================================
-- FleetTracking seed data
-- Runs after Hibernate creates the tables (idempotent).
-- Re-running is safe: existing rows are kept (ON CONFLICT DO NOTHING).
-- ============================================================

-- Admin account (used to log in as administrator)
INSERT INTO admin_user (id, nom, login, password) VALUES
  ('a1', 'Admin Système', 'admin', 'admin2026')
ON CONFLICT (id) DO NOTHING;

-- Chauffeurs (login / password are used for driver authentication)
INSERT INTO chauffeur (id, nom, telephone, email, vehicule_affecte, permis, login, password, statut) VALUES
  ('c1', 'Ahmed Benali',    '0612345678', 'ahmed.benali@fleet.ma',    'Renault Master', 'AB-2018-4471', 'ahmed.benali',    'ahmed@2026',   'Actif'),
  ('c2', 'Youssef Karim',   '0698765432', 'youssef.karim@fleet.ma',   'Dacia Dokker',   'YK-2017-1185', 'youssef.karim',   'youssef@2026', 'Actif'),
  ('c3', 'Mohamed Idrissi', '0655667788', 'mohamed.idrissi@fleet.ma', 'Peugeot Boxer',  'MI-2019-9920', 'mohamed.idrissi', 'mohamed@2026', 'Actif'),
  ('c4', 'Said Amine',      '0611223344', 'said.amine@fleet.ma',      'Iveco Daily',    'SA-2016-7732', 'said.amine',      'said@2026',    'Inactif')
ON CONFLICT (id) DO NOTHING;

-- Vehicules
INSERT INTO vehicule (id, marque, modele, immatriculation, annee, kilometrage, statut, carburant_pct, prochaine_vidange, controle_technique, conducteur_id, lat, lng, vitesse) VALUES
  ('v1', 'Renault', 'Master',  'AA-123-BB', 2021, 145320, 'Disponible',  65, 'Dans 2 500 km', '12/11/2026', 'c1',  33.5731, -7.5898, 65),
  ('v2', 'Dacia',   'Dokker',  'CC-456-DD', 2020,  98750, 'En mission',  40, 'Dans 1 200 km', '03/09/2026', 'c2',  33.5950, -7.6190, 48),
  ('v3', 'Peugeot', 'Boxer',   'EE-789-FF', 2022,  45780, 'Maintenance', 80, 'Dans 4 000 km', '20/01/2027', 'c3',  33.5600, -7.6100,  0),
  ('v4', 'Iveco',   'Daily',   'GG-012-HH', 2019, 187200, 'Disponible',  55, 'Dans 800 km',   '15/07/2026', 'c4',  33.5820, -7.6300,  0),
  ('v5', 'Ford',    'Transit', 'II-345-JJ', 2021, 112400, 'En mission',  25, 'Dans 3 100 km', '28/10/2026', NULL,  33.5500, -7.5950, 72)
ON CONFLICT (id) DO NOTHING;

-- Incidents
INSERT INTO incident (id, vehicule_nom, immatriculation, type, description, event_date, statut) VALUES
  ('i1', 'Renault Master', 'AA-123-BB', 'Accident / Collision', 'Collision arrière au feu rouge.',            '30/05/2026', 'En cours'),
  ('i2', 'Dacia Dokker',   'CC-456-DD', 'Pneu crevé',           'Pneu avant droit crevé sur autoroute.',       '29/05/2026', 'En cours'),
  ('i3', 'Peugeot Boxer',  'EE-789-FF', 'Bris de glace',        'Rayure côté droit, pare-brise intact.',       '28/05/2026', 'Résolu'),
  ('i4', 'Iveco Daily',    'GG-012-HH', 'Panne mécanique',      'Le moteur fait un bruit anormal au démarrage.','26/05/2026', 'Résolu')
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

-- Trajets
INSERT INTO trajet (id, trajet_date, vehicule_nom, depart, arrivee, heure_depart, heure_arrivee, distance_km, duree, vitesse_moyenne, consommation) VALUES
  ('t1', '30/05/2026', 'Renault Master', 'Rabat - Siège',      'Casablanca - Client',  '08:30', '12:15', 120, '03h 45', 68, 8.2),
  ('t2', '29/05/2026', 'Dacia Dokker',   'Casablanca - Dépôt', 'Mohammedia - Client',  '09:00', '10:30',  85, '01h 30', 56, 7.6),
  ('t3', '28/05/2026', 'Iveco Daily',    'Casablanca - Siège', 'Marrakech - Chantier', '06:00', '09:40', 150, '03h 40', 62, 9.1),
  ('t4', '27/05/2026', 'Renault Master', 'Rabat - Siège',      'Kénitra - Client',     '14:00', '15:50', 110, '01h 50', 60, 8.0)
ON CONFLICT (id) DO NOTHING;
