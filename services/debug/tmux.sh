#! /bin/bash

echo at this point we would connect to tmux with the following env variables

echo >tmux_script '#! /bin/bash'
printenv | grep "^(USM|LOOKUP)" >> tmux_script
echo >>tmux_script 'cd $HOME/gigaspaces/work/processing-units/${USM_APPLICATION_NAME}_${USM_SERVICE_NAME}_${USM_INSTANCE_ID}/ext'
echo >>tmux_script 'echo "starting a debugging session'
echo >>tmux_script 'bash'

chmod +x tmux_script

if ! alias debug &>/dev/null ; then  echo >>$HOME/.bashrc 'alias debug="tmux -n Debugging -S /tmp/cloudify.tmux"'; fi
. $HOME/.bashrc

debug