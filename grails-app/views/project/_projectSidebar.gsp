<%@ page import="content.Project"%>
<%@ page import="content.fileManager.UFile"%>


<div id="project-sidebar"
	style="position: relative; top: 50px; margin: 10px; padding: 10px 15px 10px 15px;"
	class="pull-right">

	<sUser:isCEPFAdmin>

		<a class="btn btn-info" style="margin-bottom:5px;"
			href="${uGroup.createLink(
						controller:'project', action:'create', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress)}">
			<i class="icon-plus"></i>Add CEPF Project
		</a>
		<br>

	</sUser:isCEPFAdmin>
	

	<ul class="nav nav-tabs sidebar" id="project-menus">
		<li><a href="/collaborate/partners">Project Partners</a></li>
		<li><a href="/project/list">Western Ghats CEPF Projects</a></li>
		<li><a href="/UFile/browser">Browse Files</a></li>

	</ul>


	<div style="width: 235px;">
		<h3>Project Tags</h3>
		<g:if test="${tags}">
			<tc:tagCloud bean="${Project}" controller="project" action="list" sort="${true}" style
				color="${[start: '#084B91', end: '#9FBBE5']}"
				size="${[start: 12, end: 30, unit: 'px']}" paramName='tag' />

			<span class="pull-right"><a href="/project/tagcloud">more
					tags</a></span>
		</g:if>
		<g:else>
			<span class="msg" style="padding-left: 50px;">No tags</span>
		</g:else>
	</div>


	<div style="width: 235px; max-height: 350px;">
		<h3>File Tags</h3>
		<g:if test="${tags}">
			<tc:tagCloud bean="${UFile}" controller="UFile" action="browser" sort="${true}" style
				color="${[start: '#084B91', end: '#9FBBE5']}"
				size="${[start: 12, end: 30, unit: 'px']}" paramName='tag' />

			<span class="pull-right"><a href="/UFile/tagcloud">more
					tags</a></span>
		</g:if>
		<g:else>
			<span class="msg" style="padding-left: 50px;">No tags</span>
		</g:else>
	</div>

</div>

