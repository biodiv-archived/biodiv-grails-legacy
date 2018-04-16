grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.target.level = 1.8
grails.project.source.level = 1.8

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
run: [maxMemory: 3072, minMemory: 2048, debug: false, maxPerm: 256, forkReserve:false, jvmArgs:["-Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator"]],
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
        excludes 'ehcache'
        excludes 'xml-apis', 'xercesImpl', 'xmlParserAPIs', 'stax-api', 'lucene-spellchecker', 'lucene-analyzers', 'bcprov-jdk14'

    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    //TODO verify if you need this setting of legacyResolve
//    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility


    repositories {  
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenCentral()
        //mavenRepo "http://repository.codehaus.org"
        mavenRepo "https://repo.grails.org/grails/plugins/" 
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo 'http://download.osgeo.org/webdav/geotools'
        mavenRepo 'http://www.hibernatespatial.org/repository'
        mavenRepo "http://repository.jboss.com/maven2/"
        //mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "https://repository.jboss.org/nexus/content/groups/public"
        //mavenRepo "http://repo.desirableobjects.co.uk/"
        mavenRepo "http://mvnrepository.com/artifact/"
        mavenRepo "http://repo.spring.io/milestone/"
        mavenRepo "http://repo1.maven.org/maven2"

        mavenRepo "http://repo.marketcetera.org/maven/"
        mavenRepo "http://maven.restlet.org/"
        //mavenRepo "http://localhost:8081/artifactory/plugins-releases-local/"
        mavenRepo "https://repository.apache.org/content/repositories/releases/"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        /*compile ('org.apache.solr:solr-solrj:4.10.0') {
            excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j'
        }*/
        compile group: 'org.apache.lucene', name: 'lucene-core', version: '4.10.0'
        compile group: 'org.apache.lucene', name: 'lucene-analyzers-common', version: '4.10.0'
        compile group: 'org.apache.lucene', name: 'lucene-suggest', version: '4.10.0'

/*        if (Environment.current == Environment.DEVELOPMENT) {
            compile ('org.apache.solr:solr-core:4.10.0') {
                excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j', 'geronimo-stax-api_1.0_spec', 'hadoop-hdfs', 'hadoop-auth', 'hadoop-annotations', 'hadoop-common'
            }
        }
*/
        compile 'org.restlet.jee:org.restlet:2.1.1'
        compile 'org.restlet.jee:org.restlet.ext.servlet:2.1.1'

        compile ('org.quartz-scheduler:quartz:2.1.7') {
            excludes 'slf4j-api', 'jcl-over-slf4j', 'c3p0'
        }
        compile (group:'org.apache.poi', name:'poi', version:'3.7'){
            excludes 'servlet-api'
        }
        compile (group:'org.apache.poi', name:'poi-contrib', version:'3.6'){
            excludes 'servlet-api'
        }

        compile group:'org.apache.poi', name:'poi-scratchpad', version:'3.7'
        compile (group:'org.apache.poi', name:'poi-ooxml', version:'3.7') {
            excludes 'geronimo-stax-api_1.0_spec'
        }
        compile ('org.springframework.social:spring-social-facebook:2.0.3.RELEASE') {
            excludes 'spring-web' 
        }
        compile ('net.sf.opencsv:opencsv:2.3')
        compile ('com.itextpdf:itextpdf:5.0.6'){
            excludes "bcprov-jdk14"
        }

	    compile "org.hibernate:hibernate-spatial:4.3"
        /*
        compile ("org.hibernatespatial:hibernate-spatial-postgis:1.1") {
            excludes 'hibernate-core', 'javassist'
        }*/

        compile 'org.imgscalr:imgscalr-lib:4.2'
        compile 'org.apache.sanselan:sanselan:0.97-incubator'
        compile 'jmimemagic:jmimemagic:0.1.2'
        runtime ('org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4:1.16') {
            excludes 'slf4j-log4j12', 'slf4j-api', 'jcl-over-slf4j'
        }
        compile 'com.esotericsoftware:kryo-shaded:3.0.3'

        runtime "org.pac4j:pac4j-jwt:2.1.0"
        compile "org.pac4j:pac4j-oauth:2.1.0"
        compile "com.zaxxer:HikariCP:3.0.0" 
        compile "com.github.debop:hibernate-redis:2.3.2", {
            excludes "hibernate-core", "hibernate-entitymanager", "logback-classic"
        }

  	    compile "com.bedatadriven:jackson-datatype-jts:2.4"
    }

    plugins { 
        build ":tomcat8:8.0.5"

        compile ":scaffolding:2.1.2"
        compile group: 'org.grails.plugins', name: 'platform-core', version: '1.0.0'
        //TODO enable this plugin
        compile (':cache:1.1.8') {
                excludes "servlet-api" 
        }
//        compile "org.grails.plugins:cache-ehcache:1.0.5"
//        runtime ":database-migration:1.4.0"

        //runtime (':hibernate:3.6.10.16') {
        runtime (":hibernate4:4.3.5.5" ){// or ":hibernate:3.6.10.17" 
            //excludes 'ehcache-core'
        }

//        runtime ':hibernate-spatial:0.0.4'
//        runtime ':hibernate-spatial-postgresql:0.0.4'
//        runtime ":resources:1.2.8"
        compile ":spring-security-core:2.0-RC3" 
        //compile ":spring-security-core:1.2.7.3" 
        compile "org.grails.plugins:spring-security-acl:2.0.1"

        compile (":spring-security-rest:1.5.4") {
                excludes 'spring-security-core', 'cors','pac4j-cas'
        }

        compile (":spring-security-facebook:0.15.2-CORE2") {
            excludes 'spring-web' 
        } 


        compile ":spring-security-openid:2.0-RC2"
        compile ":spring-security-ui:1.0-RC2"

        compile ':spring-security-oauth-google:0.3.1'

        runtime ":webxml:1.4.1" 
        compile ':plugin-config:0.1.8'
        //compile ":error-pages-fix:0.2"
        compile ":ajax-uploader:1.1"
        compile ":cache-headers:1.1.7"
//        runtime ":cached-resources:1.0"
        compile ":ckeditor:4.5.4.1"
        compile ':scaffolding:2.1.0' 
        compile (':email-confirmation:2.0.8') {
            excludes 'platform-core'
        }
        //        compile ":famfamfam:1.0.1"
        //        compile ":google-analytics:2.1.1"
        compile ":google-visualization:0.6.2"
		//compile ":grails-melody:1.59.0"
        compile ":jcaptcha:1.5.0"
        runtime ":jquery:1.11.1"
        compile ":jquery-ui:1.10.3"
        compile (":mail:1.0.7")
        compile ":quartz:1.0.2"
        compile ":rateable:0.7.1"
        //      compile ":recaptcha:0.5.2"
        //compile ':recaptcha:1.6.0'
        compile ":rest:0.8"
        compile ":tagcloud:0.3"
        compile ":taggable:1.0.1"
        //runtime ":yui-minify-resources:0.1.5"
//        runtime ":zipped-resources:1.0"
        compile ":grails-ant:0.1.3"
        compile "org.grails.plugins:twitter-bootstrap:2.3.2.2"
        compile "org.grails.plugins:asset-pipeline:2.7.4"
        //compile ":redis:1.6.6"
        //compile "org.grails.plugins:app-info:1.1.1"
        //compile "org.grails.plugins:app-info-hibernate:0.4.1"
        //compile "org.grails.plugins:profiler:0.5"
        runtime ":cors:1.3.0"
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

    /*grails.tomcat.jvmArgs = ["-server", "-XX:MaxPermSize=512m", "-XX:MaxNewSize=256m", "-XX:NewSize=256m",
    "-Xms2G", "-Xmx3G", "-XX:SurvivorRatio=128", "-XX:MaxTenuringThreshold=0",
    "-XX:+UseTLAB", "-XX:+UseConcMarkSweepGC", "-XX:+CMSClassUnloadingEnabled",
    "-XX:+CMSIncrementalMode", "-XX:-UseGCOverheadLimit", "-XX:+ExplicitGCInvokesConcurrent", "-Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator"]*/

    grails.tomcat.jvmArgs = ["-server", "-noverify", "-XX:PermSize=256m", "-XX:MaxPermSize=256m", "-Xmx3G", "-Xms1024M", "-XX:+UseParallelGC", "-Djava.net.preferIPv4Stack=true", "-Dsun.reflect.inflationThreshold=100000", "-Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator"]
}

development{
    grails.server.port.http=8080
}
