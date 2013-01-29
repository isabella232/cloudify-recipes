#! /bin/bash

WORKDIR="$HOME/gigaspaces/work/processing-units/${USM_APPLICATION_NAME}_${USM_SERVICE_NAME}_${USM_INSTANCE_ID}/ext"
#prepare an rc file for the debug shell
:>$HOME/debugrc
function write_script () {
    echo >>$HOME/debugrc "$@"
}
write_script '#the cloudify environment variables for this event:'
printenv | grep -E "^(CLOUDIFY|USM|LOOKUP)" | while read var; do write_script export $var; done
write_script ''
write_script "cd $WORKDIR"
write_script 'export JAVA_HOME=$HOME/java'
write_script 'export CLASSPATH=`find $HOME/gigaspaces -name *.jar | paste -sd:`'
write_script 'export PATH=$HOME/gigaspaces/tools/groovy/bin:$PATH'

write_script "chmod +x debug.groovy"
write_script "chmod +x $1"
write_script "PS1='Debugging[$1]: '"

#set up shortcut aliases
$WORKDIR/debug.groovy | grep -Eo '\-\-[^ ]*' | cut -c3- | \
    xargs -l -I{} write_script "alias {}=\"$WORKDIR/debug.groovy --{}\""
#some special treatment for the help alias
write_script "help= \"$WORKDIR/debug.groovy | tail -n+2 | cut -c7- \""

#a nice intro
write_script ''
write_script 'echo -n "Starting a debugging session"'
[[ -n "$1"  ]] && write_script "echo ' for hook $1'" || write_script "echo ''"
write_script ''
write_script 'echo "These are the available debug commands:"'
write_script 'help' #use our newly created alias

#set up the "debug" alias to enter the debug shell
if ! alias debug &>/dev/null ; then
    echo >>$HOME/.bashrc 'echo "A cloudify debug shell is available for you by typing \"debug\""';
    echo >>$HOME/.bashrc 'alias debug="bash --rcfile $HOME/debugrc"';
fi

#Enter a loop to avoid proceeding to the next step until the user finished debugging
:>$HOME/debugging
while [[ -f $HOME/debugging ]]; do
    echo "The service $USM_SERVICE_NAME (script $1) is waiting to be debugged on $CLOUDIFY_AGENT_ENV_PUBLIC_IP."
    echo " When finished, delete the file $HOME/debugging (or use the 'finish' debug command)"
    sleep 60
done
