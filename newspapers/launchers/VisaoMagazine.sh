#!/bin/bash
#fecha=$(date +"%Y_%W")
fileName="VisaoMagazine"
directoryPath="/home/vnc/Escritorio/Periodicos/PT-VisaoMagazine/"
tempDirectoryPath="/tmp/VisaoMagazineDownloader/"
urlsFilePath="${tempDirectoryPath}URLs.txt"
firefoxSqlitePath="${tempDirectoryPath}cookies.sqlite"


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

	rm -rf $tempDirectoryPath

        # Check directory
        if [ ! -d $tempDirectoryPath ]; then
                echo "Create ${tempDirectoryPath}"
                mkdir $tempDirectoryPath
        fi

	#Run Selenium jar
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/VisaoMagazineDownloader.jar ${tempDirectoryPath} >& /tmp/salidaVisoMagazineXVFB.log

	echo "Fin Java"

	#Iterate over URLs file and download them whit WGET and extracted cookies
	count=0
	jpgFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		let count=count+1
	
		imageName="${directoryPath}visao_${count}.pdf"
		
		echo "Imagen ${imageName}"

		jpgFileList+=("${imageName}")
	
		wget -P $directoryPath -O  $imageName $line
	
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
	rm $var $urlsFilePath
