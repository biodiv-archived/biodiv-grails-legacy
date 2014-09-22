<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>


<div id="project-sidebar" class="span4">
    <g:if test="${params.action == 'browser'}">
    	<uGroup:objectPostToGroupsWrapper 
			model="[canPullResource:canPullResource, 'objectType':Document.class.canonicalName]" />
    </g:if>
	<div class="sidebar_section">
            <h5><g:message code="documentsidebar.document.manager" /> <sup><g:message code="msg.beta" /></sup></h5>

                <p class="tile" style="margin:0px; padding:5px;">
		<g:message code="text.developed.beta.version" /><a
			href="http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html"
			target="_blank"><g:message code="link.metadata" /></a>.<g:message code="text.eml.standard" />  <a href="http://www.dataone.org/" target="_blank"><g:message code="link.dataone" /></a> <g:message code="text.we.welcome" />
		
                </p>
                
	</div>



	<g:if
		test="${userGroupInstance && userGroupInstance.name.equals('The Western Ghats')}">


		<ul class="nav nav-tabs sidebar" id="project-menus">
			<li><a href="/project/list"><g:message code="link.cepf.projects" /></a></li>
			<li><a href="/document/browser"><g:message code="heading.browse.documents" /> </a></li>

		</ul>

	</g:if>

  	<g:if test="${!documentInstance}">
      	<%
				params.offset = 0	
		%>
		<div class="sidebar_section" style="overflow:hidden">
			<h5><g:message code="documentsidebar.document.tags" /></h5>
			<project:showTagsCloud model="[tagType:'document', showMoreTagPageLink:uGroup.createLink(controller:'document', action:'tagcloud', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)]"></project:showTagsCloud>
		</div>
    </g:if>
        
</div>

