<%@page contentType="text/html"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.ImageType"%>

<html><head><title>Digest</title>

        <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
        <style type="text/css">
            h3 {margin:0;padding-left:5px;background-color: white;}
            h2 {padding-left:5px;margin:0;margin-bottom:10px;background-color: rgb(236, 233, 183);}
        </style>
    </head>
    <body>
        <table style="width:621px; border:1px solid #a1a376;">
            <tr>
                <td id="header" class="w640" align="left" bgcolor="#ffffff"><a href="${serverURL}/"><img src="${resource(dir:'images', file:'whatsnewbanner_3.gif',absolute:'true' )}" alt="${siteName}" style="border: 0px solid ;width: 627px; height: 53px;"></a></td>
            </tr>
            <tr>
                <td class="w580" style="height: 10px; background-color: white;"></td>
            </tr>
            <big style="font-weight: bold;"> <small>Dear</small><small>&nbsp;${username},</small></big>
            <p>Here is the activity digest for the <a href="${uGroup.createLink(controller:'userGroup', action:'show','userGroup':userGroup, absolute:true)}">${userGroup.name}</a> group on the India Biodiversity Portal</p>

            <g:if test = "${digestContent.announcements}">
            <g:set var="annIns" value="${digestContent.announcements}"></g:set>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Announcements</h2>
                <g:each in="${annIns}" var="annInstance">
                <%
                def discussionText = annInstance.key.plainText.replaceAll('<(.|\n)*?>', '');
                discussionText = discussionText.replaceAll('&nbsp;', '');
                if(discussionText.length() > 160) {
                discussionText = discussionText[0..158] + '...'
                }
                %> 
                    <table style="border-collapse: collapse; width:630px;">
                    <tr style="border: 2px solid burlywood; border-bottom:0;background-color: khaki;"><td style="border: 1px solid lightgray;padding-top:2px;padding-bottom:2px;"><i>${annInstance.value}</i></td></tr> 
                    </table>
                    <table style="border-collapse: collapse;margin-bottom:3px;">
                        <tr style="border: 2px solid burlywood; border-bottom:0;border-top:0;background-color: #d4ece3;"><td style="width:63px; vertical-align:top;"><i>Subject:</i></td><td> <a href="${uGroup.createLink(controller:'discussion', action:'show','id': annInstance.key.id, absolute:true,'userGroup':userGroup)}">${annInstance.key.subject}</a></td></tr>
                        
                        <tr style="border: 2px solid burlywood; border-top:0;background-color: #d4ece3;"><td style="width:63px; vertical-align:top;"><i>Message:</i></td><td> ${discussionText}</td></tr>
                    </table>   
                    </g:each>
                    <p style="text-align:right; padding-right:5px; font-weight:bold;background-color:white;margin:0;"><a href="${uGroup.createLink(controller:'discussion', action:'list','userGroup':userGroup, absolute:true)}" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>

            <g:if test = "${digestContent.observations || digestContent.unidObvs}">
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Observations (${digestContent.obvListCount})</h2>
                <div style="background-color: #d4ece3;">
                    <g:if test = "${digestContent.observations }">
                    <g:set var="obvIns" value="${digestContent.observations}"></g:set>
                    <h3>Recent</h3>
                    <table>
                        <tr align="left" style="background-color: #d4ece3">
                            <g:each in="${obvIns.size() < 5 ? obvIns : obvIns.subList(0, 5)}" var="observationInstance">
                            <g:set var="mainImage" value="${observationInstance.mainImage()}" />
                            <%
                            def imagePath = mainImage?mainImage.thumbnailUrl(null, !observationInstance.resource ? '.png' :null): null;
                            def controller = observationInstance.isChecklist ? 'checklist' :'observation'
                            def obvId = observationInstance.id
                            %>
                            <td class="w640" height="30" width="120" style=" border: 1px solid lightblue;"><a title="${observationInstance.title()}" href="${uGroup.createLink(controller:controller, action:'show','id': obvId, absolute:true,'userGroup':userGroup)}"><img src="${imagePath}" alt="" style="${observationInstance.isChecklist? 'opacity:0.7;' :''} border: 0px solid ; width: 120px; height: 120px;">
                                    <p style="text-overflow: ellipsis;overflow: hidden;width: 120px;white-space: nowrap;">${observationInstance.title()}</p>
                                    
                            </a>
                        </td>
                            </g:each>
                        </tr>
                    </table>
                    <p style="text-align:right;padding-right:5px;font-weight:bold;color:#2ba6cb;background-color:white;margin:0;"><a href="${uGroup.createLink(controller:'observation', action:'list','userGroup':userGroup, absolute:true)}" style="color:#2ba6cb;">View More</a></p>
                    </g:if>
                    <g:if test = "${digestContent.unidObvs}">
                    <g:set var="uniObvIns" value="${digestContent.unidObvs}"></g:set>
                    <h3>Unidentified</h3>
                    <table>
                        <tr align="left" style="background-color: #d4ece3">
                            <g:each in="${uniObvIns.size() < 5 ? uniObvIns : uniObvIns.subList(0, 5)}" var="uniObvInstance">
                            <g:set var="mainImage" value="${uniObvInstance.mainImage()}" />
                            <%
                            def imagePath = mainImage?mainImage.thumbnailUrl(null, !uniObvInstance.resource ? '.png' :null): null;
                            def controller = uniObvInstance.isChecklist ? 'checklist' :'observation'
                            def obvId = uniObvInstance.id
                            %>
                            <td class="w640" height="30" width="120" style=" border: 1px solid lightblue;"><a href="${uGroup.createLink(controller:controller, action:'show','id': obvId, absolute:true,'userGroup':userGroup)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 120px; height: 120px;"><p>${uniObvInstance.title()}</p></a></td>
                            </g:each>
                        </tr>
                    </table>
                
                    <p style="text-align:right;padding-right:5px;font-weight:bold;color:#2ba6cb;background-color:white;margin:0;"><a href="${uGroup.createLink(controller:'observation', action:'list','userGroup':userGroup, absolute:true, params:['speciesName':'Unknown'])}" style="color:#2ba6cb;">View More</a></p>
                    </g:if>

                    <g:if test = "${digestContent.recentTopContributors}">
                    <g:set var="recentTopContributors" value="${digestContent.recentTopContributors}"></g:set>
                    <h3>Top Contributors</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${recentTopContributors.size() < 5 ? recentTopContributors : recentTopContributors.subList(0, 5)}" var="rtc">
                            <td class="w640" height="30" width="120" style=" border: 1px solid lightblue; text-align: left;"><a href="${uGroup.createLink(action:'show', controller:'SUser', id:rtc[0].id, 'userGroup':userGroup)}">
                                    <div style="height:120px;text-align: center;"><img src="${rtc[0].profilePicture()}" title="${rtc[0].name}" style="border: 0px solid ; max-height: 120px; max-width:120px;" /></div>
                                    <p style="text-overflow: ellipsis;overflow: hidden;width: 100px;white-space: nowrap;">${rtc[0].name} </p></a><span> (${rtc[1]})</span>
                            </td>
                            </g:each>
                        </tr>
                    </table> 
                    <p style="text-align:right;padding-right:5px;font-weight:bold;color:#2ba6cb;background-color:white;margin:0;">&nbsp; &nbsp;</p>

                    </g:if>

                    <g:if test = "${digestContent.topIDProviders}">
                    <g:set var="topIDProviders" value="${digestContent.topIDProviders}"></g:set>
                    <h3>Top ID Providers</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${topIDProviders.size() < 5 ? topIDProviders : topIDProviders.subList(0, 5)}" var="tip">
                            <td class="w640" height="30" width="120" style=" border: 1px solid lightblue; text-align: left;"><a href="${uGroup.createLink(action:'show', controller:'SUser', id:tip.user.id, 'userGroup':userGroup)}">
                                    <div style="height:120px; text-align: center;">      <img src="${tip.user.profilePicture()}" title="${tip.user.name}" style="border: 0px solid ; max-height: 120px; max-width:120px;" /></div>
                                    <p style="text-overflow: ellipsis;overflow: hidden;width: 100px;white-space: nowrap;">${tip.user.name} </p></a><span>(${tip.recoCount})</span>
                            </td>
                            </g:each>
                        </tr>
                    </table> 
                    <p style="text-align:right;padding-right:5px;font-weight:bold;color:#2ba6cb;background-color:white;margin:0;">&nbsp; &nbsp;</p>
                    </g:if>



                </div>
            </div>
            </g:if>

            <g:if test = "${digestContent.species}">
            <g:set var="spIns" value="${digestContent.species}"></g:set>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Species (${digestContent.spListCount})</h2>
                <div style="background-color: #d4ece3;">
                    <h3>Recent</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${spIns.size() < 5 ? spIns : spIns.subList(0, 5)}" var="speciesInstance">
                            <g:set var="mainImage" value="${speciesInstance.mainImage()}" />
                            <%
                            def imagePath = '';
                            def speciesGroupIcon =  speciesInstance.fetchSpeciesGroup().icon(ImageType.ORIGINAL)
                            if(mainImage?.fileName == speciesGroupIcon.fileName) 
                            imagePath = mainImage.thumbnailUrl("${resourcesServerURL}", '.png');
                            else
                            imagePath = mainImage?mainImage.thumbnailUrl("${resourcesServerURL}"):null;
                            def spId = speciesInstance.id
                            imagePath = imagePath.replaceAll(' ','%20');
                            %>
                            <td class="w640" height="30" width="120" style=" border: 1px solid lightblue;"><a title="${speciesInstance.title.replaceAll('<(.|\n)*?>', '')}" href="${uGroup.createLink(controller:'species', action:'show','id': spId, absolute:true,'userGroup':userGroup)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 120px; height: 120px;">
                                    <p style="text-overflow: ellipsis;overflow: hidden;width: 120px;white-space: nowrap;">${speciesInstance.title.replaceAll('<(.|\n)*?>', '')}</p>
                            </a></td>
                            </g:each>
                        </tr>
                    </table>
                </div>
                <p style="text-align:right; padding-right:5px; font-weight:bold;background-color:white;margin:0;"><a href="${uGroup.createLink(controller:'species', action:'list','userGroup':userGroup, absolute:true)}" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>
            
            <g:if test = "${digestContent.users}">
            <g:set var="userIns" value="${digestContent.users}"></g:set>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Users (${digestContent.userListCount})</h2>
                <div style="background-color: #d4ece3;">
                    <h3>New</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${userIns.size() < 5 ? userIns : userIns.subList(0, 5)}" var="userInstance">
                            <td class="w640" height="30" width="120" style=" border: 1px solid lightblue; text-align: left;"><a title="${userInstance.name}" href="${uGroup.createLink(action:'show', controller:'SUser', id:userInstance.id, 'userGroup':userGroup)}">
                                    <div style="height:120px; text-align:center;">   <img src="${userInstance.profilePicture()}" title="${userInstance.name}" style="border: 0px solid ; max-height: 120px; max-width:120px;" /></div>
                                    <p style="text-overflow: ellipsis;overflow: hidden;width: 120px;white-space: nowrap;">${userInstance.name}</p>
                            </a></td>
                            </g:each>
                        </tr>
                    </table>
                </div>
                <p style="text-align:right; padding-right:5px; font-weight:bold; color:#2ba6cb;background-color:white;margin:0;"><a href="${uGroup.createLink(controller:'SUser', action:'list','userGroup':userGroup, absolute:true, params:['sort':'dateCreated'])}" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>

            <g:if test = "${digestContent.documents}">
            <g:set var="docIns" value="${digestContent.documents}"></g:set>
            <%
            def counter = 1
            %>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Documents (${digestContent.docListCount})</h2>
                <div style="background-color: #d4ece3;">
                    <h3>Recent</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${docIns.size() < 5 ? docIns : docIns.subList(0, 5)}" var="documentInstance">
                            <%
                            def docId = documentInstance.id
                            %>
                            <tr>
                                <td class="w640" height="30" width="627" style="font-weight:bold;padding-left:5px;">${counter}. <a href="${uGroup.createLink(controller:'document', action:'show','id': docId, absolute:true,'userGroup':userGroup)}" style="color:#2ba6cb;">${documentInstance.title}</a></td>
                            </tr>
                            <tr> 
                                <td class="w640" height="30" width="627" style="border-bottom:1px solid white">
                                    <g:if test="${documentInstance.notes != null}">
                                    <%
                                        def docNotes = documentInstance.notes
                                        docNotes = docNotes.replaceAll('<(.|\n)*?>', '');
                                        docNotes = docNotes.replaceAll('&nbsp;', '');
                                    %>
                                    <g:if test="${docNotes.length() > 160}">
                                    ${docNotes[0..158] + '...'} <br />
                                    </g:if>
                                    <g:else>
                                    ${docNotes?:''} <br />
                                    </g:else>
                                    </g:if>
                                </td>
                            </tr>
                            <%
                            counter++
                            %>
                            </g:each>
                        </tr>
                    </table>
                </div>
                <p style="text-align:right; padding-right:5px; font-weight:bold;color:#2ba6cb;background-color:white;margin:0;"><a href="${uGroup.createLink(controller:'document', action:'list','userGroup':userGroup, absolute:true)}" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>

            <g:if test = "${digestContent.discussions}">
            <g:set var="disIns" value="${digestContent.discussions}"></g:set>
            <%
            def counter = 1
            %>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Discussions (${digestContent.disListCount})</h2>
                <div style="background-color: #d4ece3;">
                    <h3>Recent</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${disIns.size() < 5 ? disIns : disIns.subList(0, 5)}" var="discussionInstance">
                            <%
                            def disId = discussionInstance.id
                            %>
                            <tr>
                                <td class="w640" height="30" width="627" style="font-weight:bold;padding-left:5px;">${counter}. <a href="${uGroup.createLink(controller:'discussion', action:'show','id': disId, absolute:true,'userGroup':userGroup)}" style="color:#2ba6cb;">${discussionInstance.subject}</a></td>
                            </tr>
                            <tr> 
                                <td class="w640" height="30" width="627" style="border-bottom:1px solid white">
                                    <g:if test="${discussionInstance.plainText != null}">
                                    <%
                                    def disText = discussionInstance.plainText
                                    disText = disText.replaceAll('<(.|\n)*?>', '');
                                    disText = disText.replaceAll('&nbsp;', '');
                                    %>
                                    <g:if test="${disText.length() > 160}">
                                    ${disText[0..158] + '...'} <br />
                                    </g:if>
                                    <g:else>
                                    ${disText?:''} <br />
                                    </g:else>
                                    </g:if>
                                </td>
                            </tr>
                            <%
                            counter++
                            %>
                            </g:each>
                        </tr>
                    </table>
                </div>
                <p style="text-align:right; padding-right:5px; font-weight:bold;color:#2ba6cb;background-color:white;margin:0;"><a href="${uGroup.createLink(controller:'discussion', action:'list','userGroup':userGroup, absolute:true)}" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>


            <p>If you don't want to recieve notifications from our portal, please unsubscribe by logging into <a href="${uGroup.createLink(controller:'SUser', action:'show','id':userID,'userGroup':userGroup, absolute:true)}"> your profile</a></p>
        </table>
    </body>
	</html>
