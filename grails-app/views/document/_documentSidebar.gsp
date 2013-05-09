<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>


<div id="project-sidebar" class="span4">


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


        <g:if test="${documentInstance}">
        <div class="sidebar_section" style="overflow:hidden">
            <g:if test="${documentInstance?.tags}">

            <a class="speciesFieldHeader" href="#tags" data-toggle="collapse"><h5>Tags</h5></a>
            <div id="tags" class="speciesField collapse in">
                <table>
                    <tr>
                        <td><g:render template="/project/showTagsList"
                            model="['instance': documentInstance, 'controller': 'document', 'action':'browser']" />
                        </td>
                    </tr>
                </table>

            </div>
            </g:if>
            <g:else>
            <span class="msg" style="padding-left: 50px;">No tags</span>
            </g:else>
            <span class="pull-right"><a href="/document/tagcloud">all
                    tags</a></span>


        </div>

        <g:if test="${documentInstance.userGroups}">
            <div class="sidebar_section">
                <h5>Document is in groups</h5>
                <ul class="tile" style="list-style: none; padding-left: 10px;">
                    <g:each in="${documentInstance.userGroups}" var="userGroup">
                    <li class=""><uGroup:showUserGroupSignature
                    model="[ 'userGroup':userGroup]" /></li>
                    </g:each>
                </ul>

            </div>
        </g:if>
        </g:if>
        <g:else>

	<div class="sidebar_section" style="overflow:hidden">
		<h5>Document Tags</h5>
		<g:if test="${tags}">
			<tc:tagCloud bean="${Document}" controller="document"
				action="browser" sort="${true}" style
				color="${[start: '#084B91', end: '#9FBBE5']}"
				size="${[start: 12, end: 30, unit: 'px']}" paramName='tag' />

			<span class="pull-right"><a href="/document/tagcloud">all
					tags</a></span>
		</g:if>
		<g:else>
			<span class="msg" style="padding-left: 50px;">No tags</span>
		</g:else>
	</div>
        </g:else>


</div>

