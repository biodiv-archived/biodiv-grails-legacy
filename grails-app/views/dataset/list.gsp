<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'showusergroupsig.title.datasets')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="observations_list" />
    </head>
    <body>
        <div class="span12">
            <obv:showSubmenuTemplate/>
            <div class="page-header clearfix">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px;">
                        <h1><g:message code="dataset.label" /></h1>
                    </div>
                </div>
                <div style="clear:both;"></div>
            </div>

            <uGroup:rightSidebar/>

            <g:render template="/dataset/showDatasetListWrapperTemplate"/>
        </div>
    </body>
</html>
