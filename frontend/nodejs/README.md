
#Frontend

##Lokal starten

### Im Docker-Container

TODO

### Node

Im Ordner frontend/nodejs

Aus Protobuf-Definitionen die commonJS-Bindings generieren

`protoc -I=./protocol  --js_out=import_style=commonjs:./frontend/nodejs/public/js/ --grpc-web_out=mode=grpcwebtext,import_style=commonjs:./frontend/nodejs/public/js/ ./protocol/*.proto`

NodeJS-Dependencies installieren

`npm install`

Backend-Server-URL auswählen

`export BACKEND_URL="https://www.example.com:8443"`

Javascript-Ressourcen bauen

`npm run-script build:dev`

NodeJS als lokalen Webserver unter Port 5000 starten

`npm run-script start`

`http://localhost:5000/` im Browser besuchen

## Grpc genrieren ohne protoc mit docker

- start docker frontend container
- docker ps, look for container name
- docker cp $container name$:frontend/public/js .

## "hidden" Setting functions

Folgende Befehle können in der Browserkonsole ausgeführt werden:

- window.debug.settings() - Zeigt die aktuellen Einstellungen an.
- window.debug.setRestingPeriod(milliseconds) - Setzt die RestingPeriod auf $milliseconds$ Millisekunden.
