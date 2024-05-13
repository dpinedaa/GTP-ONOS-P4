#!/bin/bash

compilep4c(){
    cd "$1"
    p4c -b bmv2 --p4runtime-files "$1.p4info.txt" "$1.p4"
    cd ..
    cp "$1/$1.p4info.txt" "$2/src/main/resources/$1.p4info.txt"
    cp "$1/$1.json" "$2/src/main/resources/$1.json"
    cd "$2"
    #Find the directory of PipeconfLoader.java
    pipeconfLoaderDir=$(find . -type f -name "PipeconfLoader.java" | xargs dirname)
    echo "PipeconfLoader.java directory: $pipeconfLoaderDir"
    mvn clean package
}

p4ProjectName=""
while [ ! -d "$p4ProjectName" ]; do
    echo "Please enter P4 project directory name:"
    read p4ProjectName
    if [ ! -d "$p4ProjectName" ]; then
        echo "Directory '$p4ProjectName' does not exist. Please enter a valid directory."
    fi
done

onosAppProjectName=""
while [ ! -d "$onosAppProjectName" ]; do
    echo "Please enter ONOS project directory name:"
    read onosAppProjectName
    if [ ! -d "$onosAppProjectName" ]; then
        echo "Directory '$onosAppProjectName' does not exist. Please enter a valid directory."
    fi
done

echo "You entered: $onosAppProjectName"
compilep4c "$p4ProjectName" "$onosAppProjectName"