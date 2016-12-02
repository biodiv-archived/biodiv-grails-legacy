<%@page import="species.utils.Utils"%>
<%@page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'traits.label')} ${g.message(code:'msg.beta')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
    </head>
    <body>

    <div class="row-fuild">
            <div class="page-header clearfix" style="margin: 0px;padding: 0px;">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px;">
                        <h1><g:message code="traits.label" /> <sup><g:message code="msg.beta" /></sup></h1>
                    </div>
                </div>
                <div style="clear:both;"></div>
            </div>

            <uGroup:rightSidebar/>

            <g:render template="showTraitListWrapperTemplate" model="['observationCreate':false]"/>

        </div>
    </body>
</html>


