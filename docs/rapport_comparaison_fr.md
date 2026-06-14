# Rapport de comparaison : branche `main` vs branche `detached2`

Ce document décrit en quoi le travail effectué sur la branche `main` diffère de celui effectué sur la branche `detached2`, et en quoi il le complète ou l'améliore.

---

## 1. Déclaration d'incident (côté chauffeur)

### Version detached2
Le chauffeur pouvait déclarer un incident en renseignant un type, une description, et en prenant **une seule photo** via la caméra. La photo était stockée sous forme d'une chaîne Base64 unique. L'incident était envoyé au backend via l'ancien constructeur `Incident(id, vehiculeNom, immatriculation, type, description, date, statut, chauffeurNom, photos)`. Le nom du chauffeur était lu depuis le stockage local (Prefs), sans vérification côté serveur.

### Améliorations apportées dans main
- **Photos multiples** : le formulaire affiche désormais une rangée de vignettes défilantes. Le chauffeur peut ajouter autant de photos qu'il le souhaite, chacune disposant d'un bouton de suppression. La limite d'une seule photo a été supprimée.
- **Caméra et galerie** : au lieu de la caméra seule, une boîte de dialogue propose « Appareil photo / Galerie ». Le chauffeur peut choisir une image existante depuis sa galerie.
- **Encodage des photos centralisé dans `ImageUtils`** : la logique de conversion en Base64 n'est plus dupliquée dans chaque écran. `ImageUtils.encode()` et `ImageUtils.encodeFromUri()` gèrent toute la conversion image→Base64 dans l'application.
- **Nom du chauffeur résolu depuis l'API** : au lieu de faire confiance au nom stocké localement, la liste des chauffeurs est interrogée au moment de l'envoi pour obtenir le nom exact.
- **Identifiant du véhicule inclus** : l'incident contient désormais `vehiculeId` en plus de `vehiculeNom` et `immatriculation`, ce qui permet au backend de lier correctement l'incident à un véhicule précis plutôt que de se fier à une correspondance par nom.
- **Modèle `Incident` réécrit** : l'ancien modèle utilisait un constructeur avec tous les arguments en position, ce qui le rendait fragile (mauvais ordre = mauvaises données). Le nouveau modèle utilise un constructeur sans argument et affecte les champs par nom, ce qui est également requis pour la désérialisation correcte par Gson/Retrofit.

---

## 2. Liste et détail des incidents (côté admin)

### Version detached2
L'écran des incidents listait les incidents avec un filtrage par onglet (Tous / En cours / Résolu). Appuyer sur un incident ouvrait une vue de détail. Le bouton `+` était masqué — le code précisait en commentaire que seuls les chauffeurs déclarent des incidents. Il n'était pas possible pour l'admin de modifier ou résoudre un incident depuis l'écran de détail.

### Améliorations apportées dans main
- **`IncidentDetailsActivity` créé de zéro** : un écran dédié complet qui affiche tous les champs de l'incident — type, date, véhicule, chauffeur, description, pastille de statut — ainsi qu'une galerie défilante pour toutes les photos attachées.
- **L'admin peut résoudre un incident** : l'écran de détail dispose d'un bouton « Marquer comme résolu » qui appelle `repo.updateIncident()` et met à jour le statut dans le backend. La branche detached2 ne comportait aucune opération d'écriture sur les incidents côté admin.
- **L'admin peut supprimer un incident** : un bouton de suppression avec boîte de confirmation appelle `repo.deleteIncident()`. Cette fonctionnalité n'existait pas dans detached2.
- **Le Repository prend en charge les écritures sur les incidents** : `updateIncident()` et `deleteIncident()` ont été ajoutés à `Repository.java`. Ces méthodes invalident également le cache SQLite local afin que la liste reflète immédiatement le changement au prochain chargement.

---

## 3. Affectation d'un véhicule à un chauffeur (côté admin)

### Version detached2
La branche detached2 a introduit la fonctionnalité d'affectation de véhicule dans `ChauffeurDetailsActivity`. Lorsqu'un admin ouvre le profil d'un chauffeur, un menu déroulant (Spinner) charge tous les véhicules dont le statut est « Disponible », ainsi que celui déjà affecté à ce chauffeur. À la sauvegarde, l'ancien véhicule repasse à « Disponible » et le nouveau passe à « En mission », avec `conducteurId` mis à jour. C'était le seul endroit dans les deux branches où le statut des véhicules était géré automatiquement.

### Version main (avant la fusion)
Ce menu déroulant d'affectation avait été supprimé par inadvertance lors de la refonte. Le champ avait été remplacé par une zone de texte désactivée en lecture seule, qui affichait le nom du véhicule mais ne permettait pas à l'admin de le modifier. La logique de mise à jour du statut (`handleVehicleStatusChange`, `assignNewVehicle`) était entièrement absente, ce qui signifiait que sauvegarder le profil d'un chauffeur n'avait aucun effet sur le statut du véhicule.

### Après la fusion
La logique d'affectation de véhicule de detached2 a été réintégrée dans main. Le Spinner, le chargement des véhicules et la mise à jour en deux étapes du statut (libérer l'ancien → affecter le nouveau) font maintenant partie de `main`. Le reste de `ChauffeurDetailsActivity` — upload de photo, mise en page améliorée, propagation de `RESULT_OK` — provient de la branche main.

---

## 4. Rafraîchissement de la liste des chauffeurs

### Version detached2
`ChauffeursFragment` utilisait un `ActivityResultLauncher` pour lancer `ChauffeurDetailsActivity`. Lorsque l'écran de détail renvoyait `RESULT_OK` (c'est-à-dire que l'admin avait sauvegardé), le callback du launcher rechargait la liste. La liste ne se rafraîchissait donc qu'en cas de modification effective.

### Version main (avant la fusion)
Le fragment rechargait la liste dans `onResume()`. Cela signifiait que chaque fois que l'utilisateur revenait sur l'onglet chauffeurs — même s'il avait juste ouvert un profil et appuyé sur Retour sans rien changer — une requête réseau/cache complète était déclenchée inutilement.

### Après la fusion
Le pattern `ActivityResultLauncher` de detached2 a été adopté. La liste ne se recharge désormais que lorsque `ChauffeurDetailsActivity` signale explicitement qu'une sauvegarde a eu lieu (`setResult(RESULT_OK)`).

---

## 5. Couche données — Repository et cache

### Version detached2
`Repository.java` comportait quatre lignes supplémentaires : une méthode `getIncident(id, cb)` qui cherchait un incident par ID dans la liste. Les opérations d'écriture (`createIncident`, `createVehicule`, etc.) n'effaçaient pas le cache SQLite, donc après la création ou la modification d'un enregistrement, le prochain chargement retournait des données obsolètes jusqu'à l'expiration du TTL.

### Améliorations apportées dans main
- **Invalidation du cache à chaque écriture** : chaque méthode d'écriture (`createIncident`, `updateIncident`, `deleteIncident`, `createVehicule`, `updateVehicule`, `deleteVehicule`, `createChauffeur`, `updateChauffeur`, `deleteChauffeur`) supprime désormais la clé de cache concernée immédiatement après une réponse réussie. La liste que l'utilisateur voit après une écriture est toujours à jour.
- **`DeleteCacheTask`** : une sous-classe d'`AsyncTask` qui supprime les clés de cache en dehors du thread UI, en cohérence avec la façon dont les lectures et écritures du cache sont déjà gérées.
- **`getVehicule(id, cb)`** : recherche un véhicule par ID depuis la liste mise en cache. Utilisé par le flux d'affectation de véhicule lors de la libération ou de l'assignation d'un véhicule.
- **`getCurrentVehicule(chauffeurId, cb)` corrigé** : l'ancienne version retournait le premier véhicule de la liste si aucune correspondance n'était trouvée (`list.get(0)`), ce qui faisait apparaître silencieusement un véhicule affecté à un chauffeur qui n'en avait pas. La version corrigée retourne une erreur à la place.

---

## 6. Nouveaux écrans (côté admin)

Ces écrans n'existent que dans la branche main et n'ont pas d'équivalent dans detached2 :

| Écran | Fonctionnalité |
|---|---|
| `EditProfileActivity` | L'admin peut modifier son nom, son email et son mot de passe |
| `HelpActivity` | Écran d'aide/FAQ statique |
| `SettingsActivity` | Écran de paramètres de l'application |

---

## 7. Nouveaux utilitaires

| Fichier | Rôle |
|---|---|
| `ImageUtils.java` | Classe centrale pour encoder des Bitmaps et des URIs en Base64, et pour charger des chaînes Base64 dans des `ImageView`. Utilisée pour la déclaration d'incident, le détail des incidents et l'upload de photo de chauffeur. |
| `TripManager.java` | Gère l'état de démarrage/arrêt de trajet et le suivi GPS pour le chauffeur. Prend en charge la transition d'état « En mission » / « Disponible » côté chauffeur. |
| `FleetConfig.java` | Valeurs de configuration centralisées (URL serveur, etc.) extraites des chaînes codées en dur dispersées dans l'application. |

---

## Résumé

La branche detached2 a introduit deux fonctionnalités solides : le menu déroulant d'affectation de véhicule avec gestion automatique des statuts, et le pattern `ActivityResultLauncher` pour le rafraîchissement de liste. Tout le reste était soit absent, soit incomplet, soit moins robuste par rapport à la branche main. Les principaux manques dans la refonte de main étaient la suppression accidentelle de l'interface d'affectation de véhicule et l'absence de navigation vers un écran de détail au clic sur un incident (qui était déjà présente dans l'adapter mais non connectée). Ces deux points ont été corrigés lors de la fusion.
