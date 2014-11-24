<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>


<div id="project-sidebar" class="span4">

    <div class="sidebar_section">

        <ul class="nav nav-tabs" id="project-menus" style="margin-bottom:0px;">
		<li><a href="/project/list"><g:message code="button.add.cepf.projects" /></a></li>
		<li><a href="/document/browser"><g:message code="heading.browse.documents" /></a></li>

            </ul>
    </div>
	<%
		params.offset = 0
	%>
	<div class="sidebar_section" style="overflow:hidden;">
		<h5><g:message code="heading.project.tags" /> </h5>
		<project:showTagsCloud model="[tagType:'project', showMoreTagPageLink:uGroup.createLink(controller:'project', action:'tagcloud', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)]"></project:showTagsCloud>
	</div>
</div>

