# FleetTracking — Visual Design Reference

A complete description of the app's visual language so any contributor (or an LLM) can build new screens that look native to the existing app. **Read the "Hard rules" section first — it is the single most important constraint in this project.**

---

## 0. Hard rules (non-negotiable)

1. **Nothing is hardcoded in layouts.** Every color, dimension, font size, and piece of text lives in `res/values/` and is referenced via `@color/…`, `@dimen/…`, `@string/…`, or `@style/…`. A layout file must never contain a literal hex color, a literal `dp`/`sp` number, or literal user-facing text. New screens follow the same discipline — if you need a value that doesn't exist, **add a token** to the right values file, then reference it.
2. **Reuse tokens and styles before inventing new ones.** The palette, spacing scale, type scale, and component styles below cover almost everything. Only add a token when nothing existing fits.
3. **All user-facing text is French.** Add new copy to `strings.xml`.
4. **Spacing comes only from the spacing scale** (`space_xs` … `space_xxxl`). Don't use arbitrary margins.

---

## 1. Design language at a glance

- **Style:** clean, modern, "fintech/SaaS dashboard" feel — white cards floating on a light grey background, generous rounded corners, a single confident blue as the brand color, flat vector icons.
- **Brand color:** royal blue `#1D4ED8` (`@color/primary`).
- **Surface model:** light grey screen → white rounded cards → content inside cards. Cards carry a soft elevation (`2dp`).
- **Corners:** everything is rounded. Cards use `16dp`, buttons/inputs `12dp`, pills/chips are fully rounded (`100dp`).
- **Two distinct spaces:** an **Admin** space and a **Chauffeur** space, each with its own bottom navigation. They share the same tokens and component styles, so they feel like one product.
- **Header patterns:** Admin screens use a flat solid-blue toolbar; Chauffeur screens use a taller blue header block with rounded bottom corners.

---

## 2. Color tokens (`res/values/colors.xml`)

### Brand
| Token | Hex | Use |
|---|---|---|
| `primary` | `#1D4ED8` | Brand blue: toolbars, primary buttons, active nav, links |
| `primary_dark` | `#1E40AF` | Status bar, pressed/variant |
| `primary_light` | `#3B82F6` | Lighter accents |
| `primary_pale` | `#DBEAFE` | Pale blue fills (icon circles) |
| `primary_pale_2` | `#EFF6FF` | Login screen background |
| `accent` | `#2563EB` | Secondary brand |

### Status
| Token | Hex | Pale variant |
|---|---|---|
| `success` | `#16A34A` | `success_pale` `#DCFCE7` |
| `danger` | `#DC2626` | `danger_pale` `#FEE2E2` |
| `warning` | `#F59E0B` | `warning_pale` `#FEF3C7` |
| `info` | `#0EA5E9` | `info_pale` `#E0F2FE` |

Status colors map to meaning: green = available/OK, red = incident/danger/logout, orange = maintenance/entretien, blue = info/primary action.

### Neutrals & surfaces
| Token | Hex | Use |
|---|---|---|
| `screen_bg` | `#F1F5F9` | Default screen background |
| `card_bg` / `white` | `#FFFFFF` | Cards, sheets |
| `divider` | `#E2E8F0` | Hairline separators |
| `input_bg` | `#F8FAFC` | Text field fill |
| `input_stroke` | `#E2E8F0` | Text field border |

### Text
| Token | Hex | Use |
|---|---|---|
| `text_primary` | `#0F172A` | Titles, body |
| `text_secondary` | `#64748B` | Subtitles, secondary text |
| `text_tertiary` | `#94A3B8` | Captions, hints, placeholders |
| `text_on_primary` | `#FFFFFF` | Text on blue |
| `text_on_primary_muted` | `#C7D7FE` | Muted text on blue header |

### Nav & map
`nav_selected` `#1D4ED8`, `nav_unselected` `#94A3B8`. Map placeholder accents: `map_bg` `#E5EEF6`, `map_road` `#FFFFFF`, `map_water` `#BFDBFE`.

---

## 3. Spacing scale (`res/values/dimens.xml`)

Use these for all padding/margins. Card interiors use `space_l` (16dp); gaps between stacked cards use `space_l`; tight internal gaps use `space_s`/`space_m`.

| Token | Value |
|---|---|
| `space_xs` | 4dp |
| `space_s` | 8dp |
| `space_m` | 12dp |
| `space_l` | 16dp |
| `space_xl` | 20dp |
| `space_xxl` | 24dp |
| `space_xxxl` | 32dp |

### Radius
`radius_s` 8 · `radius_m` 12 (buttons, inputs) · `radius_l` 16 (cards) · `radius_xl` 20 (header bottom corners) · `radius_pill` 100 (chips/pills).

### Elevation
`elevation_card` 2dp · `elevation_nav` 12dp.

---

## 4. Typography (`res/values/dimens.xml` + `styles.xml`)

System default font (no custom font shipped). Weight is either regular or **bold**; sizes come from the type scale.

| Size token | Value | Style class | Color | Used for |
|---|---|---|---|---|
| `text_display` 30sp | `TextDisplay` (bold) | primary | Big numbers/hero |
| `text_stat` 26sp | — | — | Stat counters |
| `text_headline` 24sp | `TextHeadline` (bold) | primary | Screen hero / greeting |
| `text_title` 20sp | `TextTitle` (bold) / `ToolbarTitle` (white) | primary / on-primary | Section/screen titles, toolbar |
| `text_subtitle` 17sp | `TextSubtitle` (bold) | primary | Card titles, section labels |
| `text_body` 15sp | `TextBody` | primary | Body text, button labels |
| `text_small` 13sp | `TextSecondary` | secondary | Secondary lines |
| `text_caption` 12sp | `TextCaption` | tertiary | Captions, hints |
| `text_micro` 10sp | — | — | Badge numbers |

Other defined sizes: `text_subtitle` for section labels (`SectionLabel` adds top/bottom margins).

**Always apply a text style** (`style="@style/TextBody"` etc.) instead of setting `textSize`/`textColor` inline. Override color inline only for special cases (e.g. white text on the blue header).

---

## 5. Component styles (`res/values/styles.xml`)

| Style | Built on | Look |
|---|---|---|
| `Card` | — | `match_parent` width, white `bg_card`, padding `space_l` |
| `PrimaryButton` | MaterialButton | Full-width, height `button_height` (52dp), blue fill, white bold text, radius `radius_m`, **not** all-caps |
| `DangerButton` | PrimaryButton | Red fill (logout, destructive) |
| `OutlineButton` | Outlined MaterialButton | Blue stroke + blue text, transparent fill |
| `Input` | — | Height 52dp, `bg_input` (grey fill + hairline border), 16dp horizontal padding, tertiary hint color |
| `FieldLabel` | — | Secondary 13sp label above an input |
| `SectionLabel` | — | Bold 17sp with top/bottom margins, separates sections |
| `ToolbarTitle` | — | White bold 20sp for toolbars/headers |

### Shape drawables (`res/drawable/bg_*.xml`)
- **Cards:** `bg_card` (white, radius 16), `bg_card_clickable` (same + ripple for list rows).
- **Headers:** `bg_header` (solid blue, rounded **bottom** corners 20dp — the chauffeur header block), `bg_header_flat` (flat blue).
- **Inputs:** `bg_input` (grey fill + border, radius 12).
- **Stat tiles:** `bg_stat_blue` / `_green` / `_red` / `_orange` (solid status color, radius 16) — the four dashboard counters.
- **Shortcut icon boxes:** `bg_shortcut_blue` / `_green` / `_orange` / `_red` (pale tinted square behind a shortcut icon).
- **Chips/badges:** `bg_chip_success` / `_danger` / `_warning` / `_info` / `_primary` (pale fill, pill radius) for status labels; `bg_badge_red` (red dot for notification count); `bg_pill_primary` (pale blue pill for plates).
- **Circles:** `bg_circle_white` / `bg_circle_pale` / `bg_circle_primary` (round backgrounds behind icons/avatars).
- **Misc:** `bg_vehicle_thumb` (grey rounded thumb behind truck icon), `bg_metric_box`, `bg_progress_track`, `bg_fab_primary` / `bg_fab_white`, `divider`.

---

## 6. Iconography (`res/drawable/ic_*.xml`)

Flat single-path vector icons, tinted at render time (`android:tint`) — never baked-in color. Sizes from `icon_s` 18 / `icon_m` 24 / `icon_l` 32 / `icon_xl` 48.

Available: `ic_home, ic_truck, ic_map_pin, ic_people, ic_history, ic_person, ic_location, ic_warning, ic_wrench, ic_fuel, ic_bell, ic_calendar, ic_play, ic_route, ic_speed, ic_compass, ic_phone, ic_sms, ic_email, ic_camera, ic_send, ic_plus, ic_edit, ic_search, ic_filter, ic_settings, ic_help, ic_logout, ic_lock, ic_id_card, ic_info, ic_check_circle, ic_clock, ic_chevron_right, ic_chevron_down, ic_back, ic_more, ic_menu, ic_locate, ic_circle_dot, ic_logo`.

Tint convention: icons on blue → `@color/white`; standalone accent icons → `@color/primary`; status icons take the matching status color (incident icon → `danger`, wrench → `warning`, fuel → `success`).

---

## 7. Navigation structure

Two `BottomNavigationView` menus, each driving fragment swaps within a single host activity.

**Admin** (`menu/admin_bottom_nav.xml`): Accueil (`ic_home`) · Véhicules (`ic_truck`) · Carte (`ic_map_pin`) · Chauffeurs (`ic_people`) · Historique (`ic_history`).

**Chauffeur** (`menu/chauffeur_bottom_nav.xml`): Accueil (`ic_home`) · Position (`ic_location`) · Véhicule (`ic_truck`) · Profil (`ic_person`).

Active item = `nav_selected` (blue), inactive = `nav_unselected` (grey). Nav bar height `bottom_nav_height` 64dp, elevation `elevation_nav`.

---

## 8. Recurring layout patterns

Build new screens by composing these:

- **Admin screen skeleton:** root vertical `LinearLayout` (bg `screen_bg`) → `<include layout="@layout/toolbar_admin"/>` (flat blue bar, white title, optional `+` action + profile avatar on the right) → `ScrollView` (weight 1) → content `LinearLayout` padded `space_l`.
- **Chauffeur screen skeleton:** `ScrollView` (bg `screen_bg`) → vertical `LinearLayout` → **blue header block** (`bg_header`, rounded bottom) containing title + bell-with-badge + greeting + avatar → content `LinearLayout` padded `space_l`.
- **Card:** white `bg_card`, padding `space_l`, `elevation_card`, stacked with `space_l` top margins. Card title = icon (`icon_s`, blue tint) + `TextSubtitle` in a horizontal row, then content below.
- **Stat tile (dashboard):** colored `bg_stat_*` rounded rectangle, horizontal: white icon (`icon_l`) + vertical (big white count + small white label). Laid out as a 2×2 grid using two horizontal rows of weighted halves.
- **List row (`item_*`):** `bg_card_clickable`, horizontal: leading thumbnail/icon in a rounded box, middle vertical text block (bold title + secondary lines), trailing `ic_chevron_right` or a status chip. Rows separated by `space_m` bottom margin.
- **Status chip:** small `TextCaption` with pill background (`bg_chip_*`) and horizontal padding `space_m`, vertical `space_xs`.
- **Shortcut grid (chauffeur home):** row of weighted cells, each a small `bg_card` with a tinted `bg_shortcut_*` icon box (`shortcut_icon_box` 56dp) above a caption.
- **Tabbed list screens** (Incidents, Entretiens, Historique): a tab row (Tous / En cours / Résolus, or À venir / Historique) above a `RecyclerView` of cards; tabs filter the list.
- **Detail screen:** image/hero on top, then stacked rows of label→value (`item_detail_row`), then action buttons (`PrimaryButton` / `OutlineButton`).

---

## 9. Screen-by-screen visual summary

**Login** (`activity_login.xml`) — centered on pale-blue (`primary_pale_2`) background: logo, "**Fleet**Tracking" wordmark (Fleet in dark, Tracking in blue), tagline, then a white card with a role toggle (Administrateur / Chauffeur), email + password inputs (with leading icons), "Mot de passe oublié ?" link, full-width blue "Se connecter" button, demo hint, and a sign-up line.

**Admin · Accueil** (`fragment_admin_accueil.xml`) — flat blue toolbar; "Bonjour, Admin" + date; 2×2 grid of colored stat tiles (Véhicules blue, Chauffeurs green, Incidents red, Entretiens orange); "Localisation des véhicules" section with "Voir tout" link and an embedded `MapView`.

**Admin · Véhicules** (`fragment_admin_vehicules.xml`) — search bar; a row of **two MaterialButtons (Incidents + Entretiens)** that open their respective screens; then a `RecyclerView` of vehicle rows (truck thumb, name, plate, status).

**Admin · Carte** — full map with vehicle markers; speed/filter accents.

**Admin · Chauffeurs** — searchable list of driver cards (avatar, name, call/SMS/email actions).

**Admin · Historique** — tabbed list of trips.

**Admin · details** (`activity_vehicule_details`, `activity_chauffeur_details`) — hero image/avatar, label/value rows, edit/save. Chauffeur details also exposes login credentials section + "Enregistrer les modifications".

**Incidents / Entretiens** (`activity_incidents`, `activity_entretiens`) — back toolbar, filter tabs, card list. Incident card: pale circle + red warning icon, type (bold) + vehicle, status chip, description, date.

**Déclarer / Nouvel incident** — form: vehicle, incident-type spinner, description field, photo capture (camera), send button. On success → toast + **system notification**.

**Chauffeur · Accueil** (`fragment_chauffeur_accueil.xml`) — blue header (title, bell+red badge, "Bonjour {nom} 👋", avatar); "Véhicule affecté" card (truck thumb, name, plate pill, mileage); "Mission du jour" card (Rabat → Casablanca, départ/arrivée, "Démarrer trajet" button with play icon); "Raccourcis" grid (Ma position / Incident / Entretien / Consommation).

**Chauffeur · Position** — real-time status card, speed/direction, "Envoyer ma position".

**Chauffeur · Véhicule** — assigned vehicle details, next service.

**Chauffeur · Profil / Admin · Profil** — avatar + name/role header, settings list rows (`item_profile_menu`), notifications switch, help, red "Déconnexion" button.

---

## 10. How to add a new screen (checklist)

1. Pick the right skeleton (admin toolbar vs chauffeur header) from §8.
2. Build with existing **styles** for every text/button/input.
3. Use only **spacing/radius/size tokens** — no literal numbers.
4. Use existing **drawables** for backgrounds/chips/cards; add a `bg_*` only if a new shape is truly needed.
5. Put every label in **`strings.xml`** (French).
6. Tint icons via `@color/…`; never ship a pre-colored icon.
7. If you must introduce a value, add the token to the matching `values/` file first, then reference it.

Following this keeps any new page visually identical in feel to the rest of FleetTracking.
