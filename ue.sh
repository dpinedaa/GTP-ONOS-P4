#!/bin/bash

UERANSIMDIR='5G_PQ/UERANSIM'
cd $UERANSIMDIR
sudo ./build/nr-ue -c config/open5gs-ue1.yaml

