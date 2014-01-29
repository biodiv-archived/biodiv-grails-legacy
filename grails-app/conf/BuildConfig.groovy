grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/${appName}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
        excludes 'xml-apis', 'xercesImpl', 'xmlParserAPIs', 'hibernate', 'stax-api', 'lucene-spellchecker', 'lucene-analyzers'

    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "https://repository.jboss.org/nexus/content/groups/public"
        mavenRepo "http://repo.marketcetera.org/maven/"
        mavenRepo "http://maven.restlet.org/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.


        // runtime 'mysql:mysql-connector-java:5.1.13'
        compile ('org.apache.solr:solr-solrj:4.4.0') {
            excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j'
        }
        if (Environment.current == Environment.DEVELOPMENT) {
                //compile ('org.apache.solr:solr-core:3.6.0') {
                compile ('org.apache.solr:solr-core:4.4.0') {
                    excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j', 'geronimo-stax-api_1.0_spec'
                }
        }
        
       compile 'org.restlet.jee:org.restlet:2.1.1'
       compile 'org.restlet.jee:org.restlet.ext.servlet:2.1.1'

	compile ('org.quartz-scheduler:quartz:1.8.4') {
            excludes 'slf4j-api', 'jcl-over-slf4j'
        }
        compile 'org.apache.lucene:lucene-analyzers:3.4.0'
        compile 'org.apache.lucene:lucene-spellchecker:3.4.0'
        compile group:'org.apache.poi', name:'poi', version:'3.7'
        compile group:'org.apache.poi', name:'poi-contrib', version:'3.6'
        compile group:'org.apache.poi', name:'poi-scratchpad', version:'3.7'
        compile(group:'org.apache.poi', name:'poi-ooxml', version:'3.7') {
            excludes 'geronimo-stax-api_1.0_spec'
        }
        compile ('org.springframework.social:spring-social-facebook:1.0.0.RELEASE') {
            excludes 'spring-web' 
            //'org.springframework.web-3.0.5.RELEASE', 'spring-aop-3.0.3.RELEASE', 'spring-beans-3.0.3.RELEASE','spring-core-3.0.3.RELEASE', 'spring-tx-3.0.3.RELEASE','spring-asm-3.0.3.RELEASE','spring-context-3.0.3.RELEASE', 'spring-expression-3.0.3.RELEASE','spring-web-3.0.3.RELEASE'
        }
        compile('net.sf.opencsv:opencsv:2.3')
        compile('com.itextpdf:itextpdf:5.0.6')
        /*   compile('org.hibernatespatial:hibernate-spatial:1.0') {
             excludes 'slf4j-api', 'jcl-over-slf4j'
        }

        compile('org.hibernatespatial:hibernate-spatial-postgis:1.0') {
        excludes 'slf4j-api', 'jcl-over-slf4j'
        }
        compile ('org.hibernate:hibernate-core:3.3.2.GA'){
        excludes 'ehcache', 'xml-apis', 'commons-logging'
        }
         */

        runtime 'postgresql:postgresql:9.0-801.jdbc4'
        runtime ('org.postgis:postgis-jdbc:1.3.3') {
            exclude 'postgresql'
        }

        //runtime 'postgresql:postgis:2.0.0SVN' {
        //  runtime 'postgresql:postgresql:8.3-603.jdbc4'
        //}

        compile 'org.imgscalr:imgscalr-lib:4.2'
        compile 'org.apache.sanselan:sanselan:0.97-incubator'
        compile 'jmimemagic:jmimemagic:0.1.2'
        
        //compile 'net.sf.jtidy:jtidy:r938'
    }
    plugins { 
/*        compile ":resources:1.1.6" 
        compile ":spring-security-core:1.2.7.3" 
        compile ":spring-security-ui:0.2" 
        compile ":webxml:1.4.1" 
        compile ':plugin-config:0.1.5'
  */  } 

    grails.war.resources = { stagingDir ->
        /*for (name in ['log4j', 'slf4j', 'jcl-over-slf4j', 'jul-to-slf4j']) {
            delete {
                fileset dir: "$stagingDir/WEB-INF/lib/",
                includes: "$name*.jar"
            }
        }*/
        //        delete(file:"${stagingDir}/WEB-INF/lib/hibernate-core-3.3.1.GA.jar")
    }
}
