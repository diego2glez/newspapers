a=$(date +"%Y%m%d")
ftpUrlName="www.elpaisonline.com"
directoryPath="/home/vnc/Escritorio/Periodicos/BOL-ElPais/"
#directoryPath="/tmp/BOL-ElPais/"
pdfName="ElPais_${fecha}"
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
	echo "${pdfPath} exists. Continue with next version."
	exit 1
fi	

#Run Selenium jar
java -jar /home/vnc/Escritorio/Periodicos/Scripts/ElPaisOnlineDownloader.jar ${directoryPath} 
																					
#Iterate over URLs file and download them
count=0
pdfFileList=()
while IFS='' read -r line || [[ -n "$line" ]]; do

let count=count+1

IFS='|' read -r -a split <<< "$line"		

temp_pdfPath="${directoryPath}${split[0]}_${fecha}.pdf"

#Push Edicion to first pos
if [[ "${split[0]}" == *"Edici"* ]]; then
pdfFileList=("${temp_pdfPath}" "${pdfFileList[@]}")
else
pdfFileList+=("${temp_pdfPath}")
fi

#Download recursively
wget -r -l 1 -R html,htm,php,tmp,txt,tmp  "${split[1]}" -P "${directoryPath}"

#Move downloaded images
find "${directoryPath}" -name '*.jpg' -exec mv -t "${directoryPath}" {} +

#Convert JPGs to PDF
convert "${directoryPath}*.jpg" "${temp_pdfPath}"

#Delete downloaded images
find "${directoryPath}" -name '*.jpg' -exec rm {} +

done < "${urlsFilePath}"
var=$( printf '%s ' "${pdfFileList[@]}" )

echo $var

pdftk $var cat output "${pdfPath}"

#Delete temp files
rm -rf $urlsFilePath $var "${directoryPath}${ftpUrlName}"

sleep 360
wget -O - "http://www.eprensa.com/paper_upload.php?filedate="$fecha"&type=ramon&approve_domain=elpaisonline.com"

