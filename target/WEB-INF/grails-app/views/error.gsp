<html>
  <head>
	  <title>Error</title>
	<g:javascript src="species/util.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
	<meta name="layout" content="main" />
  </head>

  <body>
  <div class="container_16">
    
    <div class="grid_16 ui-state-error">
    
	<p class="message">
		Oops!!! There seems to be some problem. <br/>
		Please mail us the following message as bug report here <span
					class="mailme">team(at)thewesternghats(dot)in</span><br/><br/>
					
		
		
					
					
	</p>
	<div class="ui-state-error-text">
	<strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
	</div>
  	<!-- div class="message ">
		<strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
		<strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
	  		<strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
	  		<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
	  		<strong>Class:</strong> ${exception.className} <br />
	  		<strong>At Line:</strong> [${exception.lineNumber}] <br />
	  		<strong>Code Snippet:</strong><br />
	  		<div class="snippet">
	  			<g:each var="cs" in="${exception.codeSnippet}">
	  				${cs?.encodeAsHTML()}<br />
	  			</g:each>
	  		</div>
		</g:if>
  	</div>
	<g:if test="${exception}">
	    <h2>Stack Trace</h2>
	    <div class="stack">
	      <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
	    </div>
	</g:if>
	</div-->
	</div>
  </body>
</html>