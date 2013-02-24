#! /bin/bash
#TODO: push this logic into the jar

#set up an ssh key for the debugging connection
if [[ -f debugPublicKey ]]
then
    cat debugPublicKey >>$HOME/.ssh/authorized_keys
fi
