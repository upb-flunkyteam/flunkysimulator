# Flunkyball-Simulator des UPB Flunkyteam

Dieses Projekt ist eine Kombination aus klassischem Javascript und Node.js.
Gehostet wird die resultierende Seite auf Heroku.
Die Video-Dateien sind zum Schutz der Privatsphäre nicht in diesem Repository gespeichert.
Das Kotlin backend erfragt eine Liste von Videos und served die URLs an das Frontend

## Nützliche Kommandozeilen-Befehle
MP4-Videos in kleinere Webms mit VP9 rekodieren (Siehe [FFMPEG-Wiki](https://trac.ffmpeg.org/wiki/Encode/VP9#twopass))

```
ffmpeg -i stop.mp4 -c:v libvpx-vp9 -b:v 2M -pass 1 -an -f webm /dev/null &&\
ffmpeg -i stop.mp4 -c:v libvpx-vp9 -b:v 2M -pass 2 -c:a libopus stop.webm`
```

Die javascript-resourcen aus commonJS zusammenbauen

`npm run-script build --dev`

Aus protobuffer-Definitionen die commonJS-Bindings generieren

`protoc -I=./protocol flunkyprotocol.proto game_objects.proto video_objects.proto --js_out=import_style=commonjs:./client/public/js/ --grpc-web_out=mode=grpcwebtext,import_style=commonjs:./client/public/js/`

### Server

Server bauen und starten im *./kotlinServer* Verzeichnis

`./gradlew run`

Befehlt um den [Go-Proxy](https://github.com/improbable-eng/grpc-web/tree/master/go/grpcwebproxy) zu starten

`./startProxy.sh

[tmux cheat sheet](https://tmuxcheatsheet.com)

## Versionsgeschichte

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
