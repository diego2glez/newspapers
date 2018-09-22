#!/bin/bash
fechapdf=`date +"%Y%m%d"`
fechaeu=`date +"%Y%m%d"`
dia=$(date +"%d")
mes=$(date +"%m")
yy=$(date +"%Y")

fechaJson=$(date +"%Y-%m-%d")

	echo "Vars ${dia} - ${mes} - ${yy} - ${fechaJson}"
	
	pdfName="Handelsblatt_${fechapdf}.pdf"
	directoryPath="/home/vnc/Escritorio/Periodicos/GER-Handelsblatt/"
	pdfPath="${directoryPath}${pdfName}"
	
	firefoxSqlitePath="${directoryPath}cookies.sqlite"
	wgetCookiesPath="${directoryPath}cookies.txt"

	salidaJson="${directoryPath}out.json"
	
	jsonPostLine="{\"editions\":[{\"defId\":\"11\",\"publicationDate\":\"${fechaJson}\"}],\"isAttachment\":true,\"fileName\":\"Gesamtausgabe_Handelsblatt_${fechaJson}.pdf\"}"

	# Check directory
	if [ ! -d $directoryPath ]; then
			echo "Create ${directoryPath}"
			mkdir $directoryPath
	fi

		#Check if pdf exists
	if [ -e $pdfPath ]; then
		echo "${pdfPath} exists. Exit script."
		exit 1
	fi	
	
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/HandelsblattDownloader.jar ${directoryPath} >& /tmp/salidaHandelsblattXVFB.log
	
	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	/bin/sh /home/vnc/Escritorio/Periodicos/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"
	
	curl -trace-ascii 'https://epaper.handelsblatt.com/index.cfm/epaper/1.0/getEditionDoc' -H 'Content-Type: application/json' -d ${jsonPostLine} -H 'Connection: keep-alive'  --cookie ${wgetCookiesPath} > "${salidaJson}"
		
	urlPdf=`cat ${salidaJson} | awk -F '"' '{print $6}'`
	
	echo "URL DESCARGA ${urlPdf}"
	
	wget ${urlPdf} -O ${pdfPath}
	
	rm ${wgetCookiesPath} ${firefoxSqlitePath} ${salidaJson}


