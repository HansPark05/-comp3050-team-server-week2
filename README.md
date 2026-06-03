# COMP3050 — Team Server Project

## Project Overview

This is the team project for **COMP3050 – Software System Development and Operations** at Macquarie University, 2026.

A **Java HTTP game server** for a 2D tile-based virtual world. The server communicates with a web-based client (provided by teaching staff) via a **REST API (API v3)**. Our team designs, builds, tests, secures, and deploys the server.

---

## Live Server

| Property | Value |
| -------- | ----- |
| URL | `http://52.64.32.209:8000` |
| Region | AWS ap-southeast-2 (Sydney) |
| Deploy | Auto-deploy on push to `main` via GitHub Actions |

---

## API Endpoints (v3)

| Endpoint | Method | Description | Auth |
| -------- | ------ | ----------- | ---- |
| `/login` | POST | SHA-256 password auth, returns session token | — |
| `/logout?session=` | GET | Revoke session token, remove avatar from map | token |
| `/move?dy=&dx=&session=` | GET | Move character N/S/E/W one step | token |
| `/info?y=&x=&session=` | GET | Return 11×11 map tile grid centred on player | token |
| `/take?session=` | GET | Pick up item at current location | token |
| `/place?session=` | GET | Drop first inventory item at current location | token |
| `/use?dy=&dx=&session=` | GET | Interact with adjacent map element (e.g. door) | token |

### Response Codes

| Code | Meaning |
| ---- | ------- |
| `200` | Success (with JSON body where applicable) |
| `204` | No content — blocked, invalid, or nothing to do |
| `400` | Bad request (missing fields) |
| `401` | Unauthorized — invalid or missing session token |

---

## Authentication

Login sends a POST with a JSON body:

```json
{ "name": "Baelin", "encpswrd": "<SHA-256 of 'name;password'>" }
```

The client computes `SHA-256("Baelin;Nice day for fishing.")` and sends the hex digest. The server compares it against its stored hash. On success, a 32-character hex session token is returned:

```json
{ "session": "bf227818e519487c9904b5f2583ae10e" }
```

If the same user logs in again while already online, the previous session and avatar are removed automatically before the new session is created.

---

## Game Map

The server map is a 20×20 grid loaded from `map.txt` at startup.

```
BBBBBBBBBBBBBBBBBBBB
BggggggggggggggggggB
BggggggggggggggggggB
BggBBBBBgggggggggggB
BggBwwwDgggggkgggggB   ← key (k) at (4,13), closed door (D) at (4,7)
BggBwwwSgggggggggggB   ← player spawn at (5,5)
BggBwwwSgggggggggggB
BggBBBBBgggggggggggB
BgggggggggWWWWgggggB
BgggggggggWWWWgggggB
BgggggtttggWWWgggggB
BgggggttttgggggggggB
BggwwwwwgggggagggggB   ← axe (a) at (12,13)
BggwwwwwgggggggggggB
BSSSSSSSSSgggggggggB
BggggggggggggcgggggB   ← cyan potion (c) at (15,13)
BggggggggggggggggggB
Bgg______________ggB
BggggggghggggggggggB   ← heart potion (h) at (18,8)
BBBBBBBBBBBBBBBBBBBB
```

### Tile Types

| Symbol | Tile | Blocks Movement? |
| ------ | ---- | ---------------- |
| `B` | Brick wall | ✅ Yes |
| `S` | Stone wall | ✅ Yes |
| `W` | Water | ✅ Yes |
| `D` | Door (closed) | ✅ Yes |
| `0`–`9` | Player avatars | ✅ Yes (multiplayer) |
| `d` | Door (open) | ❌ No |
| `g` | Grass | ❌ No |
| `_` | Dirt | ❌ No |
| `w` | Wooden boards | ❌ No |
| `t` | Tree | ❌ No |
| `f` | Flagstones | ❌ No |
| `b` | Bridge | ❌ No |
| `s` | Sand | ❌ No |
| `p` | Pebbles | ❌ No |
| `.` `,` `:` `;` | Rocks | ❌ No |

### Items (takeable)

| Symbol | Item | Class |
| ------ | ---- | ----- |
| `a` | Axe | tool |
| `c` | Cyan potion | drink |
| `h` | Heart potion | drink |
| `k` | Key | artifact |

Same-class items swap on TAKE (e.g. taking a heart potion while holding a cyan potion drops the cyan potion and picks up the heart potion). Up to 5 items can be held.

Items are stored in the map as multi-char tile strings (e.g. `"gk"` = grass + key). When taken, the item char is removed from the tile and added to the player's inventory list.

---

## Multiplayer

- Up to 10 simultaneous players, each assigned a unique avatar digit `0`–`9`
- Avatar digits are appended to the end of tile strings (e.g. `"g0"` = grass + player 0)
- Players block each other's movement — only one avatar per tile
- On logout, the avatar is removed from the map tile immediately
- Re-login of the same user removes the previous avatar and creates a fresh session

---

## Player Spawn & Navigation

Players spawn at map position **(row 5, col 5)** — wooden boards inside the starting building.

- The client's initial view is always `posX=5, posY=5`, so `/info?y=5&x=5` succeeds immediately after login.
- The `/info` response always reflects the server's authoritative player position — the client syncs its local `posX`/`posY` from the response `y`/`x` fields.
- To escape the starting room: move to **(4,6)** and **click the door tile** to send `USE dy=0,dx=1`. The door at (4,7) toggles between `D` (closed/blocking) and `d` (open/passable).

### Client Controls

| Key / Action | Effect |
| ------------ | ------ |
| W / ↑ | Move north |
| S / ↓ | Move south |
| A / ← | Move west |
| D / → | Move east |
| T | Take item at current tile |
| P | Place first inventory item |
| Mouse click | USE adjacent tile (e.g. toggle door) |

---

## Testing

```bash
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.8.9-hotspot" ./mvnw test
```

**54 unit tests** across 7 test classes — all must pass before merging to `main`.

| Test Class | Coverage |
| ---------- | -------- |
| `GameMapTest` (15) | Map loading, bounds, tile types, blocking |
| `InfoHandlerTest` (5) | Session state, view window |
| `ItemTest` (7) | Item symbols, classes |
| `LocationStringTest` (9) | Tile string parsing and rendering |
| `MoveHandlerTest` (6) | Move validation, wall blocking |
| `TakeHandlerTest` (7) | Item pickup, class swap, avatar preservation |
| `PlaceHandlerTest` (5) | Item drop, avatar preservation |

---

## Tech Stack

| Tool | Purpose |
| ---- | ------- |
| Java 21 (Eclipse Temurin) | Server language |
| `com.sun.net.httpserver` | Built-in HTTP server (no frameworks) |
| Maven + `mvnw` | Build and dependency management |
| JUnit 5 | Unit testing |
| Docker | Containerisation |
| Docker Hub (`hansmq/game-server`) | Image registry |
| GitHub Actions | CI/CD pipeline |
| AWS EC2 t3.micro | Cloud hosting (ap-southeast-2) |
| Terraform | Infrastructure as Code |
| Trivy | Docker image vulnerability scanning |
| Semgrep | Static application security testing (SAST) |

---

## CI/CD Pipeline

Every push to `main` triggers the full pipeline automatically:

```
git push → GitHub Actions
  └─ build-and-push: mvn test → docker build → docker push (Docker Hub)
  └─ deploy: SSH into EC2 → docker pull → docker stop/rm → docker run
```

Pipeline defined in `.github/workflows/deploy.yml`.

### GitHub Secrets Required

| Secret | Purpose |
| ------ | ------- |
| `DOCKERHUB_USERNAME` | Docker Hub account |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `EC2_SSH_PRIVATE_KEY` | Private key for EC2 SSH |
| `EC2_HOST` | EC2 public IP (`52.64.32.209`) |
| `APP_USER` | Game username (passed as env var) |
| `APP_PASS` | Game password hash (passed as env var) |

---

## Security

| Tool | What It Catches |
| ---- | --------------- |
| Trivy | CVEs in Docker base image and dependencies |
| Semgrep | Hardcoded credentials in source code |
| `.gitignore` | Prevents `.pem`, `.env`, `terraform.tfstate` from being committed |
| GitHub Secrets | Credentials never appear in source code or logs |

---

## Infrastructure (Terraform)

AWS resources are defined as code in `terraform/` — no manual console clicks needed.

| Resource | Config |
| -------- | ------ |
| EC2 instance | t3.micro, Amazon Linux 2023, ap-southeast-2 |
| Security group | Port 22 (SSH), 8000 (game server) |
| Elastic IP | Static address pinned to EC2 |

```bash
terraform init
terraform apply -var="key_pair_name=YOUR_KEY"
terraform destroy -var="key_pair_name=YOUR_KEY"   # always destroy after testing
```

---

## How to Run Locally

```bash
# Run all tests
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.8.9-hotspot" ./mvnw test

# Build and run with Docker
docker build -t comp3050-server .
docker run -d -p 8000:8000 comp3050-server

# Test login
curl -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"name":"Baelin","encpswrd":"<hash>"}'
```

---

## Branch Workflow

| Branch pattern | Purpose |
| -------------- | ------- |
| `main` | Production — auto-deploys to EC2 |
| `hans/*` | Feature/fix branches (merged via PR) |
| `<member>/*` | Team member feature branches |

All changes go through Pull Requests. `main` is protected — direct pushes not allowed.

### Commit Message Convention

```
feat:     new feature
fix:      bug fix
ci:       CI/CD pipeline changes
docs:     documentation only
test:     add or update tests
refactor: code restructuring without behaviour change
```

---

## Project Structure

```
-comp3050-team-server-week2/
│
├── src/
│   ├── main/java/
│   │   │
│   │   ├── Test.java                        # Entry point — starts HTTP server on port 8000,
│   │   │                                    #   registers all endpoint handlers
│   │   │
│   │   ├── ── HTTP Handlers (one per endpoint) ──
│   │   ├── MoveHandler.java                 # GET /move?dy=&dx=&session=
│   │   │                                    #   Validates move, updates tile strings, returns {y,x}
│   │   ├── InfoHandler.java                 # GET /info?y=&x=&session=
│   │   │                                    #   Returns 11×11 map tile grid as JSON array
│   │   ├── TakeHandler.java                 # GET /take?session=
│   │   │                                    #   Picks up item on current tile, adds to inventory
│   │   ├── PlaceHandler.java                # GET /place?session=
│   │   │                                    #   Drops first inventory item onto current tile
│   │   ├── UseHandler.java                  # GET /use?dy=&dx=&session=
│   │   │                                    #   Toggles adjacent door D (closed) <-> d (open)
│   │   ├── HelloHandler.java                # GET /hello — basic health-check endpoint
│   │   ├── MyHandler.java                   # GET /test  — legacy test endpoint
│   │   │
│   │   ├── ── Game State ──
│   │   ├── GameMap.java                     # Singleton — holds the 20×20 tile grid in memory.
│   │   │                                    #   Loads map.txt at startup. Thread-safe tile access.
│   │   │                                    #   isInBounds(), isBlocking(), getTile(), setTile()
│   │   │
│   │   ├── ── Item System ──
│   │   ├── Item.java                        # Enum — defines all takeable items:
│   │   │                                    #   AXE('a'), CYAN_POTION('c'), HEART_POTION('h'), KEY('k')
│   │   ├── ItemClass.java                   # Enum — item categories: TOOL, DRINK, ARTIFACT
│   │   │                                    #   Used for same-class swap logic on TAKE
│   │   ├── LocationString.java              # Parses a tile string (e.g. "gk0") into components:
│   │   │                                    #   terrain char, item chars, player avatar digit
│   │   │                                    #   Used by TakeHandler and PlaceHandler
│   │   │
│   │   └── comp3050/server/
│   │       ├── LoginHandler.java            # POST /login — SHA-256 auth, issues session token,
│   │       │                                #   spawns player at (5,5), assigns unique avatar digit
│   │       ├── LogoutHandler.java           # GET /logout?session= — removes avatar from map,
│   │       │                                #   invalidates session token
│   │       ├── SessionManager.java          # Singleton — stores token -> PlayerState mapping.
│   │       │                                #   createSession(), getPlayer(), invalidate(),
│   │       │                                #   getPlayerByUsername(), getUsedAvatars()
│   │       └── PlayerState.java             # Data class — holds a player's current state:
│   │                                        #   int y, int x, char avatar, List<Item> inventory,
│   │                                        #   String username
│   │
│   └── test/java/
│       ├── GameMapTest.java                 # 15 tests — map loading, bounds, tile types, isBlocking()
│       ├── InfoHandlerTest.java             #  5 tests — session validation, 11×11 view window
│       ├── ItemTest.java                    #  7 tests — item symbols, classes, same-class swap
│       ├── LocationStringTest.java          #  9 tests — tile string parsing, avatar preservation
│       ├── MoveHandlerTest.java             #  6 tests — wall blocking, diagonal block, player block
│       ├── TakeHandlerTest.java             #  7 tests — item pickup, class swap, avatar in tile
│       └── PlaceHandlerTest.java            #  5 tests — item drop, avatar preserved after place
│
├── .github/
│   └── workflows/
│       ├── deploy.yml                       # Main CI/CD pipeline — triggers on push to main:
│       │                                    #   mvn test -> Semgrep -> docker build/push ->
│       │                                    #   Trivy -> SSH deploy to EC2
│       └── ci.yml                           # Lightweight CI — runs tests on all branches/PRs
│
├── Dockerfile                               # Multi-stage build:
│                                            #   Stage 1 (builder): Maven + JDK, compiles to .jar
│                                            #   Stage 2 (runtime): JRE only, copies .jar + map.txt
├── docker-compose.yml                       # Local development compose config
├── map.txt                                  # 20×20 tile map loaded at server startup
├── pom.xml                                  # Maven project config — Java 21, JUnit 5 dependency
├── mvnw / mvnw.cmd                          # Maven wrapper scripts (cross-platform)
├── .semgrep.yml                             # Semgrep SAST rule config — scans for hardcoded secrets
├── .gitignore                               # Excludes: target/, *.pem, .env, terraform.tfstate
├── .dockerignore                            # Excludes build artifacts from Docker context
└── nginx.conf                               # Nginx config (legacy, not used in current deployment)
```

### Key Design Decisions

| Decision | Reason |
| -------- | ------ |
| No framework (raw `com.sun.net.httpserver`) | Unit spec requirement; zero external dependencies; smaller Docker image |
| Multi-char tile strings (`"gk0"`) | Single data structure encodes terrain + items + avatar; simple string ops for take/place/move |
| `GameMap` and `SessionManager` as singletons | Shared mutable state accessed by all handlers; thread-safe via `ConcurrentHashMap` |
| Spawn at row 5, col 5 | Browser client hardcodes `posX=5, posY=5` on startup — server must match |
| Avatar digits appended last in tile string | `isBlocking()` scans for digit chars to detect player presence without a separate data structure |

---

## Known Bugs Fixed

| Bug | Root Cause | Fix |
| --- | ---------- | --- |
| Client position desync (info 204 loop) | `InfoHandler` rejected requests where client's y/x didn't match server position; race condition with async move+info | Removed position check — server always returns authoritative player position |
| Avatar not removed on logout | `LogoutHandler` invalidated session before reading player position | Read `PlayerState` first, remove avatar from tile, then invalidate |
| Avatar not removed on re-login | `LoginHandler` called `invalidateUser()` without cleaning up map tile | Retrieve old `PlayerState` via `getPlayerByUsername()` before invalidating |
| All players assigned avatar `'0'` | No uniqueness check when assigning avatar digits | `findAvailableAvatar()` scans active sessions for used digits |
| Multiplayer tile blocking | `isBlocking()` only checked structural tiles, not player digits | `MoveHandler` scans target tile string for digit characters |

---

## Weekly Progress

| Week | Topic | Deliverable |
| ---- | ----- | ----------- |
| 1 | Java HTTP server | Basic `/test` and `/hello` endpoints |
| 2 | Git + GitHub + CI | Team workflow, GitHub Actions CI |
| 3 | Docker | Containerised server, Docker Hub push |
| 4 | JUnit testing | Maven project, 54 unit tests |
| 5 | Core API | `/move`, `/info` endpoints |
| 6 | DevSecOps | Trivy + Semgrep in CI, secrets management |
| 7 | AWS EC2 | Cloud deployment (Sydney region) |
| 8 | Terraform | IaC, reproducible EC2 provisioning |
| 9 | CI/CD | Full auto-deploy pipeline on push to main |
| 10–13 | API v3 | `/login`, `/logout`, `/take`, `/place`, `/use`, multiplayer, bug fixes |

---

## Team Roles

| Member | Role |
| ------ | ---- |
| Hanseong Park (Hans) | Team Manager — repo management, CI/CD, deployment, bug fixes |
| Abdul Karim | Session management, LoginHandler, LogoutHandler, TakeHandler |
| Jaehyeok Park | MoveHandler, Semgrep scanning |
| Arindam Biswas | Dockerfile, Trivy scanning, map design |
| Shoa | Maven setup, Docker Compose, JUnit test structure |
