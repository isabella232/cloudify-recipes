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
import groovy.json.JsonSlurper
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

    //load the context with a bit less verbosity
    def realOut = System.out
    System.out = new PrintStream(new ByteArrayOutputStream())
    ServiceContext context = ServiceContextFactory.getServiceContext()
    System.out = realOut


    if (options.'print-context') {
        context.getProperties().each{println it}
        return
    }

    if (options.'list-attributes') {
        //We use the REST interface for this, since context attribute iteration is unimplemented

        def managementIp = System.getenv("LOOKUPLOCATORS").split(":")[0]

        def application = context.getApplicationName()
        def service = context.getServiceName()
        def instanceID = context.getInstanceId()

        def requestUrl = "attributes/"
        switch (options.'list-attributes') {
            case "global":
                requestUrl += "globals"
                break
            case "application":
                requestUrl += "applications/${application}"
                break
            case "service":
                requestUrl += "services/${application}/${service}"
                break
            case "instance":
                requestUrl += "instances/${application}/${service}/${instanceID}"
                break
            default:
                throw new Exception("Unrecognized scope(${options.'list-attributes'}), please use one of: 'global', 'application', 'service' or 'instance'")
                break
            }

         def resultJson = new URL("http://${managementIp}:8100/${requestUrl}").text
         new JsonSlurper().parseText(resultJson).each{println it}
    }

    if (options.'script-info') {
        //TODO: should I serialize the binding from before?
        binding.variables.each{ println "${it.key.toString()}=${it.value.toString()}" }
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
