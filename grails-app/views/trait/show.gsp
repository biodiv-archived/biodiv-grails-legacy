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

		    	<tr><td><h6>Values</h6></td>
		    	<td>
		    	<g:each var="it" in="${traitValue}" status='i' >
		    		${it}</br>
		    	</g:each>
		    	</td>
		    	</tr>

		    	<g:if test="${trait.icon}">
		    	<tr><td><h6>Icon</h6></td>
		    	<td>${trait.icon}</td></tr>
		    	</g:if>

		    	
		    	<g:if test="${trait.traitTypes}">
		    	<tr><td><h6>Trait Type</h6></td>
		    	<td>${trait?.traitTypes.toString().replaceAll('_',' ')}</td></tr>
		    	</g:if>

		    	<g:if test="${trait.dataTypes}">
		    	<tr><td><h6>Data Type</h6></td>
		    	<td>${trait.dataTypes}</td></tr>
		    	</g:if>

		    	<g:if test="${trait.field}">
		    	<tr><td><h6>Species Field</h6></td>
		    	<td>${field}</td></tr>
		    	</g:if>

		    	<g:if test="${trait.taxon}">
		    	<tr><td><h6>Coverage</h6></td>
		    	<td>${coverage}</td></tr>
		    	</g:if>

		    	<tr>
		    	<td><h6>Species</h6></td>
		    	<td>
		    	<div class="pre-scrollable" style="max-height:300px;clear: both;overflow-x:hidden;">
		       	<g:each var="it" in="${species}" status='i' >
		    		<li><a href="/species/show/${it.objectId}" >${it.pageTaxon.name}</a></li>		    	
		    	</g:each>
		    	</div>
		    	</td>
		    	</tr>
		    	</table>
    </div>
    </div>	
    </body>
</html>