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
        e longOpt: 'print-context', 'Print the cloudify context for this instance'
        r longOpt: 'print-context', 'Print the cloudify context for this instance'
        g longOpt: 'print-context', 'Print the cloudify context for this instance'
    }
    def options = cli.parse(args)

    if (args.size() == 0 || !options || options.h) { //show usage text
        cli.usage()
        return
    }

    if (options.c) {
        ServiceContext context = ServiceContextFactory.getServiceContext()
        println(context.toString())
        //TODO: print all the properties in the context
        return
    }
}
debug(args)

/*
1.       context – output the service context: current instance ID, current IP, other instances and their IPs, other discovered services and their number of instances
2.       attributes – output the attributes:
    a.       –global – the attributes in global
    b.      –application – the attributes for the application
    c.       –service the attributes for current service or if other name specified the attributes for the other service
    d.      –instance the attributes for this instance or the specified instance ID
3.       Script – output the variables and properties for the current script
4.       Edit  takes the user to vi editor to edit the current script
5.       Run runs the current script
6.       Exec run a groovy statement in context
*/
