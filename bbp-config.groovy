import org.apache.log4j.Priority

appName = grails.util.Metadata.current.getApplicationName()

grails.mail.default.from="notification@biodiversity.bt"
emailConfirmation.from="notification@biodiversity.bt"
grails.plugins.springsecurity.ui.notification.emailFrom = 'notification@biodiversity.bt'
grails.plugins.springsecurity.ui.notification.emailReplyTo = "kxt5258@gmail.com";
grails.plugins.springsecurity.ui.newuser.emailFrom = 'notification@biodiversity.bt'
grails.plugins.springsecurity.ui.userdeleted.emailFrom = 'notification@biodiversity.bt'
grails.plugins.springsecurity.ui.forgotPassword.emailFrom = 'notification@biodiversity.bt'

speciesPortal {
	app.siteName = "Bhutan Biodiversity Portal BETA"
	app.siteName2 = "Bhutan Biodiversity Portal BETA2"
        app.logo = "images/demo.png"
	app.favicon = "images/favicon.ico"
	app.siteDescription = "Welcome to the Bhutan Biodiversity Portal (BBP) - A repository of information designed to harness and disseminate collective intelligence on the      biodiversity of Bhutan."
	app.notifiers_bcc = ["kxt5258@gmail.com", "moafbhutan@gmail.com", "cd.drukpa@gmail.com"]
	
	observations {
		filePicker.key = 'ASme8oTdcTSqSi3cTFIWkz'
	}
}

speciesPortal.validCrossDomainOrigins = [
	"localhost",
	"biodiversity.bt",
	"bhutanbiodiversity.bt"
]

jpegOptimProg = "/usr/sbin/jpegoptim";

//log4j
def log4jConsoleLogLevel = Priority.WARN
def log4jAppFileLogLevel = Priority.INFO

environments {
	development {
		grails.serverURL = "http://bhutanbiodiversity.localhost.org/${appName}"
		speciesPortal {
			search.serverURL = "http://localhost:8090/solr"
			names.parser.serverURL = "saturn.strandls.com"
			wgp {
				facebook {
					appId= "385215858271364"
					secret= "1c6bc3dc373abb08d2b7f9f913f09785"
				}
				supportEmail = "support(at)biodiversity(dot)bt"
			}
			ibp {
				facebook {
					appId= "385215858271364"
					secret= "1c6bc3dc373abb08d2b7f9f913f09785"
				}
				supportEmail = "support(at)biodiversity(dot)bt"
			}
		}
		google.analytics.enabled = false
		grails.resources.debug = false
		
		grails {
			mail {
			 host = "smtp.gmail.com"
			 port = 465
			 username = "kinleygrails@gmail.com"
			 password = "Fl0w3rs123"
			 props = ["mail.smtp.auth":"true", 					   
			          "mail.smtp.socketFactory.port":"465",
			          "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
			          "mail.smtp.socketFactory.fallback":"false"]
			}
		}

        	ibp.domain='bhutanbiodiversity.localhost.org'
       		wgp.domain='thewesternghats.bhutanbiodiversity.localhost.org'
		//grails.resources.debug=true
		grails.resources.mappers.hashandcache.excludes = ['**']
		//grails.resources.flatten = false
		grails.resources.mappers.yuijsminify.disable=true

                ckeditor {
                    upload {
              		    basedir = "/newsletters/"
                	    image.browser = true
        	            image.upload = true    
	                    image.allowed = ['jpg', 'gif', 'jpeg', 'png']
        		    image.denied = []
                    }
		}
		
		//log4j logger config
       		log4jConsoleLogLevel = Priority.DEBUG
       		log4jAppFileLogLevel = Priority.DEBUG
	}
	production {
		grails.serverURL = "http://biodiversity.bt/${appName}"
		speciesPortal {
			search.serverURL = "http://localhost:8080/solr"
			names.parser.serverURL = "127.0.0.1"
			ibp {
				facebook {
					appId= "385215858271364"
					secret= "1c6bc3dc373abb08d2b7f9f913f09785"
				}
				supportEmail = "support(at)bhutanbiodiversity(dot)org"
			}
		}
		google.analytics.enabled = false

		
		grails {
			mail {
				 host = "127.0.0.1"
				 port = 25
			}
		}

        	ibp.domain='biodiversity.bt'
       		wgp.domain='wgp.biodiversity.bt'
		//grails.resources.debug=true
		grails.resources.mappers.hashandcache.excludes = ['**']
		//grails.resources.flatten = false
		grails.resources.mappers.yuijsminify.disable=true
	}


	pamba {
		grails.serverURL = "http://biodiversity.bt/${appName}"
		jpegOptimProg = '/usr/sbin/jpegoptim'
		
		speciesPortal {
			app.rootDir = "/data/bbp/species"
			data.rootDir = "${app.rootDir}/data"
			search.serverURL = "http://localhost:8080/solr"
			names.parser.serverURL = "172.0.0.1"
			
			resources {
				rootDir = "${app.rootDir}/images"
				serverURL = "http://biodiversity.bt/${appName}/images"
			}
			nameSearch.indexStore = "${app.rootDir}/data/names"
			observations {
				rootDir = "${app.rootDir}/observations"
				serverURL = "http://biodiversity.bt/${appName}/observations"
			}
			userGroups {
				rootDir = "${app.rootDir}/userGroups"
				serverURL = "http://biodiversity.bt/${appName}/userGroups"
			}
			users {
				rootDir = "${app.rootDir}/users"
				serverURL = "http://biodiversity.bt/${appName}/users"
			}

			content{
				rootDir = "${app.rootDir}/content"
				serverURL = "http://biodiversity.bt/${appName}/content"
			}	

			search.serverURL="http://127.0.0.1:8080/solr"
			grails {
				mail {
					 host = "127.0.0.1"
					 port = 25
				}
			}
			ibp {
				facebook {
					appId= "385215858271364"
					secret= "1c6bc3dc373abb08d2b7f9f913f09785"
				}
				supportEmail = "support(at)biodiversity(dot)bt"
			}
		}
		
        	ibp.domain='biodiversity.bt'
        	wgp.domain='biodiversity.bt'
		
		grails.plugins.springsecurity.successHandler.defaultTargetUrl = "/"
		grails.plugins.springsecurity.logout.afterLogoutUrl = '/'

                ckeditor {
                	upload {
        	            baseurl = "/newsletters"
	                    basedir = "/data/bbp/species/newsletters/"
                	    image.browser = true
        	            image.upload = true    
	                    image.allowed = ['jpg', 'gif', 'jpeg', 'png']
                	    image.denied = []
                	}
		}
		
	}
}


//log4j configuration
log4j = {
	error  	'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.hibernate',
            'net.sf.ehcache.hibernate',
            'org.springframework.security',
            'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'grails.plugin',
            'org.springframework.security.web',
            'grails.app.tagLib.org.grails.plugin.resource'

	warn   'org.mortbay.log'

	debug	'species',
		'speciespage',
		'grails.app'
    	
	info    'species.auth'	
	
	appenders {
		console name:'stdout', layout:pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold: log4jConsoleLogLevel
		rollingFile name: "bbplog", maxFileSize: '10MB', file: "/home/kinley/logs/bbp-stacktrace.log", layout:pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold:log4jAppFileLogLevel 
	}
	
	environments  {
		development {
			root {
				error 'stdout', 'bbplog'
			}
		}
		production {
			root {
				error 'stdout', 'bbplog'
			}
		}
		pamba {
			root {
				error 'bbplog'
			}
		}
	}
}
