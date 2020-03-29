# Flunkyball-Simulator des UPB Flunkyteam

Dieses Projekt ist eine Kombination aus klassischem Javascript und Node.js.
Gehostet wird die resultierende Seite auf Heroku.
Die Video-Dateien sind zum Schutz der Privatsphäre nicht in diesem Repository gespeichert. Es können aber eigene Videos im public_html/videos Ordner abgelegt werden.

## Nützliche Kommandozeilen-Befehle
MP4-Videos in kleinere Webms mit VP9 rekodieren (Siehe [FFMPEG-Wiki](https://trac.ffmpeg.org/wiki/Encode/VP9#twopass))

`ffmpeg -i stop.mp4 -c:v libvpx-vp9 -b:v 2M -pass 1 -an -f webm /dev/null &&  ffmpeg -i stop.mp4 -c:v libvpx-vp9 -b:v 2M -pass 2 -c:a libopus stop.webm`

Den lokalen Branch "videos", der nicht im Github-Repository vorhanden ist, auf Heroku deployen

`git push -f heroku videos:master`
