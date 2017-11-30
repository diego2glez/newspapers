#!/bin/bash
fechaeu=`date +"%Y%m%d"`
fecha=`date +"%d-%m-%y"`
#fecha="10-11-2015"
echo "Descargando publicacion de "$fecha""
mkdir ../BOL-ElPais 2>/dev/null
pene=0
while [ $pene -eq 0 ]
	do
		if wget -r -l 1 -R html,htm,php,tmp www.elpaisonline.com/images/virtual/edicion/$fecha/ 2>/dev/null
			then
				pene=1
				mv www.elpaisonline.com/images/virtual/edicion/$fecha/*.jpg ../BOL-ElPais
				
			else
				sleep 300
		fi
	done
convert ../BOL-ElPais/*.jpg ../BOL-ElPais/ElPais_$fechaeu.pdf
rm -r www.elpaisonline.com
rm -r ../BOL-ElPais/*.jpg
exit 0