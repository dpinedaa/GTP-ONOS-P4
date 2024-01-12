#!/bin/bash

# Check if the number of arguments is correct
if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <command> [additional parameters]"
    exit 1
fi

# Set a default value for netcfg_file
NETCFG_FILE="netcfg.json"

# Check the command provided by the user
case "$1" in
    "compile")
        # Check if the number of arguments for compile is correct
        if [ "$#" -ne 2 ]; then
            echo "Usage: $0 compile <p4_file_name>"
            exit 1
        fi

        P4_FILE="$2"

        # Run the p4c command with the provided P4 file
        p4c -b bmv2 --p4runtime-files "${P4_FILE}/${P4_FILE}.p4info.txt" "${P4_FILE}/${P4_FILE}.p4"

        # Check if the p4c command was successful
        if [ $? -eq 0 ]; then
            echo "Compilation successful."
        else
            echo "Compilation failed."
            exit 1
        fi

        # Copy additional files
        cp "${P4_FILE}/${P4_FILE}.json" app/src/main/resources
        cp "${P4_FILE}/${P4_FILE}.p4info.txt" app/src/main/resources

        # Check if the copy commands were successful
        if [ $? -eq 0 ]; then
            echo "Files copied successfully."
        else
            echo "File copying failed."
        fi
        ;;

    "run")
        # Check if the number of arguments for run is correct
        if [ "$#" -ne 2 ]; then
            echo "Usage: $0 run <p4_file_name>"
            exit 1
        fi

        P4_FILE="$2"

        # Prompt user for USERNAME
        read -p "Enter the USERNAME for sudo stratum_bmv2: " USERNAME

        # Execute the sudo stratum_bmv2 command
        sudo stratum_bmv2 -device_id=1 -chassis_config_file="/home/$USERNAME/chassis-config.txt" \
        -forwarding_pipeline_configs_file="/home/$USERNAME/pipe.txt" \
        -persistent_config_dir="/home/$USERNAME" \
        -initial_pipeline="/home/$USERNAME/${P4_FILE}/${P4_FILE}.json" \
        -cpu_port=255 -external_stratum_urls=0.0.0.0:50001 \
        -local_stratum_url=localhost:44400 \
        -max_num_controllers_per_node=10 \
        -write_req_log_file="/home/$USERNAME/write-reqs.txt" \
        -logtosyslog=false - -bmv2_log_level=trace logtostderr=true 2>&1 | grep -v "StratumErrorSpace::ERR_UNIMPLEMENTED: DataRequest field loopback_status is not supported yet!"
        ;;

    "upload")
        # Check if the number of arguments for upload is correct
        if [ "$#" -ne 4 ]; then
            echo "Usage: $0 upload <controller_ip> <artifact_id>"
            exit 1
        fi

        IP="$2"
        ArtifactID="$3"

        # Execute the curl command to upload the application
        curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H "Content-Type: application/octet-stream" \
             "http://$IP:8181/onos/v1/applications?activate=true" \
             --data-binary "@target/$ArtifactID.oar"

        # Check if the curl command was successful
        if [ $? -eq 0 ]; then
            echo "Upload successful."
        else
            echo "Upload failed."
            exit 1
        fi
        ;;

    "delete")
        # Check if the number of arguments for delete is correct
        if [ "$#" -ne 3 ]; then
            echo "Usage: $0 delete <controller_ip> <pipeconf>"
            exit 1
        fi

        IP="$2"
        Pipeconf="$3"

        # Execute the curl command to delete the application
        curl --fail -sSL --user onos:rocks --noproxy localhost -X DELETE "http://$IP:8181/onos/v1/applications/$Pipeconf"

        # Check if the curl command was successful
        if [ $? -eq 0 ]; then
            echo "Deletion successful."
        else
            echo "Deletion failed."
            exit 1
        fi
        ;;

    "config")
        # Check if the number of arguments for config is correct
        if [ "$#" -ne 2 ]; then
            echo "Usage: $0 config <controller_ip>"
            exit 1
        fi

        IP="$2"

        # Execute the curl command to upload the network configuration
        curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H 'Content-Type:application/json' \
             "http://$IP:8181/onos/v1/network/configuration" -d@"$NETCFG_FILE"

        # Check if the curl command was successful
        if [ $? -eq 0 ]; then
            echo "Configuration upload successful."
        else
            echo "Configuration upload failed."
            exit 1
        fi
        ;;

    "compile-onos")
        # Check if the number of arguments for compile-onos is correct
        if [ "$#" -ne 1 ]; then
            echo "Usage: $0 compile-onos"
            exit 1
        fi

        # Change to the app directory
        cd app || exit 1

        # Run mvn clean package
        mvn clean package

        # Check if mvn command was successful
        if [ $? -eq 0 ]; then
            echo "Compilation and packaging successful."
        else
            echo "Compilation and packaging failed."
            exit 1
        fi

        # Return to the original directory
        cd - || exit 1
        ;;

    *)
        echo "Usage: $0 <compile|run|upload|delete|config|compile-onos> [additional parameters]"
        exit 1
        ;;
esac
