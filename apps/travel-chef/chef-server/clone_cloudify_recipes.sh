#! /bin/bash
#This command has been split off from chef_server_loadCookbooks to allow testing the code from a fork.
TARGET_DIR=$1
git clone -b global_attributes https://github.com/Fewbytes/cloudify-recipes.git $TARGET_DIR
