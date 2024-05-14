#!/bin/bash

# Get the output from ip a
ip_output=$(ip a)

#MACVTAP
# Search for lines containing "macvtap" and "brd" using awk
filtered_output_macvtap=$(echo "$ip_output" | awk '/macvtap/,/brd/')

# Initialize counter
counter=0

# Iterate over the filtered output
while read -r line; do
    if [[ $line =~ macvtap ]]; then
        # Extract interface name
        interface=$(echo "$line" | awk '{print $2}')
        # Increment counter
        ((counter++))
    elif [[ $line =~ "link/ether" ]]; then
        # Extract MAC address
        mac_address=$(echo "$line" | awk '{print $2}')
        # Create variables dynamically
        declare "interfacename$counter=$interface"
        declare "macaddress$counter=$mac_address"
    fi
done <<< "$filtered_output_macvtap"

# Print the variables
for ((i = 1; i <= counter; i++)); do
    interface_var="interfacename$i"
    mac_var="macaddress$i"
    echo "${!interface_var}: ${!mac_var}"
done

# Create arp.sh file
echo "#!/bin/bash" > arp.sh

# Create ARP entries for each interface
for ((i = 1; i <= counter; i++)); do
    interface_var="interfacename$i"
    mac_var="macaddress$i"
    # If interface contains veth1 then assign
    if [[ ${!interface_var} == *"veth1"* ]]; then
        echo "sudo arp -s 192.168.233.2 ${!mac_var}" >> arp.sh
    elif [[ ${!interface_var} == *"veth3"* ]]; then
        echo "sudo arp -s 192.168.233.3 ${!mac_var}" >> arp.sh
    elif [[ ${!interface_var} == *"veth5"* ]]; then
        echo "sudo arp -s 192.168.233.4 ${!mac_var}" >> arp.sh
    elif [[ ${!interface_var} == *"veth7"* ]]; then
        echo "sudo arp -s 192.168.233.5 ${!mac_var}" >> arp.sh
    elif [[ ${!interface_var} == *"veth9"* ]]; then
        echo "sudo arp -s 192.168.233.6 ${!mac_var}" >> arp.sh
    fi
done

#BR0 BRIDGE 
# Search for lines containing "br0" and "brd" using awk
filtered_output_br=$(echo "$ip_output" | awk '/br0/,/brd/')

# Extract interface name and MAC address for BR0
while read -r line; do
    if [[ $line =~ "br0" ]]; then
        # Extract interface name
        interface_br=$(echo "$line" | awk '{print $2}')
    elif [[ $line =~ "link/ether" ]]; then
        # Extract MAC address
        mac_address_br=$(echo "$line" | awk '{print $2}')
    fi
done <<< "$filtered_output_br"

# echo "BR0 interface: $interface_br"
echo "BR0 MAC address: $mac_address_br"

echo "sudo arp -s 192.168.233.1 $mac_address_br" >> arp.sh

echo "Number of MACVTAP interfaces found: $counter"


sudo scp arp.sh cp@192.168.123.73:~/
sudo scp arp.sh up@192.168.123.197:~/
sudo scp arp.sh ue@192.168.123.130:~/
sudo scp arp.sh gnb@192.168.123.116:~/
