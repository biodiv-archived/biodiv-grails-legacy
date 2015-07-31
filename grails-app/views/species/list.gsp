<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>

<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'species', action:'list', base:Utils.getIBPServerDomain()])}" />
<g:set var="title" value="${g.message(code:'showobservationstoryfooter.title.species')}"/>
<g:render template="/common/titleTemplate" model="['title':title, 'description':'', 'canonicalUrl':canonicalUrl, 'imagePath':'']"/>

<r:require modules="species"/>
<r:require modules="species_list" />

</head>
<body>
	<div class="span12">
      <%
    def species_title=g.message(code:'default.species.label')
    %>

		<s:showSubmenuTemplate model="['entityName':species_title]" />
                    <uGroup:rightSidebar/>
                    <obv:featured
            model="['controller':params.controller, 'action':'related', 'filterProperty': 'featureBy', 'filterPropertyValue': true, 'id':'featureBy', 'userGroupInstance':userGroupInstance]" />
            <h4><g:message code="species.list.browse.species" /></h4>

		<div class="tabbable" style="margin-left:0px;clear:both;">
			<ul class="nav nav-tabs species-list-tabs" style="margin-bottom: 0px">
				<li class="active"><a href="#list" ><g:message code="button.gallery" /></a>
				</li>
				<li><a href="#contribute"><g:message code="button.contribute" /></a>
				</li>

			</ul>

			<div class="tab-content">
				<div id="list" class="tab-pane active">
						<s:speciesFilter></s:speciesFilter>
						<% /*
                        <sUser:isAdmin>
							<s:showDownloadAction model="['source':'Species', 'requestObject':request ]" />
						</sUser:isAdmin>
                        */%>
	
				<obv:download
					model="['source':'Species', 'requestObject':request, 'downloadTypes':[DownloadType.DWCA], 'onlyIcon': 'false', 'downloadFrom' : 'speciesList']" />


						<div class="list span8 right-shadow-box" style="margin: 0px;">
							<s:showSpeciesList/>
						</div>
                        <div id="taxonBrowser" class="span4" style="position:relative">

						    <uGroup:objectPostToGroupsWrapper model="['objectType':Species.class.canonicalName, canPullResource:canPullResource]"/>
                            <div class="taxonomyBrowser sidebar_section" data-name="classification" data-speciesid="${speciesInstance?.id}" style="position:relative">
                                <h5><g:message code="button.taxon.browser" /></h5>	
                                <div id="taxaHierarchy">

                                    <%
                                    def classifications = [];
                                    Classification.list().each {
                                    classifications.add([it.id, it, null]);
                                    }
                                    classifications = classifications.sort {return it[1].name}; 
                                    %>

                                    <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, selectedClassification:queryParams.classification, 'expandAll':false]"/>
                                </div>
                            </div>
                            <!--div class="section help-block"> 
                                <ul>
                                    <li>
                                    <g:message code="text.reasearcher.procedure" /> <span class="mailme">${grailsApplication.config.speciesPortal.ibp.supportEmail}</span> <g:message code="text.alloted.rights" />
                                    </li>
                                </ul>
                            </div>

                            <g:render template="/species/inviteForContribution"/>
                            -->

                            <g:render template="/observation/summaryTemplate" model="['speciesCount':speciesCount, 'subSpeciesCount':subSpeciesCount, queryParams:queryParams]"/>
                               
                        </div>

                   </div>
                    <div id="contribute" class="tab-pane">
                        <g:render template="contributeTemplate"/>
                    </div>
                </div>
            </div>
	</div>
	
	<script type="text/javascript">
		$(document).ready(function(){
			window.params.tagsLink = "${uGroup.createLink(controller:'species', action: 'tags')}";
			$('#speciesGallerySort').change(function(){
				updateGallery(window.location.pathname + window.location.search, ${params.limit?:40}, 0, undefined, false);
				return false;
            });

            // Javascript to enable link to tab
            var url = document.location.toString();
            if (url.match('#')) {
            $('.nav-tabs a[href=#'+url.split('#')[1]+']').tab('show') ;
            } 

            // Change hash for page-reload
            $('.nav-tabs a').on('shown', function (e) {
                window.location.hash = e.target.hash;
            })
        });
        var taxonRanks = [];
        <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
        </g:each>
	</script>

	<r:script>
    $(document).ready(function() {
        var taxonBrowserOptions = {
            expandAll:false,
            controller:"${params.controller?:'species'}",
            action:"${params.action?:'list'}",
            expandTaxon:"${params.taxon?true:false}"
        }
        if(${params.taxon?:false}){
        taxonBrowserOptions['taxonId'] = "${params.taxon}";
        }
        var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	
        $('.species-list-tabs a').click(function (e) {
          e.preventDefault();
          $('.nav-tabs li').removeClass('active');
          $(this).parent().addClass('active');
          var href = $(this).attr('href');
          $('.tab-pane').removeClass('active');
          $(href).addClass('active');
          //$(this).tab('show');
          return false;
        })
    });

	</r:script>
    </body>
</html>
