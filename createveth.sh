#!/bin/bash

# Function to create a veth pair and set MTU
create_veth_pair() {
  local veth_name1="$1"
  local veth_name2="$2"
  local mtu_value="$3"

  # Create veth pair
  sudo ip link add name "$veth_name1" type veth peer name "$veth_name2"

  # Bring up veth interfaces
  sudo ip link set dev "$veth_name1" up
  sudo ip link set dev "$veth_name2" up

  # Set MTU value
  sudo ip link set "$veth_name1" mtu "$mtu_value"
  sudo ip link set "$veth_name2" mtu "$mtu_value"
  
  # Configure ethtool settings for both interfaces in the pair
  ethtool -K "$veth_name1" rx off		# RX checksumming
  ethtool -K "$veth_name1" tx off		# TX checksumming
  ethtool -K "$veth_name1" sg off		# scatter gather
  ethtool -K "$veth_name1" tso off		# TCP segmentation offload
  ethtool -K "$veth_name1" ufo off		# UDP fragmentation offload
  ethtool -K "$veth_name1" gso off		# generic segmentation offload
  ethtool -K "$veth_name1" gro off		# generic receive offload
  ethtool -K "$veth_name1" lro off		# large receive offload
  ethtool -K "$veth_name1" rxvlan off	# RX Vlan acceleration
  ethtool -K "$veth_name1" txvlan off	# TX vlan acceleration
  ethtool -K "$veth_name1" ntuple off	# RX ntuple filters and actions
  ethtool -K "$veth_name1" rxhash off	# RX hashing offload
  ethtool --set-eee "$veth_name1" eee off	# Energy Efficient Ethernet

  ethtool -K "$veth_name2" rx off		# RX checksumming
  ethtool -K "$veth_name2" tx off		# TX checksumming
  ethtool -K "$veth_name2" sg off		# scatter gather
  ethtool -K "$veth_name2" tso off		# TCP segmentation offload
  ethtool -K "$veth_name2" ufo off		# UDP fragmentation offload
  ethtool -K "$veth_name2" gso off		# generic segmentation offload
  ethtool -K "$veth_name2" gro off		# generic receive offload
  ethtool -K "$veth_name2" lro off		# large receive offload
  ethtool -K "$veth_name2" rxvlan off	# RX Vlan acceleration
  ethtool -K "$veth_name2" txvlan off	# TX vlan acceleration
  ethtool -K "$veth_name2" ntuple off	# RX ntuple filters and actions
  ethtool -K "$veth_name2" rxhash off	# RX hashing offload
  ethtool --set-eee "$veth_name2" eee off	# Energy Efficient Ethernet
}

# Create veth pairs
create_veth_pair "veth0" "veth1" 8500
create_veth_pair "veth2" "veth3" 8500
create_veth_pair "veth4" "veth5" 8500
create_veth_pair "veth6" "veth7" 8500
create_veth_pair "veth8" "veth9" 8500
create_veth_pair "veth10" "veth11" 8500
