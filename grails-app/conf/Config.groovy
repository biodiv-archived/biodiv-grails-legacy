import species.auth.SUser;
import java.awt.Font
import java.awt.Color
import com.octo.captcha.service.multitype.GenericManageableCaptchaService
import com.octo.captcha.engine.GenericCaptchaEngine
import com.octo.captcha.image.gimpy.GimpyFactory
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator
import com.octo.captcha.component.image.backgroundgenerator.GradientBackgroundGenerator
import com.octo.captcha.component.image.color.SingleColorGenerator
import com.octo.captcha.component.image.textpaster.NonLinearTextPaster
import grails.plugins.springsecurity.SecurityConfigType;
import com.octo.captcha.service.sound.DefaultManageableSoundCaptchaService
import org.apache.log4j.Priority

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

//if(System.properties["${appName}.config.location"]) {
//   grails.config.locations << "file:" + System.properties["${appName}.config.location"]
//}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = false // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: [
		'text/html',
		'application/xhtml+xml'
	],
	xml: [
		'text/xml',
		'application/xml'
	],
	text: 'text/plain',
	js: 'text/javascript',
	rss: 'application/rss+xml',
	atom: 'application/atom+xml',
	css: 'text/css',
	csv: 'text/csv',
	all: '*/*',
	json: [
		'application/json',
		'text/json'
	],
	form: 'application/x-www-form-urlencoded',
	multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

def log4jConsoleLogLevel = Priority.INFO
// log4j configuration


grails.gorm.default.mapping = {
	cache true
	id generator:'increment'
   /* Added by the Hibernate Spatial Plugin. */
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.GeometryCollection)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.LineString)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Point)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Polygon)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.MultiLineString)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.MultiPoint)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.MultiPolygon)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.LinearRing)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Puntal)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Lineal)
   'user-type'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Polygonal)
	'user-type'( type:org.hibernate.type.YesNoType, class:Boolean )
}

grails.views.javascript.library="jquery"

grails.project.dependency.resolution = {
	inherits("global") {
		if (Environment.current == Environment.PRODUCTION) {
			exclude "servlet-api-2.3"
		}
	}
}


// Prevent any client side caching for now
cache.headers.enabled = true

cache.headers.presets = [
	authed_page: false, // No caching for logged in user
	content: [shared:true, validFor: 3600], // 1hr on content
	search_results: [validFor: 60, shared: true],
	taxonomy_results: [validFor: 60, shared: true]
]

/**
 * Loading configuration file.
 *  1. A command line option overrides everything.
 *  	grails -Ddivr.config.location=C:\temp\divr-config.groovy run-app
 *  2. Looking for ${userHome}/.grails/${appName}-config.groovy
 *  3. Using system environment configuration file: " + System.getenv(ENV_NAME)
 *  4. Using user defined config: file:${userHome}/.grails/${appName}-config.properties.
 */
def ENV_NAME = "${appName}.config.location"
if (!grails.config.locations || !(grails.config.locations instanceof List)) {
	grails.config.locations = []
}
if (System.getProperty(ENV_NAME) && new File(System.getProperty(ENV_NAME)).exists()) {
	println "Using configuration file specified on command line: " + System.getProperty(ENV_NAME)
	grails.config.locations << "file:" + System.getProperty(ENV_NAME)
}
else if (new File("${userHome}/.grails/${appName}-config.groovy").exists()) {
	println "*** User defined config: file:${userHome}/.grails/${appName}-config.groovy. ***"
	grails.config.locations = [
		"file:${userHome}/.grails/${appName}-config.groovy"
	]
}
else if (System.getenv(ENV_NAME) && new File(System.getenv(ENV_NAME)).exists()) {
	println("Using system environment configuration file: " + System.getenv(ENV_NAME))
	grails.config.locations << "file:" + System.getenv(ENV_NAME)
}
else if (new File("${userHome}/.grails/${appName}-config.properties").exists()) {
	println "*** Using user defined config: file:${userHome}/.grails/${appName}-config.properties. ***"
	grails.config.locations = [
		"file:${userHome}/.grails/${appName}-config.properties"
	]
}
else {
	println "*** No external configuration file defined. ***"
}



speciesPortal {
    app.siteName = "India Biodiversity Portal"
    app.siteDescription = "Welcome to the ${app.siteName} - A repository of information designed to harness and disseminate collective intelligence on the biodiversity of the Indian subcontinent."
    app.siteCode = 'ibp'

    app.twitterUrl = "https://twitter.com/thewesternghats"
    app.facebookUrl = "https://www.facebook.com/pages/India-Biodiversity-Portal/130062180358038?fref=ts"
    app.feedbackFormUrl = "http://indiabiodiversity.org/feedback_form"

	app.rootDir = "${userHome}/git/biodiv/app-conf"
	data.rootDir = "${app.rootDir}/data"
	download.rootDir = "${data.rootDir}/datarep/downloads"
 
    app.logo = "logo/IBP.png"
    app.favicon = "logo/favicon.png"
   
    app.notifiers_bcc = ["prabha.prabhakar@gmail.com", "thomas.vee@gmail.com", "sandeept@strandls.com", "balachandert@gmail.com", "rahulk@strandls.com"]

	species {
		speciesDownloadDir = "${download.rootDir}/species"
	}
	domain = "localhost"
	resources {
		rootDir = "${app.rootDir}/img"
		serverURL = "http://indiabiodiversity.localhost.org/${appName}/img"
		images {
			defaultType = "jpg"
			thumbnail {
				suffix = "_th1"+".${defaultType}"
				width = 200
				height = 200
			}

			gallery {
				suffix = "_gall"+".${defaultType}"
				width = 500
				height = 300

			}
			galleryThumbnail {
				suffix = "_gall_th"+".${defaultType}"
				width = 50
				height = 50
			}
		}
	}
	observations {
		rootDir = "${app.rootDir}/observations"
		observationDownloadDir = "${download.rootDir}/observations"
		serverURL = "http://indiabiodiversity.localhost.org/${appName}/observations"
		//serverURL = "http://localhost/${appName}/observations"
		MAX_IMAGE_SIZE = 104857600
        filePicker.key = 'Az2MIh1LOQC2OMDowCnioz'
	} 
	 userGroups {
		rootDir = "${app.rootDir}/userGroups"
		serverURL = "http://indiabiodiversity.localhost.org/${appName}/userGroups"
		//serverURL = "http://localhost/${appName}/userGroups"
		logo {
			MAX_IMAGE_SIZE = 51200
		}
	}

	users {
		rootDir = "${app.rootDir}/users"
		serverURL = "http://localhost/${appName}/users"
		logo {
			MAX_IMAGE_SIZE = 2097000
		}
	}
	
	checklist{
		rootDir = "${app.rootDir}/checklist"
		serverURL = "http://localhost/${appName}/checklist"
		checklistDownloadDir = "${download.rootDir}/checklist"
	}

    maps {
        SRID = 4326;
    }

	content{
		rootDir = "${app.rootDir}/content"
		serverURL = "http://localhost/${appName}/content"
		MAX_DOC_SIZE = 50*1024*1024 //10 mb
		MAX_IMG_SIZE = 2*1024*1024 // 2mb
	}	
		
	names.parser.serverURL = "127.0.0.1"
	names.parser.port = 4334
	search {
		serverURL = "http://localhost:8090/solr"
		queueSize = 1000
		threadCount = 3
		soTimeout = 10000;
		connectionTimeout = 100;
		defaultMaxConnectionsPerHost = 100;
		maxTotalConnections = 100;
		followRedirects = false;
		allowCompression = true;
		maxRetries = 1;
	}
	nameSearch {
		serverURL = "http://localhost:8090/solr/names"
		indexStore = "${app.rootDir}/data/names"
		queueSize = 1000
		threadCount = 3
		soTimeout = 1000;
		connectionTimeout = 100;
		defaultMaxConnectionsPerHost = 100;
		maxTotalConnections = 100;
		followRedirects = false;
		allowCompression = true;
		maxRetries = 1;
	}
	fields  {
		COMBINED_TAXONOMIC_HIERARCHY = "Combined Taxonomy Hierarchy"
		AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY = "Author Contributed Taxonomy Hierarchy"
		GBIF_TAXONOMIC_HIERARCHY = "GBIF Taxonomy Hierarchy"
		CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY = 'Catalogue of Life Taxonomy Hierarchy'
		EFLORA_TAXONOMIC_HIERARCHY = 'Eflora Taxonomy Hierarchy'
		FISHBASE_TAXONOMIC_HIERARCHY = 'FishBase Taxonomy Hierarchy'
		ITIS_TAXONOMIC_HIERARCHY = 'ITIS Taxonomy Hierarchy'
		IUCN_TAXONOMIC_HIERARCHY = 'IUCN Taxonomy Hierarchy (2010)'
		WIKIPEDIA_TAXONOMIC_HIERARCHY = 'Wikipedia Taxonomy Hierarchy'
		SIBLEY_AND_MONROE_TAXONOMIC_HIERARCHY = 'Sibley and Monroe Taxonomy Hierarchy (1996)'
		HOWARD_AND_MOORE_TAXONOMIC_HIERARCHY = 'Howard and Moore Taxonomy Hierarchy (3rd Edition)'
		CLEMENTS_TAXONOMIC_HIERARCHY = 'Clements 6th edition Taxonomy Hierarchy (2009)'
		IOC_TAXONOMIC_HIERARCHY = 'IOC Taxonomy Hierarchy (2009)'
		EBIRD_TAXONOMIC_HIERARCHY = 'eBird Taxonomy Hierarchy (2010)'
		OBC_TAXONOMIC_HIERARCHY = 'OBC Taxonomy Hierarchy (2001)'
		FLOWERS_OF_INDIA_TAXONOMIC_HIERARCHY = 'Flowers of India Taxonomy Hierarchy'

		COMMON_NAME = "Common Name"
		SYNONYMS = "Synonyms"
		INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY = "Local Distribution Geographic Entity"
		INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY = "Local Endemicity Geographic Entity"
		GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY = "Global Distribution Geographic Entity"
		GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY = "Global Endemicity Geographic Entity"
		TAXONOMIC_HIERARCHY = "Taxonomy Hierarchy"
		FAMILY = "Family"
		GENUS = "Genus"
		SUB_GENUS = "Sub-Genus"
		SPECIES = "Species"
		GENERIC_SPECIFIC_NAME = "Generic Specific Name"
		SCIENTIFIC_NAME = "Scientific Name"
		NOMENCLATURE_AND_CLASSIFICATION = "Nomenclature and Classification"
		TAXON_RECORD_NAME = 'Taxon Record Name'
		OVERVIEW = 'Overview'
		OCCURRENCE_RECORDS = 'Occurrence Records'
		BRIEF = "Brief"
		SUMMARY = "Summary"
		TAXONRECORDID = "TaxonRecordID"
		GLOBALUNIQUEIDENTIFIER = "GlobalUniqueIdentifier"
		NOMENCLATURE_AND_CLASSIFICATION = "Nomenclature and Classification"
		TAXON_RECORD_NAME = "Taxon Record Name"
		ATTRIBUTIONS = "attributions"
		CONTRIBUTOR = "contributor"
		LICENSE = 'license'
		AUDIENCE = 'audience'
		STATUS = 'status'
		INFORMATION_LISTING = "Information Listing"
		REFERENCES = "References"
		
		CONCEPT = "concept"
		CATEGORY = "category"
		SUBCATEGORY = "subcategory"
		IMAGES = "images"
		ICONS = "icons"
		AUDIO = "audio"
		VIDEO = "video"
	}
	group {
		ALL = "All"
		OTHERS = "Others"
	}
	searchFields {
		ID = "id"
		GUID = "guid"
		TITLE = "title"
		CONTRIBUTOR = "contributor"
		NAME = "name"
		NAME_EXACT = "name_exact"
		COMMON_NAME = "common_name"
		COMMON_NAME_EXACT = "common_name_exact"
		LOCATION = "location"
		LOCATION_EXACT = "location_exact"
		ATTRIBUTION = "attribution"
		PERCENT_OF_INFO = "percent_of_info"
		
		REFERENCE = "reference"
		TAXON = "taxon"
		MESSAGE = "text"
		CANONICAL_NAME = "canonical_name"
		CANONICAL_NAME_EXACT = "canonical_name_exact"
		UNINOMIAL = "uninomial"
		UNINOMIAL_EXACT = "uninomial_exact"
		AUTHOR = "author"
		AUTHOR_ID = "author_id"
		YEAR = "year"
		GENUS = "genus"
		SPECIES = "species"
		INFRASPECIES = "infraspecies"
		INFRAGENUS = "infragenus"
		SYNONYM = "synonym"
		SYNONYM_CANONICAL = "synonym_canonical"
		SCIENTIFIC_NAME = "scientific_name"
		OBSERVED_ON = "observedon"
		UPLOADED_ON = "createdon"
		UPDATED_ON = "lastrevised"
		FROM_DATE = "fromdate"
		TO_DATE = "todate"
		SGROUP = "sgroup"
		HABITAT = "habitat"
		LATITUDE = "latitude"
		LONGITUDE = "longitude"
		MAX_VOTED_SPECIES_NAME = "maxvotedspeciesname"
		TAG = "tag"
		ISFLAGGED = "isflagged"
		IS_CHECKLIST = "ischecklist"
		IS_SHOWABLE = "isshowable"
		SOURCE_ID = "source_id"
		SOURCE_TEXT = "source_text"
		LATLONG = "latlong"
		USER_GROUP = "group"
		USER_GROUP_WEBADDRESS = "group_webaddress"
		
		GRANTEE_ORGANIZATION = "grantee_organization"
		SITENAME = "sitename"
		CORRIDOR = "corridor"
		DESCRIPTION = "description"
		TYPE = "type"
        	TOPOLOGY = "topology"
        	SCORE = "score"

		EMAIL = "email"
		USERNAME = "username"
		ABOUT_ME = "about_me"
		LAST_LOGIN = "lastlogindate"
	}

	nameSearchFields {
		SCINAME_ID = "sciName_id"
		PREFERRED_SCINAME = "preferred_sciName"
		SCINAME = "sciName"
		COMMONNAME = "commonName"
		SYNONYM = "synonym"
		ICON = "icon"
		SPECIES_ID = "species_id"
		AUTOCOMPLETE = "autocomplete"
	}
	
	drupal {
		getAuthentication = "/getAuthentication.php"
	}
	flushImmediately = true
}

speciesPortal.validCrossDomainOrigins = [
	"localhost",
	"wgp.saturn.strandls.com",
	"wgp.pamba.strandls.com",
	"ibp.saturn.strandls.com",
	"ibp.pamba.strandls.com"
]

//uiperformance.enabled = false
imageConverterProg = "/usr/bin/convert";
jpegOptimProg = "/usr/bin/jpegoptim";

environments {
	development {
        grails.serverURL = "http://indiabiodiversity.localhost.org/${appName}"
        speciesPortal {
            search.serverURL = "http://localhost:8090/solr"
            names.parser.serverURL = "saturn.strandls.com"
            wgp {
                facebook {
                    appId= "424071494335902"
                    secret= "bb87b98979ae30936342364178c7b170"
                }
                supportEmail = "team(at)thewesternghats(dot)in"
            }
            ibp {
                facebook {
                    appId= "347177228674021"
                    secret= "82d91308b5437649bfe891a027205501"
                }
                supportEmail = "support(at)indiabiodiversity(dot)org"
            }
        }
        google.analytics.enabled = false
        grails.resources.debug = false

        grails {
            mail {
                host = "127.0.0.1"
                port = 25
            }
        }

        ibp.domain='indiabiodiversity.localhost.org'
        wgp.domain='thewesternghats.indiabiodiversity.localhost.org'
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


        log4jConsoleLogLevel = Priority.DEBUG
	    log4j = {
		appenders {
		    console name:'stdout', layout:pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold: log4jConsoleLogLevel
        }
        error   'org.codehaus.groovy.grails.web.pages', //  GSP
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
		debug   'speciespage',
                'grails.app',
                'species'
        info    'species.auth'
	}
	}
	test {
		grails.serverURL = "http://indiabiodiversity.localhost.org/${appName}"
		google.analytics.enabled = false
	}
	production {
		grails.serverURL = "http://indiabiodiversity.localhost.org/${appName}"
		speciesPortal {
			search.serverURL = "http://localhost:8090/solr"
			names.parser.serverURL = "127.0.0.1"
			wgp {
				facebook {
					appId= "327308053982589"
					secret= "f36074901fc24b904794692755796fd1"
				}
				supportEmail = "team(at)thewesternghats(dot)in"
			}
			ibp {
				facebook {
					appId= "347177228674021"
					secret= "82d91308b5437649bfe891a027205501"
				}
				supportEmail = "support(at)indiabiodiversity(dot)org"
			}
		}
		google.analytics.enabled = false

		
		grails {
			mail {
				 host = "127.0.0.1"
				 port = 25
			}
		}

        ibp.domain='indiabiodiversity.localhost.org'
        wgp.domain='thewesternghats.indiabiodiversity.localhost.org'
		//grails.resources.debug=true
		grails.resources.mappers.hashandcache.excludes = ['**']
		//grails.resources.flatten = false
		grails.resources.mappers.yuijsminify.disable=true
	}

	saturn {
		grails.serverURL = "http://ibp.saturn.strandls.com/${appName}"
		
		speciesPortal {
			app.rootDir = "/data/species"
			data.rootDir = "${app.rootDir}/data"

			resources {
				rootDir = "${app.rootDir}/images"
				serverURL = "http://saturn.strandls.com/${appName}/images"
			}

			nameSearch.indexStore = "${app.rootDir}/data/names"

			observations {
				rootDir = "${app.rootDir}/observations"
				serverURL = "http://wgp.saturn.strandls.com/${appName}/observations"
                filePicker.key = 'AXCVl73JWSwe7mTPb2kXdz'
	//serverURL = "http://localhost/${appName}/observations"
			}
			userGroups {
				rootDir = "${app.rootDir}/userGroups"
				serverURL = "http://wgp.saturn.strandls.com/${appName}/userGroups"
				//serverURL = "http://localhost/${appName}/observations"
			}
			users {
				rootDir = "${app.rootDir}/users"
				serverURL = "http://wgp.saturn.strandls.com/${appName}/users"
			}
			search.serverURL="http://saturn.strandls.com:8080/solr"
			grails.project.war.file = "/data/jetty-6.1.26/webapps/${appName}.war"
			grails {
				mail {
					 host = "127.0.0.1"
					 port = 25
				}
			}
			wgp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "team(at)thewesternghats(dot)in"
			}
			ibp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "support(at)indiabiodiversity(dot)org"
			}
		}
		google.analytics.enabled = false

	    ibp.domain='ibp.saturn.strandls.com'
        wgp.domain='wgp.saturn.strandls.com' 
		
		grails.plugins.springsecurity.successHandler.defaultTargetUrl = "/"
		grails.plugins.springsecurity.logout.afterLogoutUrl = '/'

                ckeditor {
                    upload {
                    baseurl = "/newsletters"
                    basedir = "/data/species/newsletters/"
                    image.browser = true
                    image.upload = true    
                    image.allowed = ['jpg', 'gif', 'jpeg', 'png']
                    image.denied = []
                }

		}
		log4j = {
			appenders {
				console name:'stdout', layout:pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold: log4jConsoleLogLevel
			}
			debug	'species',
				'speciespage'
		}
	}
	pambaTest {
		appName = "biodiv_test"
		grails.serverURL = "http://indiabiodiversity.saturn.strandls.com/${appName}"
		
		speciesPortal {
			app.rootDir = "/data/pambaTest/species"
			data.rootDir = "${app.rootDir}/data"

			resources {
				rootDir = "${app.rootDir}/images"
				serverURL = "http://saturn.strandls.com/${appName}/images"
			}

			nameSearch.indexStore = "${app.rootDir}/data/names"

			observations {
				rootDir = "${app.rootDir}/observations"
				serverURL = "http://indiabiodiversity.saturn.strandls.com/${appName}/observations"
				//serverURL = "http://localhost/${appName}/observations"
                filePicker.key = 'AXCVl73JWSwe7mTPb2kXdz'
			}
			userGroups {
				rootDir = "${app.rootDir}/userGroups"
				serverURL = "http://indiabiodiversity.saturn.strandls.com/${appName}/userGroups"
				//serverURL = "http://localhost/${appName}/observations"
			}
			users {
				rootDir = "${app.rootDir}/users"
				serverURL = "http://indiabiodiversity.saturn.strandls.com/${appName}/users"
			}
			content{
				rootDir = "${app.rootDir}/content"
				serverURL = "http://indiabiodiversity.saturn.strandls.com/${appName}/content"
			}	

			search.serverURL="http://saturn.strandls.com:8080/solrPamba"
			grails.project.war.file = "/data/jetty-6.1.26/webapps/${appName}.war"
			grails {
				mail {
					 host = "127.0.0.1"
					 port = 25
				}
			}
			wgp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "team(at)thewesternghats(dot)in"
			}
			ibp {
				facebook {
					appId= "310694198984953"
					secret= "eedf76e46272190fbd26e578ae764a60"
				}
				supportEmail = "support(at)indiabiodiversity(dot)org"
			}
		}
		google.analytics.enabled = false

	    ibp.domain='indiabiodiversity.saturn.strandls.com'
            wgp.domain='thewesternghats.indiabiodiversity.saturn.strandls.com' 
		
		grails.plugins.springsecurity.successHandler.defaultTargetUrl = "/"
		grails.plugins.springsecurity.logout.afterLogoutUrl = '/'

                ckeditor {
                    upload {
                    baseurl = "/newsletters"
                    basedir = "/data/pambaTest/species/newsletters/"
                    image.browser = true
                    image.upload = true    
                    image.allowed = ['jpg', 'gif', 'jpeg', 'png']
                    image.denied = []
                }

		}
		log4j = {
			appenders {
				console name:'stdout', layout:pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold: log4jConsoleLogLevel
			}
			info	'species',
				'speciespage'
		}

	}


	pamba {
		grails.serverURL = "http://indiabiodiversity.org/${appName}"
		jpegOptimProg = '/usr/local/bin/jpegoptim'
		
        speciesPortal {
            app.rootDir = "/data/species"
            data.rootDir = "${app.rootDir}/data"
            names.parser.serverURL = "saturn.strandls.com"

            resources {
                rootDir = "${app.rootDir}/images"
                serverURL = "http://pamba.strandls.com/${appName}/images"
            }
            nameSearch.indexStore = "${app.rootDir}/data/names"
            observations {
                rootDir = "${app.rootDir}/observations"
                serverURL = "http://thewesternghats.in/${appName}/observations"
            }
            userGroups {
                rootDir = "${app.rootDir}/userGroups"
                serverURL = "http://thewesternghats.in/${appName}/userGroups"
            }
            users {
                rootDir = "${app.rootDir}/users"
                serverURL = "http://thewesternghats.in/${appName}/users"
            }

            content{
                rootDir = "${app.rootDir}/content"
                serverURL = "http://thewesternghats.in/${appName}/content"
            }	

            search.serverURL="http://thewesternghats.in:8080/solr"
            grails {
                mail {
                    host = "127.0.0.1"
                    port = 25
                }
            }
            wgp {
                facebook {
                    //					appId= "327308053982589"
                    //					secret= "f36074901fc24b904794692755796fd1"
                    appId= "320284831369968"
                    secret= "900d0811194fe28503006b31792690ae"
                }
                supportEmail = "team(at)thewesternghats(dot)in"
            }
            ibp {
                facebook {
                    appId= "320284831369968"
                    secret= "900d0811194fe28503006b31792690ae"
                }
                supportEmail = "support(at)indiabiodiversity(dot)org"
            }
        }

        ibp.domain='indiabiodiversity.org'
        wgp.domain='thewesternghats.indiabiodiversity.org'   
		
		grails.plugins.springsecurity.successHandler.defaultTargetUrl = "/"
		grails.plugins.springsecurity.logout.afterLogoutUrl = '/'

        ckeditor {
            upload {
                baseurl = "/newsletters"
                basedir = "/data/species/newsletters/"
                image.browser = true
                image.upload = true    
                image.allowed = ['jpg', 'gif', 'jpeg', 'png']
                image.denied = []
            }
        }
		
		log4j = {
			appenders {
				console name:'stdout', layout:pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold: Priority.INFO
			}
			info	'species',
				'speciespage'
			warn 	'grails.app',
				'org.springframework.security.web'


		}
	}
}

/*
navigation.species_dashboard = [
        [controller:'species', title:'Species Gallery', order:1, action:"list"],
        [controller:'species', title:'Taxonomy Browser', order:10, action:'taxonBrowser'],
		[controller:'species', title:'Contribute', order:30, action:'contribute'],
]

navigation.observation_dashboard = [
        [controller:'observation', title:'Browse Observations', order:1, action:'list'],
        [controller:'observation', title:'Add Observation', order:10, action:"create"],
]

navigation.users_dashboard = [
	[controller:'species', title:'Species Gallery', order:1, action:"list"],
	[controller:'observation', title:'Browse Observations', order:1, action:'list'],	
	[controller:'userGroup', title:'Groups', order:20, action:'list'],
	[controller:'SUser', title:'Users', order:20, action:'list']
]

navigation.user_group_dashboard = [
	[controller:'userGroup', title:'Browse Groups', order:1, action:'list'],
    [controller:'userGroup', title:'Create New Group', order:10, action:"create"],
]

navigation.userGroup_species_dashboard = [
	[controller:'species', title:'Species Gallery', order:1, action:"list"],
	[controller:'species', title:'Taxonomy Browser', order:10, action:'taxonBrowser'],
	[controller:'species', title:'Contribute', order:30, action:'contribute'],
]

navigation.userGroup_observation_dashboard = [
	[controller:'userGroup', action:'observations', title:'Browse Observations', order:1, action:'list'],
	[controller:'observation', title:'Add Observation', order:10, action:"create"],
]

navigation.dashboard = [
	[group:'species', controller:'species', order:10, title:'Species', action:'list', subItems:[
			[controller:'species', title:'Thumbnail Gallery', order:1, action:"list"],
			[controller:'species', title:'Taxonomy Browser', order:10, action:'taxonBrowser'],
			[controller:'species', title:'Contribute', order:20, action:'contribute']
		]],
	[group:'observation', order:40, controller:'observation', title:'Observations', action:'list', subItems:[
			[controller:'observation', title:'Browse', order:1, action:'list'],
			[controller:'observation', title:'Add Observation', order:10, action:"create"],
		]],
	[group:'users', order:50, controller:'SUser', title:'Users', action:'list'],
	[group:'search', order:60, controller:'search', title:'Advanced Search', action:'advSelect'],
]
*/

jquery {
	sources = 'jquery'
	coreSuffix = 'core'
	cssFolder = 'theme'
	cssDefault = 'base'
	minFolder = 'minified'
	minExtentsion = 'min'
}



// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'species.auth.SUser'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'species.auth.SUserRole'
grails.plugins.springsecurity.authority.className = 'species.auth.Role'
grails.plugins.springsecurity.userLookup.usernamePropertyName = 'email'
//grails.plugins.springsecurity.auth.loginFormUrl = "/login/authFromDrupal"
grails.plugins.springsecurity.successHandler.useReferer = true;

//grails.plugins.springsecurity.auth.defaultRoleNames = ['ROLE_USER']
//grails.plugins.springsecurity.apf.filterProcessesUrl = '/j_drupal_spring_security_check'
//grails.plugins.springsecurity.providerNames = [
//	'drupalAuthentiactionProvider',
//	'daoAuthenticationProvider',
//	'anonymousAuthenticationProvider',
//	'rememberMeAuthenticationProvider'
//];

//grails.plugins.springsecurity.openid.nonceMaxSeconds =  600;

//
//grails.plugins.springsecurity.facebook.appId='308606395828381'
//grails.plugins.springsecurity.facebook.secret='7ddb140cd81ff6b9be38853a0f43d6d3'
//grails.plugins.springsecurity.facebook.bean.dao='facebookAuthDao'

//disabling as uid and name params sent for drupalauthcookiefilter needs to be parsed and
//multipart requests stream cant be read twice.
//grails.disableCommonsMultipart=true
//grails.web.disable.multipart=true

checkin.drupal = false;

grails.plugins.springsecurity.openid.domainClass = 'species.auth.OpenID'
grails.plugins.springsecurity.openid.userLookup.openIdsPropertyName = "openIds"
grails.plugins.springsecurity.rememberMe.persistent = true
grails.plugins.springsecurity.rememberMe.persistentToken.domainClassName = 'species.auth.PersistentLogin'
grails.plugins.springsecurity.roleHierarchy = '''
	ROLE_ADMIN > ROLE_USER
'''

grails.plugins.springsecurity.facebook.domain.classname='species.auth.FacebookUser'
 
grails.taggable.tag.autoImport=true
grails.taggable.tagLink.autoImport=true

grails.mail.default.from="notification@indiabiodiversity.org"
emailConfirmation.from="notification@indiabiodiversity.org"

grails.plugins.springsecurity.password.algorithm = 'MD5'

grails.plugins.springsecurity.ui.password.minLength=6
grails.plugins.springsecurity.ui.password.maxLength=64
grails.plugins.springsecurity.ui.password.validationRegex='^.*$'
grails.plugins.springsecurity.ui.register.postRegisterUrl  = "${grails.serverURL}/user/myprofile" // use defaultTargetUrl if not set
grails.plugins.springsecurity.ui.register.defaultRoleNames = ['ROLE_USER']

//grails.plugins.springsecurity.ui.notification.emailFrom = 'notification@indiabiodiversity.org'
grails.plugins.springsecurity.ui.notification.emailReplyTo = "prabha.prabhakar@gmail.com";

grails.plugins.springsecurity.ui.register.emailBody = '''Hi $username,<br/><br/>You (or someone pretending to be you) created an account with this email address.<br/><br/>If you made the request, please click <a href="$url">here</a> to finish the registration and activate your account.'''
//grails.plugins.springsecurity.ui.register.emailFrom = 'notification@indiabiodiversity.org'
grails.plugins.springsecurity.ui.register.emailSubject = 'Activate your account with $domain'

grails.plugins.springsecurity.ui.newuser.emailBody = '''\
Hi $username,<br/>
<br/>
Thank you for registering with us at <b>$domain</b>.<br/>
<br/> 
We look forward to your contribution on the portal. The portal is a public participatory portal that thrives by participation from users like you. Will also appreciate any feedback you may have to offer.<br/>
<br/>
You will be notified by mail on any social activity on the observation.<br/>
Please update your <a href="$userProfileUrl">user profile</a>.<br/>
<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
-The portal team'''
//grails.plugins.springsecurity.ui.newuser.emailFrom = 'notification@indiabiodiversity.org'
grails.plugins.springsecurity.ui.newuser.emailSubject = 'Welcome to $domain'

grails.plugins.springsecurity.ui.userdeleted.emailBody = '''\
Hi Admin,<br/>
<br/>
A user with email address $email is being deleted from <b>$domain</b>.<br/>
<br/>
-The portal team'''
//grails.plugins.springsecurity.ui.userdeleted.emailFrom = 'notification@indiabiodiversity.org'
grails.plugins.springsecurity.ui.userdeleted.emailSubject = 'User is being deleted on $domain'

grails.plugins.springsecurity.ui.forgotPassword.emailBody = '''\
Hi $username,<br/>
<br/>
You (or someone pretending to be you) requested that your password be reset.<br/>
<br/>
If you didn't make this request then ignore the email; no changes have been made.<br/>
<br/>
If you did make the request, then click <a href="$url">here</a> to reset your password.
'''
//grails.plugins.springsecurity.ui.forgotPassword.emailFrom = 'notification@indiabiodiversity.org'
grails.plugins.springsecurity.ui.forgotPassword.emailSubject = "Password Reset"

grails.plugins.springsecurity.ui.addObservation.emailSubject = 'Observation added'
grails.plugins.springsecurity.ui.addObservation.emailBody = '''
Hi $username,<br/>
<br/>
You have uploaded an observation to <b>$domain</b> and it is available <a href="$obvUrl">here</a><br/>
<br/>
You will be notified by mail on any social activity on the observation.<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
Thank you for your contribution to the portal.<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.addChecklist.emailSubject = 'Checklist added'
grails.plugins.springsecurity.ui.addChecklist.emailBody = '''
Hi $username,<br/>
<br/>
You have uploaded a checklist to <b>$domain</b> and it is available <a href="$obvUrl">here</a><br/>
<br/>
You will be notified by mail on any social activity on the checklist.<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
Thank you for your contribution to the portal.<br/>
<br/>
-The portal team'''


grails.plugins.springsecurity.ui.addRecommendationVote.emailSubject = 'Species name suggested'
grails.plugins.springsecurity.ui.addRecommendationVote.emailBody = '''
Hi $username,<br/>
<br/>

                       <a href="${actorProfileUrl}">
                               <img class="small_profile_pic"
                                       src="$actorIconUrl"
                                       title="$actorName" /> $actorName
                       </a>
               
: $activity on the observation <a href="$obvUrl">here</a><br/>
<br/>
You will be notified by mail on any social activity on the observation.<br/>
<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.newComment.emailSubject = 'New comment'
grails.plugins.springsecurity.ui.newComment.emailBody = '''
Hi $username,<br/>
<br/>
A new comment got added to your observation on <b>$domain</b> which is available <a href="$obvUrl">here</a><br/>
<br/>
You will be notified by mail on any social activity on the observation.<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
Thank you for your contribution to the portal.<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.removeComment.emailSubject = 'Removed a comment'
grails.plugins.springsecurity.ui.removeComment.emailBody = '''
Hi $username,<br/>
<br/>
A comment got removed from your observation on <b>$domain</b> which is available <a href="$obvUrl">here</a><br/>
<br/>
You will be notified by mail on any social activity on the observation.<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
Thank you for your contribution to the portal.<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.observationFlagged.emailBody ='''
Hi $username,<br/>
<br/>
Your <a href="$obvUrl">observation</a> has some social activity.<br/>
$currentUser.username has flagged your Observation.<br/>
<br/>
You can see the posting on your observation <a href="$obvUrl">here</a><br/>
<br/>
You will be notified by mail on any social activity on the observation.<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.askIdentification.emailSubject = 'Please identify the species name'
grails.plugins.springsecurity.ui.askIdentification.staticMessage = '''
The user $currentUser has shared this $activitySource from $domain with you.'''
grails.plugins.springsecurity.ui.askIdentification.emailBody = '''
The user $currentUser has shared this  <a href="$activitySourceUrl">$activitySource</a> from $domain with you.
<br/>
$userMessage
<br/>
If you do not want to receive such mails please click <a href="$unsubscribeUrl">here</a>.
<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.observationDeleted.emailSubject = 'Observation deleted'
grails.plugins.springsecurity.ui.observationDeleted.emailBody = '''
Hi $username,<br/>
<br/>
Your <a href="$obvUrl">observation</a> has been deleted on <b>$domain</b>.<br/>
<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.checklistDeleted.emailSubject = 'Checklist deleted'
grails.plugins.springsecurity.ui.checklistDeleted.emailBody = '''
Hi $username,<br/>
<br/>
Your <a href="$obvUrl">checklist</a> has been deleted on <b>$domain</b>.<br/>
<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
-The portal team'''


grails.plugins.springsecurity.ui.userGroup.inviteMember.emailSubject = 'Request to join the group'
grails.plugins.springsecurity.ui.userGroup.inviteMember.emailBody = '''
Hi $username,<br/>
<br/>
$user has invited you to be member of the group <a href="$groupUrl">$group</a> on <b>$domain</b>.<br/>
<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
-The portal team'''

grails.plugins.springsecurity.ui.userGroup.inviteFounder.emailSubject = 'Request to be founder of the group'
grails.plugins.springsecurity.ui.userGroup.inviteFounder.emailBody = '''
Hi $username,<br/>
<br/>
$user has invited you to be founder of the group <a href="$groupUrl">$group</a> on <b>$domain</b>.<br/>
<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
-The portal team'''
grails.plugins.springsecurity.ui.bBird.emailSubject = "Welcome to the ${app.siteName}"
grails.plugins.springsecurity.ui.bBird.emailBody = '''\
Hi $username,<br/>
<br/>
An account has been created for you on <b>$domain</b> as part of the <a href="$bBirdUrl">Mumbai BirdRace 2013</a>. You are now a member of the group on the portal.
<br/>
Please use the following details to login<br/>
email : $email <br/>
password : $password
<br/>
Please change your password from the <a href="$changePasswordUrl">reset password</a> page.
<br/>
<br/>
We look forward to your contribution on the portal. You can add images of Birds observed during the BirdRace on the  <a href="$obvModuleUrl">Observation module</a>.<br/> 
The portal is a public participatory portal that thrives by participation from users like you.<br/><br/>
We will appreciate any feedback you may have to offer.<br/><br/>
-The portal team'''

grails.plugins.springsecurity.ui.bBirdExistingUser.emailBody = '''\
Hi $username,<br/>
<br/>
You have been added as a member of the <a href="$bBirdUrl">Mumbai BirdRace 2013</a> group on the ${app.siteName}. Please use your existing credentials to login. <br/>
<br/>
We look forward to your contribution on the portal. You can add images of Birds observed during the BirdRace on the  <a href="$obvModuleUrl">Observation module</a>.<br/> 
The portal is a public participatory portal that thrives by participation from users like you.<br/><br/>
We will appreciate any feedback you may have to offer.<br/><br/>
-The portal team
'''

grails.plugins.springsecurity.ui.downloadRequest.emailSubject = 'Download request'
grails.plugins.springsecurity.ui.downloadRequest.message = " data download request has been processed. The download link will be visible once you log in to your profile."
grails.plugins.springsecurity.ui.downloadRequest.emailBody = '''\
Hi $username,<br/>
<br/>
Your data download request on the <b>$domain</b> has been processed. 
<br/>
You can download your data from your <a href="$userProfileUrl">user profile</a>.
<br/> 
Please note that you will need to be logged in to see the download link.
<br/><br/>
-The portal team
'''

grails.plugins.springsecurity.ui.removeRecommendationVote.emailSubject = 'Species name deleted'

grails.plugins.springsecurity.ui.observationPostedToGroup.emailSubject = 'Observation posted to group'
grails.plugins.springsecurity.ui.observationPostedToGroup.emailBody = '''\
Hi $username,<br/>
<br/>
<a href="$actorProfileUrl">$actorName</a> has posted an <a href="$obvUrl">$actionObject</a> to $groupNameWithlink.
<br/><br/>
-The portal team
'''

grails.plugins.springsecurity.ui.checklistPostedToGroup.emailSubject = 'Checklist posted to group'
grails.plugins.springsecurity.ui.checklistPostedToGroup.emailBody = '''\
Hi $username,<br/>
<br/>
<a href="$actorProfileUrl">$actorName</a> has posted a <a href="$obvUrl">$actionObject</a> to $groupNameWithlink.
<br/><br/>
-The portal team
'''


grails.plugins.springsecurity.ui.observationRemovedFromGroup.emailSubject = 'Observation removed from group'
grails.plugins.springsecurity.ui.observationRemovedFromGroup.emailBody = '''\
Hi $username,<br/>
<br/>
<a href="$actorProfileUrl">$actorName</a> has removed an <a href="$obvUrl">$actionObject</a> from $groupNameWithlink.
<br/><br/>
-The portal team
'''

grails.plugins.springsecurity.ui.checklistRemovedFromGroup.emailSubject = 'Checklist removed from group'
grails.plugins.springsecurity.ui.checklistRemovedFromGroup.emailBody = '''\
Hi $username,<br/>
<br/>
<a href="$actorProfileUrl">$actorName</a> has removed a <a href="$obvUrl">$actionObject</a> from $groupNameWithlink.
<br/><br/>
-The portal team
'''


grails.plugins.springsecurity.ui.addDocument.emailSubject = 'Document added'
grails.plugins.springsecurity.ui.addDocument.emailBody = '''
Hi $username,<br/>
<br/>
You have uploaded a document to <b>$domain</b> and it is available <a href="$obvUrl">here</a><br/>
<br/>
You will be notified by mail on any social activity on the document.<br/>
If you do not want to receive notifications please go to your <a href="$userProfileUrl">user profile</a> and switch it off.<br/>
<br/>
Thank you for your contribution to the portal.<br/>
<br/>
-The portal team'''



grails.plugins.springsecurity.ui.encodePassword = false

grails.plugins.springsecurity.useSecurityEventListener = true
grails.plugins.springsecurity.onInteractiveAuthenticationSuccessEvent = { e, appCtx ->
	Class<?> User = SUser.class
	println "updating lastlogin date : "+appCtx.springSecurityService.principal.id
	if (!User) {
		println "Can't find domain: $domainClassName"
		return null
	}
	def user = null

	User.withTransaction {
		user = User.get(appCtx.springSecurityService.principal.id)
		user.lastLoginDate = new Date()
		user.save()
	}
}

grails.plugins.springsecurity.openid.registration.requiredAttributes = [email: 'http://axschema.org/contact/email', location: 'http://axschema.org/contact/country/home',firstname:'http://axschema.org/namePerson/first', lastname: 'http://axschema.org/namePerson/last', profilePic:'http://axschema.org/media/image/default']


//TODO:Need to change
grails.plugins.springsecurity.useRunAs = true
grails.plugins.springsecurity.runAs.key = 'run-asKey'

grails.plugins.springsecurity.acl.authority.modifyAuditingDetails = 'ROLE_ADMIN'//'ROLE_ACL_MODIFY_AUDITING'
grails.plugins.springsecurity.acl.authority.changeOwnership =       'ROLE_ADMIN'
grails.plugins.springsecurity.acl.authority.changeAclDetails =      'ROLE_RUN_AS_ADMIN'//'ROLE_ACL_CHANGE_DETAILS'

grails.plugins.springsecurity.securityConfigType = SecurityConfigType.Annotation 
grails.plugins.springsecurity.controllerAnnotations.staticRules = [
	'/role/**': ['ROLE_ADMIN'],
	'/persistentLogin/**': ['ROLE_ADMIN'],
	'/abstractS2Ui/**': ['ROLE_ADMIN'],
	'/aclClass/**': ['ROLE_ADMIN'],
	'/aclEntry/**': ['ROLE_ADMIN'],
	'/aclObjectIdentity/**': ['ROLE_ADMIN'],
	'/aclSid/**': ['ROLE_ADMIN'],
	'/registrationCode/**': ['ROLE_ADMIN'],
	'/requestmap/**': ['ROLE_ADMIN'],
	'/securityInfo/**': ['ROLE_ADMIN'],
	'/securityInfo/**': ['ROLE_ADMIN'],
    '/rateable/rate/**': ['ROLE_USER']
 ]



jcaptchas {
	imageCaptcha = new GenericManageableCaptchaService(
		new GenericCaptchaEngine(
			new GimpyFactory(
				new RandomWordGenerator(
					"abcdefghijklmnopqrstuvwxyz1234567890"
				),
				new ComposedWordToImage(
					new RandomFontGenerator(
						20, // min font size
						30, // max font size
						[new Font("Arial", 0, 10)] as Font[]
					),
					new GradientBackgroundGenerator(
						140, // width
						35, // height
						new SingleColorGenerator(new Color(229, 229, 229)),
						new SingleColorGenerator(new Color(229, 229, 229))
					),
					new NonLinearTextPaster(
						6, // minimal length of text
						6, // maximal length of text
						new Color(0, 0, 0)
					)
				)
			)
		),
		180, // minGuarantedStorageDelayInSeconds
		180000 // maxCaptchaStoreSize
	)

	/*soundCaptcha = new DefaultManageableSoundCaptchaService()*/
}

NamesIndexerService.FILENAME = "${appName}_tstLookup.dat";
ObservationController.COMMIT = false;

grails.rateable.rater.evaluator = { 
	Class<?> User = SUser.class
	if (!User) {
		println "Can't find domain: $domainClassName"
		return null
    }
    def u = org.springframework.security.core.context.SecurityContextHolder.context.authentication.principal
    if(u && !(u instanceof String)) {
        def user = User.get(u.id);
        return user
    }
}

