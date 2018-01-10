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
	count=0
	pdfFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		let count=count+1
	
		IFS='|' read -r -a split <<< "$line"		
		
		temp_pdfPath="${directoryPath}${split[0]}_${fecha}.pdf"
		
		#Push Edicion to first pos
		if [[ "${split[0]}" == *"dici"* ]]; then
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

			imageName="${directoryPath}page_${page}.jpg"
		
			wget -P $directoryPath  -O $imageName "https://cdn.flipsnack.com/collections/items/${split[1]}/covers/page_${page}/original?v=1"
			let result=$?
			
			jpgFileList+=("${directoryPath}page_${page}.jpg")
			let page=page+1
						
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
	
