/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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

//TODO: put these debug definitions manually in a common jar file (or something similar)
//import static org.cloudifysource.Debug.*

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

class DebugHook {
    String keepaliveFilename = '$HOME/.cloudify_debugging'

    //these are different entry points for creating a debug environment around a lifecycle script
    def debug_hook(String  arg , mode="instead") { return debug_hook([arg], mode) }
    def debug_hook(GString arg , mode="instead") { return debug_hook([arg.toString()], mode) }
    def debug_hook(Map     args, mode="instead") { //TODO: this is unsupported yet
        return args.inject([:]) {h, k ,v -> h[k] = debug_hook(v, mode); h }
    }

    //The main hook function
    def debug_hook(List args, mode="instead") { 
        prepare_debug_env(args.join(" "))
        println("prepare_debug_env complete")

        //return a closure that will sleep until the debug is complete
        return {
            keepalive = new File(keepaliveFilename).createNewFile()
            while (keepalive.exists()) {
                print "The service $USM_SERVICE_NAME (script $SCRIPT_NAME) is waiting to be debugged on $CLOUDIFY_AGENT_ENV_PUBLIC_IP."
                print "When finished, delete the file $KEEPALIVE_FILE (or use the 'finish' debug command)"
                sleep(60 * 1000)
            }
        }
    }

    def prepare_debug_env(debugTarget) {
        ServiceContext context = ServiceContextFactory.getServiceContext()
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
        def debugTemplate = new File(context.getServiceDirectory() + "/debugrc").getText()
        def preparedTemplate = templateEngine.createTemplate(debugTemplate).make(
            [debugTarget: debugTarget,
             keepaliveFile: keepaliveFilename,
             bashCommands: bash_commands, 
        ])
        def targetDebugrc = new File(System.properties["user.home"] +"/.debugrc")
        targetDebugrc.withWriter() {it.write(preparedTemplate)}

        //set up the "debug" alias to enter the debug shell
        def bashrc = new File(System.properties["user.home"] +"/.bashrc")
        if (! bashrc.getText() =~ /alias debug/) {
            FileWriter fileWriter = new FileWriter(bashrc, true)
            fileWriter.write('echo "A cloudify debug shell is available for you by typing \"debug\""\n')
            fileWriter.write('alias debug="bash --rcfile $HOME/debugrc"\n')
            fileWriter.flush()
            fileWriter.close()
        }
    }
}


//TODO!!: add flag to enable first trial run of the script, before/instead of entering debug
//def debug_hook(List    args) { return ['debug-hook.sh'] + args }
//def debug_hook(String  arg ) { return debug_hook([arg]) }
//def debug_hook(GString arg ) { return debug_hook([arg.toString()]) }
//def debug_hook(Map     args) { return args.inject([:]) {h, k ,v -> h[k] = debug_hook(v); h }}

service {
    extend "../../../services/debug"
    name "printContext"
    type "APP_SERVER"
    
    elastic false
    numInstances 1

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle{
        install {
            hookMaker = new DebugHook()
            debugger = hookMaker.debug_hook("printContext.groovy")
            debugger() //TODO: why doesn't this work?
        }
    }
}
