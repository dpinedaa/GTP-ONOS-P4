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


#Replace ONOSARTIFACTID in all files based on the directory name
find $PWD/$projectName -type f -exec sed -i "s#ONOSARTIFACTID#$artifactId#g" {} \;
find $PWD/$projectName -type f -exec sed -i "s#ONOSURL#$onos_app_url#g" {} \;
find $PWD/$projectName -type f -exec sed -i "s#ONOSTITLE#$projectTitle App#g" {} \;
find $PWD/$projectName -type f -exec sed -i "s#ONOSAPPNAME#$onos_app_name#g" {} \;
find $PWD/$projectName -type f -exec sed -i "s#ONOSP4DIR#$p4ProjectName#g" {} \;


#Copy the P4 project directory to the project directory
cp $PWD/$p4ProjectName/$p4ProjectName.p4info.txt $PWD/$projectName/src/main/resources/$p4ProjectName.p4info.txt
cp $PWD/$p4ProjectName/$p4ProjectName.json $PWD/$projectName/src/main/resources/$p4ProjectName.json

echo "Project created successfully in $PWD/$projectName."
