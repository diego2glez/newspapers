#!/bin/bash
fechapdf=`date +"%Y%m%d"`

	pdfName="Valor_${fechapdf}.pdf"
	directoryPath="/home/vnc/Escritorio/Periodicos/BR-Valor/"
	pdfPath="${directoryPath}${pdfName}"

	firefoxSqlitePath="${directoryPath}cookies.sqlite"
	wgetCookiesPath="${directoryPath}cookies.txt"
	urlsFilePath="${directoryPath}URLs.txt"	

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

	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/ValorDownloader.jar ${directoryPath} >& /tmp/salidaValorXVFB.log
	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	#/bin/sh /home/vnc/Escritorio/Periodicos/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"

        count=0
        jpgFileList=()
        while IFS='' read -r line || [[ -n "$line" ]]; do
    
                let count=count+1
        
                imageName="${directoryPath}Valor_${count}.jpeg"
                
                echo "Imagen ${imageName}"

                wget -P $directoryPath -O  $imageName $line
        
                ret=$?
        
                if [ $ret -ne 0 ]; then
                        break
                fi      

		pdfPageName="${directoryPath}Valor_${count}.pdf"

		convert "${imageName}" "${pdfPageName}"
        
		jpgFileList+=("${pdfPageName}")

		rm "${imageName}"

        done < "${urlsFilePath}"

        #Convert JPGs to PDF
        var=$( printf '%s ' "${jpgFileList[@]}" )
        echo "List of files to merge: ${var}"

        pdftk $var cat output "${pdfPath}"

	rm ${wgetCookiesPath} ${firefoxSqlitePath} ${urlsFilePath} ${var}

