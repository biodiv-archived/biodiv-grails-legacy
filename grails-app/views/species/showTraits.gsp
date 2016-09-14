<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'facts.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style>
        .super-section{background-color:white;}
        .super-section h5{text-transform: uppercase;}
        </style>
    </head>
    <body>
    <div class="container">
    <div id="content" class="super-section">
	   <h5>${trait.name}</h5>
	    	<table class="table">
	    		<g:if test="${trait.description}">
		    	<tr><td><h6>Description</h6></td>
		    	<td>${trait.description}</td></tr>
		    	</g:if>
	    	<tr>
	    		<tr><td><h6>Values</h6></td>
	    		<td>
		    		<ul>
		    		<% traitValue=trait.values?.tokenize("|")%>
		    			<g:each in='${traitValue}' var="values">
		        		<li> ${values}</li>
		        		</g:each>
		        	</ul>
		    		</td>
		    		</tr>
		    	<g:if test="${trait.icon}">
		    	<tr><td><h6>Icon</h6></td>
		    	<td>${trait.icon}</td></tr>
		    	</g:if>

		    	
		    	<g:if test="${trait.traitTypes}">
		    	<tr><td><h6>Trait Type</h6></td>
		    	<td>${trait.traitTypes}</td></tr>
		    	</g:if>

		    	<g:if test="${trait.dataTypes}">
		    	<tr><td><h6>Data Type</h6></td>
		    	<td>${trait.dataTypes}</td></tr>
		    	</g:if>

		    	<g:if test="${trait.field}">
		    	<tr><td><h6>Species Field</h6></td>
		    	<td>${field}</td></tr>
		    	</g:if>

		    	<g:if test="${trait.taxonomyDefinition}">
		    	<tr><td><h6>Coverage</h6></td>
		    	<td>${coverage}</td></tr>
		    	</g:if>

		    	<tr>
		    	<td>Species</td>
		    	<td>
		    	<ul>
		    	<g:each var="it" in="${species}" >
		    	<li>
		    	${it}
		    	</li>
		    	</g:each>
				</ul>
		    	</td>
		    	</tr>

		    	</table>
    </div>
    </div>	
    </body>
</html>