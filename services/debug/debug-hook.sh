#! /bin/bash

SCRIPT_NAME=$1
CLOUDIFY_WORKDIR="$HOME/gigaspaces/work/processing-units/${USM_APPLICATION_NAME}_${USM_SERVICE_NAME}_${USM_INSTANCE_ID}/ext"
KEEPALIVE_FILE=$HOME/.cloudify_debugging
#prepare an rc file for the debug shell
:>$HOME/debugrc
function write_script () {
    echo >>$HOME/debugrc "$@"
}
write_script "# Generated on `date` by debug-hook.sh for host `hostname`, part of Cloudify debug subsystem"
write_script 'echo "Loading the debug environment..."'
write_script '#the cloudify environment variables for this event:'
printenv | grep -E "^(CLOUDIFY|USM|LOOKUP)" | while read var; do write_script export $var; done
write_script ''
write_script "export CLOUDIFY_WORKDIR=$CLOUDIFY_WORKDIR"
write_script "export SCRIPT_NAME=$SCRIPT_NAME"
write_script "export KEEPALIVE_FILE=$KEEPALIVE_FILE"
write_script 'cd $CLOUDIFY_WORKDIR'
write_script 'export JAVA_HOME=$HOME/java'
write_script 'export CLASSPATH=`find $HOME/gigaspaces/lib/{required,platform/cloudify} -name *.jar | paste -sd:`'
write_script 'export PATH=$HOME/gigaspaces/tools/groovy/bin:$PATH'

write_script "chmod +x debug.groovy"
write_script "chmod +x $SCRIPT_NAME"
write_script "PS1='Debugging[$SCRIPT_NAME]: '"

#set up shortcut aliases
write_script '[[ -f debug_commands ]] || (./debug.groovy | tail -n+2 >debug_commands)'
write_script 'for COMMAND in `grep -Eo "\-\-[^ ]*" debug_commands | cut -c3- `; do
                  alias $COMMAND="$CLOUDIFY_WORKDIR/debug.groovy --$COMMAND" 
              done'

#some special treatment for the help alias
write_script 'alias help="cut -c7- <$CLOUDIFY_WORKDIR/debug_commands"'

#a nice intro
write_script 'clear'
write_script 'echo -n "Starting a debugging session"'
[[ -n "$SCRIPT_NAME"  ]] && write_script 'echo ": for hook $SCRIPT_NAME"' || write_script "echo ''"
write_script ''
write_script 'echo "These are the available debug commands:"'
write_script 'help' #use our newly created alias
write_script 'echo'

#set up the "debug" alias to enter the debug shell
if ! alias debug &>/dev/null ; then
    echo >>$HOME/.bashrc 'echo "A cloudify debug shell is available for you by typing \"debug\""';
    echo >>$HOME/.bashrc 'alias debug="bash --rcfile $HOME/debugrc"';
fi

#Enter a loop to avoid proceeding to the next step until the user finished debugging
:>$KEEPALIVE_FILE
while [[ -f $KEEPALIVE_FILE ]]; do
    echo "The service $USM_SERVICE_NAME (script $SCRIPT_NAME) is waiting to be debugged on $CLOUDIFY_AGENT_ENV_PUBLIC_IP."
    echo "When finished, delete the file $KEEPALIVE_FILE (or use the 'finish' debug command)"
    sleep 60
done
