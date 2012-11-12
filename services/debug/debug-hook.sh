#! /bin/bash

#prepare an rc file for the debug shell
:>$HOME/debugrc
function write_script () {
    echo >>$HOME/debugrc "$@"
}
write_script 'the cloudify env variables for this event:'
printenv | grep -E "^(CLOUDIFY|USM|LOOKUP)" | while read var; do write_script export $var; done
write_script ''
write_script 'cd $HOME/gigaspaces/work/processing-units/${USM_APPLICATION_NAME}_${USM_SERVICE_NAME}_${USM_INSTANCE_ID}/ext'
write_script 'export JAVA_HOME=$HOME/java'
write_script 'export CLASSPATH=`find $HOME/gigaspaces -name *.jar | paste -sd:`'
write_script 'export PATH=$HOME/gigaspaces/tools/groovy/bin:$PATH'
write_script ''
write_script 'echo -n "starting a debugging session"'
[[ -n "$1"  ]] && write_script "echo ' for hook $1'" || write_script "echo ''"

#TODO: move these to be with the other (yet unwritten) debug aliases
write_script "chmod +x $1"
write_script "alias runscript=./$1"

if ! alias debug &>/dev/null ; then
    echo >>$HOME/.bashrc  'alias debug="bash --rcfile $HOME/debugrc"';
fi