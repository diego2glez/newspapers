#!/bin/bash
fechapdf=`date +"%Y%m%d"`
fechaeu=`date +"%Y%m%d"`
dia=$(date +"%d")
mes=$(date +"%m")
yy=$(date +"%Y")

fechaJson=$(date +"%Y-%m-%d")

	echo "Vars ${dia} - ${mes} - ${yy} - ${fechaJson}"
	
	pdfName="Handelsblatt_${fechapdf}.pdf"
	directoryPath="/home/vnc/Escritorio/Periodicos/GER-Handelsblatt/"
	pdfPath="${directoryPath}${pdfName}"
	
	firefoxSqlitePath="${directoryPath}cookies.sqlite"
	wgetCookiesPath="${directoryPath}cookies.txt"

	salidaJson="${directoryPath}out.json"
	
	jsonPostLine="{\"deviceInfo\":{\"width\":1920,\"height\":969},\"editions\":[{\"defId\":\"11\",\"publicationDate\":\"${fechaJson}\"}]}"

	echo "${jsonPostLine}" > /tmp/salidaHandelsblattJS.log

	# Pages URL csv format file
	urlsPath="${directoryPath}urls.csv"

	# Pages Number csv format file
	pagesNumPath="${directoryPath}pageNums.csv"

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

	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/HandelsblattDownloader.jar ${directoryPath} >& /tmp/salidaHandelsblattXVFB.log

	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	/bin/sh /home/vnc/Escritorio/Periodicos/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"

	#Get Pages JSON info	
	curl --trace "/tmp/curl.out" -trace-ascii 'https://epaper.handelsblatt.com/index.cfm/epaper/1.0/getPages' -H 'Content-Type: application/json' -d ${jsonPostLine} -H 'Connection: keep-alive'  --cookie ${wgetCookiesPath} > "${salidaJson}"

	echo "Parsing JSON"

	#Extract pages url
	cat ${salidaJson} | jq -r '[.data.pages[].pageDocUrl.HIGHRES.url] | @csv' | tr -d '"' > ${urlsPath}

	#Extract pages number
	cat ${salidaJson} | jq -r '[.data.pages[].pmPagination] | @csv' | tr -d '"' > ${pagesNumPath}

	echo "Parsing csvs"

	OIFS=$IFS;
	IFS=",";

	stringCSV=`cat ${urlsPath}`

	#Parse urls to collection
	urls=($stringCSV)
	
	#Parse  nums to collection
	stringCSV=`cat ${pagesNumPath}`
	pages=($stringCSV)


	echo "${urls[@]}"
	echo "--------------------------"
	echo "${pages[@]}"


	let counter=1
	let pointer=0
	pdfFileList=()

	pdfTempMergin="${directoryPath}Page_Temp.pdf"
	#Create blank temp
	`convert xc:none -page A4 "${pdfTempMergin}"`

	for i in "${pages[@]}"
	do
		#Break if pagenumber restart
		if [ "${pages[$pointer]}" -ne "${counter}" ]
		then
			break
		fi

                pdfPagePath="${directoryPath}Page_${counter}.pdf"
                
                pdfFileList+=("${pdfPagePath}")

		wget --load-cookies $wgetCookiesPath -O  "${pdfPagePath}" "${urls[$pointer]}"

                counter=$((counter+1))
                pointer=$((pointer+1))

		if [ "${pages[$pointer]}" -eq "1" ]
                then
                        break
                fi

		pdftk "${pdfTempMergin}" "${pdfPagePath}" cat output "${directoryPath}temp.pdf"

		mv "${directoryPath}temp.pdf" "${pdfTempMergin}" 

	done

	pdftk "${pdfTempMergin}" cat 2-end output "${pdfPath}" 

	#Merge pdf pages
	var=$( printf '%s ' "${pdfFileList[@]}" )

	#wget ${urlPdf} -O ${pdfPath}

	rm ${wgetCookiesPath} ${firefoxSqlitePath} ${salidaJson} ${pdfTempMergin} ${urlsPath} ${pagesNumPath} 
