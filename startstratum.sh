#!/bin/bash

#Get the Username
USERNAME=$(whoami)
echo "Username: $USERNAME"

#Ask the user for the p4 code directory
echo "Enter the p4 code directory"
read P4CODEDIR

#verify the p4 code directory exists and ask the user to enter the correct directory if it does not exist
while [ ! -d $P4CODEDIR ]
do
    echo "The directory does not exist. Please enter the correct directory"
    read P4CODEDIR
done

#sudo stratum_bmv2 -device_id=1 -chassis_config_file=/home/$USERNAME/chassis-config.txt -forwarding_pipeline_configs_file=/home/$USERNAME/pipe.txt -persistent_config_dir=/home/$USERNAME -initial_pipeline=/home/$USERNAME/$P4CODEDIR/$P4CODEDIR.json -cpu_port=255 -external_stratum_urls=0.0.0.0:50001 -local_stratum_url=localhost:44400 -max_num_controllers_per_node=10 -write_req_log_file=/home/$USERNAME/write-reqs.txt -logtosyslog=false - -bmv2_log_level=trace logtostderr=true 2>&1 | grep -v "StratumErrorSpace::ERR_UNIMPLEMENTED: DataRequest field loopback_status is not supported yet!"

sudo stratum_bmv2 -device_id=1 -chassis_config_file=/home/$USERNAME/chassis-config.txt -forwarding_pipeline_configs_file=/home/$USERNAME/pipe.txt -persistent_config_dir=/home/$USERNAME -initial_pipeline=/home/$USERNAME/$P4CODEDIR/$P4CODEDIR.json -cpu_port=255 -external_stratum_urls=0.0.0.0:50001 -local_stratum_url=localhost:44400 -max_num_controllers_per_node=10 -write_req_log_file=/home/$USERNAME/write-reqs.txt -logtosyslog=false logtostderr=true 2>&1 | grep -v "StratumErrorSpace::ERR_UNIMPLEMENTED: DataRequest field loopback_status is not supported yet!"
