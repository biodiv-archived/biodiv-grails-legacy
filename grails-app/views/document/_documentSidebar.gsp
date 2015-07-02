<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>
<%@ page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>



<div id="project-sidebar" class="span4">
    <g:if test="${params.action == 'browser'}">
    	<uGroup:objectPostToGroupsWrapper 
			model="[canPullResource:canPullResource, 'objectType':Document.class.canonicalName]" />
    </g:if>
    <div id="taxonBrowser">
        <div class="taxonomyBrowser sidebar_section" data-name="classification" data-speciesid="${speciesInstance?.id}" style="position:relative">
            <h5><g:message code="button.classifications" /></h5>	
            <div id="taxaHierarchy">

                <%
                def classifications = [];
                Classification.list().each {
                classifications.add([it.id, it, null]);
                }
                classifications = classifications.sort {return it[1].name}; 
                %>

                <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, 'expandAll':false]"/>
            </div>
        </div>
    </div>


	<!--div class="sidebar_section">
            <h5><g:message code="documentsidebar.document.manager" /> <sup><g:message code="msg.beta" /></sup></h5>

                <p class="tile" style="margin:0px; padding:5px;">
		<g:message code="text.developed.beta.version" /><a
			href="http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html"
			target="_blank"><g:message code="link.metadata" /></a>.<g:message code="text.eml.standard" />  <a href="http://www.dataone.org/" target="_blank"><g:message code="link.dataone" /></a> <g:message code="text.we.welcome" />
		
                </p>
                
	</div-->



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
        <script type="text/javascript">
            var taxonRanks = [];
            <g:each in="${TaxonomyRank.list()}" var="t">
            taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
            </g:each>
            </script>

            <r:script>

            $(document).ready(function() {
            var taxonBrowserOptions = {
            expandAll:false,
            controller:"${params.controller?:'document'}",
            action:"${params.action?:'browser'}",
            expandTaxon:"${params.taxon?true:false}"
            }
            if(${params.taxon?:false}){
            taxonBrowserOptions['taxonId'] = "${params.taxon}";
            }
            var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	
            });

            </r:script>


