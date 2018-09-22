#!/bin/bash
fecha=$(date +"%Y%m%d")
fileName="Repubblica_${fecha}"
directoryPath="/home/vnc/Escritorio/Periodicos/IT-Repubblica/"
#directoryPath="/tmp/IT-Repubblica/"

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
	
	pdfTempName=`ls ${directoryPath}`
	
	pdfTempPath="${directoryPath}${pdfTempName}"
	
	#If cookies file not exists download paper
	if [ ! -e $pdfTempPath ]; then
        	echo "${pdfTempPath} not exists. So exit."
        	exit
	fi
		
	pdftk $pdfTempPath input_pw jramongil@hotmail.com output $pdfPath
	
	#Delete temp files
	rm $pdfTempPath
	