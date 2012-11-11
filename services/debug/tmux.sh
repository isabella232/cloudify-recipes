#! /bin/bash

function write_script () {
    echo >>tmux_script "$@" 
}

write_script '#! /bin/bash'
printenv | grep "^(USM|LOOKUP)" >> tmux_script
write_script 'cd $HOME/gigaspaces/work/processing-units/${USM_APPLICATION_NAME}_${USM_SERVICE_NAME}_${USM_INSTANCE_ID}/ext'
write_script 'export CLASSPATH=`find ~/gigaspaces -name *.jar | paste -sd:`'
write_script 'export PATH=$HOME/gigaspaces/tools/groovy/bin:$PATH'
write_script 'echo -n "starting a debugging session "'
[[ -n "$1"  ]] && write_script "echo 'for hook $1.'"|| write_script "echo ''"
write_script "bash --rcfile <(echo chmod +x $1 ; echo alias runscript=./$1)"

chmod +x tmux_script

export JAVA_HOME=$HOME/java

if ! alias debug &>/dev/null ; then
    echo >>$HOME/.bashrc 'alias debug="tmux -L cloudify.tmux attach-session -t Debugging"';
fi

tmux -L cloudify.tmux new-session -s Debugging -d
tmux -L cloudify.tmux new-window -n "${1:-Debug}" ./tmux_script
