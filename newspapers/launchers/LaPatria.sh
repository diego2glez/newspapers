#!/bin/bash
fecha=$(date +"%Y%m%d")
directoryPath="/home/vnc/Escritorio/Periodicos/BOL-LaPatria/"
#directoryPath="/tmp/BOL-LaPatria/"
pdfName="LaPatria_${fecha}"
pdfPath="${directoryPath}${pdfName}.pdf"
urlsFilePath="${directoryPath}URLs.txt"
#wgetCookiesPath="${directoryPath}cookiesLaPatria"

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

	function login {
		wget -q --save-cookies cookiesLaPatria --keep-session-cookies "http://lapatriaenlinea.com/habilitador.php" --post-data "email=jramongil%40hotmail.com&contrasena=lapatria17&button=Entrar" -O /dev/null
	}
	login
	
	#Run Selenium jar
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/LaPatriaDownloader.jar ${directoryPath} >& /tmp/salidaLaPatriaXVFB.log
		
	#Iterate over URLs file and download them
	count=0
	pdfFileList=()
	while IFS='' read -r line || [[ -n "$line" ]]; do
    
		let count=count+1
	
		IFS='|' read -r -a split <<< "$line"		
		
		temp_pdfPath="${directoryPath}${split[0]}_${fecha}.pdf"
		
		#Push Edicion to first pos
		if [[ "${split[0]}" == *"RINCIPA"* ]]; then
			pdfFileList=("${temp_pdfPath}" "${pdfFileList[@]}")
		else
			pdfFileList+=("${temp_pdfPath}")
		fi

		echo "------------- ${split[1]} -----------------"
		
		login
		
		#Download PDFs		
		wget --load-cookies cookiesLaPatria --max-redirect 0 "${split[1]}"  -O $temp_pdfPath
		
	done < "${urlsFilePath}"

	var=$( printf '%s ' "${pdfFileList[@]}" )
		
	pdftk $var cat output "${pdfPath}"
			
	#Delete temp files
	rm -rf $urlsFilePath $var $wgetCookiesPath
	
