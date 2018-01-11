#!/bin/bash
fecha=$(date +"%Y%m%d")
fileName="AdnParaguayo"
downloadPath="/home/vnc/Escritorio/Periodicos/PY-ADNParaguayo"

directoryPath="${downloadPath}${i}/"
echo "Check if exists $directoryPath"

# Check directory
if [ ! -d $directoryPath ]; then
		echo "Create ${directoryPath}"
		mkdir $directoryPath
fi

pdfPath="${directoryPath}${fileName}_${fecha}.pdf"
echo "Check if exists ${pdfPath}"

#Check if pdf exists
if [ -e $pdfPath ]; then
		echo "${pdfPath} exists. Continue with next version."
		exit 1
fi

#Download JPGs
result=0
count=1
jpgFileList=()
while  [ $result -eq 0 ];
do

	wget -P $directoryPath "http://www.adndigital.com.py/hojeable/${fecha}/files/mobile/${count}.jpg"
	let result=$?
	
	if [ $result -eq 0 ];
	then
		jpgFileList+=("${directoryPath}${count}.jpg")
	fi
	
	let count=count+1

done

#Stop if no pages where downloaded
if [ $count -le 3 ];
then
	echo "No data to download"
	exit 1
fi 

#List of JPGs to merge
var=$( printf '%s ' "${jpgFileList[@]}" )
echo "List of files to merge: ${var}"

convert $var "${pdfPath}"

#Delete JPGs
rm $var


