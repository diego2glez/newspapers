#!/bin/bash
fecha=$(date +"%Y%m%d")
fileName="Expansion${fecha}"
directoryPath="/tmp/Expansion/"
urlsFilePath="${directoryPath}URLs.txt"
firefoxSqlitePath="${directoryPath}cookies.sqlite"
wgetCookiesPath="${directoryPath}cookies.txt"

dia=`date +%w`
finalPath="/home/FTPusers/cprofesional/${dia}/"


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
	xvfb-run -a java -jar /home/vnc/Escritorio/Scripts/ExpansionDownloader.jar ${directoryPath} >& /tmp/salidaExpansionXVFB.log
		
	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	/bin/sh /home/vnc/Escritorio/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"
	
	#Iterate over URLs file and download them whit WGET and extracted cookies
	count=0
	pdfFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		let count=count+1
	
		pdfName="${directoryPath}expansion_${count}.pdf"
		
		pdfFileList+=("${pdfName}")
	
		wget -P $directoryPath --load-cookies $wgetCookiesPath -O  $pdfName $line
	
		ret=$?
	
		if [ $ret -ne 0 ]; then
			break
		fi	
	
	done < "${urlsFilePath}"
	
	#Merge PDF
	var=$( printf '%s ' "${pdfFileList[@]}" )
	echo "List of files to merge: ${var}"
                
        pdftk $var cat output "${pdfPath}"

	mv "${pdfPath}" "${finalPath}"

	#Delete temp files
	rm -rf $var $urlsFilePath $firefoxSqlitePath $wgetCookiesPath $directoryPath
	
