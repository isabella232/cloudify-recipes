service { 
    extend "../puppet"
    name "puppet-master"
    icon "puppet.png"
    compute { 
        template "SMALL_UBUNTU"
        }

    lifecycle {
        start {
            def privateIp = System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
            bootstrap = PuppetBootstrap.getBootstrap(context:context)
            bootstrap.loadManifest(puppetRepo.repoType, puppetRepo.repoUrl)
            bootstrap.applyClasses(puppetRepo.classes)

            //setting the global attributes to be available for all puppet agents
            context.attributes.global["puppet_master_ip"] = privateIp
        }
		startDetectionTimeoutSecs 600
		startDetection {
            // puppetmaster port
			ServiceUtils.isPortOccupied(8140) && 
            // puppetdb
            ServiceUtils.isPortOccupied(8081)
		}
    }
    customCommands([ 
        "puppetCertSign": { agentName -> 
            Shell.sudo("puppet cert sign ${agentName}")                
        }
    ])
}
