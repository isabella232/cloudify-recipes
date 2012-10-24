#! /bin/bash

echo at this point we would connect to tmux with the following env variables

function write_script () {
    echo >>tmux_script "$@" 
}

write_script '#! /bin/bash'
printenv | grep "^(USM|LOOKUP)" >> tmux_script
write_scipt 'cd $HOME/gigaspaces/work/processing-units/${USM_APPLICATION_NAME}_${USM_SERVICE_NAME}_${USM_INSTANCE_ID}/ext'
write_script 'echo -n "starting a debugging session"'
[[ -n "$1"  ]] && write_script "echo 'for hook $1.'"|| write_script "echo ''"
write_script 'bash'

chmod +x tmux_script

if ! alias debug &>/dev/null ; then  echo >>$HOME/.bashrc 'alias debug="tmux -n Debugging -S /tmp/cloudify.tmux"'; fi

tmux -S /tmp/cloudify.tmux new-session -s Debugging -d
tmux -S /tmp/cloudify.tmux new-window -n "${1:-Debug}" ./tmux_script
