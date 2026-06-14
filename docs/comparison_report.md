# Comparison Report: Adil's Version vs Wissal's Version

This document explains how the work done on `main` (Adil) differs from and improves upon the work done on `detached2` (Wissal). It is written from Adil's perspective to describe what changed and why.

---

## 1. Incident Declaration (Driver Side)

### Wissal's version
The driver could declare an incident by filling in a type, a description, and taking **one photo** using the camera. The photo was stored as a single Base64 string. The incident was sent to the backend using the old `Incident(id, vehiculeNom, immatriculation, type, description, date, statut, chauffeurNom, photos)` constructor. The driver's name was read directly from local storage (Prefs), not verified against the server.

### Adil's improvements
- **Multiple photos**: The form now shows a scrollable row of photo thumbnails. The driver can add as many photos as they want, and each one has a remove button. There is no longer a one-photo limit.
- **Camera and gallery**: Instead of camera-only, a dialog appears asking "Appareil photo / Galerie". Drivers can pick an existing image from their gallery instead of being forced to take a new photo.
- **Photo encoding extracted to `ImageUtils`**: The Base64 encoding logic is no longer duplicated inline. `ImageUtils.encode()` and `ImageUtils.encodeFromUri()` handle all image-to-Base64 conversion across the app.
- **Driver name resolved from the API**: Instead of trusting the locally stored name, the backend chauffeur list is queried to get the accurate name at submission time.
- **Vehicle ID included**: The incident now carries `vehiculeId` in addition to `vehiculeNom` and `immatriculation`, which lets the backend correctly link the incident to a specific vehicle record rather than relying on name matching.
- **Incident model rewritten**: The old model used an all-arguments constructor with positional parameters, which made it fragile (wrong order = wrong data). The new model uses a no-argument constructor and sets fields by name, which is also required for correct Gson/Retrofit deserialization.

---

## 2. Incident List and Details (Admin Side)

### Wissal's version
The incidents screen listed incidents with tab filtering (All / En cours / Résolu). Tapping an incident opened a details view. There was no `+` button — the comment in the code noted that only drivers can declare incidents. There was no way for the admin to update or resolve an incident from the details screen.

### Adil's improvements
- **`IncidentDetailsActivity` built from scratch**: A full dedicated screen that shows all incident fields — type, date, vehicle, driver, description, status chip — plus a scrollable photo gallery for all attached images.
- **Admin can resolve an incident**: The details screen has a "Marquer comme résolu" button that calls `repo.updateIncident()` and updates the status in the backend. Wissal's branch had no write operations on incidents from the admin side.
- **Admin can delete an incident**: A delete button with a confirmation dialog calls `repo.deleteIncident()`. This did not exist in Wissal's branch.
- **Repository supports incident writes**: `updateIncident()` and `deleteIncident()` were added to `Repository.java`. These methods also invalidate the local SQLite cache so the list reflects the change immediately on the next load.

---

## 3. Vehicle Assignment (Admin Side)

### Wissal's version
Wissal built the vehicle assignment feature in `ChauffeurDetailsActivity`. When an admin opens a chauffeur's profile, a dropdown (Spinner) loads all vehicles with status "Disponible", plus whichever vehicle was already assigned to that driver. On save, the old vehicle is set back to "Disponible" and the new one is set to "En mission", with `conducteurId` updated accordingly. This was the only place in either branch where vehicle status was automatically managed.

### Adil's version (before the merge)
This vehicle assignment dropdown was accidentally removed during the rework. The field was replaced with a disabled, read-only text box that showed the vehicle name but gave the admin no way to change it. The status update logic (`handleVehicleStatusChange`, `assignNewVehicle`) was entirely absent, meaning saving a chauffeur's profile had no effect on vehicle status.

### After the merge
Wissal's vehicle assignment logic has been restored into Adil's version. The spinner, the vehicle loading logic, and the two-step status update (release old → assign new) are now part of `main`. The rest of `ChauffeurDetailsActivity` — photo upload, improved layout, `RESULT_OK` propagation — comes from Adil's version.

---

## 4. Chauffeur List Refresh Behaviour

### Wissal's version
`ChauffeursFragment` used an `ActivityResultLauncher` to launch `ChauffeurDetailsActivity`. When the details screen returned `RESULT_OK` (i.e. the admin saved changes), the launcher callback reloaded the chauffeur list. This meant the list only refreshed when something actually changed.

### Adil's version (before the merge)
The fragment reloaded the list in `onResume()`. This meant every time the user navigated back to the chauffeur tab — even if they just opened a profile and immediately pressed back without changing anything — a full network/cache request was made.

### After the merge
Wissal's `ActivityResultLauncher` pattern has been adopted. The list now only reloads when `ChauffeurDetailsActivity` explicitly signals that a save occurred (`setResult(RESULT_OK)`).

---

## 5. Data Layer — Repository and Caching

### Wissal's version
`Repository.java` had four additional lines: a `getIncident(id, cb)` method that searched the incident list by ID. Write operations (`createIncident`, `createVehicule`, etc.) did not clear the SQLite cache, so after creating or updating a record, the next load would return stale cached data until the TTL expired.

### Adil's improvements
- **Cache invalidation on every write**: Every write method (`createIncident`, `updateIncident`, `deleteIncident`, `createVehicule`, `updateVehicule`, `deleteVehicule`, `createChauffeur`, `updateChauffeur`, `deleteChauffeur`) now deletes the relevant cache key immediately after a successful response. The list the user sees after a write is always fresh.
- **`DeleteCacheTask`**: An `AsyncTask` subclass that removes cache keys off the UI thread, consistent with how cache reads and writes are already handled.
- **`getVehicule(id, cb)`**: Looks up a single vehicle by ID from the cached vehicle list. This is used by the vehicle assignment flow when releasing or assigning a vehicle.
- **`getCurrentVehicule(chauffeurId, cb)` corrected**: The previous version returned the first vehicle in the list if no match was found (`list.get(0)`), which meant a chauffeur with no assigned vehicle would silently appear to have one. The corrected version returns an error instead.

---

## 6. New Screens (Admin Side)

These screens exist only in Adil's version and have no equivalent in Wissal's branch:

| Screen | What it does |
|---|---|
| `EditProfileActivity` | Admin can update their own name, email, and password |
| `HelpActivity` | Static help/FAQ screen |
| `SettingsActivity` | App settings screen |

---

## 7. New Utilities

| File | What it does |
|---|---|
| `ImageUtils.java` | Central class for encoding Bitmaps and URIs to Base64, and for loading Base64 strings into `ImageView`s. Used by incident declaration, incident details, and chauffeur photo upload. |
| `TripManager.java` | Manages trip start/stop state and GPS tracking for the driver. Handles the "En mission" / "Disponible" state from the driver's side. |
| `FleetConfig.java` | Central configuration values (server URL, etc.) extracted from hardcoded strings scattered across the app. |

---

## Summary

Wissal's branch introduced two solid features: the vehicle assignment dropdown with automatic status management, and the `ActivityResultLauncher` refresh pattern. Everything else was either absent, incomplete, or less robust compared to Adil's version. The main gaps in Adil's rework were the accidental removal of the vehicle assignment UI and the missing incident click-through to a details screen (which was already present in Adil's adapter but not connected). Both have been resolved in the merge.
