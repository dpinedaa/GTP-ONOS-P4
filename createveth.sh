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
}

# Create veth pairs
create_veth_pair "veth0" "veth1" 9500
create_veth_pair "veth2" "veth3" 9000
create_veth_pair "veth4" "veth5" 8000
create_veth_pair "veth6" "veth7" 7000
create_veth_pair "veth8" "veth9" 6000
create_veth_pair "veth10" "veth11" 5500
