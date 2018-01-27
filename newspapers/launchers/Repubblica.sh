#!/bin/bash
fecha=$(date +"%Y%m%d")
fecha=$(date +"%d%m%Y")
fileName="Repubblica_${fecha}"
directoryPath="/home/vnc/Escritorio/Periodicos/IT-Repubblica/"
#directoryPath="/tmp/IT-Repubblica/"
firefoxSqlitePath="${directoryPath}cookies.sqlite"
wgetCookiesPath="${directoryPath}cookies.txt"

	echo "Check if exists $directoryPath"

	# Check directory
	if [ ! -d $directoryPath ]; then
        	echo "Create ${directoryPath}"
       		mkdir $directoryPath
	fi

	pdfPath="${directoryPath}${fileName}.pdf"
	pdfPathTemp="${directoryPath}${fileName}_TEMP.pdf"
	echo "Check if exists ${pdfPath}"

	#Check if pdf exists
	if [ -e $pdfPath ]; then
        	echo "${pdfPath} exists. Continue with next version."
        	exit
	fi

	#Run Selenium jar
	xvfb-run -a java -jar /home/vnc/Escritorio/Periodicos/Scripts/RepubblicaDownloader.jar ${directoryPath}
	
	#If cookies file not exists download paper
	if [ ! -e $firefoxSqlitePath ]; then
        	echo "${firefoxSqlitePath} not exists. So paper neither."
        	exit
	fi
	
	#Run extract_firefox_cookies.sh script on Firefox cookies.sqlite to convert them to wget "cookies.txt" format
	/bin/sh /home/vnc/Escritorio/Periodicos/Scripts/extract_firefox_cookies.sh "${firefoxSqlitePath}" > "${wgetCookiesPath}"
	
	#Download pdf with cookies
	
	curl 'https://quotidiano.repubblica.it/edicola/manager?service=download.pdf&data=27012018&issue=27012018&testata=repubblica&sezione=nz_all' -H 'Host: quotidiano.repubblica.it' -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' -H 'Accept-Language: en-GB,en;q=0.5' --compressed -H 'Cookie: KWSOCIAL=KWSOCIAL_ec8e9e13cb35a7ba16937a8de12793; navid=520f868c98798846ad2e2f2a43f68717; gig_hasGmid=ver2; glt_2_f2dVDjSaZJfKNEEXrNo2D5Ddq9SOrxbbavtbRsi9p9rVZlG6PcIXdATccwuSQsc5=LT3_sS5NErDNpl-xzFyhugL51sZutFN7YL3GhWRPAsRStps%7CUUID%3Df3ec907f2f3747878cf7edb228d886fb; amp-access=amp-7R9RE3qvJsrHtnueF7GHpw; cto_lwid=8021b7b9-b49e-4157-8de6-2cc562f3b8e8; repubblicasfoglio-active=520f868c98798846ad2e2f2a43f68717; repubblicasfoglio=8E95A2E653AFCAF29BB37DD8ED253CE42387D984C34B14629C5A77C4B8956C83B4333DBE2189B8CA392E1B9B10604A1F73EEC330FAD655D9DCA7F33A3457A820F450106E24449978D64035F07F4AC3D9DD7C74A774CA676E487592C8D81435277DA06FA4BFAB1AEB93F577D20F863C35' -H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1'
	
	echo "Processing download..."
	sleep 10
		
	curl 'http://quotidiano.repubblica.it/download/repubblica/20180127/nz_all-520f868c98798846ad2e2f2a43f68717.pdf?issue=27012018&testata=repubblica' -H 'Host: quotidiano.repubblica.it' -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' -H 'Accept-Language: en-GB,en;q=0.5' --compressed -H 'Cookie: KWSOCIAL=KWSOCIAL_ec8e9e13cb35a7ba16937a8de12793; navid=520f868c98798846ad2e2f2a43f68717; gig_hasGmid=ver2; glt_2_f2dVDjSaZJfKNEEXrNo2D5Ddq9SOrxbbavtbRsi9p9rVZlG6PcIXdATccwuSQsc5=LT3_sS5NErDNpl-xzFyhugL51sZutFN7YL3GhWRPAsRStps%7CUUID%3Df3ec907f2f3747878cf7edb228d886fb; amp-access=amp-7R9RE3qvJsrHtnueF7GHpw; cto_lwid=8021b7b9-b49e-4157-8de6-2cc562f3b8e8; repubblicasfoglio-active=520f868c98798846ad2e2f2a43f68717; repubblicasfoglio=8E95A2E653AFCAF29BB37DD8ED253CE42387D984C34B14629C5A77C4B8956C83B4333DBE2189B8CA392E1B9B10604A1F73EEC330FAD655D9DCA7F33A3457A820F450106E24449978D64035F07F4AC3D9DD7C74A774CA676E487592C8D81435277DA06FA4BFAB1AEB93F577D20F863C35' -H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' > $pdfPath
	
	pdftk $pdfPath input_pw jramongil@hotmail.com output $pdfPathTemp
	rm $pdfPath
	pdftk $pdfPathTemp output $pdfPath uncompress
	rm $pdfPathTemp
	grep -v -a "520f868c98798846ad2e2f2a43f68717" $pdfPath > $pdfPathTemp
	pdftk $pdfPathTemp output $pdfPath compress
		
	#Delete temp files
	rm $firefoxSqlitePath $wgetCookiesPath $pdfPathTemp
	