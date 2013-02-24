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

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

class DebugHook {
    ServiceContext ctx = null
    String keepaliveFilename = "/tmp/x"
    DebugHook(options=[:]) {
        println("A1")

        if ("context" in options) {
            ctx = options["context"]
        }
        else {
            ctx = ServiceContextFactory.getServiceContext()
        }
        println(ctx)
        // keepaliveFilename = "${ctx.getServiceDirectory()}/.cloudify_debugging"
    }

    def debug_hook(String  arg , mode="instead") { return debug_hook([arg], mode) }
    def debug_hook(GString arg , mode="instead") { return debug_hook([arg.toString()], mode) }
    def debug_hook(Map     args, mode="instead") { //TODO: this is unsupported yet
        return args.inject([:]) {h, k ,v -> h[k] = debug_hook(v, mode); h }
    }

    //The main hook function
    def debug_hook(List args, mode="instead") { 
        prepare_debug_env(args.join(" "))
    
        File keepalive = new File(keepaliveFilename)
        keepalive.createNewFile()

        FileWriter fileWriter = new FileWriter(keepalive, true)
        fileWriter.write(
    """
    #set up the 'debug' alias to enter the debug shell
    if ! alias debug &>/dev/null ; then
        echo >>\$HOME/.bashrc 'echo A cloudify debug shell is available for you by typing \"debug\"';
        echo >>\$HOME/.bashrc 'alias debug=\"bash --rcfile \$HOME/debugrc\"';
    fi
    
    while [[ -f \$0 ]]; do
        echo A debug environment is ready on \$CLOUDIFY_AGENT_ENV_PUBLIC_IP.
        echo When finished, delete the file \$0 (or use the \'finish\' debug command)
        sleep 60)
    done
    """)
        fileWriter.flush()
        fileWriter.close()
        return keepaliveFilename
    }

    def prepare_debug_env(debugTarget) {        
        def bash_commands = [
            [name:"run-script", comment:"Run the current script",
                command:'$CLOUDIFY_WORKDIR/$DEBUG_TARGET'],
            [name:"edit-script", comment:"Edit the current script",
                command:'vim $CLOUDIFY_WORKDIR/$DEBUG_TARGET'],
            [name:"launch-groovysh", comment:"Launch a groovy shell",
                command:'$HOME/gigaspaces/tools/groovy/bin/groovysh -q'],
            [name:"finish", comment:"Finish debugging (move on to the next lifecycle event)",
                command:'rm $KEEPALIVE_FILE'],
        ]

        def templateEngine = new groovy.text.SimpleTemplateEngine()

        def debugTemplate = new File(ctx.getServiceDirectory(),"debugrc").getText()
    
        def preparedTemplate = templateEngine.createTemplate(debugTemplate).make(
            [debugTarget: debugTarget,
             keepaliveFile: keepaliveFilename,
             bashCommands: bash_commands,
        ])
        def targetDebugrc = new File(System.properties["user.home"] +"/.debugrc")
        targetDebugrc.withWriter() {it.write(preparedTemplate)}

//        //set up the "debug" alias to enter the debug shell
//        def bashrc = new File(System.properties["user.home"] +"/.bashrc")
//        if (! bashrc.getText() =~ /alias debug/) {
//            FileWriter fileWriter = new FileWriter(bashrc, true)
//            fileWriter.write(
//    """echo 'A cloudify debug shell is available for you by typing "debug"'
//    alias debug="bash --rcfile \$HOME/debugrc"
//    """)
//            fileWriter.flush()
//            fileWriter.close()
//        }
    }
}