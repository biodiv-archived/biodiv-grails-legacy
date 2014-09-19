grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/${appName}.war"

grails.project.fork = [
// configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
//  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

// configure settings for the test-app JVM, uses the daemon by default
test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
// configure settings for the run-app JVM
run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false, jvmArgs:["-Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator"]],
// configure settings for the run-war JVM
war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false, jvmArgs:["-Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator"]],
// configure settings for the Console UI JVM
console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]


grails.project.dependency.resolver="maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
        excludes 'xml-apis', 'xercesImpl', 'xmlParserAPIs', 'stax-api', 'lucene-spellchecker', 'lucene-analyzers'

    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    //TODO verify if you need this setting of legacyResolve
//    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility


    repositories {  
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo 'http://download.osgeo.org/webdav/geotools'
        mavenRepo 'http://www.hibernatespatial.org/repository'
        mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "https://repository.jboss.org/nexus/content/groups/public"
        mavenRepo "http://repo.desirableobjects.co.uk/"
        mavenRepo "http://mvnrepository.com/artifact/"
        mavenRepo "http://repo.spring.io/milestone/"
        mavenRepo "http://repo1.maven.org/maven2"

        mavenRepo "http://repo.marketcetera.org/maven/"
        mavenRepo "http://maven.restlet.org/"
        //mavenRepo "http://localhost:8081/artifactory/plugins-releases-local/"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        compile ('org.apache.solr:solr-solrj:4.10.0') {
            excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j'
        }

        if (Environment.current == Environment.DEVELOPMENT) {
            compile ('org.apache.solr:solr-core:4.10.0') {
                excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j', 'geronimo-stax-api_1.0_spec', 'hadoop-hdfs', 'hadoop-auth', 'hadoop-annotations', 'hadoop-common'
            }
        }

        compile 'org.restlet.jee:org.restlet:2.1.1'
        compile 'org.restlet.jee:org.restlet.ext.servlet:2.1.1'

        compile ('org.quartz-scheduler:quartz:2.1.7') {
            excludes 'slf4j-api', 'jcl-over-slf4j', 'c3p0'
        }
//        compile 'org.apache.lucene:lucene-analyzers:3.4.0'
//        compile 'org.apache.lucene:lucene-spellchecker:3.4.0'
        compile (group:'org.apache.poi', name:'poi', version:'3.7'){
            excludes 'servlet-api'
        }
        compile (group:'org.apache.poi', name:'poi-contrib', version:'3.6'){
            excludes 'servlet-api'
        }

        compile group:'org.apache.poi', name:'poi-scratchpad', version:'3.7'
        compile(group:'org.apache.poi', name:'poi-ooxml', version:'3.7') {
            excludes 'geronimo-stax-api_1.0_spec'
        }
        compile ('org.springframework.social:spring-social-facebook:1.0.0.RELEASE') {
            excludes 'spring-web' 
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
        compile ("org.hibernatespatial:hibernate-spatial-postgis:1.1") {
            excludes 'hibernate-core', 'javassist'
        }

        runtime 'postgresql:postgresql:9.0-801.jdbc4'
        //runtime 'postgresql:postgresql:8.4-702.jdbc4'
        /*        runtime ('org.postgis:postgis-jdbc:1.3.3') {
                  exclude 'postgresql'
        }
         */
        compile 'org.imgscalr:imgscalr-lib:4.2'
        compile 'org.apache.sanselan:sanselan:0.97-incubator'
        compile 'jmimemagic:jmimemagic:0.1.2'
        //compile 'net.sf.jtidy:jtidy:r938'
        compile 'com.mchange:c3p0:0.9.5-pre6'
        runtime ('org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4:1.16') {
            excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j'
        }
    }

    plugins { 
        build   ":tomcat:7.0.52.1"
        //build ":tomcat8:8.0.5"
        //build ':jetty:2.0.3'

        compile ":scaffolding:2.0.1"
        //TODO enable this plugin
        compile (':cache:1.1.6') {
                excludes "servlet-api" 
        }
//        runtime ":database-migration:1.4.0"

        compile ':hibernate:3.6.10.15'
        compile ':hibernate-spatial:0.0.4'
        compile ':hibernate-spatial-postgresql:0.0.4'
        runtime ":resources:1.2.8"
        compile ":spring-security-core:2.0-RC3" 
        //compile ":spring-security-core:1.2.7.3" 
        compile ":spring-security-acl:2.0-RC1"
        compile (":spring-security-facebook:0.15.2-CORE2") {
            excludes 'spring-web' 
        } 

        compile ":spring-security-openid:2.0-RC2"
        compile ":spring-security-ui:1.0-RC2"

        compile (":spring-security-rest:1.3.4") {
                excludes 'spring-security-core', 'cors'
        }

        runtime ":webxml:1.4.1" 
        compile ':plugin-config:0.1.8'
        //compile ":error-pages-fix:0.2"
        compile ":ajax-uploader:1.1"
        compile ":cache-headers:1.1.5"
        runtime ":cached-resources:1.0"
        compile ":ckeditor:3.6.3.0"
        compile ':scaffolding:2.0.3' 
        compile ':email-confirmation:1.0.5'
        //        compile ":famfamfam:1.0.1"
        //        compile ":google-analytics:2.1.1"
        compile ":google-visualization:0.6.2"
        //        compile ":grails-melody:1.47.2"
        compile ":jcaptcha:1.2.1"
        runtime ":jquery:1.11.1"
        compile ":jquery-ui:1.10.3"
        compile (":mail:1.0.7")
        compile ":quartz:1.0.2"
        compile ":rateable:0.7.1"
        //      compile ":recaptcha:0.5.2"
        compile ":rest:0.8"
        compile ":tagcloud:0.3"
        compile ":taggable:1.0.1"
        runtime ":yui-minify-resources:0.1.5"
        runtime ":zipped-resources:1.0"
    
    } 

    grails.war.resources = { stagingDir ->
        for (name in ['servlet-api-2.3', 'jsp-api-2.1']) {
          delete {
          fileset dir: "$stagingDir/WEB-INF/lib/",
          includes: "$name*.jar"
          }
          }
        //        delete(file:"${stagingDir}/WEB-INF/lib/hibernate-core-3.3.1.GA.jar")
    }

    grails.tomcat.jvmArgs = ["-server", "-XX:MaxPermSize=512m", "-XX:MaxNewSize=256m", "-XX:NewSize=256m",
    "-Xms768m", "-Xmx1024m", "-XX:SurvivorRatio=128", "-XX:MaxTenuringThreshold=0",
    "-XX:+UseTLAB", "-XX:+UseConcMarkSweepGC", "-XX:+CMSClassUnloadingEnabled",
    "-XX:+CMSIncrementalMode", "-XX:-UseGCOverheadLimit", "-XX:+ExplicitGCInvokesConcurrent", "-Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator"]
}
