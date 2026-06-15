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

-- Trajets -- all 8 include GPS waypoints; distances/durations derived from actual haversine path length
INSERT INTO trajet (id, trajet_date, chauffeur_id, vehicule_id, vehicule_nom, depart, arrivee, heure_depart, heure_arrivee, distance_km, duree, vitesse_moyenne, consommation, en_cours, depart_lat, depart_lng, arrivee_lat, arrivee_lng, waypoints) VALUES

-- t1: Rabat Siege -> Casablanca Client (A1, 89 km haversine, 90 km/h avg, 59 min)
('t1', '30/05/2026', 'c1', 'v5', 'Ford Transit', 'Rabat - Siege', 'Casablanca - Client',
 '08:30:00', '09:29:00', 89, '59min', 90, 7.8, false,
 34.0209, -6.8416, 33.5731, -7.5898,
 '[[34.0209,-6.8416],[33.9980,-6.8520],[33.9700,-6.8680],[33.9400,-6.8920],[33.9100,-6.9160],[33.8800,-6.9440],[33.8520,-6.9760],[33.8230,-7.0130],[33.7950,-7.0520],[33.7640,-7.0940],[33.7350,-7.1380],[33.7090,-7.1820],[33.6850,-7.2270],[33.6620,-7.2750],[33.6410,-7.3240],[33.6190,-7.3750],[33.5990,-7.4270],[33.5810,-7.4780],[33.5780,-7.5170],[33.5750,-7.5530],[33.5731,-7.5898]]'),

-- t2: Casablanca Depot -> Mohammedia Client (N1, 24 km haversine, 60 km/h avg, 24 min)
('t2', '29/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Casablanca - Depot', 'Mohammedia - Client',
 '09:00:00', '09:24:00', 24, '24min', 61, 1.9, false,
 33.5950, -7.6190, 33.6861, -7.3828,
 '[[33.5950,-7.6190],[33.6010,-7.5980],[33.6080,-7.5760],[33.6150,-7.5520],[33.6220,-7.5280],[33.6300,-7.5040],[33.6390,-7.4800],[33.6480,-7.4560],[33.6570,-7.4320],[33.6650,-7.4100],[33.6720,-7.3950],[33.6861,-7.3828]]'),

-- t3: Casablanca Siege -> Marrakech Chantier (A7, 222 km haversine, 100 km/h avg, 2h 13min)
('t3', '28/05/2026', 'c1', 'v5', 'Ford Transit', 'Casablanca - Siege', 'Marrakech - Chantier',
 '06:00:00', '08:13:00', 222, '2h 13min', 100, 19.5, false,
 33.5731, -7.5898, 31.6295, -7.9811,
 '[[33.5731,-7.5898],[33.5400,-7.6100],[33.5050,-7.6280],[33.4680,-7.6420],[33.4300,-7.6520],[33.3900,-7.6570],[33.3490,-7.6570],[33.3070,-7.6530],[33.2640,-7.6460],[33.2200,-7.6390],[33.1760,-7.6380],[33.1310,-7.6390],[33.0860,-7.6420],[33.0400,-7.6480],[32.9930,-7.6570],[32.9460,-7.6690],[32.8980,-7.6840],[32.8490,-7.7020],[32.7990,-7.7230],[32.7480,-7.7470],[32.6960,-7.7730],[32.6430,-7.8000],[32.5890,-7.8270],[32.5340,-7.8530],[32.4780,-7.8770],[32.4200,-7.8990],[32.3600,-7.9180],[32.2990,-7.9350],[32.2360,-7.9490],[32.1710,-7.9600],[32.1040,-7.9680],[32.0350,-7.9740],[31.9640,-7.9780],[31.8910,-7.9800],[31.8160,-7.9806],[31.7390,-7.9810],[31.6850,-7.9811],[31.6295,-7.9811]]'),

-- t4: Rabat Siege -> Kenitra Client (N1, 37 km haversine, 72 km/h avg, 31 min)
('t4', '27/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Rabat - Siege', 'Kenitra - Client',
 '14:00:00', '14:31:00', 37, '31min', 72, 2.8, false,
 34.0209, -6.8416, 34.2610, -6.5802,
 '[[34.0209,-6.8416],[34.0280,-6.8330],[34.0382,-6.8134],[34.0470,-6.8010],[34.0531,-6.7987],[34.0630,-6.7820],[34.0740,-6.7570],[34.0870,-6.7300],[34.1010,-6.7040],[34.1170,-6.6810],[34.1340,-6.6580],[34.1520,-6.6370],[34.1700,-6.6190],[34.1910,-6.6070],[34.2100,-6.5980],[34.2300,-6.5910],[34.2470,-6.5850],[34.2610,-6.5802]]'),

-- t5: Rabat Agdal -> Kenitra Centre (N1, 39 km haversine, 73 km/h avg, 32 min)
('t5', '13/06/2026', 'c1', 'v5', 'Ford Transit', 'Rabat - Agdal', 'Kenitra - Centre',
 '07:15:00', '07:47:00', 39, '32min', 73, 3.5, false,
 34.0061, -6.8546, 34.2610, -6.5802,
 '[[34.0061,-6.8546],[34.0110,-6.8495],[34.0170,-6.8450],[34.0209,-6.8416],[34.0260,-6.8360],[34.0330,-6.8270],[34.0382,-6.8134],[34.0430,-6.8060],[34.0480,-6.8000],[34.0531,-6.7987],[34.0590,-6.7900],[34.0660,-6.7760],[34.0740,-6.7580],[34.0820,-6.7410],[34.0900,-6.7240],[34.0990,-6.7080],[34.1080,-6.6930],[34.1180,-6.6790],[34.1290,-6.6650],[34.1400,-6.6510],[34.1520,-6.6390],[34.1650,-6.6280],[34.1790,-6.6180],[34.1930,-6.6100],[34.2070,-6.6030],[34.2210,-6.5970],[34.2360,-6.5920],[34.2490,-6.5870],[34.2580,-6.5830],[34.2610,-6.5802]]'),

-- t6: Kenitra Centre -> Rabat Hassan (N1 retour, 37 km haversine, 68 km/h avg, 32 min)
('t6', '13/06/2026', 'c2', 'v2', 'Dacia Dokker', 'Kenitra - Centre', 'Rabat - Hassan',
 '09:00:00', '09:32:00', 37, '32min', 68, 2.8, false,
 34.2610, -6.5802, 34.0132, -6.8326,
 '[[34.2610,-6.5802],[34.2560,-6.5840],[34.2480,-6.5880],[34.2370,-6.5930],[34.2240,-6.5980],[34.2100,-6.6050],[34.1960,-6.6120],[34.1820,-6.6200],[34.1680,-6.6310],[34.1540,-6.6420],[34.1410,-6.6540],[34.1290,-6.6670],[34.1180,-6.6810],[34.1070,-6.6950],[34.0960,-6.7100],[34.0850,-6.7260],[34.0740,-6.7430],[34.0640,-6.7620],[34.0560,-6.7810],[34.0500,-6.7960],[34.0440,-6.8060],[34.0370,-6.8130],[34.0290,-6.8210],[34.0220,-6.8270],[34.0132,-6.8326]]'),

-- t7: Rabat Hassan -> Sale -> Rabat Agdal (urbain, 18 km haversine, 30 km/h avg, 35 min)
('t7', '12/06/2026', 'c3', 'v3', 'Peugeot Boxer', 'Rabat - Hassan', 'Rabat - Agdal',
 '11:30:00', '12:05:00', 18, '35min', 30, 1.6, false,
 34.0132, -6.8326, 34.0061, -6.8546,
 '[[34.0132,-6.8326],[34.0175,-6.8285],[34.0220,-6.8240],[34.0280,-6.8185],[34.0340,-6.8150],[34.0382,-6.8134],[34.0420,-6.8080],[34.0460,-6.8040],[34.0510,-6.8010],[34.0531,-6.7987],[34.0570,-6.7940],[34.0610,-6.7890],[34.0645,-6.7850],[34.0640,-6.7800],[34.0610,-6.7760],[34.0570,-6.7800],[34.0530,-6.7840],[34.0480,-6.7920],[34.0420,-6.8000],[34.0360,-6.8070],[34.0300,-6.8150],[34.0230,-6.8240],[34.0170,-6.8360],[34.0120,-6.8440],[34.0085,-6.8500],[34.0061,-6.8546]]'),

-- t8: Kenitra Depot -> Sidi Allal -> retour Kenitra (boucle livraison, 33 km haversine, 55 km/h avg, 35 min)
-- depart=depot entree (34.2620,-6.5790), arrivee=depot parking (34.2595,-6.5815): distincts pour afficher 2 marqueurs
('t8', '11/06/2026', 'c4', 'v4', 'Iveco Daily', 'Kenitra - Depot', 'Kenitra - Depot (retour)',
 '13:00:00', '13:35:00', 33, '35min', 55, 3.0, false,
 34.2620, -6.5790, 34.2595, -6.5815,
 '[[34.2620,-6.5790],[34.2560,-6.5860],[34.2480,-6.5940],[34.2380,-6.6030],[34.2260,-6.6110],[34.2130,-6.6210],[34.2000,-6.6280],[34.1870,-6.6330],[34.1740,-6.6390],[34.1610,-6.6460],[34.1490,-6.6530],[34.1390,-6.6580],[34.1300,-6.6600],[34.1390,-6.6580],[34.1500,-6.6510],[34.1640,-6.6430],[34.1790,-6.6350],[34.1940,-6.6270],[34.2090,-6.6190],[34.2240,-6.6100],[34.2380,-6.6010],[34.2490,-6.5920],[34.2560,-6.5870],[34.2595,-6.5815]]')

ON CONFLICT (id) DO NOTHING;