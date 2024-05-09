#!/bin/bash

# Define function to create project directory
createprojectdir() {
    mkdir -p "$PWD/$1/src/main/java"
    mkdir -p "$PWD/$1/src/main/resources"
    mkdir -p "$PWD/$1/target"
}

createmaindir() {
    mkdir -p "$PWD/$1/src/main/java/$2/$3/$4/$1/cli"
    mkdir -p "$PWD/$1/src/main/java/$2/$3/$4/$1/common"
    mkdir -p "$PWD/$1/src/main/java/$2/$3/$4/$1/pipeconf"
}

create-onos-app(){
    # Ask for project name
    echo "Please enter your project name:"
    read projectName
    projectName="${projectName// /}"  # Remove spaces
    projectName="${projectName:-app}"  # Default to app if not provided

    # Convert to lowercase
    projectName="${projectName,,}"

    # Print the input
    echo "You entered: $projectName"

    # Check if the directory already exists
    if [ -d "$PWD/$projectName" ]; then
    echo "Directory '$projectName' already exists in $PWD."
    echo "Do you want to recreate it? (y/n)"
    read recreateChoice
    if [ "$recreateChoice" = "y" ]; then
        rm -r "$PWD/$projectName"
        createprojectdir "$projectName"
        echo "Directory '$projectName' recreated successfully in $PWD."
    else
        echo "Exiting without recreating the directory."
        exit 0
    fi
    else
    # Create a directory based on the user input
    createprojectdir "$projectName"
    # Check if directory creation was successful
    if [ $? -eq 0 ]; then
        echo "Directory '$projectName' created successfully in $PWD."
    else
        echo "Failed to create directory."
        exit 1
    fi
    fi

    echo "Country Name (2 letter code) [US]:"
    read countryName
    countryName="${countryName:-US}"  # Default to US if not provided
    countryName="${countryName,,}"  # Convert to lowercase

    echo "Organization Name (eg, company) [FIU]:"
    read organizationName
    organizationName="${organizationName:-FIU}"  # Default to FIU if not provided
    organizationName="${organizationName,,}"  # Convert to lowercase

    echo "Organizational Unit Name (eg, section) [ADWISE]:"
    read organizationalUnitName
    organizationalUnitName="${organizationalUnitName:-ADWISE}"  # Default to IT if not provided
    organizationalUnitName="${organizationalUnitName,,}"  # Convert to lowercase

    # ArtifactId
    artifactId="${projectName}-app"
    echo "Artifact ID: $artifactId"

    onos_app_url="https://${projectName}.${organizationalUnitName}.${organizationName}.${countryName}"
    echo "ONOS App URL: $onos_app_url"

    onos_app_name="${countryName}.${organizationName}.${organizationalUnitName}.${projectName}"
    echo "ONOS App Name: $onos_app_name"

    # Create the src directory structure
    createmaindir "$projectName" "$countryName" "$organizationName" "$organizationalUnitName"

    # Convert project name to uppercase for title
    projectTitle="${projectName^^}"
    echo "Project Title: $projectTitle App"

    # Ask for P4 code directory
    p4ProjectName=""
    while [ ! -d "$p4ProjectName" ]; do
        echo "Please enter P4 project directory name:"
        read p4ProjectName
        if [ ! -d "$p4ProjectName" ]; then
            echo "Directory '$p4ProjectName' does not exist. Please enter a valid directory."
        fi
    done

    echo "You entered: $p4ProjectName"

    #Copy files from baseline directory
    #pom.xml
    cp $PWD/baseline/pom.xml $PWD/$projectName/pom.xml
    #FabricDeviceConfig.java
    cp $PWD/baseline/FabricDeviceConfig.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/common/FabricDeviceConfig.java
    #Utils.java
    cp $PWD/baseline/Utils.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/common/Utils.java
    #InterpreterImpl.java
    cp $PWD/baseline/InterpreterImpl.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/pipeconf/InterpreterImpl.java
    #PipeconfLoader.java
    cp $PWD/baseline/PipeconfLoader.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/pipeconf/PipeconfLoader.java
    #PipelinerImpl.java
    cp $PWD/baseline/PipelinerImpl.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/pipeconf/PipelinerImpl.java
    #AppConstants.java
    cp $PWD/baseline/AppConstants.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/AppConstants.java
    #MainComponent.java
    cp $PWD/baseline/MainComponent.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/MainComponent.java
    #BaselineClass.java
    cp $PWD/baseline/BaselineClass.java $PWD/$projectName/src/main/java/$countryName/$organizationName/$organizationalUnitName/$projectName/BaselineClass.java
    #Replace ONOSARTIFACTID in all files based on the directory name
    find $PWD/$projectName -type f -exec sed -i "s#ONOSARTIFACTID#$artifactId#g" {} \;
    find $PWD/$projectName -type f -exec sed -i "s#ONOSURL#$onos_app_url#g" {} \;
    find $PWD/$projectName -type f -exec sed -i "s#ONOSTITLE#$projectTitle App#g" {} \;
    find $PWD/$projectName -type f -exec sed -i "s#ONOSAPPNAME#$onos_app_name#g" {} \;
    find $PWD/$projectName -type f -exec sed -i "s#ONOSP4DIR#$p4ProjectName#g" {} \;
    #Copy the P4 project directory to the project directory
    cp $PWD/$p4ProjectName/$p4ProjectName.p4info.txt $PWD/$projectName/src/main/resources/$p4ProjectName.p4info.txt
    cp $PWD/$p4ProjectName/$p4ProjectName.json $PWD/$projectName/src/main/resources/$p4ProjectName.json
    cd $PWD/$projectName 
    mvn clean package
    echo "Project created successfully in $PWD/$projectName."
    cd ..
}

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
    cd ..
}

compile-p4(){
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
}

createonosmodule(){
    echo "Creating ONOS Module"
    #Find the directory of BaselineClass.java
    baselineClass=$(find $1 -type f -name "BaselineClass.java" | xargs dirname)
    echo "BaselineClass.java directory: $baselineClass"
    cp "$baselineClass/BaselineClass.java" "$baselineClass/$2.java"
    # Replace BaselineClass in the new java file $2.java
    sed -i "s#BaselineClass#$2#g" "$baselineClass/$2.java"    
    cd $1
    mvn clean package 
    echo "ONOS Module created successfully in $1."
    cd ..
}

create-onos-module(){
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
}

upload(){
    cd ..
    # Find BaselineClass.java directory
    baselineClass=$(find "$1" -type f -name "BaselineClass.java" -exec dirname {} \;)
    echo "BaselineClass.java directory: $baselineClass"

    # Extract directory path
    baselineClass=${baselineClass#*java/}
    echo "BaselineClass.java directory: $baselineClass"

    # Replace slashes with dots
    pipeconf=${baselineClass//\//.}
    echo "Pipeconf: $pipeconf"

    # Prepare artifact ID
    artifactId="${1}-app-1.0-SNAPSHOT"

    #Delete the app
    curl --fail -sSL --user onos:rocks --noproxy localhost -X DELETE "http://$2:8181/onos/v1/applications/$pipeconf" 

    echo "Deleted the app"

    # Upload the app
    curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H "Content-Type: application/octet-stream" \
        "http://$2:8181/onos/v1/applications?activate=true" \
        --data-binary "@$1/target/$artifactId.oar"
}

compile-onos-app(){
    # Get machine IP
    machineIP=$(hostname -I | awk '{print $1}')
    echo "Machine IP: $machineIP"

    # Prompt user for ONOS project directory name
    while true; do
        read -p "Please enter ONOS project directory name: " onosAppProjectName
        if [ -d "$onosAppProjectName" ]; then
            break
        else
            echo "Directory '$onosAppProjectName' does not exist. Please enter a valid directory."
        fi
    done
    echo "You entered: $onosAppProjectName"

    # Change directory to the ONOS project
    cd $onosAppProjectName

    # Build the project and display output
    echo "Building project..."
    # Run mvn clean package command
    output=$(mvn clean package)

    # Print the output
    echo "$output"

    # Check if the output contains the success message
    if grep -q "BUILD SUCCESS" <<< "$output"; then
        echo "Build successful!"
        upload $onosAppProjectName $machineIP
    else
        echo "Build failed."
    fi
}

upload-p4-config(){
    # Get machine IP
    machineIP=$(hostname -I | awk '{print $1}')
    echo "Machine IP: $machineIP"

    curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H 'Content-Type:application/json' \
                http://$machineIP:8181/onos/v1/network/configuration -d@./netcfg.json

}



echo "Welcome to the ONOS app generator script!"
echo "This script will help you create a new ONOS app project."

while true; do
    echo "Menu:"
    echo "1.- create-onos-app (Create a new ONOS app project)"
    echo "2.- compile-p4 (Compile P4 code)"
    echo "3.- create-onos-module (Create a new ONOS module)"
    echo "4.- compile-onos-app (Compile ONOS app)"
    echo "5.- upload-p4-config (Upload P4 config to ONOS)"
    echo "6.- exit (Exit the script)"
    echo "Please enter your choice:"
    read choice

    case "$choice" in
        *"1"* | "create-onos-app" | *"create"*"onos"*"app"* | *"onos"*"app"*"create"*| *"create"*"app"*"onos"*)
            echo "You chose to create a new ONOS app project."
            create-onos-app
            ;;
        *"2"* | *"compile-p4"* | *"compile"*"p4"* | *"p4"*"compile"* | *"p4"* )
            echo "You chose to compile P4 code."
            compile-p4
            ;;
        *"3"* | *"create-onos-module"* | *"create"*"onos"*"module"* | *"onos"*"module"*"create"* | *"create"*"module"*"onos"*)
            echo "You chose to create a new ONOS module."
            create-onos-module
            ;;
        *"4"* | *"compile-onos-app"* | *"compile"*"onos"*"app"* | *"onos"*"app"*"compile"* | *"compile"*"app"*"onos"*)
            echo "You chose to compile ONOS app."
            compile-onos-app
            ;;
        *"5"* | *"upload-p4-config"* | *"upload"*"p4"*"config"* | *"p4"*"config"*"upload"* | *"upload"*"config"*"p4"*)
            echo "You chose to upload P4 config to ONOS."
            upload-p4-config
            ;;
        *"6"* | *"exit"*)
            echo "Exiting the script."
            exit 0
            ;;
        *)
            echo "Invalid choice. Please try again."
            ;;  
    esac
done
