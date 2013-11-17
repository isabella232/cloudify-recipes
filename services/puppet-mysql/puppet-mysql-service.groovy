service {
	
    extend "../puppet-agent"
	name "mysql"

	icon "mysql.png"
	type "DATABASE"

	lifecycle{
 
        def mysqlPort = 3306
		
		startDetectionTimeoutSecs 900
		
		startDetection {	
			ServiceUtils.isPortOccupied(mysqlPort)
		}
		
		locator {	
			//hack to avoid monitoring started processes by cloudify
			  //return  [] as LinkedList	
			 
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.re=mysql.*\\.exe|mysqld")
			println ":mysql: current PIDs: ${myPids}"
			return myPids
		}
		
		details {
			def currPublicIP
			
			if ( context.isLocalCloud() ) {
				currPublicIP = InetAddress.localHost.hostAddress	
			}
			else {
				currPublicIP =context.getPublicAddress()	
			}
			return [	
				"MySQL IP":currPublicIP,
				"MySQL Port":mysqlPort
			]
		}	
    }
}
