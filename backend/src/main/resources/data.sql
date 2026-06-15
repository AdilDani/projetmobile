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

-- Trajets — all 8 include GPS waypoints for smooth polyline rendering
INSERT INTO trajet (id, trajet_date, chauffeur_id, vehicule_id, vehicule_nom, depart, arrivee, heure_depart, heure_arrivee, distance_km, duree, vitesse_moyenne, consommation, en_cours, depart_lat, depart_lng, arrivee_lat, arrivee_lng, waypoints) VALUES

-- t1 : Rabat Siège → Casablanca Client (A1 autoroute, ~120 km, Ahmed / Ford Transit)
('t1', '30/05/2026', 'c1', 'v5', 'Ford Transit', 'Rabat - Siège', 'Casablanca - Client', '08:30:00', '12:15:00', 120, '3h 45min', 68, 8.2, false,
 34.0209, -6.8416, 33.5731, -7.5898,
 '[[34.0209,-6.8416],[34.0050,-6.8560],[33.9820,-6.8780],[33.9550,-6.9080],[33.9250,-6.9420],[33.8950,-6.9780],[33.8650,-7.0150],[33.8350,-7.0530],[33.8050,-7.0920],[33.7780,-7.1310],[33.7510,-7.1700],[33.7240,-7.2090],[33.6970,-7.2520],[33.6700,-7.2970],[33.6430,-7.3420],[33.6180,-7.3880],[33.5960,-7.4360],[33.5800,-7.4780],[33.5731,-7.5898]]'),

-- t2 : Casablanca Dépôt → Mohammedia Client (côtière N1, ~25 km, Youssef / Dacia Dokker)
('t2', '29/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Casablanca - Dépôt', 'Mohammedia - Client', '09:00:00', '10:30:00', 25, '1h 30min', 56, 7.6, false,
 33.5950, -7.6190, 33.6861, -7.3828,
 '[[33.5950,-7.6190],[33.6010,-7.5980],[33.6080,-7.5760],[33.6150,-7.5520],[33.6220,-7.5280],[33.6300,-7.5040],[33.6390,-7.4800],[33.6480,-7.4560],[33.6570,-7.4320],[33.6650,-7.4100],[33.6720,-7.3950],[33.6861,-7.3828]]'),

-- t3 : Casablanca Siège → Marrakech Chantier (A7 autoroute, ~240 km, Ahmed / Ford Transit)
('t3', '28/05/2026', 'c1', 'v5', 'Ford Transit', 'Casablanca - Siège', 'Marrakech - Chantier', '06:00:00', '09:40:00', 240, '3h 40min', 65, 9.1, false,
 33.5731, -7.5898, 31.6295, -7.9811,
 '[[33.5731,-7.5898],[33.5420,-7.6150],[33.5100,-7.6400],[33.4750,-7.6700],[33.4380,-7.6960],[33.4010,-7.7210],[33.3640,-7.7450],[33.3250,-7.7690],[33.2840,-7.7900],[33.2420,-7.8090],[33.1990,-7.8280],[33.1560,-7.8440],[33.1110,-7.8580],[33.0640,-7.8700],[33.0160,-7.8810],[32.9670,-7.8920],[32.9160,-7.9020],[32.8640,-7.9110],[32.8090,-7.9200],[32.7520,-7.9300],[32.6940,-7.9420],[32.6340,-7.9540],[32.5730,-7.9640],[32.5090,-7.9710],[32.4420,-7.9780],[32.3740,-7.9800],[32.3040,-7.9800],[32.2330,-7.9800],[32.1600,-7.9805],[32.0840,-7.9808],[31.9980,-7.9810],[31.9100,-7.9810],[31.8190,-7.9811],[31.7250,-7.9811],[31.6295,-7.9811]]'),

-- t4 : Rabat Siège → Kénitra Client (N1, ~47 km, Youssef / Dacia Dokker)
('t4', '27/05/2026', 'c2', 'v2', 'Dacia Dokker', 'Rabat - Siège', 'Kénitra - Client', '14:00:00', '15:50:00', 47, '1h 50min', 60, 7.9, false,
 34.0209, -6.8416, 34.2610, -6.5802,
 '[[34.0209,-6.8416],[34.0280,-6.8330],[34.0382,-6.8134],[34.0470,-6.8010],[34.0531,-6.7987],[34.0630,-6.7820],[34.0740,-6.7570],[34.0870,-6.7300],[34.1010,-6.7040],[34.1170,-6.6810],[34.1340,-6.6580],[34.1520,-6.6370],[34.1700,-6.6190],[34.1910,-6.6070],[34.2100,-6.5980],[34.2300,-6.5910],[34.2470,-6.5850],[34.2610,-6.5802]]'),

-- t5 : Rabat Agdal → Kénitra Centre (N1, ~47 km, Ahmed / Ford Transit)
('t5', '13/06/2026', 'c1', 'v5', 'Ford Transit', 'Rabat - Agdal', 'Kénitra - Centre', '07:15:00', '08:20:00', 47, '1h 05min', 64, 4.1, false,
 34.0061, -6.8546, 34.2610, -6.5802,
 '[[34.0061,-6.8546],[34.0110,-6.8495],[34.0170,-6.8450],[34.0209,-6.8416],[34.0260,-6.8360],[34.0330,-6.8270],[34.0382,-6.8134],[34.0430,-6.8060],[34.0480,-6.8000],[34.0531,-6.7987],[34.0590,-6.7900],[34.0660,-6.7760],[34.0740,-6.7580],[34.0820,-6.7410],[34.0900,-6.7240],[34.0990,-6.7080],[34.1080,-6.6930],[34.1180,-6.6790],[34.1290,-6.6650],[34.1400,-6.6510],[34.1520,-6.6390],[34.1650,-6.6280],[34.1790,-6.6180],[34.1930,-6.6100],[34.2070,-6.6030],[34.2210,-6.5970],[34.2360,-6.5920],[34.2490,-6.5870],[34.2580,-6.5830],[34.2610,-6.5802]]'),

-- t6 : Kénitra Centre → Rabat Hassan (retour N1, ~47 km, Youssef / Dacia Dokker)
('t6', '13/06/2026', 'c2', 'v2', 'Dacia Dokker', 'Kénitra - Centre', 'Rabat - Hassan', '09:00:00', '10:05:00', 47, '1h 05min', 61, 3.6, false,
 34.2610, -6.5802, 34.0132, -6.8326,
 '[[34.2610,-6.5802],[34.2560,-6.5840],[34.2480,-6.5880],[34.2370,-6.5930],[34.2240,-6.5980],[34.2100,-6.6050],[34.1960,-6.6120],[34.1820,-6.6200],[34.1680,-6.6310],[34.1540,-6.6420],[34.1410,-6.6540],[34.1290,-6.6670],[34.1180,-6.6810],[34.1070,-6.6950],[34.0960,-6.7100],[34.0850,-6.7260],[34.0740,-6.7430],[34.0640,-6.7620],[34.0560,-6.7810],[34.0500,-6.7960],[34.0440,-6.8060],[34.0370,-6.8130],[34.0290,-6.8210],[34.0220,-6.8270],[34.0132,-6.8326]]'),

-- t7 : Rabat Hassan → Salé → retour Rabat Agdal (circuit urbain, ~18 km, Mohamed / Peugeot Boxer)
('t7', '12/06/2026', 'c3', 'v3', 'Peugeot Boxer', 'Rabat - Hassan', 'Rabat - Agdal', '11:30:00', '12:05:00', 18, '35min 00s', 31, 1.6, false,
 34.0132, -6.8326, 34.0061, -6.8546,
 '[[34.0132,-6.8326],[34.0175,-6.8285],[34.0220,-6.8240],[34.0280,-6.8185],[34.0340,-6.8150],[34.0382,-6.8134],[34.0420,-6.8080],[34.0460,-6.8040],[34.0510,-6.8010],[34.0531,-6.7987],[34.0570,-6.7940],[34.0610,-6.7890],[34.0645,-6.7850],[34.0640,-6.7800],[34.0610,-6.7760],[34.0570,-6.7800],[34.0530,-6.7840],[34.0480,-6.7920],[34.0420,-6.8000],[34.0360,-6.8070],[34.0300,-6.8150],[34.0230,-6.8240],[34.0170,-6.8360],[34.0120,-6.8440],[34.0085,-6.8500],[34.0061,-6.8546]]'),

-- t8 : Kénitra Dépôt → Sidi Allal el Bahraoui → retour Kénitra (livraison boucle, ~32 km, Said / Iveco Daily)
-- Start: dépôt gate (34.2620,-6.5790)  End: dépôt parking (34.2595,-6.5815) — distinct so both flags render
('t8', '11/06/2026', 'c4', 'v4', 'Iveco Daily', 'Kénitra - Dépôt', 'Kénitra - Dépôt (retour)', '13:00:00', '13:50:00', 32, '50min 00s', 38, 2.9, false,
 34.2620, -6.5790, 34.2595, -6.5815,
 '[[34.2620,-6.5790],[34.2560,-6.5860],[34.2480,-6.5940],[34.2380,-6.6030],[34.2260,-6.6110],[34.2130,-6.6210],[34.2000,-6.6280],[34.1870,-6.6330],[34.1740,-6.6390],[34.1610,-6.6460],[34.1490,-6.6530],[34.1390,-6.6580],[34.1300,-6.6600],[34.1390,-6.6580],[34.1500,-6.6510],[34.1640,-6.6430],[34.1790,-6.6350],[34.1940,-6.6270],[34.2090,-6.6190],[34.2240,-6.6100],[34.2380,-6.6010],[34.2490,-6.5920],[34.2560,-6.5870],[34.2595,-6.5815]]')

ON CONFLICT (id) DO NOTHING;
