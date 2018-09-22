#!/bin/bash
fechapdf=`date +"%Y%m%d"`

	pdfName="SuedDeutsche_${fechapdf}.pdf"
	directoryPath="/home/vnc/Escritorio/Periodicos/GER-SuedDeutsche/"
	pdfPath="${directoryPath}${pdfName}"

	firefoxSqlitePath="${directoryPath}cookies.sqlite"
	wgetCookiesPath="${directoryPath}cookies.txt"
	urlsFilePath="${directoryPath}URLs.txt"	

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

	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/SuedDeutscheDownloader.jar ${directoryPath} >& /tmp/salidaSuedDeutscheXVFB.log

	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	/bin/sh /home/vnc/Escritorio/Periodicos/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"

	urlPdf=`cat ${urlsFilePath}`

	echo "URL DE DESCARGA: ${urlPdf}"

	wget --load-cookies $wgetCookiesPath -O ${pdfPath} ${urlPdf}

	ret=$?
        
        if [ $ret -ne 0 ]; then
		rm ${pdfPath}
        fi 

	rm ${wgetCookiesPath} ${firefoxSqlitePath} ${urlsFilePath}

