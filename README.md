# Flunkyball-Simulator des UPB Flunkyteam

Dieses Projekt ist eine Kombination aus Kotlin, klassischem Javascript und Node.js. Gehostet wird die resultierende
Seite in Docker Containern. Die Video-Dateien sind zum Schutz der Privatsphäre nicht in diesem Repository gespeichert.
Das Kotlin backend erfragt eine Liste von Videos und served die URLs an das Frontend

# Manueller Build & Deploy

## Frontend

[NodeJS Frontend Readme](./frontend/nodejs/README.md)

## Server

Server bauen und starten im *./backend* Verzeichnis

`./gradlew run`

Befehlt um den [grpcWebProxy](https://github.com/improbable-eng/grpc-web/tree/master/go/grpcwebproxy) zu starten

`./startProxy.sh`

# Docker

## Docker-compose

In der `docker-compose.yaml` muss die video list url überschrieben werden

```
<videolist url> -> https://url to videolist file
```

```bash
# build all containers
docker-compose build

# run all containers
docker-compose up -d
```

## Manuel pro Container

### Beispiel Frontend

```bash
docker build -t frontend -f docker/frontend/Dockerfile .
docker run -e BACKEND_URL=https://<server domain>:8443 -p 80:5000 frontend
```

# Geplante Versionen

- Version 1: anything to play (by @ApolloLV)
- Version 2: Distributed Client-Server Game with solid technical base and very loose Rule enforcement to allow easy
  recovering from faulty game states.
- Version 3: Enforcing Rules and extra features.

## Nützliche Kommandozeilen-Befehle

MP4-Videos in kleinere Webms mit VP9 rekodieren (Siehe [FFMPEG-Wiki](https://trac.ffmpeg.org/wiki/Encode/VP9#twopass))

```
VIDEO=foldername/filename
ffmpeg -y -i $VIDEO.mp4 -c:v libvpx-vp9 -b:v 2M -pass 1 -an -f webm /dev/null &&
ffmpeg -y -i $VIDEO.mp4 -c:v libvpx-vp9 -b:v 2M -pass 2 -c:a libopus $VIDEO.webm
```

[tmux cheat sheet](https://tmuxcheatsheet.com)

## Versionsgeschichte

### 2.5:
- Hinzufügen von Siegesfeiervideos (Danke Basti)
- Stoppuhrbefehl ".stoppuhr ${"$"}Sekunden${"$"}"
- Schere-Stein-Papier-Minispiel .ssp

### 2.4:
- Mehrere Spieler in einem Browsertab über die "Spieler hinzufügen"-Funktion
- Keine Strafbierveränderungen während eines aktiven Wurfs
- Urhebernotiz hinzugefügt

### 2.3:
- Refactoring in Services insbesondere für Protokoll und Client.
- Clientauthentifizierung und Zuweisung von Spielern
- Clientonlinestatusanzeige und Wiederverbindungsversuche
- Unterscheidung von Chat- und Systemnachrichten

### 2.2:
- Strafbiere haben auch Videos
- Gesamte Infrastruktur auf Docker-Images umgestellt
- Infrastruktur umgezogen
    
### 2.1

- Kleinere Fehlerbehebungen; richtige Teamnamen im Chat
- Abgaben können nur vom Gegnerteam abgenommen werden
- Sophies & Daniels Videos gespiegelt
- Erholungsphase nach Würfen eingeführt
- Spielername in Tabtitel, wenn dran mit Werfen
- Sicherheitsfrage beim Kicken und neuem Spiel
- Strafbierlimit, Strafbiericon

### 2.0

- Client/Server Applikation mit Basisfunktionalität

### 1.0

- Client only Webpage mit Basisfunktionalität
