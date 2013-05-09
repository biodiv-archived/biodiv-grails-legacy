<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>


<div id="project-sidebar" class="span4"	class="pull-right" style=" position: relative; margin: 0px; padding: 10px 15px 10px 30px; width: 265px;">


	<ul class="nav nav-tabs sidebar" id="project-menus">
		<li><a href="/project/list">Western Ghats CEPF Projects</a></li>
		<li><a href="/document/browser">Browse Documents</a></li>

	</ul>

	<%
		params.offset = 0
	%>
	<div style="width: 235px; margin:30px 0px;">
		<h4>Project Tags</h4>
		<g:if test="${tags}">
			<tc:tagCloud bean="${Project}" controller="project" action="list"
				sort="${true}" style color="${[start: '#084B91', end: '#9FBBE5']}"
				size="${[start: 12, end: 30, unit: 'px']}" paramName='tag' />

			<span class="pull-right"><a href="/project/tagcloud">more
					tags</a></span>
		</g:if>
		<g:else>
			<span class="msg" style="padding-left: 50px;">No tags</span>
		</g:else>
	</div>


	<div style="width: 235px; max-height: 350px; margin:30px 0px;">
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

