#! /usr/bin/env groovy
println("Welcome to printContext.groovy\n")

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

ServiceContext context = ServiceContextFactory.getServiceContext()
println("\nBelow is the context for this instance:\n${context}")
//TODO: print all the properties in the context