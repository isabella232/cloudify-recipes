#!/usr/bin/env groovy
//This script provides various commands to help debug cloudify recipes

//TODO: perhaps set up everything but the argparsing in a class hierarchy shared with the rest of the debug stuff

import java.text.*
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

def debug(args) {
    def cli = new CliBuilder(usage: "${this.class.name}.groovy OPTION")
    cli._(longOpt:'help', 'Show usage information')
    cli._(longOpt:'print-context', 'Print the cloudify context for this instance')
    cli._(longOpt:'list-attributes', args:1, argName:'scope', 'Output cloudify attributes (global/application/service/instance)')
    cli._(longOpt:'script-info', 'Output the variables and properties for the current script')
    cli._(longOpt:'run-groovy', args:1, argName:'command', 'Run a groovy command with this context')
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
        //TODO: print the cloudify properties file's variables (from binding?)
        //and all other relevant variables - do toString on everything
        return
    }

    if (options.'run-groovy') {
        Binding binding = new Binding();
        binding.setVariable("context", context);
        new GroovyShell(binding).evaluate(options.'run-groovy')
        return
    }
}
debug(args)
