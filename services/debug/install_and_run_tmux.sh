#! /bin/bash

#TODO: abstract package management to be distro-independent (through groovy?)
sudo apt-get update
sudo apt-get -y install tmux

#set up an ssh key for the debugging connection
if [[ -f debugPublicKey ]]
then
    cat debugPublicKey >>$HOME/.ssh/authorized_keys
fi

printenv | grep "^(USM|LOOKUP)" > env-vars

tmux -L cloudify.tmux new-session -d -s "cloudify-debug"