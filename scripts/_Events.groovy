

	eventConfigureTomcat = {
		tomcat ->
		String contextFile = "${basedir}/web-app/META-INF/context.xml";
		println contextFile
		if (new File(contextFile).exists()) {
			println "------- CONFIGURING TOMCAT CONTEXT --------"

			def bSettings = grails.util.BuildSettingsHolder.getSettings();
			String tomcatConfDir =
			bSettings.getProjectWorkDir().getAbsolutePath() + "/tomcat/conf";
			ant.copy(file:"${contextFile}", todir:"${tomcatConfDir}");

			println "--- FINISHED CONFIGURING TOMCAT CONTEXT ---"
		}
	}
