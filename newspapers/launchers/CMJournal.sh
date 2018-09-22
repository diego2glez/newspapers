#!/bin/bash
fecha=$(date +"%Y%m%d")
directoryPath="/home/vnc/Escritorio/Periodicos/PT-Correiodamanha"
#directoryPath="/tmp/CMJournal/"
pdfName="CMJournal_${fecha}"
pdfPath="${directoryPath}${pdfName}.pdf"
urlsFilePath="${directoryPath}URLs.txt"

	echo "Check if exists $directoryPath"

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

	#Run Selenium jar
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/CMJournalDownloader.jar ${directoryPath} >& /tmp/salidaCMJournalXVFB.log
	
	#Check if urlsFile exists
	if [ -e $urlsFilePath ]; then
		echo "${urlsFilePath} exists. Exit script."
		exit 1
	fi	
	
	#Iterate over URLs file and download them
	result=0
	page=1
	jpgFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		#Download JPGs	
		wget -O "${directoryPath}${page}.png" "${line}"
		let result=$?
		
		#if [[ $result -eq 0 ]]; then
		jpgFileList+=("${directoryPath}${page}.png")
		let page=page+1
		#fi
			
	done < "${urlsFilePath}"

	if [ "${page}" -lt 5 ]; then
		echo "No hay suficientes paginas, salimos de la ejecucion"
		exit 1
	fi	
	
	varJpg=$( printf '%s ' "${jpgFileList[@]}" )
			
	echo "Convirtiendo..."
			
	#Convert JPGs to PDF
	convert $varJpg "${pdfPath}"
	
	#Delete temp files
	rm -rf $varJpg $urlsFilePath
	
