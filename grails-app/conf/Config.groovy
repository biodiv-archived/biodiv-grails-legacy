// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
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

// log4j configuration
log4j = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	//appenders {
	//    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
	//}

	error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
			'org.codehaus.groovy.grails.web.pages', //  GSP
			'org.codehaus.groovy.grails.web.sitemesh', //  layouts
			'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			'org.codehaus.groovy.grails.web.mapping', // URL mapping
			'org.codehaus.groovy.grails.commons', // core / classloading
			'org.codehaus.groovy.grails.plugins', // plugins
			'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
			'org.springframework',
			'org.hibernate',
			'net.sf.ehcache.hibernate'

	warn   'org.mortbay.log'
	debug	'species',
			'speciespage',
			'grails.app'

}

grails.views.javascript.library="jquery"

grails.project.dependency.resolution = {
	inherits("global") {
		if (Environment.current == Environment.PRODUCTION) {
			exclude "servlet-api-2.3"
		}
	}
}

fileuploader {
	docs {
		maxSize = 1000 * 1024 * 1
		allowedExtensions = ["xlsx"]
		path = "/tmp/docs/"
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
	data.rootDir = "${userHome}/species/data"

	images.rootDir = "${userHome}/species/images/"
	images.uploadDir = "${userHome}/sravanthi/projects/westernghats/images"
	images.serverURL = "http://localhost/${appName}"


	search {
		serverURL = "http://localhost:8090/solr/species"
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
		indexStore = "${userHome}/species/data/names"
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
		FLOWERS_OF_INDIA_TAXONOMIC_HIERARCHY = 'Flowers of India Taxonomy Hierarchy (2001)'

		COMMON_NAME = "Common Name"
		SYNONYMS = "Synonyms"
		GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY = "Global Distribution Geographic Entity"
		GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY = "Global Endemicity Geographic Entity"
		TAXONOMIC_HIERARCHY = "Taxonomy Hierarchy"
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
		REFERENCES = "References"
		TAXONRECORDID = "TaxonRecordID"
		GLOBALUNIQUEIDENTIFIER = "GlobalUniqueIdentifier"
		NOMENCLATURE_AND_CLASSIFICATION = "Nomenclature and Classification"
		TAXON_RECORD_NAME = "Taxon Record Name"


		CONCEPT = "concept"
		CATEGORY = "category"
		SUBCATEGORY = "subcategory"
		IMAGES = "images"
		ICONS = "icons"
		AUDIO = "audio"
		VIDEO = "video"
	}

	searchFields {
		ID = "id"
		GUID = "guid"
		CONTRIBUTOR = "contributor"
		NAME = "name"
		NAME_EXACT = "name_exact"
		COMMON_NAME = "common_name"
		COMMON_NAME_EXACT = "common_name_exact"
		LOCATION = "location"
		ATTRIBUTION = "attribution"
		REFERENCE = "reference"
		TAXON = "taxon"
		MESSAGE = "text"
		CANONICAL_NAME = "canonical_name"
		CANONICAL_NAME_EXACT = "canonical_name_exact"
		UNINOMIAL = "uninomial"
		UNINOMIAL_EXACT = "uninomial_exact"
		AUTHOR = "author"
		YEAR = "year"
		GENUS = "genus"
		SPECIES = "species"
		INFRASPECIES = "infraspecies"
		INFRAGENUS = "infragenus"
		SYNONYM = "synonym"
		SYNONYM_CANONICAL = "synonym_canonical"
		SCIENTIFIC_NAME = "scientific_name"
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

}

speciesPortal.validCrossDomainOrigins = [
	"localhost",
	"wgp.saturn.strandls.com",
	"wgp.pamba.strandls.com",
	"ibp.saturn.strandls.com",
	"ibp.pamba.strandls.com"
]

//uiperformance.enabled = false

environments {
	development {
		grails.serverURL = "http://localhost:8080/${appName}"
		speciesPortal {
			search.serverURL = "http://localhost:8090/solr/species"

			//uiperformance.enabled = false
		}
		google.analytics.enabled = false
	}
	test {
		grails.serverURL = "http://localhost:8080/${appName}"

		google.analytics.enabled = false
	}
	production {
		grails.serverURL = "http://localhost:8080/${appName}"

		google.analytics.enabled = false
	}

	saturn {
		grails.serverURL = "http://saturn.strandls.com:8080/${appName}"
		speciesPortal {
			images.serverURL = "http://saturn.strandls.com/${appName}"

			search.serverURL="http://saturn.strandls.com:8080/solr/species"
			grails.project.war.file = "/data/jetty-6.1.26/webapps/${appName}.war"
		}
		google.analytics.webPropertyID = "UA-xxxxxx-x"
		google.analytics.enabled = false
		google.analytics.customTrackingCode = [
			[_setDetectFlash: false],
			[_setCampaignCookieTimeout: 31536000000],
			"_trackPageview",
			"_trackPageLoadTime"
		]
	}

	pamba {
		grails.serverURL = "http://pamba.strandls.com:8080/${appName}"
		speciesPortal {
			images.serverURL = "http://pamba.strandls.com/${appName}"

			search.serverURL="http://pamba.strandls.com:8080/solr/species"
		}
		google.analytics.webPropertyID = "UA-xxxxxx-x"
		google.analytics.enabled = false
		google.analytics.customTrackingCode = [
			[_setDetectFlash: false],
			[_setCampaignCookieTimeout: 31536000000],
			"_trackPageview",
			"_trackPageLoadTime"
		]
	}
}

imageConverterProg = "/usr/bin/convert";
jpegOptimProg = "/usr/local/bin/jpegoptim";

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
	[group:'search', order:50, controller:'search', title:'Advanced Search', action:'advSelect'],
	[group:'admin', order:60, controller:'admin', title:'Admin', action:'index']
]

ckeditor  = {
	skipAllowedItemsCheck = false
	defaultFileBrowser = "ofm"
	upload {
		basedir = "/images/resources/"
		overwrite = false
		link {
			browser = true
			upload = false
			allowed = []
			denied = [
				'html',
				'htm',
				'php',
				'php2',
				'php3',
				'php4',
				'php5',
				'phtml',
				'pwml',
				'inc',
				'asp',
				'aspx',
				'ascx',
				'jsp',
				'cfm',
				'cfc',
				'pl',
				'bat',
				'exe',
				'com',
				'dll',
				'vbs',
				'js',
				'reg',
				'cgi',
				'htaccess',
				'asis',
				'sh',
				'shtml',
				'shtm',
				'phtm'
			]
		}
		image {
			browser = true
			upload = true
			allowed = ['jpg', 'gif', 'jpeg', 'png']
			denied = []
		}
		flash {
			browser = false
			upload = false
			allowed = ['swf']
			denied = []
		}
	}
}




