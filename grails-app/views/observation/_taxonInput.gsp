<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.utils.Utils"%>
<%@ page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>

<div class="control-group ${hasErrors(bean: instance, field: 'taxons', 'error')}">
    <label for="group" class="control-label"><g:message
        code="observation.group.label" default="${g.message(code:'default.group.label')}" /> <span class="req">*</span></label>
    <div class="filters controls textbox" style="position: relative;">
        <div id="speciesGroupFilter" data-toggle="buttons-radio">
            <%def othersGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS)%>
            <g:each in="${SpeciesGroup.list() }" var="sGroup" status="i">
                <g:if test="${sGroup != othersGroup }">
                    <button class="btn species_groups_sprites ${sGroup.iconClass()}"
                        id="${"group_" + sGroup.id}" value="${sGroup.id}"
                        title="${sGroup.name}"></button>
                </g:if>

            </g:each>
            <button class="btn species_groups_sprites ${ othersGroup.iconClass()}"
                id="${"group_" + othersGroup.id}" value="${othersGroup.id}"
                title="${othersGroup.name}"></button>
        </div>
         <!--div id="taxonBrowser">
            <div class="taxonomyBrowser sidebar_section" style="position:relative">
                <h5><g:message code="button.taxon.browser" /></h5> 
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
        </div-->

        <div class="help-inline">
            <g:hasErrors bean="${instance}" field="taxons">
            <g:message code="observation.group.not_selected" />
            </g:hasErrors>
        </div>
    </div>
</div>
<script type="text/javascript">
    var taxonRanks = [];
    <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
    </g:each>



$(document).ready(function(){
	$("#speciesGroupFilter").button();
	$('#speciesGroupFilter button[value="${instance?.groupId}"]').addClass('active');
    $('#speciesGroupFilter button').tooltip({placement:'top'});

        var taxonBrowserOptions = {
            expandAll:false,
            controller:"${params.controller?:'observation'}",
            action:"${params.action?:'list'}",
            expandTaxon:"${params.taxon?true:false}"
        }

        if(${params.taxon?:false}){
            taxonBrowserOptions['taxonId'] = "${params.taxon}";
        }
        
//        $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	
    });

</script>

