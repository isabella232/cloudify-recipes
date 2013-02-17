#!/usr/bin/env groovy
//TODO: wrap the below in a nice class hierarchy before compiling

//these are different entry points for creating a debug environment around a lifecycle script
def debug_hook(String  arg , mode="instead") { return debug_hook([arg], mode) }
def debug_hook(GString arg , mode="instead") { return debug_hook([arg.toString()], mode) }
def debug_hook(Map     args, mode="instead") { //TODO: this is unsupported yet
    return args.inject([:]) {h, k ,v -> h[k] = debug_hook(v, mode); h }
}

//The main hook function
def debug_hook(List args, mode="instead") { return ['debug-hook.sh'] + args }


/*TODO: define these:
SCRIPT_NAME=$1
CLOUDIFY_WORKDIR="$HOME/gigaspaces/work/processing-units/${USM_APPLICATION_NAME}_${USM_SERVICE_NAME}_${USM_INSTANCE_ID}/ext"
KEEPALIVE_FILE=$HOME/.cloudify_debugging
*/


def bash_commands = [
    [name:"run-script", comment:"Run the current script"
        command:'$CLOUDIFY_WORKDIR/$SCRIPT_NAME'],
    [name:"edit-script", comment:"Edit the current script"
        command:'vim $CLOUDIFY_WORKDIR/$SCRIPT_NAME'],
    [name:"launch-groovysh", comment:"Launch a groovy shell"
        command:'$HOME/gigaspaces/tools/groovy/bin/groovysh -q'],
    [name:"finish", comment:"Finish debugging (move on to the next lifecycle event)"
        command:'rm $KEEPALIVE_FILE'],
]


def writeTemplate(templatePath, options=[:], targetPath=null) {
    targetPath = targetPath ?: pathJoin(webapp_dir, templatePath)
    String templatesDir = pathJoin(context.getServiceDirectory(),"templates")
    def templateEngine = new groovy.text.SimpleTemplateEngine()

    def template = new File(templatesDir, templatePath).getText()
    def preparedTemplate = templateEngine.createTemplate(template).make([options: options])
    sudoWriteFile(targetPath, preparedTemplate.toString())
}


/*TODO: write the debug.sh file with the required aliases from both here and the groovy commands (and write the help command)
TODO: and then create the keepalive and wait for finish
:>$KEEPALIVE_FILE
while [[ -f $KEEPALIVE_FILE ]]; do
    echo "The service $USM_SERVICE_NAME (script $SCRIPT_NAME) is waiting to be debugged on $CLOUDIFY_AGENT_ENV_PUBLIC_IP."
    echo "When finished, delete the file $KEEPALIVE_FILE (or use the 'finish' debug command)"
    sleep 60
done
*/