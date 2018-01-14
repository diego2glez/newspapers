#!/bin/bash
fecha=$(date +"%Y%m%d")
directoryPath="/home/vnc/Escritorio/Periodicos/GER-FrankfurterAllgemeine/"
#directoryPath="/tmp/GER-FrankfurterAllgemeine/"
pdfName="FrankfurterAllgemeine_${fecha}"
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
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/FrankfurterDownloader.jar ${directoryPath} >& /tmp/salidaFrankfurterXVFB.log
	
	#Iterate over URLs file and download them
	count=0
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		let count=count+1
	
		IFS='|' read -r -a split <<< "$line"		
				
		#Download JPGs
		result=0
		page=1
		jpgFileList=()
		while  [ $result -eq 0 ];
		do

			wget -O "${directoryPath}${page}.jpg" "${split[1]}${page}.jpg"
			let result=$?
			
			#if [[ $result -eq 0 ]]; then
				jpgFileList+=("${directoryPath}${page}.jpg")
				let page=page+1
			#fi
			
		done

		varJpg=$( printf '%s ' "${jpgFileList[@]}" )
				
		#Convert JPGs to PDF
		convert $varJpg "${pdfPath}"
		
		#Delete downloaded images
		rm -rf  $varJpg		
		
	done < "${urlsFilePath}"
			
	#Delete temp files
	rm -rf $urlsFilePath
	
