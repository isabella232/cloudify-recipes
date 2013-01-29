#!/usr/bin/env groovy
//This script provides various commands to help debug cloudify recipes
import java.text.*
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

def debug(args) {
    def cli = new CliBuilder(usage: "${this.class.name}.groovy OPTION")
    cli._(longOpt:'help', 'Show usage information')
    cli._(longOpt:'print-context', 'Print the cloudify context for this instance')
    cli._(longOpt:'list-attributes', args:1, argName:'scope', 'Output cloudify attributes (global/application/service/instance)')
    cli._(longOpt:'script-info', 'Output the variables and properties for the current script')
    cli._(longOpt:'edit-script', 'Edit the current script')
    cli._(longOpt:'run-script', 'Run the current script')
    cli._(longOpt:'run-groovy', 'Run a groovy command in this context')
    cli._(longOpt:'launch-groovysh', 'Launch a groovy shell with this context')
    cli._(longOpt:'exit', 'Exit debugging shell (with option of returning)')
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
}
debug(args)
