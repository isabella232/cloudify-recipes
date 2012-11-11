#! /bin/bash

#TODO: abstract package management to be distro-independent (through groovy?)
sudo apt-get update
sudo apt-get -y install tmux

printenv | grep "^(USM|LOOKUP)" > env-vars

tmux -L cloudify.tmux new-session -d -s "cloudify-debug"