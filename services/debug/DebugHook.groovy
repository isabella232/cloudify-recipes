#!/usr/bin/env groovy
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

//TODO: wrap the below in a nice class hierarchy before compiling
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