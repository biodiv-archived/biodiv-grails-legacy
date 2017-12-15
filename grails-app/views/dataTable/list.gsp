<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.dataTable.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="observations_list" />
    </head>
    <body>
        <div class="span12">
            <obv:showSubmenuTemplate/>
            <div class="page-header clearfix">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px;">
                        <h1><g:message code="default.dataTable.label" /></h1>
                    </div>
                </div>
                <div style="clear:both;"></div>
            </div>

            <uGroup:rightSidebar/>

            <g:render template="/dataTable/showDataTableListWrapperTemplate"/>
        </div>
    </body>
</html>
