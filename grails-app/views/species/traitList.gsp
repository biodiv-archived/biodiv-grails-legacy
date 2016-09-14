<%@page import="species.utils.Utils"%>
<%@page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'facts.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style>
        .traitName{display:inline;}
        .traitValue{display:inline;}
        </style>

    </head>
    <body>
           <%
            def classifications = [];
            Classification.list().each {
            classifications.add([it.id, it, null]);
            }
            classifications = classifications?.sort {return it[1].name}; 
            %>
            <div class="span4">
        <div class="taxonomyBrowser sidebar_section" style="position:relative">
                                <h5><g:message code="button.taxon.browser" /></h5>  
                                <div id="taxaHierarchy">
                    <g:render template="/common/taxonBrowserUserTemplate" model="['classifications':classifications, selectedClassification:265799, 'expandAll':false]"/>
                    </div>
                    </div>
                    </div>
                    <div class="span8">
                    <div class="trait_list">
    <g:each in="${traitList}" var="traits" status="i">
    <h6><div class="traitName">${traits.name}</div>-<div class="traitValue">${traits.values}</div></h6>
    </g:each>
    </div>
    </div>




        <script type="text/javascript">
        var taxonRanks = [];
        <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
        </g:each>
        </script>
        <asset:script>
    $(document).ready(function() {
        var taxonBrowserOptions = {
            expandAll:false,
            controller:"${params.controller?:'species'}",
            action:"${params.action?:'list'}",
            expandTaxon:"${params.taxon?true:false}"

        }
        var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);   
    });
      </asset:script>
    </body>
</html>


