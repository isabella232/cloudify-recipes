#!/usr/bin/env groovy
//This script provides various commands to help debug cloudify recipes
import java.text.*
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext
import static Shell.* //This is apparently not the way to go

def getScriptPath() {
    def env = System.getenv()
    return pathJoin(env["CLOUDIFY_WORKDIR"], env["SCRIPT_NAME"])
}

def debug(args) {
    def cli = new CliBuilder(usage: "${this.class.name}.groovy OPTION")
    cli._(longOpt:'help', 'Show usage information')
    cli._(longOpt:'print-context', 'Print the cloudify context for this instance')
    cli._(longOpt:'list-attributes', args:1, argName:'scope', 'Output cloudify attributes (global/application/service/instance)')
    cli._(longOpt:'script-info', 'Output the variables and properties for the current script')
    cli._(longOpt:'edit-script', 'Edit the current script')
    cli._(longOpt:'run-script', 'Run the current script')
    cli._(longOpt:'run-groovy', args:1, argName:'command', 'Run a groovy command with this context as "x"')
    cli._(longOpt:'launch-groovysh', 'Launch a groovy shell with this context')
    cli._(longOpt:'finish', 'Finish debugging (move to next lifecycle event)')
    def options = cli.parse(args)

    if (args.size() == 0 || !options || options.'help') { //show usage text
        cli.usage()
        return
    }

    ServiceContext context = ServiceContextFactory.getServiceContext()

    if (options.'print-context') {
        println(context.toString())
        //TODO: print all the properties in the context
        return
    }

    if (options.'list-attributes') {
        //for (attr in context.attributes.thisInstance) {println attr}
        for (attr in context.attributes.global) {println attr}
        return
    }

    if (options.'script-info') {

        return
    }

    if (options.'edit-script') {
        sh("vim ${getScriptPath()}") //TODO: This is broken - I can't get groovy to return the tty
        return
    }

    if (options.'run-script') {
        sh(getScriptPath()) //TODO: broken - get this to process the groovy shebang
        return
    }

    if (options.'run-groovy') {
        Binding binding = new Binding();
        binding.setVariable("context", context);
        new GroovyShell(binding).evaluate(options.'run-groovy')
        return
    }

    if (options.'launch-groovysh') {
        sh('$HOME/gigaspaces/tools/groovy/bin/groovysh -q') //TODO: This is broken - I can't get groovy to return the tty
        return
    }

    if (options.'finish') {
        new File(System.getenv()['KEEPALIVE_FILE']).delete()
        return
    }
}
debug(args)
