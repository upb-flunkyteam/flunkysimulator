
## How to run 

### Local

- installier NPM
- dann solltest du die 3 befehle im dockerfile ausführen können
- navigier in frontend/nodejs
- dann npm install, npm run-scropt build:dev
- und dann npm runscript start

### Docker

#### Get Grpc generated with docker

- start docker frontend container
- docker ps, look for container name
- docker cp $container name$:frontend/public/js .
