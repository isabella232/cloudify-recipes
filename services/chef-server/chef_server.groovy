import org.cloudifysource.dsl.context.ServiceContextFactory
import static Shell.*

def context = ServiceContextFactory.getServiceContext() 

bootstrap = ChefBootstrap.getBootstrap(installFlavor:"gem")
Map chef_server_attributes = bootstrap.runSolo([
    "chef_server": [
        "server_url": "http://localhost:8080",
        "init_style": "runit"
//        "proxy": ["api_port": 443], 
//        "api_port": 8080
    ],
    "chef_packages": [
        "chef": [
            "version": "0.10.8"
        ]
    ],
    "chef_packages": [
        "chef": [
            "version": "0.10.8"
        ]
    ],
    "run_list": ["recipe[build-essential]", "recipe[chef-server::rubygems-install]", "recipe[chef-server::apache-proxy]" ]
])


// eventually we will want to use a global attribute
context.attributes.global["chef_validation.pem"] = sudoReadFile("/etc/chef/validation.pem")
context.attributes.global["chef_server_url"] = "https://" + chef_server_attributes["ipaddress"] + ":" + chef_server_attributes["chef_server"]["proxy"]["api_port"]
