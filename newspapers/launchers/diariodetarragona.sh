#!/bin/bash
fecha=`date +"%Y%m%d"`
#fecha=20170913
pdfName="DiaroDeTarrgona_${fecha}.pdf"
carpeta=diaridetarragona
rm enlaceslv 2>/dev/null
rm -r tmp/$carpeta 2>/dev/null
mkdir -p tmp/$carpeta
mkdir ../$carpeta/ 2>/dev/null
pag=1
listPages=()
echo "Descargando..."
while [ 1 -eq 1 ]
	do		
		if wget --load-cookie cookiekios "http://lector.kioskoymas.com/epaper/services/OnlinePrintHandler.ashx?issue=3817"$fecha"00000000001001&page="$pag"&paper=Letter" -O enlaceslv 2>/dev/null
			then
				enlace=`grep -Eoi '<img [^>]+>' enlaceslv | cut -d '"' -f2`
				if wget $enlace -O tmp/$carpeta/Pag"$pag".png 2>/dev/null
					then
						if [ 0 = `ls -l tmp/$carpeta/Pag"$pag".png |tr -s " "|cut -d " " -f5` ]
							then
							rm ../$carpeta/Pag"$pag".png
							exit 0
						else
							res=`identify tmp/$carpeta/Pag"$pag".png |cut -d " " -f3`
							convert -crop "$res"+0-70 -resize 50% -density 0 -quality 92 tmp/$carpeta/Pag"$pag".png ../$carpeta/Pag"$pag".png
							echo "Descargada pagina: "$pag""
							listPages+=("../$carpeta/Pag${pag}.png")
							pag=`expr $pag + 1`
						fi
					else
						rm -r tmp/$carpeta 2>/dev/null
						exit 0 
			
				fi
			else
				if [ $pag = 1 ]
				then
					sleep 500
				else
					rm -r /tmp/$carpeta 2>/dev/null
					rm enlaceslv 2>/dev/null

					varPngs=$( printf '%s ' "${listPages[@]}" )
							
					#Convert PNGs to PDF
					convert $varPngs "../${carpeta}/${pdfName}"
					rm $varPngs
					exit 0
				fi
		fi 
		
	done
	
