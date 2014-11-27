<%@page contentType="text/html"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>


<!------------------------------------ 
---- HEADER --------------------------
------------------------------------->
<table class="head-wrap" bgcolor="#ECE9B7" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%; border-top: 1px solid #A1A376; border-left: 1px solid #A1A376; border-right: 1px solid #A1A376; width:610px;">
	<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
		<td class="header container" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;display: block;max-width: 600px;clear: both;">
			
			<div class="content" style="margin: 0 auto;padding: 2px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px;display: block; text-align:center;">
                            <a style="margin: 0;padding: 0;font-family: &quot;HelveticaNeue-Light&quot;, &quot;Helvetica Neue Light&quot;, &quot;Helvetica Neue&quot;, Helvetica, Arial, &quot;Lucida Grande&quot;, sans-serif;line-height: 1;color: #000;font-weight: 200;font-size: 20px; text-decoration:none;">${siteName}</a>
			</div>
			
		</td>
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
	</tr>
</table>

<!------------------------------------ 
---- BODY ----------------------------
------------------------------------->
<table class="body-wrap" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%; border: 1px solid #A1A376;width:610px;">
	<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
		<td class="container" bgcolor="#FFFFFF" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;display: block;max-width: 600px;clear: both;">
			
			<!-- content -->
			<div class="content" style="margin: 0 auto;padding: 2px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 610px;display: block; ">
				<table style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
					<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
						<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<p class="lead" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 10px;font-weight: normal;font-size: 14px;line-height: 1;">
				Bonjour ${username},<br /><br />

				<a href="${actorProfileUrl?: obvOwnUrl}"><img src="${currentUser?.profilePicture(ImageType.SMALL)}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif; max-width: 30px; max-height:30px; display:inline-block; vertical-align: middle;"></a>

				<g:set var="currentAction" value="${action}"></g:set>
				<g:if test="${currentAction == 'downloadRequest'}">
					Votre 
				</g:if>
				<g:elseif test="${currentUser?.username == tousername}">
					Vous 
				</g:elseif>
				<g:else>
					<g:if test="${currentAction == 'observationAdded' || currentAction == 'observationDeleted'}">
					 		<a href="${obvOwnUrl}"> ${obvOwner} </a>
					</g:if>
					<g:else>
							<a href="${actorProfileUrl}"> ${currentUser?.name} </a>
					</g:else>
				</g:else>
                ${raw(message? message: activity?.activityTitle[0].toLowerCase() + activity?.activityTitle.substring(1))}
                <g:if test="${spFDes}">
                    <p style="margin-left:35px;">
                        ${spFDes}
                    </p>
                </g:if>
                <g:if test="${resURLs}">
                    <table>
                        <tr align="left">
                            <g:each in="${resURLs.size() < 8 ? resURLs : resURLs.subList(0, 8)}" var="ru">
                            <td height="50" width="50" style=" border: 1px solid lightblue; text-align: left;">
                                <a href="${uGroup.createLink(action:'show', controller:'species', 'id': obvId , absolute:true)}">
                                    <img src="${ru}" title="" style="border: 0px solid ; max-height: 50px; width:50px;" />
                                </a>
                            </td>
                            </g:each>
                        </tr>
                    </table>
                </g:if>

				<g:if test="${activity?.text }">

			<!-- Callout Panel -->
			<p class="callout" style="margin: 0;padding: 5px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 2px;font-weight: normal;font-size: 14px;line-height: 1; ">

				<g:if test="${activity.text}"> 
					<g:if test="${activity.text != null && activity.text.length() > 400}">
						${raw(activity.text[0..400] + '....')} 
					</g:if>
					<g:else>
						${raw(activity.text?:'')}
					</g:else>
	
				</g:if>

			</p><!-- /Callout Panel -->

</g:if>
</p>
	
						</td>
					</tr>
				</table>
			</div>
			
				<g:if test="${(currentAction == 'downloadRequest' || currentAction == 'Document created' || actionObject == 'checklist' || domainObjectType == 'document' || domainObjectType == 'checklists' || domainObjectType == 'species' || domainObjectType == 'usergroup' || domainObjectType == 'newsletter')}">
				 		<div class="clear" class="content" style="margin: 0 auto;padding: 10px 0px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px;display: block; background-color:#D4ECE3; align:left; clear: both;">
							<!-- Callout Panel -->
							<p class="callout" style="margin: 0;padding: 0 5px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 2px;font-weight: normal;font-size: 14px;line-height: 1; background-color: #D4ECE3;">

								<g:if test="${domainObjectType == 'document' || currentAction == 'Document created'}">
								 	Le document peut être consulté  <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> ici &raquo;</a> 
                                </g:if>
								<g:elseif  test="${currentAction == 'downloadRequest'}">
									Vous pouvez accéder à votre profil <a href="${userProfileUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> ici &raquo;</a>
                                </g:elseif>
								<g:elseif test="${domainObjectType == 'checklists'}">
								 	La liste peut être consultée  <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> ici &raquo;</a> 
                                </g:elseif>
								<g:elseif test="${domainObjectType == 'species'}">
								 	L'espèce peut être consultée  <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> ici &raquo;</a> 

                                </g:elseif>
                                <g:elseif test="${domainObjectType == 'usergroup' && action=='Added a comment'}">
                                    <b>Objet</b> : ${commentInstance.subject?:'No Subject'}<br/>
                                    <b>Message</b> : ${commentInstance.body} <br/>
                                    <b>Groupe</b> :
                                    <span style="height: 30px; width: 186px; position: relative; background-color: #D4ECE3;  margin:2px 6px; " >
                                        <a href="${uGroup.createLink(controller:'userGroup', action:'show', absolute:true, 'userGroup':group)}"> 
                                            <img src="${group.icon(ImageType.SMALL).fileName}" style="width: 30px; height: 30px; align: left; vertical-align:middle;"/>


                                            <g:if test="${group.name}"> 
                                            <g:if test="${group.name != null && group.name.length() > 19}">
                                            ${group.name[0..17] + '...'} <br />
                                            </g:if>
                                            <g:else>
                                            ${group.name?:''} <br />
                                            </g:else>

                                            </g:if>
                                        </a>   
                                    </span><br/>
                                    <br/>      
                                    <b> By </b> : 
                                    <a href="${feedActorProfileUrl}"><img src="${feedInstance.author?.profilePicture(ImageType.SMALL)}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif; max-width: 30px; max-height:30px; display:inline-block; vertical-align: middle;"></a>
                                    <a href="${feedActorProfileUrl}">${feedInstance.author.name}<a/><br/>

                                    La discussion peut être consultée  <a href="${discussionUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> ici &raquo;</a> 
                                </g:elseif>
								<g:elseif test="${domainObjectType == 'newsletter'}">
                                La page peut être consultée  <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> ici &raquo;</a> 
                                </g:elseif>
								<g:else>
                                La ${domainObjectType} peut être consultée <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> ici &raquo;</a> 
                                </g:else>
								</p><!-- /Callout Panel -->
						</div>
				</g:if>
				<g:else>
			<!-- COLUMN WRAP -->
			<div class="column-wrap" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px; background-color: #D4ECE3;">
			
				<div class="column" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 135px;float: left;">
					<table align="left" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
						<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<td style="margin: 0;padding: 2px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">				

								<a href="${obvUrl}"><img src="${obvImage}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif; max-width: 130px; max-height:130px; display:inline-block; vertical-align: middle;"></a>
							</td>
						</tr>
					</table>
				</div>
			
				<div class="column" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 450px;float: left; background-color: #D4ECE3;">
					<table align="right" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
						<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<td style="margin: 0;padding: 2px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">				
															
								<p style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 0px;font-weight: normal;font-size: 14px;line-height: 1.4;">

								<g:set var="scientific" value="${obvSName}"></g:set>
								<g:set var="common" value="${obvCName}"></g:set>

								<g:if test="${scientific}"> 
									<b>Nom scientifique: </b> ${scientific} <br />
								</g:if>
								<g:elseif test="${common}"> 
								</g:elseif>
								<g:else>
									<b>Nom scientifique: </b> Aide à l'identification <br />
								</g:else>


								<g:if test="${common}"> 
									<b>Nom Commun:</b> ${common}<br />
								</g:if>
								<g:elseif test="${scientific}"> 
								</g:elseif>
								<g:else>
									<b>Nom Commun:</b> Aide à l'identification <br />
								</g:else>

								<b>Lieu: </b> ${obvPlace}<br /><b>Observé le:</b>  ${obvDate}<br />

								</p>

		
								
							</td>
						</tr>
					</table>

					
				</div>

				<div class="clear" class="content" style="margin: 0 auto;padding: 10px 0px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px;display: block; background-color:#D4ECE3; align:left; clear: both;">
							<!-- Callout Panel -->
							<p class="callout" style="margin: 0;padding: 0 5px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 2px;font-weight: normal;font-size: 14px;line-height: 1; background-color: #D4ECE3;">

									Pour plus d'informations, merci de visiter la page <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;">ici &raquo;</a>							
							</p><!-- /Callout Panel -->
				</div>
			</div><!-- /COLUMN WRAP -->	
</g:else>
				<g:if test="${groups}">
				<div class="clear" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif; clear: both; background-color: #D4ECE3;">
					<a style="padding:0 2px">L'observation ci-dessus fait partie des groupes suivants: <a/><br />
						<g:each in="${groups}">
							<div style="height: 30px; width: 186px; border: 1px solid #A1A376; position: relative; background-color: #D4ECE3; float:left; margin:2px 6px; " >
							    <a href="${uGroup.createLink(controller:'userGroup', action:'show', absolute:true, 'userGroup':it)}" style="text-decoration: none; color: #222222;"> 
								<img src="${it.icon(ImageType.SMALL).fileName}" style="width: 30px; height: 30px; align: left; vertical-align:middle;"/>

									<g:set var="groupName" value="${it.name}"></g:set>

									<g:if test="${groupName}"> 
										<g:if test="${groupName != null && groupName.length() > 19}">
											${groupName[0..17] + '...'} <br />
										</g:if>
										<g:else>
											${groupName?:''} <br />
										</g:else>
	
									</g:if>
							    </a>   
							</div>
						</g:each>
						</div>	
					</g:if>
						

			
			
		</td>
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
	</tr>
</table>

			
<!-- FOOTER -->
<table class="footer-wrap" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;clear: both;border-left: 1px solid #A1A376; border-right: 1px solid #A1A376; border-bottom: 1px solid #A1A376; width:610px;">


	<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
		<td class="container" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;display: block;max-width: 600px;clear: both;">
		

		
		</td>
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
	</tr>


<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
		<td class="container" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;display: block;max-width: 600px;clear: both;">
			
				<!-- content -->
				<div class="content" style="margin: 0 auto;padding: 0px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px;display: block;">
					<table style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
						<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<td align="left" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
								<p style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 10px;font-weight: normal;font-size: 12px;line-height: 1;">
									Si vous ne voulez pas recevoir les notifications de notre portail, merci de vous désabonner en vous connectant à <a href="${userProfileUrl?:obvOwnUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;"><unsubscribe style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"> votre profil "</unsubscribe></a>
								</p>
							</td>
						</tr>
					</table>
				</div><!-- /content -->
				
		</td>
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
	</tr>



</table><!-- /FOOTER -->


