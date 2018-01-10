#!/bin/bash
fecha=$(date +"%Y%m%d")
directoryPath="/home/vnc/Escritorio/Periodicos/BOL-Cambio/"
#directoryPath="/tmp/BOL-Cambio/"
pdfName="Cambio_${fecha}"
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
	java -jar /home/vnc/Escritorio/Periodicos/Scripts/CambioDownloader.jar ${directoryPath} 
	
	#Iterate over URLs file and download them
	count=0
	pdfFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		let count=count+1
	
		IFS='|' read -r -a split <<< "$line"		
		
		temp_pdfPath="${directoryPath}${split[0]}_${fecha}.pdf"
		
		#Push Edicion to first pos
		if [[ "${split[0]}" == *"dicion"* ]]; then
			pdfFileList=("${temp_pdfPath}" "${pdfFileList[@]}")
		else
			pdfFileList+=("${temp_pdfPath}")
		fi
		
		#Download JPGs
		result=0
		page=1
		jpgFileList=()
		while  [ $result -eq 0 ];
		do

			wget -P $directoryPath "${split[1]}page_${page}.jpg"
			let result=$?
			
			if [[ $result -eq 0 ]]; then
				jpgFileList+=("${directoryPath}page_${page}.jpg")
				let page=page+1
			fi
			
		done

		varJpg=$( printf '%s ' "${jpgFileList[@]}" )
				
		#Convert JPGs to PDF
		convert $varJpg "${temp_pdfPath}"
		
		#Delete downloaded images
		rm -rf  $varJpg		
		
	done < "${urlsFilePath}"

	var=$( printf '%s ' "${pdfFileList[@]}" )
		
	pdftk $var cat output "${pdfPath}"
			
	#Delete temp files
	rm -rf $urlsFilePath $var
	
	
