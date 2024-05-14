#!/bin/bash

UERANSIMDIR='5G_PQ/UERANSIM'
cd $UERANSIMDIR
./build/nr-gnb -c config/open5gs-gnb1.yaml

