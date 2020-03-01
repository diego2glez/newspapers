#!/bin/bash
fechapdf=`date +"%Y%m%d"`

	pdfName="FTUK_${fechapdf}.pdf"
	directoryPath="/home/vnc/Escritorio/Periodicos/UK-FinancialTimes/"
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

	ret=0
	count=0
	year=`date +"%Y"`
	month=`date +"%m"`
	day=`date +"%d"`
	pdfFileList=()
	while [ $ret -eq 0 ]; do

		let count=count+1

		imageName="${directoryPath}FTUK_${count}.png"

		wget -O  $imageName "https://digital.olivesoftware.com/Olive/ODN/FTEurope/get/image.ashx?kind=page&href=FTE%2F${year}%2F${month}%2F${day}&page=${count}&res=120"

		ret=$?

                if [ $ret -ne 0 ]; then
                        break
                fi      

                pdfPageName="${directoryPath}UKFT_${count}.pdf"

                convert "${imageName}" "${pdfPageName}"

                pdfFileList+=("${pdfPageName}")

                rm "${imageName}"

	done

        var=$( printf '%s ' "${pdfFileList[@]}" )
        echo "List of files to merge: ${var}"

        pdftk $var cat output "${pdfPath}"

	rm ${wgetCookiesPath} ${firefoxSqlitePath} ${urlsFilePath} ${var}

