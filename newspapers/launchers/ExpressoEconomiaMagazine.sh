#!/bin/bash
#fecha=$(date +"%Y_%W")
fileName="ExpressoEconomia"
directoryPath="/home/vnc/Escritorio/Periodicos/PT-ExpressoMagazine/"
#directoryPath="/tmp/ExpressoMagazineDownloader/"
urlsFilePath="${directoryPath}URLs.txt"
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
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/ExpressoEconomiaMagazineDownloader.jar ${directoryPath} >& /tmp/salidaExpressoEconomiaMagazineXVFB.log
		
	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	/bin/sh /home/vnc/Escritorio/Periodicos/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"
	
	#Iterate over URLs file and download them whit WGET and extracted cookies
	count=0
	jpgFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		let count=count+1
	
		imageName="${directoryPath}expresso_economia_${count}.jpg"
		
		jpgFileList+=("${imageName}")
	
		wget -P $directoryPath --load-cookies $wgetCookiesPath -O  $imageName $line
	
		ret=$?
	
		if [ $ret -ne 0 ]; then
			break
		fi	
	
	done < "${urlsFilePath}"
	
	#Convert JPGs to PDF
	var=$( printf '%s ' "${jpgFileList[@]}" )
	echo "List of files to merge: ${var}"

	convert $var "${pdfPath}"
	
	#Delete temp files
	rm $var $urlsFilePath $firefoxSqlitePath $wgetCookiesPath
	
