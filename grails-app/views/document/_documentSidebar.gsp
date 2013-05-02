<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>


<div id="project-sidebar"
	style="position: relative; margin: 0px; padding: 10px 15px 10px 15px; width:265px;"
	class="pull-right">
	
	
	
	<sec:ifLoggedIn>
		<a class="btn btn-info" style="margin-bottom: 5px;"
			href="${uGroup.createLink(
						controller:'document', action:'create', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress)}">
			<i class="icon-plus"></i>Add Document
		</a>
		<br>

	</sec:ifLoggedIn>
	
		<div style="margin-bottom: 10px;
border: 1px solid green;
padding: 5px;">
	<h4>Document Manager in Beta</h4>

We have deployed a beta version of the document manager to facilitate sharing ecological datasets and documents with a <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html" target="_blank">eml-2.1.1 metadata</a>. The eml standard has been developed by the ecology discipline and for the ecology discipline. The metadata can be shared and searched globally and has been adopted by the global <a href="http://www.dataone.org/" target="_blank">DataOne</a> project. We will comments and suggestions from users to further develop this function in the portal. 
	</div>



						<g:if test="${userGroupInstance && userGroupInstance.name.equals('The Western Ghats')}">


	<ul class="nav nav-tabs sidebar" id="project-menus" ">
		<li><a href="/project/list">Western Ghats CEPF Projects</a></li>
		<li><a href="/document/browser">Browse Documents</a></li>

	</ul>

</g:if>

	<div style="width: 235px; max-height: 350px;">
		<h4>Document Tags</h4>
		<g:if test="${tags}">
			<tc:tagCloud bean="${Document}" controller="document" action="browser"
				sort="${true}" style color="${[start: '#084B91', end: '#9FBBE5']}"
				size="${[start: 12, end: 30, unit: 'px']}" paramName='tag' />

			<span class="pull-right"><a href="/document/tagcloud">more
					tags</a></span>
		</g:if>
		<g:else>
			<span class="msg" style="padding-left: 50px;">No tags</span>
		</g:else>
	</div>

</div>

