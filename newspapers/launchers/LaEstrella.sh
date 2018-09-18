#!/bin/bash
fecha=$(date +"%Y%m%d")
directoryPath="/home/vnc/Escritorio/Periodicos/BOL-LaEstrella/"
#directoryPath="/tmp/BOL-LaEstrella/"
pdfName="LaEstrella_${fecha}"
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
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/LaEstrellaDelOrienteDownloader.jar ${directoryPath} >& /tmp/salidaLaEstrellaDelOrienteXVFB.log

	#Iterate over URLs file and download them
	jpgFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do

		IFS='|' read -r -a split <<< "$line"

		pageNumber="${split[0]}"
		pageUrl="${split[1]}"

		temp_jpgPath="${directoryPath}page_${pageNumber}.jpg"

		wget -P $directoryPath -O $temp_jpgPath $pageUrl
		let result=$?

		jpgFileList+=("${directoryPath}page_${pageNumber}.jpg")

	done < "${urlsFilePath}"

	varJpg=$( printf '%s ' "${jpgFileList[@]}" )

	#Convert JPGs to PDF
	convert $varJpg "${pdfPath}"

	#Delete downloaded images
	rm -rf  $varJpg	$urlsFilePath
