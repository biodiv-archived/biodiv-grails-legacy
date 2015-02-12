<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.participation.ActivityFeedService"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'showusergroupsig.title.discussions')}"/>
        <g:set var="entityName" value="${g.message(code:'button.discussions')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="add_file, content_view, activityfeed" />
        <style type="text/css">
            .thumbnails>.thumbnail {
            margin: 0 0 10px 0px;
            width:100%;
            }
        </style>
    </head>
    <body>
        <div class="span12">           
            <g:render template="/discussion/discussionSubMenuTemplate" model="['entityName': entityName]" />
            <uGroup:rightSidebar/>

            <obv:featured model="['controller':params.controller, 'action':'related', 'filterProperty': 'featureBy', 'filterPropertyValue': true, 'id':'featureBy', 'userGroupInstance':userGroupInstance]" />

            <h4><g:message code="heading.browse.discussions" /></h4>

            <div class="document-list span8 right-shadow-box" style="margin:0;">
               

                <obv:showObservationFilterMessage />

                <g:render template="/discussion/discussionListTemplate" model="['userGroupInstance':userGroupInstance]"/>
            </div>

            <g:render template="/discussion/discussionSidebar" />
        </div>
	    </body>
</html>
