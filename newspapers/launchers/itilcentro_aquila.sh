#!/bin/bash

#Inicialización de variables

carpeta="IT-IlCentro-Aquila"
fecha=$(date +"%Y%m%d")
yy=$(date +"%Y")
#fecha=20170928
fechaeu=${fecha}
nodescargado=true
carpetatmp="tmp/${carpeta}"
url="http://digital.ilcentro.it"
#

#Creación de carpetas y borrado de archivos anteriores
rm -r tmp/$carpeta 2>/dev/null
mkdir ../${carpeta}
mkdir -p ${carpetatmp}
#

function login {
	wget --quiet --save-cookies cookitcentro --keep-session-cookies "${url}/ilcentro/webservice/internal_login.jsp" --post-data "username=jramongil%40hotmail.com&password=art59ba3&remember=true&device=web" -O /dev/null
}
login

pag=1
while $nodescargado; do
	sleep 2
	wget -q --load-cookies cookitcentro --max-redirect 0 "http://digital.ilcentro.it/ilcentro/books/aquila/"${yy}"/"${fecha}"aquila/" 2>/dev/null
	if [ $? -eq 0 ]; then
		while $nodescargado; do
		wget -nv --load-cookies cookitcentro "${url}/ilcentro/books/"${fecha}"aquila/singole/"${pag}".pdf&download=true" -O ${carpetatmp}/Pag${pag}.pdf		
		if [ $? -eq 0 ]; then
		size=$(du ${carpetatmp}/Pag${pag}.pdf | cut -f1)
		if [ $size -lt 10 ]; then
			login
			sleep 10
		else
			if [ $pag -lt 10 ]; then
				mv ${carpetatmp}/Pag$pag.pdf ${carpetatmp}/Pag00$pag.pdf
			else if [ $pag -lt 100 ]; then
				mv ${carpetatmp}/Pag$pag.pdf ${carpetatmp}/Pag0$pag.pdf
			fi
			fi
			pag=$(( pag+1 ))
		fi
		else
		if [ $pag -gt 1 ]; then
			rm ${carpetatmp}/Pag${pag}.pdf
			gs -dBATCH -dNOPAUSE -q -sDEVICE=pdfwrite -sOutputFile=../${carpeta}/Pub${fecha}.pdf ${carpetatmp}/*.pdf
			nodescargado=false
		fi
		fi
		done
	else
			echo "No disponible"
			sleep 360
	fi
done
rm -r tmp/$carpeta 2>/dev/null

exit 0
