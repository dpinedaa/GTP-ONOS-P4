#!/bin/bash
ue_number=$1
ue_running=false
ue_failed=false

while [[ $ue_running == false ]]; do
    # Run the process in the background and save its PID
    sudo ./UERANSIM/build/nr-ue -c UERANSIM/config/open5gs-ue${ue_number}.yaml > ue-track/output${ue_number}.txt 2>&1 &
    echo $! > pid.txt
    sleep 5

    # Save the related processes to ue-track/processes${ue_number}.txt
    ps aux | grep open5gs-ue${ue_number} > ue-track/processes${ue_number}.txt

    # Search for the specified string in ue-track/output${ue_number}.txt
    if grep -q "Connection setup for PDU session\\[1\\] is successful, TUN interface\\[uesimtun" ue-track/output${ue_number}.txt; then
        echo "UE ${ue_number} created successfully"
        ue_failed=false
        while [[ $ue_failed == false ]]; do
             if grep -q "\\[error\\]" ue-track/output${ue_number}.txt; then
                echo "Error detected, killing processes..."

                while IFS= read -r line; do
                    #echo "$line"
                    kill -9 $(echo "$line" | awk '{print $2}') 2>/dev/null
                done < ue-track/processes${ue_number}.txt
                ue_failed=true
                ue_running=false
                echo "Processes killed"
                sleep 2
                echo "Restarting UE ${ue_number}..."
             fi
             sleep 1
        done
    else
        echo "UE ${ue_number} failed"
        echo "Killing processes..."

        while IFS= read -r line; do
            #echo "$line"
            kill -9 $(echo "$line" | awk '{print $2}') 2>/dev/null
        done < ue-track/processes${ue_number}.txt
        echo "Processes killed"
    fi    
done
