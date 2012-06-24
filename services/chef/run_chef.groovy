import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def context = ServiceContextFactory.getServiceContext()
def chef_server_service = context.waitForService("chef-server", 20, TimeUnit.SECONDS)
def chefServerURL = context.attributes.global["chef_server_url"]
def validationCert = context.attributes.global["chef_validation.pem"]

println "Using Chef server URL: ${chefServerURL}"

ChefBootstrap.getBootstrap(
    serverURL: chefServerURL,
    validationCert: validationCert,
    context: context
).runClient([run_list: "role[${context.serviceName}]".toString()])
