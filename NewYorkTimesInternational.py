import time
from datetime import date
from datetime import timedelta
import json
import requests
import re
import os
import sys

from seleniumwire import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from seleniumwire.utils import decode

#fecha="20230719"
today = date.today()
fecha=today.strftime("%Y%m%d")

print("Inicio script con fecha " + fecha)

folderPath="/home/vnc/Escritorio/Periodicos/USA-NewYorkTimesInternational"

folderExist = os.path.exists(folderPath)
if not folderExist:
   os.makedirs(folderPath)
   print("Creado directorio: " + folderPath)


######
### Navegacion Login

driver = webdriver.Chrome()

driver.get('https://inytimes.pressreader.com/the-new-york-times-international-edition')

WebDriverWait(driver, 30).until(EC.presence_of_element_located((By.XPATH,"//a[@class='toolbar-button-signin']")));

btn = driver.find_element(By.XPATH,"//a[@class='toolbar-button-signin']");

btn.click();

WebDriverWait(driver, 30).until(EC.presence_of_element_located((By.ID,"SignInEmailAddress")));

emailBox = driver.find_element(By.ID,"SignInEmailAddress");

emailBox.send_keys("jramongil@hotmail.com");

passBox = driver.find_element(By.XPATH,"//input[@placeholder='Password']");

passBox.send_keys("digital");

submitBtn = driver.find_element(By.XPATH,"//button[@type='submit']");

submitBtn.click();

time.sleep(20)

######
### Capturar Bearer Token de autenticacion

tokenJson = None

for request in driver.requests:

        if request.response:

                if "auth/?ticket" in request.url:
                        print(f"URL: {request.url}")
                        print(f"Content Type: {request.response.headers}")
                        tokenJson = decode(request.response.body, request.response.headers.get('Content-Encoding', 'identity'))
                        print(tokenJson)


if tokenJson is not None:

        data = json.loads(tokenJson)

        bearerToken = data['BearerToken']

        headers = {"Authorization": "Bearer " + bearerToken}

        #page counter inc
        pageCounter=1

        #Stop flag
        flag=True

        while flag:

                url = "https://ingress.pressreader.com/se2skyservices/print/GetImageByRegion?issue=1003"+fecha+"00000000001001&page="+str(pageCounter)+"&paper=Letter&scale=false&scaleToLandscape=false"

                print("Url de descarga pagina " + str(pageCounter) + ": " + url)

                response = requests.get(url, headers=headers)

                print("Url response:")

                print(response)

                try:

                        pageUrl = re.findall('https.*==', response.text)[0]

                        print(pageUrl)

                        response = requests.get(pageUrl)

                        pdfPageName = folderPath + "/page"+str(pageCounter)+".pdf"

                        with open(pdfPageName, 'wb') as f:
                                f.write(response.content)

                        pageCounter = pageCounter + 1

                except IndexError:

                        flag=False
                        if pageCounter < 2:
                                print("No hay paginas")
                        else:
                                print("Ultima pagina alcanzada")

        driver.quit()

else:

        print("No se ha podido obtener token")

####
# Logout y fin navegacion

#WebDriverWait(driver, 30).until(EC.presence_of_element_located((By.ID,"toolbar_botton_menu")));

#menuBtn = driver.find_element(By.ID,"toolbar_botton_menu");

#menuBtn.click();

#WebDriverWait(driver, 30).until(EC.presence_of_element_located((By.XPATH,"//span[@class='pri pri-logout']")));

#singOutBtn = driver.find_element(By.XPATH,"//span[@class='pri pri-logout']");

#singOut.click();
