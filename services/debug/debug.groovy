#!/usr/bin/env groovy
//This script provides various commands to help debug cloudify recipes
import java.text.*
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

def debug(args) {
    def cli = new CliBuilder(usage: "${this.class.name}.groovy OPTION")
    cli.with {
        h longOpt: 'help', 'Show usage information'
        c longOpt: 'print-context', 'Print the cloudify context for this instance'
        a longOpt: 'list-attributes', 'Output cloudify attributes (global/application/service/instance)'
        s longOpt: 'script-info', 'Output the variables and properties for the current script'
        e longOpt: 'edit-script', 'Edit the current script'
        r longOpt: 'run-script', 'Run the current script'
        g longOpt: 'run-groovy', 'Run a groovy command in this context'
    }
    def options = cli.parse(args)

    if (args.size() == 0 || !options || options.h) { //show usage text
        cli.usage()
        return
    }

    ServiceContext context = ServiceContextFactory.getServiceContext()

    if (options.c) {
        println(context.toString())
        //TODO: print all the properties in the context
        return
    }

    if (options.a) {
        //for (attr in context.attributes.thisInstance) {println attr}
        for (attr in context.attributes.global) {println attr}
        return
    }
}
debug(args)
