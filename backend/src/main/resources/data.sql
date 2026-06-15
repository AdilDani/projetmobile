-- ============================================================
-- FleetTracking seed data  —  updated schema
-- Runs after Hibernate creates the tables (idempotent).
-- Re-running is safe: existing rows are kept (ON CONFLICT DO NOTHING).
-- NOTE: if you previously seeded with the OLD schema, run
--   docker compose down -v && docker compose up -d --build
-- to wipe the volume so the tables are recreated from scratch.
-- ============================================================

-- Admin account
INSERT INTO admin_user (id, nom, login, password) VALUES
  ('a1', 'Admin Système', 'admin', 'admin2026')
ON CONFLICT (id) DO NOTHING;

-- Chauffeurs
INSERT INTO chauffeur (id, nom, telephone, email, permis, login, password, statut, photo) VALUES
  ('c1', 'Ahmed Benali',    '0612345678', 'ahmed.benali@fleet.ma',    'AB-2018-4471', 'ahmed.benali',    'ahmed@2026',   'Actif',   NULL),
  ('c2', 'Youssef Karim',   '0698765432', 'youssef.karim@fleet.ma',   'YK-2017-1185', 'youssef.karim',   'youssef@2026', 'Actif',   NULL),
  ('c3', 'Mohamed Idrissi', '0655667788', 'mohamed.idrissi@fleet.ma', 'MI-2019-9920', 'mohamed.idrissi', 'mohamed@2026', 'Actif',   NULL),
  ('c4', 'Said Amine',      '0611223344', 'said.amine@fleet.ma',      'SA-2016-7732', 'said.amine',      'said@2026',    'Inactif', NULL)
ON CONFLICT (id) DO NOTHING;

-- Vehicules  (prochaine_vidange / controle_technique replaced by vidange_cible_km / controle_technique_date)
INSERT INTO vehicule (id, marque, modele, immatriculation, annee, kilometrage, statut, carburant_pct, consommation, photo, vidange_cible_km, controle_technique_date, conducteur_id, lat, lng, vitesse) VALUES
  ('v1', 'Renault', 'Master',  'AA-123-BB', 2021, 145320, 'Disponible',   65, 8.2, NULL, 147820, '2026-11-12', NULL, 33.5731, -7.5898,  0),
  ('v2', 'Dacia',   'Dokker',  'CC-456-DD', 2020,  98750, 'En mission',   40, 7.6, NULL,  99950, '2026-09-03', 'c2', 33.5950, -7.6190, 48),
  ('v3', 'Peugeot', 'Boxer',   'EE-789-FF', 2022,  45780, 'Indisponible', 80, 9.0, NULL,  49780, '2027-01-20', NULL, 33.5600, -7.6100,  0),
  ('v4', 'Iveco',   'Daily',   'GG-012-HH', 2019, 187200, 'Disponible',   55, 9.1, NULL, 187000, '2026-06-10', NULL, 33.5820, -7.6300,  0),
  ('v5', 'Ford',    'Transit', 'II-345-JJ', 2021, 112400, 'En mission',   25, 8.8, NULL, 115500, '2026-10-28', 'c1', 33.5500, -7.5950, 72)
ON CONFLICT (id) DO NOTHING;

-- Incidents
INSERT INTO incident (id, chauffeur_id, chauffeur_nom, vehicule_id, vehicule_nom, immatriculation, type, description, event_date, statut) VALUES
  ('i1', 'c2', 'Youssef Karim', 'v2', 'Dacia Dokker', 'CC-456-DD', 'Accident / Collision', 'Collision arrière au feu rouge.',              '30/05/2026', 'En cours'),
  ('i2', 'c1', 'Ahmed Benali',  'v5', 'Ford Transit', 'II-345-JJ', 'Pneu crevé',           'Pneu avant droit crevé sur autoroute.',        '29/05/2026', 'En cours'),
  ('i3', 'c2', 'Youssef Karim', 'v2', 'Dacia Dokker', 'CC-456-DD', 'Bris de glace',        'Rayure côté droit, pare-brise intact.',        '28/05/2026', 'Résolu'),
  ('i4', 'c1', 'Ahmed Benali',  'v5', 'Ford Transit', 'II-345-JJ', 'Panne mécanique',      'Le moteur fait un bruit anormal au démarrage.','26/05/2026', 'Résolu')
ON CONFLICT (id) DO NOTHING;

-- Entretiens (new schema)
-- Auto-generated: one vidange (km-based) + one CT (date-based) per vehicle.
-- v4 vidange is intentionally overdue (vehicle at 187200, cible at 187000).
-- v4 CT is also overdue (due 2026-06-10, today is 2026-06-15).
-- Plus two admin-created custom ones and two historique entries.
INSERT INTO entretien (id, vehicule_id, vehicule_nom, immatriculation, type, est_km_base, cible_km, cible_date, interval_km, interval_jours, statut, date_effectuee, retard_jours) VALUES
  -- Vidanges (km-based, recurring every 15 000 km)
  ('e1',  'v1', 'Renault Master', 'AA-123-BB', 'Vidange',            true,  147820, NULL,         15000, 0,   'aVenir',   NULL,         0),
  ('e2',  'v2', 'Dacia Dokker',   'CC-456-DD', 'Vidange',            true,   99950, NULL,         15000, 0,   'aVenir',   NULL,         0),
  ('e3',  'v3', 'Peugeot Boxer',  'EE-789-FF', 'Vidange',            true,   49780, NULL,         15000, 0,   'aVenir',   NULL,         0),
  ('e4',  'v4', 'Iveco Daily',    'GG-012-HH', 'Vidange',            true,  187000, NULL,         15000, 0,   'aVenir',   NULL,         0),
  ('e5',  'v5', 'Ford Transit',   'II-345-JJ', 'Vidange',            true,  115500, NULL,         15000, 0,   'aVenir',   NULL,         0),
  -- Contrôles Techniques (date-based, recurring every 365 days)
  ('e6',  'v1', 'Renault Master', 'AA-123-BB', 'Contrôle Technique', false, 0,      '2026-11-12', 0,    365, 'aVenir',   NULL,         0),
  ('e7',  'v2', 'Dacia Dokker',   'CC-456-DD', 'Contrôle Technique', false, 0,      '2026-09-03', 0,    365, 'aVenir',   NULL,         0),
  ('e8',  'v3', 'Peugeot Boxer',  'EE-789-FF', 'Contrôle Technique', false, 0,      '2027-01-20', 0,    365, 'aVenir',   NULL,         0),
  ('e9',  'v4', 'Iveco Daily',    'GG-012-HH', 'Contrôle Technique', false, 0,      '2026-06-10', 0,    365, 'aVenir',   NULL,         0),
  ('e10', 'v5', 'Ford Transit',   'II-345-JJ', 'Contrôle Technique', false, 0,      '2026-10-28', 0,    365, 'aVenir',   NULL,         0),
  -- Admin-created one-off entretiens
  ('e11', 'v2', 'Dacia Dokker',   'CC-456-DD', 'Changement pneus',   false, 0,      '2026-07-01', 0,    0,   'aVenir',   NULL,         0),
  ('e12', 'v5', 'Ford Transit',   'II-345-JJ', 'Révision générale',  false, 0,      '2026-08-15', 0,    0,   'aVenir',   NULL,         0),
  -- Historique (already done)
  ('e13', 'v1', 'Renault Master', 'AA-123-BB', 'Vidange',            true,  130000, NULL,         15000, 0,   'effectue', '2026-02-10', 0),
  ('e14', 'v5', 'Ford Transit',   'II-345-JJ', 'Contrôle Technique', false, 0,      '2026-01-15', 0,    365, 'effectue', '2026-01-17', 2)
ON CONFLICT (id) DO NOTHING;

-- Trajets
INSERT INTO trajet (id, trajet_date, chauffeur_id, vehicule_id, vehicule_nom, depart, arrivee, heure_depart, heure_arrivee, distance_km, duree, vitesse_moyenne, consommation, en_cours, depart_lat, depart_lng, arrivee_lat, arrivee_lng) VALUES
  ('t1', '30/05/2026', 'c1', 'v5', 'Ford Transit', 'Rabat - Siège',      'Casablanca - Client',  '08:30:00', '12:15:00', 120, '3h 45min', 68, 8.2, false, 34.0209, -6.8416, 33.5731, -7.5898),
  ('t2', '29/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Casablanca - Dépôt', 'Mohammedia - Client',  '09:00:00', '10:30:00',  85, '1h 30min', 56, 7.6, false, 33.5950, -7.6190, 33.6861, -7.3828),
  ('t3', '28/05/2026', 'c1', 'v5', 'Ford Transit', 'Casablanca - Siège', 'Marrakech - Chantier', '06:00:00', '09:40:00', 150, '3h 40min', 62, 9.1, false, 33.5731, -7.5898, 31.6295, -7.9811),
  ('t4', '27/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Rabat - Siège',      'Kénitra - Client',     '14:00:00', '15:50:00', 110, '1h 50min', 60, 7.9, false, 34.0209, -6.8416, 34.2610, -6.5802)
ON CONFLICT (id) DO NOTHING;

-- Trajets avec waypoints GPS — zone Rabat–Kénitra
INSERT INTO trajet (id, trajet_date, chauffeur_id, vehicule_id, vehicule_nom, depart, arrivee, heure_depart, heure_arrivee, distance_km, duree, vitesse_moyenne, consommation, en_cours, depart_lat, depart_lng, arrivee_lat, arrivee_lng, waypoints) VALUES

-- t5 : Rabat Agdal → Kénitra centre (N1, ~47 km, Ahmed / Ford Transit)
('t5', '13/06/2026', 'c1', 'v5', 'Ford Transit', 'Rabat - Agdal', 'Kénitra - Centre', '07:15:00', '08:20:00', 47, '1h 05min', 64, 4.1, false,
 34.0061, -6.8546, 34.2610, -6.5802,
 '[[34.0061,-6.8546],[34.0120,-6.8490],[34.0209,-6.8416],[34.0280,-6.8310],[34.0382,-6.8134],[34.0450,-6.8010],[34.0531,-6.7987],[34.0620,-6.7820],[34.0720,-6.7580],[34.0870,-6.7310],[34.1010,-6.7050],[34.1160,-6.6820],[34.1320,-6.6590],[34.1500,-6.6390],[34.1680,-6.6210],[34.1890,-6.6080],[34.2080,-6.5990],[34.2280,-6.5920],[34.2450,-6.5860],[34.2610,-6.5802]]'),

-- t6 : Kénitra centre → Rabat Hassan (retour N1, ~47 km, Youssef / Dacia Dokker)
('t6', '13/06/2026', 'c2', 'v2', 'Dacia Dokker', 'Kénitra - Centre', 'Rabat - Hassan', '09:00:00', '10:05:00', 47, '1h 05min', 61, 3.6, false,
 34.2610, -6.5802, 34.0132, -6.8326,
 '[[34.2610,-6.5802],[34.2450,-6.5855],[34.2270,-6.5915],[34.2080,-6.5990],[34.1880,-6.6085],[34.1670,-6.6215],[34.1490,-6.6400],[34.1310,-6.6600],[34.1150,-6.6830],[34.1000,-6.7060],[34.0850,-6.7320],[34.0710,-6.7590],[34.0610,-6.7830],[34.0520,-6.8000],[34.0440,-6.8060],[34.0350,-6.8140],[34.0250,-6.8240],[34.0180,-6.8300],[34.0132,-6.8326]]'),

-- t7 : Rabat Hassan → Salé Tabriquet → retour Rabat Agdal (circuit local, ~18 km, Mohamed / Peugeot Boxer)
('t7', '12/06/2026', 'c3', 'v3', 'Peugeot Boxer', 'Rabat - Hassan', 'Rabat - Agdal', '11:30:00', '12:05:00', 18, '35min 00s', 31, 1.6, false,
 34.0132, -6.8326, 34.0061, -6.8546,
 '[[34.0132,-6.8326],[34.0200,-6.8260],[34.0300,-6.8160],[34.0382,-6.8134],[34.0450,-6.8040],[34.0531,-6.7987],[34.0590,-6.7910],[34.0650,-6.7840],[34.0610,-6.7750],[34.0540,-6.7810],[34.0430,-6.7990],[34.0360,-6.8080],[34.0280,-6.8200],[34.0200,-6.8320],[34.0130,-6.8420],[34.0080,-6.8490],[34.0061,-6.8546]]'),

-- t8 : Kénitra dépôt → Sidi Allal el Bahraoui → retour Kénitra (livraison, ~30 km, Said / Iveco Daily)
('t8', '11/06/2026', 'c4', 'v4', 'Iveco Daily', 'Kénitra - Dépôt', 'Kénitra - Dépôt', '13:00:00', '13:45:00', 30, '45min 00s', 40, 2.7, false,
 34.2610, -6.5802, 34.2610, -6.5802,
 '[[34.2610,-6.5802],[34.2530,-6.5870],[34.2430,-6.5960],[34.2300,-6.6060],[34.2150,-6.6160],[34.2000,-6.6250],[34.1860,-6.6300],[34.1730,-6.6350],[34.1600,-6.6420],[34.1480,-6.6490],[34.1400,-6.6550],[34.1320,-6.6590],[34.1400,-6.6550],[34.1510,-6.6480],[34.1660,-6.6360],[34.1820,-6.6280],[34.1980,-6.6230],[34.2140,-6.6140],[34.2310,-6.6040],[34.2460,-6.5940],[34.2560,-6.5870],[34.2610,-6.5802]]')

ON CONFLICT (id) DO NOTHING;
