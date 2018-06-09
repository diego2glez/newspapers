#!/bin/bash
carpeta="GER-Sueddeutsche"

#Sólo se puede descargar hasta la séptima publicación anterior a la actual.
fecha=$(date +"%Y%m%d")
rm -r tmp/$carpeta 2>/dev/null
mkdir ../$carpeta 2>/dev/null
mkdir -p tmp/$carpeta
cd tmp/$carpeta
condicion=true
while [ $condicion = true ]
do
	curl 'http://epaper.sueddeutsche.de/app/epaper/pdfversion/szglobal_down.php' -H 'Upgrade-Insecure-Requests: 1' -H 'Referer: http://epaper.sueddeutsche.de/digiPaper/html/sde/siteheader_startseite.html' -H 'Cookie: ns_session=true; szidsession=ceabf32594d456d46edae870a57447c1; ns_cookietest=true; creid=1570730809075215962; BIGipServerlb-pay_http=1107959468.20480.0000; BIGipServerlb-epaper-prod_app=973741740.36895.0000; POPUPCHECK=1498403219222; BIGipServerlb-epaper-prod_http=973741740.20480.0000; osc_cuid=3008106838; oscUserState=mitAbos; drerzPrio=EP-60' --data "param_date="$fecha"" --compressed > suedd 2>/dev/null
	url="http://epaper.sueddeutsche.de"$(sed -e 's/>/>\n/g' suedd|grep "Bayernausgabe_komplett"|cut -d '"' -f2|head -n 1)
	echo $url |grep $fecha 2>/dev/null
	if [ $? -gt 0 ]
	then
		echo "No disponible en estos momentos..."
		sleep 360
	else
		wget $url -O Pub$fecha.pdf
		if [ $(head -c 4 Pub$fecha.pdf) = "%PDF" ]
		then
			cd ../..
			mv tmp/$carpeta/Pub$fecha.pdf ../$carpeta/
		else
			echo "Error."
		fi
		condicion=false
	fi
done
rm -r tmp/$carpeta 2>/dev/null
sleep 360
#wget -O - "http://www.eprensa.com/paper_upload.php?filedate="$fechaeu"&type=ramon&approve_domain=sueddeutsche.de"
exit 0

