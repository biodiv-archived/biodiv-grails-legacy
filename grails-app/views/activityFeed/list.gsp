<%@page import="species.utils.Utils"%>
<%@page import="species.groups.UserGroup"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.pagetitle.activity')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="activityfeed,comment"/>
        <style>
            .thumbnail .observation_story {
            width:715px;
            }
        </style>
    </head>
    <body>

        <div class="span12">
            <div class="page-header clearfix">
                <h1>
                    <g:message code="default.observation.heading" args="[title]" />
                </h1>
            </div>

            <g:if test="${flash.message}">
            <div class="message alert alert-info">
                ${flash.message}
            </div>
            </g:if>
            <uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]"/>
            <div class="userGroup-section">
           
                <feed:showFeedWithFilter model="[feedType:feedType, feedCategory:UserGroup.class.canonicalName,'feedOrder':'latestFirst']" />
            </div>
        </div>

        <r:script>
        </r:script>
    </body>
</html>
