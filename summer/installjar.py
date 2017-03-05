#! /usr/bin/python
import os

os.system('mvn clean install')

os.system('cp -r ./target/Summer-0.0.1.jar  ../app/libs')

os.chdir('../app')

os.system('gradle clean assembleDebug')

	
