<%@ page import="content.eml.UFile"%>
<%@ page import="content.eml.Document"%>
<%@page import="species.utils.Utils"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.participation.ActivityFeedService"%>

<html>
    <head>
        <g:set var="title" value="${g.message(code:'showusergroupsig.title.documents')}"/>
        <g:set var="entityName" value="${g.message(code:'button.documents')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style type="text/css">
            .thumbnails>.thumbnail {
            margin: 0 0 10px 0px;
            width:100%;
            }
            tr.mainContent td {
                word-break:break-word;
            }
        .prop .value{
        line-height: 20px;
        margin: 2px 0 2px 100px;
        }
        </style>
    </head>
    <body>
        <div class="span12">           
            <g:render template="/document/documentSubMenuTemplate" model="['entityName': entityName]" />
            <uGroup:rightSidebar/>
            <obv:featured model="['controller':params.controller, 'action':'related', 'filterProperty': 'featureBy', 'filterPropertyValue': true, 'id':'featureBy', 'userGroupInstance':userGroupInstance]" />
            <h4><g:message code="heading.browse.documents" /></h4>
            <div class="row">
            <div class="document-list span8 right-shadow-box" style="margin:0;">
                <div class="list span8 right-shadow-box">
                    <obv:showObservationFilterMessage />
                    <g:render template="/document/documentListTemplate" model="${model}"/>
                </div>
            </div>
            <g:render template="/document/documentSidebar" model="${model}"/>
            </div>
        </div>
        </body>
    </html>

