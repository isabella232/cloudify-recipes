#!/usr/bin/env groovy
/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
//TODO: wrap the below in a nice class hierarchy before compiling

import groovy.util.logging.*

class DebugHook {
    def context
    def eventLogger
    def debugMode

    //mode is one of:
    // #instead - just create debug environment instead of running the target script
    // #after - run the target script and then let me debug the outcome state 
    // #instead - like 'after', but only stop for debugging if the script fails
    //A current limitation is that the mode for all hooks on the machine needs to be the same
    //(since the preparation is performed before deployment)
    DebugHook(serviceContext, mode="instead") {
        context = serviceContext
        def loggerName = "org.cloudifysource.usm.USMEventLogger.${context.getApplicationName()}.${context.getServiceName()}"
        eventLogger = java.util.logging.Logger.getLogger(loggerName)

        debugMode = mode
    }

    def groovyDebugParams = "-DXdebug -DXrunjdwp:transport=dt_socket,address=10000,server=y,suspend=n"

    //These are the debug commands that can ran from bash,
    //as opposed to those available from the debug groovy class
    def bashCommands = [
            [name:"run-script", comment:"Run the current script",
                command:'$CLOUDIFY_WORKDIR/$DEBUG_TARGET'],
            [name:"edit-script", comment:"Edit the current script",
                command:'vim $CLOUDIFY_WORKDIR/$DEBUG_TARGET'],
            [name:"launch-groovysh", comment:"Launch a groovy shell",
                command:"\$HOME/gigaspaces/tools/groovy/bin/groovysh -q ${groovyDebugParams}"],
            [name:"finish", comment:"Finish debugging (move on to the next lifecycle event)",
                command:'rm $KEEPALIVE_FILE && echo "Debug step finished"'],
        ]

    //The contents of bash files that will be created for the debug environment:
    def preparationScript = ("""\
#! /bin/bash

#preserve the env variables
touch \$HOME/.cloudify_env
printenv | grep -E \"^(CLOUDIFY|USM|LOOKUP)\" | \
    while read var; do echo >>\$HOME/.cloudify_env \"export \$var\"; done

#import extra ssh public key(s) for the debugging connection
CLOUDIFY_WORKDIR=\$HOME/gigaspaces/work/processing-units/\${USM_APPLICATION_NAME}_\${USM_SERVICE_NAME}_\${USM_INSTANCE_ID}/ext
cd \$CLOUDIFY_WORKDIR
if [[ -f ./debugPublicKey ]] && \
   grep -vxF -f \$HOME/.ssh/authorized_keys ./debugPublicKey #there's at least one added line
then
    logger -it \"CloudifyDebug\"  \"Adding public key from \$CLOUDIFY_WORKDIR/debugPublicKey \"
    cat ./debugPublicKey >>\$HOME/.ssh/authorized_keys
fi

#chmod the actual debug target script
chmod +x \$1

#set up the 'debug' alias to enter the debug shell
if ! grep 'debugrc' \$HOME/.bashrc &>/dev/null
then
    echo >>\$HOME/.bashrc 'echo A cloudify debug shell is available for you by typing \\\"debug\\\"'
    echo >>\$HOME/.bashrc 'alias debug=\"bash --rcfile \$HOME/.debugrc\"'
fi

""")

    String keepaliveFilename = "${System.properties["user.home"]}/.cloudify_debugging"
    
    def waitForFinishLoop = ("""\
touch ${keepaliveFilename}
logger -it \"CloudifyDebug\" \"Beginning debug loop of \$1, until deletion of ${keepaliveFilename}\"
while [[ -f ${keepaliveFilename} ]]; do
    echo \"The service \$USM_SERVICE_NAME (script \$1) is waiting to be debugged on \$CLOUDIFY_AGENT_ENV_PUBLIC_IP.\"
    echo \"When finished, delete the file \$KEEPALIVE_FILE (or use the 'finish' debug command)\"
    logger -it \"CloudifyDebug\" \"Still debugging \$1\"
    sleep 60
done
logger -it \"CloudifyDebug\" \"${keepaliveFilename} was deleted - debug of \$1 finished\"
""")

    def debugrcTemplate = ('''\
# Generated by the Cloudify debug subsystem
echo Loading the debug environment...

#load cloudify environment variables saved for this lifecyle event:
source \\\$HOME/.cloudify_env

export CLOUDIFY_WORKDIR=\\\$HOME/gigaspaces/work/processing-units/\\\${USM_APPLICATION_NAME}_\\\${USM_SERVICE_NAME}_\\\${USM_INSTANCE_ID}/ext
export DEBUG_TARGET=${debugTarget}
export KEEPALIVE_FILE=${keepaliveFile}

cd \\\$CLOUDIFY_WORKDIR
export JAVA_HOME=\\\$HOME/java
export CLASSPATH=`find \\\$HOME/gigaspaces/lib/{required,platform/cloudify} -name *.jar | paste -sd:`
export PATH=\\\$HOME/gigaspaces/tools/groovy/bin:\\\$PATH
chmod +x debug.groovy

#the bash command aliases:
<% bashCommands.each{
    println("alias ${it.name}=\'${it.command}\'")
} %>

#set up shortcut aliases
if [[ ! -f debug_commands ]] ; then
    (groovy ${groovyDebugParams} debug.groovy | tail -n+2 >debug_commands)
<% bashCommands.each{
    println(sprintf("echo >>debug_commands \'      %-26s%s\' ; ", it.name, it.comment))
} %>
fi

for COMMAND in `grep -Eo \'\\\\-\\\\-[^ ]*\' debug_commands | cut -c3- `; do
    alias \\\$COMMAND=\"\\\$CLOUDIFY_WORKDIR/debug.groovy --\\\$COMMAND\"
done
#some special treatment for the help alias
alias help=\"cut -c7- <\\\$CLOUDIFY_WORKDIR/debug_commands\"

clear
PS1=\"Debugging[\\\$DEBUG_TARGET]: \"
echo -en \"\\\\e[0;36m" #change to cyan
echo Starting a debugging session for hook \\\$DEBUG_TARGET
echo These are the available debug commands:
echo -en \"\\\\e[2;37m\" #reset to gray
help
echo
''')

    //Variants of the debug hook accessible from script dsl
    def debug(String  arg) { return debug([arg]) }
    def debug(GString arg) { return debug([arg.toString()],) }

    //The main hook function
    def debug(List args) {
        prepare_debugrc(args.join(" "))

        def debugScriptContents = preparationScript
        switch (debugMode) {
            case "instead":
                debugScriptContents += waitForFinishLoop
                break
            case "after":
                debugScriptContents += './$@ \n' + waitForFinishLoop
                break
            case "onError":
                debugScriptContents += './$@ && exit 0 \n' + waitForFinishLoop
                break
            default:
                throw new Exception("Unrecognized debug mode (${debugMode}), please use one of: 'instead', 'after' or 'onError'")
                break
            }

        def debughookScriptName = System.properties["user.home"] +"/debug-hook.sh"
        new File(debughookScriptName).withWriter() {it.write(debugScriptContents)}

        eventLogger.info "IMPORTANT: A debug environment will be waiting for you on ${context.getPublicAddress()} after the instance has launched"
        return [debughookScriptName] + args
    }

    def prepare_debugrc(debugTarget) {
        def templateEngine = new groovy.text.SimpleTemplateEngine()
        def preparedTemplate = templateEngine.createTemplate(debugrcTemplate).make(
            [debugTarget: debugTarget,
             keepaliveFile: keepaliveFilename,
             bashCommands: bashCommands,
             groovyDebugParams: groovyDebugParams,
        ])
        def targetDebugrc = new File(System.properties["user.home"] +"/.debugrc")
        targetDebugrc.withWriter() {it.write(preparedTemplate)}
    }
}