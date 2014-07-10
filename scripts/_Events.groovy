
eventConfigureTomcat = {tomcat ->
    println "Configuring embedded tomcat"
    tomcat.getConnector().maxPostSize=5242880;
    println "Setting maxPostSize to 5MB"
}

