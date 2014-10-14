<%@page contentType="text/html"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.ImageType"%>

<html>
    <head>
        <title><g:message code="msg.Competitions.Prizes" /></title>
        <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
        <style type="text/css">
        </style>
    </head>
    <body>
        <table style="width:621px; border:1px solid #a1a376;">
            <tr>
                <td id="header" class="w640" align="left" bgcolor="#ffffff"><a href="${grailsApplication.config.grails.serverURL}/"><img src="${resource(dir:'images', file:'whatsnewbanner_3.gif',absolute:'true' )}" alt="${grailsApplication.config.speciesPortal.app.siteName}" style="border: 0px solid ;width: 627px; height: 53px;"></a></td>
            </tr>
            <tr>
                <td class="w580" style="height: 10px; background-color: white;"></td>
            </tr>
            <tr>
                <td>
                    <big style="font-weight: bold;"> <small><g:message code="msg.Dear" /></small> <small>&nbsp;${username},</small></big>
                    <p><g:message code="msg.issue" /> <a href="http://treesindia.indiabiodiversity.org/"><g:message code="msg.Trees.campaign" /></a><g:message code="msg.campaign.close" /> <a href="http://treesindia.indiabiodiversity.org/page/56"><g:message code="msg.here" /></a> <g:message code="msg.shortly" /> <br><br><a href="http://treesindia.indiabiodiversity.org/"><g:message code="msg.Trees.India" /></a> <g:message code="msg.will.accept" /><br><br>
                    <g:message code="msg.Thanks" /><br>
                    <g:message code="msg.Trees.India.Team" /></p>
                </td>
            </tr>
        </table>
    </body>
</html>
