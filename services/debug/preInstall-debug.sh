#! /bin/bash

#set up an ssh key for the debugging connection
if [[ -f debugPublicKey ]]
then
    cat debugPublicKey >>$HOME/.ssh/authorized_keys
fi
