# Common Debug service files
This folder contains common files and a base service recipe for debugging cloudify services. The idea is using `extend` to include this recipe inside the service to be debugged.

> *Important*: Currently the debug recipes has only been tested on an Ubuntu environment on Amazon EC2. Please make sure to use the [EC2 ubuntu cloud driver](https://github.com/CloudifySource/cloudify-cloud-drivers/tree/master/ec2-ubuntu) when installing services or applications that are based on this recipe

Extending this service allows a developer to examine and debug the environment in which a service's event handler is run. To do so, the recipe developer should extend this service and wrap the lifecycle event(s) he would want to debug with a debug_hook.

When the wrapped service is run, the developer will be able to connect to the service server in the midst of deployment, inspect prerequisites, the environment variables and the cloudify context and then run the event handler manually.

This is made possible via a bash rc file loaded with the environment details for the lifecycle event script.

## Example
In a service recipe, one might use:

    service {
        extend "../../../services/debug"
        name "printContext"
        type "APP_SERVER"
        
        elastic true
        numInstances 1
        minAllowedInstances 1
        maxAllowedInstances 1

        compute {
            template "SMALL_UBUNTU"
        }

        lifecycle{
            install debug_hook("printContext.groovy")
        }
    }

