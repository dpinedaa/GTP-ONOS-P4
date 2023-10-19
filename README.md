# GTP-ONOS-P4
 
## Install BAZEL

```bash
sudo apt upgrade -y
```

```bash
sudo apt install apt-transport-https curl gnupg -y
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
sudo apt update && sudo apt install bazel -y
```

## INSTALL PROTOBUF

```bash
sudo apt-get install g++ git bazel
```

```bash
git clone https://github.com/protocolbuffers/protobuf/
```

```bash
cd protobuf
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

## INSTALL NODE JS

```bash
sudo apt-get update
```

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

```bash
git clone https://github.com/opennetworkinglab/onos.git
```

```bash
cd onos
```

```bash
git checkout tags/2.7.0 -b onos-2.7.0
```

```bash
sudo apt update && sudo apt install bazel-3.7.2 -y
```

```bash
sudo apt update
```

```bash
sudo apt install python3
```

```bash
sudo apt install python3-dev -y
```

```bash
sudo ln -s /usr/bin/python3 /usr/bin/python
```

```bash
USERNAME=vm
```


```bash
sudo chmod -R u+rwx /home/$USERNAME/onos
```

```bash
sudo chown -R $USERNAME:$USERNAME /home/$USERNAME/onos/tools/gui
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

**Run ONOS**

```bash
bazel run onos-local -- [clean] [debug] | grep -iv "Unable to translate flow rule for pipeconf" | tee -a onos.log
```

## INSTALL DOCKER


```bash
sudo apt-get update
```

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
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

```bash
sudo usermod -aG docker $USER
```

```bash
sudo reboot
```

## INSTALL STRATUM_BMV2

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

```bash
sudo apt update
```

```bash
sudo apt-get install -y --reinstall ./stratum_bmv2_deb.deb
```

```bash
stratum_bmv2 \
    -chassis_config_file=/etc/stratum/chassis_config.pb.txt \
    -bmv2_log_level=debug

```


```bash
touch write-reqs.txt
```


## INSTALL P4 

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


## COMPILE GTP-P4 program 

```bash
cd GTP-ONOS-P4
```

```bash
mv -r gtp-stratum ~/
mv generatepipe.py ~/
mv app ~/
mv chassis-config.txt ~/
mv createveth.sh ~/
mv netcfg.json ~/
```

```bash
cd
```

```bash
cd gtp-stratum
```

```bash
p4c -b bmv2 --p4runtime-files gtp-stratum.p4info.txt gtp-stratum.p4
```

```bash
cd 
```

```bash
sudo python3 generatepipe.py gtp-stratum/gtp-stratum.json gtp-stratum/gtp-stratum.p4info.txt
```

## Run the Stratum switch 

```bash
sudo stratum_bmv2 -device_id=1 -chassis_config_file=/home/$USERNAME/chassis-config.txt -forwarding_pipeline_configs_file=/home/$USERNAME/pipe.txt -persistent_config_dir=/home/$USERNAME -initial_pipeline=/home/$USERNAME/gtp-stratum/gtp-stratum.json -cpu_port=255 -external_stratum_urls=0.0.0.0:50001 -local_stratum_url=localhost:44400 -max_num_controllers_per_node=10 -write_req_log_file=/home/$USERNAME/write-reqs.txt -logtosyslog=false - -bmv2_log_level=trace logtostderr=true 2>&1 | grep -v "StratumErrorSpace::ERR_UNIMPLEMENTED: DataRequest field loopback_status is not supported yet!"
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
