# Puppet mysql
This recipe installs a mysql instance using puppet agent (NOT puppet apply).
To use this recipe, you must first have a puppet master running. You can install one using the `puppet-master` recipe in which case it will auto register in a global attribute.

## How the agent finds the puppet master
To let the puppet agent recipe know where you puppet master is, set the master ip in a global attribute or service properties file.
In a properties file:

    puppetMasterIp = "10.33.24.66"

In a global attribute:

    set-attribute -scope global '{"puppet_master_ip": "10.33.24.66"}'

If you are using the puppet master recipe then there is no need to set anything.

## Node classification
In your puppet `site.pp` on the master, put the following in `node default`:
    
    if $cloudify_service != undef { 
        case $cloudify_service {
            'mysql' : {
                class { '::mysql::server':
                    root_password    => 'strongpassword',
                }
            }
        }
    }

Install the mysql puppet module on the master:

    puppet module install puppetlabs-mysql
    puppet agent --test # to install stdlib plugins

That's it! now you can `install-service cloudify-recipes/services/puppet-mysql`
