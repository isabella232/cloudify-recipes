#! /usr/bin/env groovy
println("Welcome to testInstall.groovy")

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext

ServiceContext context
context = ServiceContextFactory.getServiceContext()

println(context)