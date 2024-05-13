#!/bin/bash 

# Function to create a veth pair and set MAC addresses and MTU
create_veth_pair() {
  local veth_name1="$1"
  local veth_name2="$2"
  local mac_addr1="$3"
  local mac_addr2="$4"
  local mtu_value="$5"

  # Create veth pair
  sudo ip link add name "$veth_name1" type veth peer name "$veth_name2"

  # Set MAC addresses
  sudo ip link set dev "$veth_name1" address "$mac_addr1"
  sudo ip link set dev "$veth_name2" address "$mac_addr2"

  # Bring up veth interfaces
  sudo ip link set dev "$veth_name1" up
  sudo ip link set dev "$veth_name2" up

  # Set MTU value
  sudo ip link set "$veth_name1" mtu "$mtu_value"
  sudo ip link set "$veth_name2" mtu "$mtu_value"
}

# Create veth pairs
create_veth_pair "veth0" "veth1" "00:1A:2B:3C:4D:00" "00:1A:2B:3C:4D:01" 8500
create_veth_pair "veth2" "veth3" "00:1A:2B:3C:4D:02" "00:1A:2B:3C:4D:03" 8500
create_veth_pair "veth4" "veth5" "00:1A:2B:3C:4D:04" "00:1A:2B:3C:4D:05" 8500
create_veth_pair "veth6" "veth7" "00:1A:2B:3C:4D:06" "00:1A:2B:3C:4D:07" 8500
create_veth_pair "veth8" "veth9" "00:1A:2B:3C:4D:08" "00:1A:2B:3C:4D:09" 8500
create_veth_pair "veth10" "veth11" "00:1A:2B:3C:4D:10" "00:1A:2B:3C:4D:11" 8500


sudo brctl addbr br0 
sudo ip link set dev br0 address 00:1A:2B:3C:4D:12
sudo ip link set dev br0 up
sudo ip link set dev veth11 master br0
sudo ip addr add 192.168.233.1/24 dev br0
sudo sysctl net.ipv4.ip_forward=1
#sudo iptables -t nat -A POSTROUTING -s 192.168.233.0/24 -o wlp114s0 -j MASQUERADE
sudo iptables -t nat -A POSTROUTING -s 192.168.233.0/24 -o ens160 -j MASQUERADE

# Disable RX checksumming
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" rx off
done

# Disable TX checksumming
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" tx off
done

# Disable scatter gather
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" sg off
done

# Disable TCP segmentation offload
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" tso off
done

# Disable UDP fragmentation offload
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" ufo off
done

# Disable generic segmentation offload
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" gso off
done

# Disable generic receive offload
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" gro off
done

# Disable large receive offload
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" lro off
done

# Disable RX VLAN acceleration
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" rxvlan off
done

# Disable TX VLAN acceleration
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" txvlan off
done

# Disable RX ntuple filters and actions
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" ntuple off
done

# Disable RX hashing offload
for intf in {0..11}; do
    sudo ethtool -K "veth$intf" rxhash off
done

# Disable Energy Efficient Ethernet
for intf in {0..11}; do
    sudo ethtool --set-eee "veth$intf" eee off
done




