eventConfigureTomcat = {tomcat ->
	def ctx=tomcat.host.findChild(serverContextPath)
	ctx.allowLinking = true
}
