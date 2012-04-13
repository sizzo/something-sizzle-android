**If we have done [preparation] section,then we can use below commands for daily activities********

$ cd /cygdrive/d/sizzoworkspace/something-sizzle

$ adb connect 192.168.129.132
connected to 192.168.129.132:5555
 
$ mvn clean install

$ mvn clean install -Prelease \
  -Dsign.keystore=D:/sizzoworkspace/something-parent/test-key.keystore \
  -Dsign.alias=mykey \
  -Dsign.storepass=testtest \
  -Dsign.keypass=testtest

**Preparation************************************
We are using android-archetypes to manage SomethingSizzle project lifecycle, 
for more information about android-archetypes, 
please refer to http://stand.spree.de/wiki_details_maven_archetypes
==================


