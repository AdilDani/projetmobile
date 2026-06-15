"""
Downloads real vehicle, chauffeur, and incident photos,
compresses them, converts to base64, and injects into fleet-db.
"""
import requests, base64, subprocess, time
from PIL import Image
from io import BytesIO

SESSION = requests.Session()
SESSION.headers.update({
    "User-Agent": "FleetTrackingStudent/1.0 (university project demo)"
})

def fetch_b64(url, max_w=480, max_h=360, quality=65, delay=2):
    time.sleep(delay)
    print(f"  GET {url[:95]}")
    r = SESSION.get(url, timeout=25, allow_redirects=True)
    r.raise_for_status()
    img = Image.open(BytesIO(r.content)).convert("RGB")
    img.thumbnail((max_w, max_h), Image.LANCZOS)
    buf = BytesIO()
    img.save(buf, format="JPEG", quality=quality, optimize=True)
    b64 = base64.b64encode(buf.getvalue()).decode()
    print(f"  => {img.size[0]}x{img.size[1]}  {len(buf.getvalue())//1024}KB  b64:{len(b64)} chars")
    return b64

def wiki_thumbnail(article):
    time.sleep(2)
    url = f"https://en.wikipedia.org/api/rest_v1/page/summary/{article}"
    print(f"  Wikipedia: {article}")
    r = SESSION.get(url, timeout=15)
    r.raise_for_status()
    data = r.json()
    thumb = data.get("thumbnail", {}).get("source")
    if not thumb:
        raise ValueError(f"No thumbnail in article: {article}")
    # bump to larger size
    for small in ["/320px-", "/200px-", "/150px-", "/100px-"]:
        thumb = thumb.replace(small, "/480px-")
    print(f"  Thumbnail: {thumb[:80]}")
    return thumb

def psql(sql):
    result = subprocess.run(
        ["docker", "exec", "-i", "fleet-db", "psql", "-U", "fleet", "fleetdb"],
        input=sql, capture_output=True, text=True
    )
    if result.returncode != 0 or "ERROR" in result.stderr:
        print(f"  PSQL ERR: {result.stderr[:200]}")
    else:
        print(f"  PSQL OK: {result.stdout.strip()}")

# ─── VEHICLES ─────────────────────────────────────────────────────────────────
vehicles = [
    ("v1", "Renault Master",  "Renault_Master"),
    ("v2", "Dacia Dokker",    "Dacia_Dokker"),
    ("v3", "Peugeot Boxer",   "Peugeot_Boxer"),
    ("v4", "Iveco Daily",     "Iveco_Daily"),
    ("v5", "Ford Transit",    "Ford_Transit"),          # simpler slug
]

print("\n=== VEHICLES ===")
for vid, name, article in vehicles:
    print(f"\n{name} ({vid})")
    try:
        img_url = wiki_thumbnail(article)
        b64 = fetch_b64(img_url)
        escaped = b64.replace("'", "''")
        psql(f"UPDATE vehicule SET photo = '{escaped}' WHERE id = '{vid}';")
    except Exception as e:
        print(f"  SKIP: {e}")

# ─── CHAUFFEURS ───────────────────────────────────────────────────────────────
chauffeurs = [
    ("c1", "Ahmed Benali",    "https://randomuser.me/api/portraits/men/41.jpg"),
    ("c2", "Youssef Karim",   "https://randomuser.me/api/portraits/men/55.jpg"),
    ("c3", "Mohamed Idrissi", "https://randomuser.me/api/portraits/men/62.jpg"),
    ("c4", "Said Amine",      "https://randomuser.me/api/portraits/men/77.jpg"),
]

print("\n=== CHAUFFEURS ===")
for cid, name, url in chauffeurs:
    print(f"\n{name} ({cid})")
    try:
        b64 = fetch_b64(url, max_w=200, max_h=200, quality=75, delay=1)
        escaped = b64.replace("'", "''")
        psql(f"UPDATE chauffeur SET photo = '{escaped}' WHERE id = '{cid}';")
    except Exception as e:
        print(f"  SKIP: {e}")

# ─── INCIDENTS ────────────────────────────────────────────────────────────────
# Photos live in incident_image table (incident_id, image columns)
incidents = [
    ("i1", "Accident / Collision", "Traffic_collision"),
    ("i2", "Pneu crevé",           "Flat_tire"),
    ("i3", "Bris de glace",        "Safety_glass"),
    ("i4", "Panne mécanique",      "Automobile_repair_shop"),
]

print("\n=== INCIDENTS ===")
for iid, name, article in incidents:
    print(f"\n{name} ({iid})")
    try:
        img_url = wiki_thumbnail(article)
        b64 = fetch_b64(img_url, delay=4)   # extra delay — Wikimedia rate limits thumbnails
        escaped = b64.replace("'", "''")
        # Delete any existing image for this incident, then insert fresh
        psql(f"DELETE FROM incident_image WHERE incident_id = '{iid}';")
        psql(f"INSERT INTO incident_image (incident_id, image) VALUES ('{iid}', 'data:image/jpeg;base64,{escaped}');")
    except Exception as e:
        print(f"  SKIP: {e}")

print("\n=== DONE ===")
