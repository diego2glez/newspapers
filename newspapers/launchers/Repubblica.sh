#!/bin/bash
fecha=$(date +"%Y%m%d")
fileName="Repubblica_${fecha}"
directoryPath="/home/vnc/Escritorio/Periodicos/IT-Repubblica/"
#directoryPath="/tmp/IT-Repubblica/"
firefoxSqlitePath="${directoryPath}cookies.sqlite"
wgetCookiesPath="${directoryPath}cookies.txt"

	echo "Check if exists $directoryPath"

	# Check directory
	if [ ! -d $directoryPath ]; then
        	echo "Create ${directoryPath}"
       		mkdir $directoryPath
	fi

	pdfPath="${directoryPath}${fileName}.pdf"
	echo "Check if exists ${pdfPath}"

	#Check if pdf exists
	if [ -e $pdfPath ]; then
        	echo "${pdfPath} exists. Continue with next version."
        	exit
	fi

	#Run Selenium jar
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/RepubblicaDownloader.jar ${directoryPath}
	
	#If cookies file not exists download paper
	if [ ! -e $firefoxSqlitePath ]; then
        	echo "${firefoxSqlitePath} not exists. So paper neither."
        	exit
	fi
	
	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	/bin/sh /home/vnc/Escritorio/Periodicos/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"
	
	#Download pdf with cookies
	
	wget --load-cookies $wgetCookiesPath -O  $pdfPath "https://quotidiano.repubblica.it/edicola/manager?service=download.pdf&data=${fecha}&issue=${fecha}&testata=repubblica&sezione=nz_all"
	
	#Delete temp files
	#rm $firefoxSqlitePath $wgetCookiesPath
	
