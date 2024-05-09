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
sudo apt update && sudo apt install bazel bazel-3.7.2 -y
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
wget https://github.com/opennetworkinglab/onos/archive/refs/tags/2.7.0.zip
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





































