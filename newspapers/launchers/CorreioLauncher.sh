#!/bin/bash
fecha=$(date +"%Y%m%d")
fileName="correiodamanha$fecha.pdf"
downloadPath="/home/vnc/Escritorio/Periodicos/PT-Correiodamanha/"

echo "Looking for $downloadPath$fileName"

#Check if directory exists
if [ ! -d "$downloadPath" ]; then
	echo "Create $downloadPath"
	mkdir $downloadPath
fi

#Check if pdf exists
if [ ! -e "$downloadPath$fileName" ]; then
	echo "Launch downloader"
        xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/CorreiodamanhaDownloader.jar >& /tmp/salidaCorreiodamanha.log
else
	echo "$fileName found"

fi

