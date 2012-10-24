#! /bin/bash

sudo apt-get -y install tmux

printenv | grep "^(USM|LOOKUP)" > env-vars

tmux -S /tmp/cloudify.tmux new-session -d -s "cloudify-debug"