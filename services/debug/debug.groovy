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
//This script provides various commands to help debug cloudify recipes

//TODO: perhaps set up everything but the argparsing in a class hierarchy shared with the rest of the debug stuff

import java.text.*
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

def debug(args) {
    def cli = new CliBuilder(usage: "${this.class.name}.groovy OPTION", width:80)
    cli._(longOpt:'help', 'Show this usage information')
    cli._(longOpt:'print-context', 'Print the cloudify context for this instance')
    cli._(longOpt:'list-attributes', args:1, argName:'scope', 'Output cloudify attributes [global/application/service/instance]')
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
        switch (options.'list-attributes') {
            case "global":
                attrs = context.attributes.global
                break
            case "application":
                attrs = context.attributes.thisApplication
                break
            case "service":
                attrs = context.attributes.thisService
                break
            case "instance":
                attrs = context.attributes.thisInstance
                break
            default:
                throw new Exception("Unrecognized scope(${options.'list-attributes'}), please use one of: 'global', 'application', 'service' or 'instance'")
                break
            }
        for (attr in attrs) {println attr}
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
