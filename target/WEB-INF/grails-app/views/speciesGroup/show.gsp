
<%@ page import="species.groups.SpeciesGroup" %>
<%@ page import="species.Species" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${fieldValue(bean: speciesGroupInstance, field:'name')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
         <div class="container_12">

		<div class="grid_12">
            <h1>
				${entityName}
			</h1>
			
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            
            <div class="list">
                <table>
                     <tbody>
                    	<g:each in="${speciesGroupInstance.taxonConcept}" status="i" var="taxonomyDefinitionInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                        	
	                        	<td>${taxonomyDefinitionInstance.italicisedForm}</td>
	                        	
	                        	<g:set var="species" value="${Species.findAllByTaxonConcept(taxonomyDefinitionInstance)}" />
	                        	
	                            
	                        </tr>
	                        <g:if test="${species}">
	                        	<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                        		<g:each in="${species}" var="s">
	                            		<td>&nbsp;&gt;&nbsp;<g:link controller="species" action="show" id="${s.id}">${s.title}</g:link></td>
	                            	</g:each>
	                            <tr/>
	                        </g:if>
                    	</g:each>
                    </tbody>
                </table>
            </div>
           
        </div>
        </div>
    </body>
</html>
