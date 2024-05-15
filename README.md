# P4-ONOS-GTP

## Bazel

```bash
sudo apt install apt-transport-https curl gnupg ethtool -y
```

```bash
curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor >bazel-archive-keyring.gpg
```

```bash
sudo mv bazel-archive-keyring.gpg /usr/share/keyrings
```

```bash
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list
```

```bash
sudo apt install maven -y 
```

```bash
sudo apt update
```

```bash
sudo apt install bazel -y
```


```bash
sudo apt install bazel-3.7.2 -y
```



## INSTALL PROTOBUF

```bash
sudo apt-get install g++ git bazel
```

```bash
cd ~/GTP-ONOS-P4
cp protoc-3.20.3-linux-x86_64.zip ~/
cd 
unzip protoc-3.20.3-linux-x86_64.zip
sudo mv bin/protoc /usr/local/bin/
echo $PATH
export PATH="/usr/local/bin:$PATH"
```

* Verify protobuf version 

```bash
protoc --version
```

```output
libprotoc 3.20.3
```



```bash
cd protobuf
```

https://github.com/protocolbuffers/protobuf/archive/refs/tags/v3.24.0-3688-ge912bc2e3.zip

```bash
git checkout tags/v3.24.0-3688-ge912bc2e3
```



```bash
 git clone --branch v3.24.0 --single-branch https://github.com/protocolbuffers/protobuf.git
```


```bash
git submodule update --init --recursive
```

```bash
bazel build :protoc :protobuf
```

```bash
sudo cp bazel-bin/protoc /usr/local/bin
```

```bash
sudo apt install maven -y 
```

```bash
cd
```


## Node js


```bash
sudo apt-get install -y ca-certificates curl gnupg
```


```bash
sudo mkdir -p /etc/apt/keyrings
```

```bash
curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | sudo gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
```

```bash
NODE_MAJOR=20
```

```bash
echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" | sudo tee /etc/apt/sources.list.d/nodesource.list
```

```bash
sudo apt-get update
```

```bash
sudo apt-get install nodejs -y
```

## ONOS

```bash
sudo apt install openjdk-11-jdk -y
```

```bash
echo "JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64" | sudo tee -a /etc/environment
```

```bash
sudo apt install python3
sudo apt install python3-dev -y
```

```bash
sudo ln -s /usr/bin/python3 /usr/bin/python
```

```bash
wget https://github.com/opennetworkinglab/onos/archive/refs/tags/2.7.0.zip --no-check-certificate
```

```bash
unzip 2.7.0
```

```bash
mv onos-2.7.0 onos
cd onos
```

```bash
touch .bazelrc
```

```bash
nano .bazelrc
```

```bash
build --host_force_python=PY3
```

```bash
bazel build onos
```

**TEST**

```bash
bazel run onos-local -- [clean] [debug] | grep -iv "Unable to translate flow rule for pipeconf" | tee -a onos.log
```







## P4-STRATUM 

### DOCKER


```bash
sudo apt-get install ca-certificates curl gnupg
```

```bash
sudo install -m 0755 -d /etc/apt/keyrings
```

```bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
```

```bash
sudo chmod a+r /etc/apt/keyrings/docker.gpg
```

```bash
echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

```bash
sudo apt-get update
```

```bash
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y
```

```bash
sudo usermod -aG docker $USER
```

```bash
sudo reboot
```

### STRATUM_BMV2


```bash
sudo apt install git -y
```


```bash
git clone https://github.com/stratum/stratum.git
```

```bash
cd stratum
```

```bash
bash setup_dev_env.sh 
```

```bash
bazel build //stratum/hal/bin/bmv2:stratum_bmv2_deb
```

```bash
cp -f /stratum/bazel-bin/stratum/hal/bin/bmv2/stratum_bmv2_deb.deb /stratum/
```

**Exit the docker container**

```bash
sudo apt update
```

```bash
sudo apt-get install -y --reinstall ./stratum_bmv2_deb.deb
```

**TO TEST**

```bash
stratum_bmv2 \
    -chassis_config_file=/etc/stratum/chassis_config.pb.txt \
    -bmv2_log_level=debug
```

### P4

```bash
sudo apt install curl -y 
```

```bash
source /etc/lsb-release
```

```bash
echo "deb http://download.opensuse.org/repositories/home:/p4lang/xUbuntu_${DISTRIB_RELEASE}/ /" | sudo tee /etc/apt/sources.list.d/home:p4lang.list
```

```bash
curl -fsSL https://download.opensuse.org/repositories/home:p4lang/xUbuntu_${DISTRIB_RELEASE}/Release.key | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/home_p4lang.gpg > /dev/null
```

```bash
sudo apt-get update
```

```bash
sudo apt install p4lang-p4c -y
```




### Linux Bridge 

```bash
sudo apt update
sudo apt-get install bridge-utils -y
```

```bash
sudo brctl addbr br0
```

```bash
sudo brctl addif br0 veth11
```

```bash
sudo ip addr add 192.168.233.1/24 dev br0
sudo ip link set dev br0 up
```


```bash 
sudo sysctl -w net.ipv4.ip_forward=1
sudo iptables -t nat -A POSTROUTING -s 192.168.233.0/24 ! -o enp1s0 -j MASQUERADE
```































































## Setup VMs 

### Install Virt-manager 

```bash
sudo apt install virt-manager -y 
```

### Install Linux bridge 

```bash
sudo apt install bridge-utils -y 
```

## Download the Ubuntu VM 

```bash
wget https://releases.ubuntu.com/focal/ubuntu-20.04.6-desktop-amd64.iso
```


### Create the Veth pairs 

```bash
sudo bash createveth.sh
```




### Create a VM 

```bash
sudo virt-manager 
```

#### VM details 
**CPU:** 2
**RAM:** 4GB
**Disk:** 40 GiB





## Open5GS - CP 

### Getting MongoDB

* Import the public key used by the package management system.

```bash
sudo apt update
sudo apt install wget gnupg -y
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -
```

* Create the list file /etc/apt/sources.list.d/mongodb-org-6.0.list for your version of Ubuntu.

```bash
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list

```

* Install the MongoDB packages.

```bash
sudo apt update 
sudo apt install -y mongodb-org
sudo systemctl start mongod
sudo systemctl enable mongod
```


### Getting Open5GS as a source 


* Install the dependencies for building the source code.

```bash
sudo apt install python3-pip python3-setuptools python3-wheel ninja-build build-essential flex bison git cmake libsctp-dev libgnutls28-dev libgcrypt-dev libssl-dev libidn11-dev libmongoc-dev libbson-dev libyaml-dev wget libnghttp2-dev libmicrohttpd-dev libcurl4-gnutls-dev libnghttp2-dev libtins-dev libtalloc-dev meson -y
```

* Git clone 

```bash
wget https://github.com/open5gs/open5gs/archive/refs/tags/v2.6.6.zip --no-check-certificate
```

* Unzip the repo 


```bash
unzip v2.6.6.zip
mv open5gs-2.6.6/ open5gs
cd open5gs 
```

* To compile with meson:

```bash
cd open5gs
meson build --prefix=`pwd`/install
ninja -C build
```

* perform the installation process.

```bash
cd build
ninja install
cd ../
```

* Modify the config files for AMF and SMF. This is only for the control Plane

```bash
sudo nano install/etc/open5gs/amf.yaml
```

```diff
amf:
    sbi:
      - addr: 127.0.0.5
        port: 7777
    ngap:
-      - addr: 127.0.0.5
+      - addr: 192.168.233.2
    metrics:
      - addr: 127.0.0.5
        port: 9090
    guami:
      - plmn_id:
-          mcc: 999
-          mnc: 70
+          mcc: 001
+          mnc: 01
        amf_id:
          region: 2
          set: 1
    tai:
      - plmn_id:
-          mcc: 999
-          mnc: 70
+          mcc: 001
+          mnc: 01
+        tac: 1
    plmn_support:
      - plmn_id:
-          mcc: 999
-          mnc: 70
+          mcc: 001
+          mnc: 01
        s_nssai:
          - sst: 1
    security:
        integrity_order : [ NIA2, NIA1, NIA0 ]
        ciphering_order : [ NEA0, NEA1, NEA2 ]
    network_name:
        full: Open5GS
    amf_name: open5gs-amf0
```


```bash
sudo nano install/etc/open5gs/smf.yaml
```


```diff
smf:
    sbi:
      - addr: 127.0.0.4
        port: 7777
    pfcp:
-      - addr: 127.0.0.4
-      - addr: ::1
+      - addr: 192.168.233.3
+      #- addr: ::1
    gtpc:
      - addr: 127.0.0.4
-      - addr: ::1
+      #- addr: ::1
    gtpu:
      - addr: 127.0.0.4
-      - addr: ::1
+      #- addr: ::1
    metrics:
      - addr: 127.0.0.4
        port: 9090
    subnet:
      - addr: 10.45.0.1/16
      - addr: 2001:db8:cafe::1/48
    dns:
      - 8.8.8.8
      - 8.8.4.4
      - 2001:4860:4860::8888
      - 2001:4860:4860::8844
    mtu: 1400
    ctf:
      enabled: auto
    freeDiameter: /home/cp/open5gs/install/etc/freeDiameter/smf.conf




upf:
    pfcp:
-      - addr: 127.0.0.7
+      - addr: 192.168.233.4

```





## Building the WebUI of Open5GS

The WebUI allows you to interactively edit subscriber data. While it is not essential to use this, it makes things easier when you are just starting out on your Open5GS adventure. (A command line tool is available for advanced users).


Node.js is required to build WebUI of Open5GS

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | sudo gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg

NODE_MAJOR=20
echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" | sudo tee /etc/apt/sources.list.d/nodesource.list


sudo apt update
sudo apt install nodejs -y

```

* Install the dependencies to run WebUI

```bash
cd webui
npm ci
```

* The WebUI runs as an npm script.

```bash
npm run dev &
```

## Register Subscriber Information

Connect to http://127.0.0.1:3000 and login with admin account.

Username : admin
Password : 1423

You can change the password after login 

<p align="center">
  <img src="figures/login.png" alt="Image description">
</p>



<p align="center">
  <img src="figures/home.png" alt="Image description">
</p>


<p align="center">
  <img src="figures/createsubscriber.png" alt="Image description">
</p>

<p align="center">
  <img src="figures/createsubscriber2.png" alt="Image description">
</p>


<p align="center">
  <img src="figures/subscriberlist.png" alt="Image description">
</p>







## Add subscriber using CLI 

```bash
mongosh 
show dbs
use open5gs
```

Make sure to specify the wanted parameters. 
We wanted 
IMSI 001010000000001
UE IP: 10.45.0.3


```bash
db.subscribers.insertOne({
  imsi: '001010000000001',
  msisdn: [],
  imeisv: '4301816125816151',
  mme_host: [],
  mme_realm: [],
  purge_flag: [],
  security: {
    k: '465B5CE8 B199B49F AA5F0A2E E238A6BC',
    op: null,
    opc: 'E8ED289D EBA952E4 283B54E8 8E6183CA',
    amf: '8000',
    sqn: NumberLong("513")
  },
  ambr: { downlink: { value: 1, unit: 3 }, uplink: { value: 1, unit: 3 } },
  slice: [
    {
      sst: 1,
      default_indicator: true,
      session: [
        {
          name: 'internet',
          type: 3,
          qos: { index: 9, arp: { priority_level: 8, pre_emption_capability: 1, pre_emption_vulnerability: 1 } },
          ambr: { downlink: { value: 1, unit: 3 }, uplink: { value: 1, unit: 3 } },
          ue: { addr: '10.45.0.3' },
          _id: ObjectId("6473fd45a07e473e0b5334ce"),
          pcc_rule: []
        }
      ],
      _id: ObjectId("6473fd45a07e473e0b5334cd")
    }
  ],
  access_restriction_data: 32,
  subscriber_status: 0,
  network_access_mode: 0,
  subscribed_rau_tau_timer: 12,
  __v: 0
})
```

```bash
db.subscribers.insertOne({
  imsi: '001010000000002',
  msisdn: [],
  imeisv: '4301816125816151',
  mme_host: [],
  mme_realm: [],
  purge_flag: [],
  security: {
    k: '465B5CE8 B199B49F AA5F0A2E E238A6BC',
    op: null,
    opc: 'E8ED289D EBA952E4 283B54E8 8E6183CA',
    amf: '8000',
    sqn: NumberLong("513")
  },
  ambr: { downlink: { value: 1, unit: 3 }, uplink: { value: 1, unit: 3 } },
  slice: [
    {
      sst: 1,
      default_indicator: true,
      session: [
        {
          name: 'internet',
          type: 3,
          qos: { index: 9, arp: { priority_level: 8, pre_emption_capability: 1, pre_emption_vulnerability: 1 } },
          ambr: { downlink: { value: 1, unit: 3 }, uplink: { value: 1, unit: 3 } },
          ue: { addr: '10.45.0.4' },
          _id: ObjectId("6473fd45a07e473e0b5334ce"),
          pcc_rule: []
        }
      ],
      _id: ObjectId("6473fd45a07e473e0b5334cd")
    }
  ],
  access_restriction_data: 32,
  subscriber_status: 0,
  network_access_mode: 0,
  subscribed_rau_tau_timer: 12,
  __v: 0
})

```





```bash
cd ~/open5gs
```




## Running Open5GS

```bash
./install/bin/open5gs-nrfd &
./install/bin/open5gs-scpd &
./install/bin/open5gs-amfd &
./install/bin/open5gs-smfd &
./install/bin/open5gs-ausfd &
./install/bin/open5gs-udmd &
./install/bin/open5gs-pcfd &
./install/bin/open5gs-nssfd &
./install/bin/open5gs-bsfd &
./install/bin/open5gs-udrd &
```































# Open5GS - UP 



```bash
sudo apt update && sudo apt upgrade -y 
sudo apt install openssh-server -y 
```

**Remember to make a snapshot**



### Getting Open5GS as a source 


* Install the dependencies for building the source code.

```bash
sudo apt install python3-pip python3-setuptools python3-wheel ninja-build build-essential flex bison git cmake libsctp-dev libgnutls28-dev libgcrypt-dev libssl-dev libidn11-dev libmongoc-dev libbson-dev libyaml-dev wget libnghttp2-dev libmicrohttpd-dev libcurl4-gnutls-dev libnghttp2-dev libtins-dev libtalloc-dev meson -y
```

* Git clone 

```bash
wget https://github.com/open5gs/open5gs/archive/refs/tags/v2.6.6.zip --no-check-certificate
```

* Unzip the repo 


```bash
unzip v2.6.6.zip
mv open5gs-2.6.6/ open5gs
cd open5gs 
```

* To compile with meson:

```bash
cd open5gs
meson build --prefix=`pwd`/install
ninja -C build
```

* perform the installation process.

```bash
cd build
ninja install
cd ../
```


* Modify the config files for UPF. This is only for the User Plane

```bash
nano install/etc/open5gs/upf.yaml
```

```diff
upf:
    pfcp:
-      - addr: 127.0.0.7
+      - addr: 192.168.233.4
    gtpu:
-      - addr: 127.0.0.7
+      - addr: 192.168.233.4
    subnet:
      - addr: 10.45.0.1/16
      - addr: 2001:db8:cafe::1/48
    metrics:
      - addr: 127.0.0.7
        port: 9090
```


## Running Open5GS

```bash
cd ~/open5gs
./install/bin/open5gs-upfd &
```

* Allow UE network traffic to access the internet. 

```bash
sudo sysctl -w net.ipv4.ip_forward=1
sudo iptables -t nat -A POSTROUTING -s 10.45.0.0/16 ! -o ogstun -j MASQUERADE
```




















































































# UERANSIM - gNB

## Getting the UERANSIM

```bash
sudo apt update && sudo apt upgrade -y 
sudo apt install openssh-server -y 
sudo apt install git -y
```

* Clone repo 

```bash 
cd ~
wget https://github.com/aligungr/UERANSIM/archive/refs/heads/master.zip  --no-check-certificate
unzip master.zip
mv UERANSIM-master/ UERANSIM
cd UERANSIM
```

* Install the required dependencies 

```bash 
sudo apt install make gcc g++ libsctp-dev lksctp-tools iproute2 build-essential -y
sudo snap install cmake --classic
```

## Build UERANSIM

```bash 
cd ~/UERANSIM
make
```

## gNB Configuration

```bash 
cd config
sudo cp open5gs-gnb.yaml open5gs-gnb1.yaml
sudo nano open5gs-gnb1.yaml
```

```diff
-mcc: '999'          # Mobile Country Code value
+mcc: '001'          # Mobile Country Code value

-mnc: '70'           # Mobile Network Code value (2 or 3 digits)
+mnc: '01'           # Mobile Network Code value (2 or 3 digits)

nci: '0x000000010'  # NR Cell Identity (36-bit)
idLength: 32        # NR gNB ID length in bits [22...32]
+tac: 1              # Tracking Area Code

-linkIp: 127.0.0.1   # gNB's local IP address for Radio Link Simulation (Usually same with local IP)
-ngapIp: 127.0.0.1   # gNB's local IP address for N2 Interface (Usually same with local IP)
-gtpIp: 127.0.0.1    # gNB's local IP address for N3 Interface (Usually same with local IP)

+linkIp: 192.168.233.5   # gNB's local IP address for Radio Link Simulation (Usually same with local IP)
+ngapIp: 192.168.233.5   # gNB's local IP address for N2 Interface (Usually same with local IP)
+gtpIp: 192.168.233.5    # gNB's local IP address for N3 Interface (Usually same with local IP)


# List of AMF address information
amfConfigs:
-  - address: 127.0.0.5
+  - address: 192.168.233.2
    port: 38412

# List of supported S-NSSAIs by this gNB
slices:
  - sst: 1

# Indicates whether or not SCTP stream number errors should be ignored.
ignoreStreamIds: true```


## Start using the gNB - UERANSIM 

After completing configurations and setups, now you can start using UERANSIM.
```

Run the following command to start the gNB:

```bash 
cd ..
./build/nr-gnb -c config/open5gs-gnb1.yaml
```















































# UERANSIM - UE

## Getting the UERANSIM

```bash
sudo apt update and sudo apt upgrade -y 
sudo apt install openssh-server -y 
sudo apt install git -y
```


* Clone repo 

```bash 
cd ~
wget https://github.com/aligungr/UERANSIM/archive/refs/heads/master.zip  --no-check-certificate
unzip master.zip
mv UERANSIM-master/ UERANSIM
cd UERANSIM
```

* Install the required dependencies 

```bash 
sudo apt install make gcc g++ libsctp-dev lksctp-tools iproute2 build-essential -y
sudo snap install cmake --classic
```

## Build UERANSIM

```bash 
cd ~/UERANSIM
make
```

## UE Configuration

```bash 
cd config
sudo cp open5gs-ue.yaml open5gs-ue1.yaml
sudo nano open5gs-ue1.yaml
```

```diff
# IMSI number of the UE. IMSI = [MCC|MNC|MSISDN] (In total 15 digits)
-supi: 'imsi-999700000000001'
+supi: 'imsi-001010000000001'
# Mobile Country Code value of HPLMN
-mcc: '999'
+mcc: '001'
# Mobile Network Code value of HPLMN (2 or 3 digits)
-mnc: '70'
+mnc: '01'

# Permanent subscription key
key: '465B5CE8B199B49FAA5F0A2EE238A6BC'
# Operator code (OP or OPC) of the UE
op: 'E8ED289DEBA952E4283B54E88E6183CA'
# This value specifies the OP type and it can be either 'OP' or 'OPC'
opType: 'OPC'
# Authentication Management Field (AMF) value
amf: '8000'
# IMEI number of the device. It is used if no SUPI is provided
imei: '356938035643803'
# IMEISV number of the device. It is used if no SUPI and IMEI is provided
imeiSv: '4370816125816151'

# List of gNB IP addresses for Radio Link Simulation
gnbSearchList:
-  - 127.0.0.1
+  - 192.168.233.5

# UAC Access Identities Configuration
uacAic:
  mps: false
  mcs: false

# UAC Access Control Class
uacAcc:
  normalClass: 0
  class11: false
  class12: false
  class13: false
  class14: false
  class15: false

# Initial PDU sessions to be established
sessions:
  - type: 'IPv4'
    apn: 'internet'
    slice:
      sst: 1

# Configured NSSAI for this UE by HPLMN
configured-nssai:
  - sst: 1

# Default Configured NSSAI for this UE
default-nssai:
  - sst: 1
    sd: 1

# Supported integrity algorithms by this UE
integrity:
  IA1: true
  IA2: true
  IA3: true

# Supported encryption algorithms by this UE
ciphering:
  EA1: true
  EA2: true
  EA3: true

# Integrity protection maximum data rate for user plane
integrityMaxRate:
  uplink: 'full'
  downlink: 'full'

```


## Start using the UE - UERANSIM 

After completing configurations and setups, now you can start using UERANSIM.

Run the following command to start the UE:

```bash 
cd ..
sudo ./build/nr-ue -c config/open5gs-ue1.yaml
```



























































## GTP-P4-ONOS 

```bash
touch write-reqs.txt
```

* Clone the repo  

```bash
git clone https://github.com/dpinedaa/GTP-ONOS-P4.git
cd GTP-ONOS-P4
```

```bash
cp -r baseline/  ~/
cp chassis-config.txt ~/
cp createveth.sh ~/
cp generatepipe.py ~/
cp -r gtp-p4/ ~/
cp libbmpi.so.0.0.0 ~/
cp libsimpleswitch_runner.so.0.0.0 ~/
cp netcfg.json ~/
cp onos-app.sh ~/
cp generate.sh ~/
cp macaddress.sh ~/
```

```bash
sudo bash createveth.sh
```

```bash
sudo cp libsimpleswitch_runner.so.0.0.0 /lib/x86_64-linux-gnu/
sudo cp libbmpi.so.0.0.0 /lib/x86_64-linux-gnu/
```

```bash
cd
```

```bash
cd gtp-p4
```

```bash
p4c -b bmv2 --p4runtime-files gtp-p4.p4info.txt gtp-p4.p4
```

```bash
cd 
```

```bash
sudo python3 generatepipe.py gtp-p4/gtp-p4.json gtp-p4/gtp-p4.p4info.txt
```

## Run the Stratum switch 

```bash
sudo stratum_bmv2 -device_id=1 -chassis_config_file=/home/$USERNAME/chassis-config.txt -forwarding_pipeline_configs_file=/home/$USERNAME/pipe.txt -persistent_config_dir=/home/$USERNAME -initial_pipeline=/home/$USERNAME/gtp-p4/gtp-p4.json -cpu_port=255 -external_stratum_urls=0.0.0.0:50001 -local_stratum_url=localhost:44400 -max_num_controllers_per_node=10 -write_req_log_file=/home/$USERNAME/write-reqs.txt -logtosyslog=false - -bmv2_log_level=trace logtostderr=true 2>&1 | grep -v "StratumErrorSpace::ERR_UNIMPLEMENTED: DataRequest field loopback_status is not supported yet!"

```

```bash
cd
cp gtp-p4/gtp-p4.json app/src/main/resources
cp gtp-p4/gtp-p4.p4info.txt app/src/main/resources
```

```bash
cd
cd app
mvn clean package 
```


**Config ONOS**
```bash
ssh -o "UserKnownHostsFile=/dev/null" -o "StrictHostKeyChecking=no" -o "HostKeyAlgorithms=+ssh-rsa" -o LogLevel=ERROR -p 8101 onos@localhost
```

```bash
app activate org.onosproject.drivers.stratum
app activate org.onosproject.drivers.bmv2
app activate org.onosproject.hostprovider
app activate org.onosproject.netconf
```

```bash
IP=10.102.211.11
ArtifactID=p4-gtp-app-1.0-SNAPSHOT

curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H "Content-Type: application/octet-stream" \
     "http://$IP:8181/onos/v1/applications?activate=true" \
     --data-binary "@target/$ArtifactID.oar"
```

**DELETE APP**
```bash
IP=10.102.211.11
Pipeconf=edu.fiu.adwise.p4_gtp
curl --fail -sSL --user onos:rocks --noproxy localhost -X DELETE "http://$IP:8181/onos/v1/applications/$Pipeconf"
```

```bash
IP=10.102.211.11
curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H 'Content-Type:application/json' \
                http://$IP:8181/onos/v1/network/configuration -d@./netcfg.json
```

```bash

```

```bash

```

```bash

```

```bash

```



```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```




```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```


```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```


```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```


```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```



```bash

```

```bash

```

```bash

```

```bash

```

```bash

```

```bash

```





































## COMPILE GTP-P4 program 

```bash
git clone https://github.com/dpinedaa/GTP-ONOS-P4.git
```

```bash
cd GTP-ONOS-P4
```

```bash
mv gtp-p4 ~/
mv generatepipe.py ~/
mv app ~/
mv chassis-config.txt ~/
mv createveth.sh ~/
mv netcfg.json ~/
mv libbmpi.so.0.0.0 ~/
mv libsimpleswitch_runner.so.0.0.0 ~/
```