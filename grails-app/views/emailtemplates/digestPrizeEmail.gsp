<%@page contentType="text/html"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.ImageType"%>

<html>
    <head>
        <title>Competitions and Prizes</title>
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
                    <big style="font-weight: bold;"> <small>Dear</small> <small>&nbsp;${username},</small></big>
                    <p>Due to an issue we experienced with a third party plugin some users were unable to upload observations. To compensate, we have decided to extend the Neighborhood Trees Campaign for one more day.The campaign will close at midnight today (28/4/2014) and prize winners and campaign results will be announced <a href="http://treesindia.indiabiodiversity.org/page/56">here</a> shortly. TreesIndia will continue to accept tree observation uploads even after the campaign.<br><br>
                    Thanks,<br>
                    The TreesIndia Team</p>
                </td>
            </tr>
        </table>
    </body>
</html>
