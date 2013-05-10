<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>


<div id="project-sidebar" class="span4">

    <div class="sidebar_section">

        <ul class="nav nav-tabs" id="project-menus" style="margin-bottom:0px;">
		<li><a href="/project/list">Western Ghats CEPF Projects</a></li>
		<li><a href="/document/browser">Browse Documents</a></li>

            </ul>
    </div>

	<%
		params.offset = 0
	%>
	<div class="sidebar_section" style="overflow:hidden;">
		<h5>Project Tags</h5>
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


	<div class="sidebar_section" style="overflow:hidden;">
		<h5>Document Tags</h5>
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

