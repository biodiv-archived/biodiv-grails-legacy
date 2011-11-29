
<%@ page import="species.SpeciesGroup" %>
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
                        	<g:set var="species" value="${Species.findByTaxonConcept(taxonomyDefinitionInstance)}" />
                        	<g:if test="${species}">
                            	<td><g:link controller="species" action="show" id="${species.id}">${taxonomyDefinitionInstance.italicisedForm}</g:link></td>
                            </g:if>
                            <g:else>
                            	<td>${taxonomyDefinitionInstance.italicisedForm}</td>
                            </g:else>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
           
        </div>
        </div>
    </body>
</html>
