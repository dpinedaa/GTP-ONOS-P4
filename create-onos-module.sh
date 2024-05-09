#!/bin/bash

createonosmodule(){
    echo "Creating ONOS Module"
    #Find the directory of BaselineClass.java
    baselineClass=$(find $1 -type f -name "BaselineClass.java" | xargs dirname)
    echo "BaselineClass.java directory: $baselineClass"

    cp "$baselineClass/BaselineClass.java" "$baselineClass/$2.java"
    
    # Replace BaselineClass in the new java file $2.java
    sed -i "s#BaselineClass#$2#g" "$baselineClass/$2.java"
    
    mvn clean package 
    echo "ONOS Module created successfully in $1."
}


onosAppProjectName=""
while [ ! -d "$onosAppProjectName" ]; do
    echo "Please enter ONOS project directory name:"
    read onosAppProjectName
    if [ ! -d "$onosAppProjectName" ]; then
        echo "Directory '$onosAppProjectName' does not exist. Please enter a valid directory."
    fi
done
echo "You entered: $onosAppProjectName"

echo "Enter the new Onos Module Name"
read onosModuleName
echo "You entered: $onosModuleName"

createonosmodule "$onosAppProjectName" "$onosModuleName"


