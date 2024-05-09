#!/bin/bash

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
# #create boolean variable 
# buildSuccess=false
# # Check if build was successful
# if [ $? -eq 0 ]; then
#     echo "Build successful"
#     buildSuccess=true
    
# else
#     echo "Build failed"
#     exit 1
# fi

# cd ..

# #if it's true 
# if buildSuccess; then
#     upload "$onosAppProjectName"
#     echo "Upload successful"
#     exit 0
# else
#     echo "Build failed"
#     exit 1
# fi



