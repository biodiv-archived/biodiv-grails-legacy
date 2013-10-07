<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>


<div id="project-sidebar">


	<div class="sidebar_section">
            <h5>Document Manager <sup>Beta</sup></h5>

                <p class="tile" style="margin:0px; padding:5px;">
		We have deployed a beta version of the document manager to facilitate
		sharing ecological datasets and documents with a <a
			href="http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html"
			target="_blank">eml-2.1.1 metadata</a>. The eml standard has been
		developed by the ecology discipline and for the ecology discipline.
		The metadata can be shared and searched globally and has been adopted
		by the global <a href="http://www.dataone.org/" target="_blank">DataOne</a>
		project. We welcome comments and suggestions from users to further
                develop this function in the portal.
                </p>
                
	</div>



	<g:if
		test="${userGroupInstance && userGroupInstance.name.equals('The Western Ghats')}">


		<ul class="nav nav-tabs sidebar" id="project-menus"">
			<li><a href="/project/list">Western Ghats CEPF Projects</a></li>
			<li><a href="/document/browser">Browse Documents</a></li>

		</ul>

	</g:if>

  	<g:if test="${!documentInstance}">
      	<%
				params.offset = 0	
		%>
		<div class="sidebar_section" style="overflow:hidden">
			<h5>All Document Tags</h5>
			<project:showTagsCloud model="[tagType:'document', showMoreTagPageLink:uGroup.createLink(controller:'document', action:'tagcloud', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)]"></project:showTagsCloud>
		</div>
    </g:if>
        
</div>

