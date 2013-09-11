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
			<a style="margin: 0;padding: 0;font-family: &quot;HelveticaNeue-Light&quot;, &quot;Helvetica Neue Light&quot;, &quot;Helvetica Neue&quot;, Helvetica, Arial, &quot;Lucida Grande&quot;, sans-serif;line-height: 1;color: #000;font-weight: 200;font-size: 20px; text-decoration:none;">${domain}</a>
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
				Hi ${username},<br /><br />

				<a href="${obvOwnUrl?: actorProfileUrl}"><img src="${currentUser?.profilePicture(ImageType.SMALL)}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif; max-width: 30px; max-height:30px; display:inline-block; vertical-align: middle;"></a>

				<g:set var="currentAction" value="${action}"></g:set>
				<g:if test="${currentAction == 'downloadRequest'}">
				</g:if>
				<g:elseif test="${currentUser?.username == tousername}">
					You 
				</g:elseif>
				<g:else>
					<g:if test="${currentAction == 'observationAdded' || currentAction == 'observationDeleted'}">
					 		<a href="${obvOwnUrl}"> ${obvOwner} </a>
					</g:if>
					<g:else>
							<a href="${actorProfileUrl}"> ${currentUser?.name} </a>
					</g:else>
				</g:else>

				${message? message.toLowerCase(): activity?.activityTitle.toLowerCase()}


				<g:if test="${activity?.text }">

			<!-- Callout Panel -->
			<p class="callout" style="margin: 0;padding: 5px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 2px;font-weight: normal;font-size: 14px;line-height: 1; background-color: #ECE9B7;">

				<g:if test="${activity.text}"> 
					<g:if test="${activity.text != null && activity.text.length() > 160}">
						${activity.text[0..160] + '....'} 
					</g:if>
					<g:else>
						${activity.text?:''}
					</g:else>
	
				</g:if>

			</p><!-- /Callout Panel -->

</g:if>
</p>
	
						</td>
					</tr>
				</table>
			</div>
			
				<g:if test="${(currentAction == 'Document created' || currentAction == 'downloadRequest')}">
				 		
				</g:if>
				<g:elseif test="${actionObject == 'checklist'}">
	
				</g:elseif>
				<g:else>
			<!-- COLUMN WRAP -->
			<div class="column-wrap" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px; background-color: #E2ECEC;">
			
				<div class="column" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 135px;float: left;">
					<table align="left" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
						<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<td style="margin: 0;padding: 2px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">				

								<a href="${obvUrl}"><img src="${obvImage}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif; max-width: 130px; max-height:130px; display:inline-block; vertical-align: middle;"></a>
							</td>
						</tr>
					</table>
				</div>
			
				<div class="column" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 450px;float: left; background-color: #E2ECEC;">
					<table align="right" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
						<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<td style="margin: 0;padding: 2px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">				
															
								<p style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 0px;font-weight: normal;font-size: 14px;line-height: 1.4;">

								<g:set var="scientific" value="${obvSName}"></g:set>

								<g:if test="${scientific}"> 
									<b>Scientific Name:</b> ${scientific} <br />
								</g:if>
								<g:else>
									<b>Scientific Name:</b> Help Identify <br />
								</g:else>

								<g:set var="common" value="${obvCName}"></g:set>

								<g:if test="${common}"> 
									<b>Common Name:</b> ${common}<br />
								</g:if>
								<g:else>
									<b>Common Name:</b> Help Identify <br />
								</g:else>

								<b>Location: </b> ${obvPlace}<br /><b>Observed On:</b>  ${obvDate}<br />

								</p>

		
								
							</td>
						</tr>
					</table>

					
				</div>
				
				<div class="clear" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;display: block;clear: both; background-color: #E2ECEC;">

					<g:if test="${groups}">
						<br />The above observation is a member of the following groups: <br />
						<g:each in="${groups}">
							<div style="height: 30px; width: 186px; border: 1px solid #ECE9B7; position: relative; background-color: #E2ECEC; float:left; margin:2px 6px; " >
							    <a href="${baseUrl}/group/${it.webaddress}" style="text-decoration: none; color: #222222;"> 
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
					</g:if>
					</div>			

			</div><!-- /COLUMN WRAP -->	
</g:else>
			<div class="content" style="margin: 0 auto;padding: 10px 3px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px;display: block;">
							<!-- Callout Panel -->
							<p class="callout" style="margin: 0;padding: 0 5px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 2px;font-weight: normal;font-size: 12px;line-height: 1; background-color: #ECF8FF;">

				<g:if test="${currentAction == 'Document created'}">
				 	You can get the documents <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> here &raquo;</a>
				</g:if>
				<g:elseif  test="${currentAction == 'downloadRequest'}">
					You can log into your profile <a href="${userProfileUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;"> here &raquo;</a>
				</g:elseif>
				<g:else>
					For more information, please visit the page <a href="${obvUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;font-weight: bold;">here &raquo;</a>							
				</g:else>
				</p><!-- /Callout Panel -->
			</div>
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
									If you don't want to recieve notifications from our portal, please unsubscribe by logging into <a href="${obvOwnUrl?:userProfileUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;"><unsubscribe style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"> your profile</unsubscribe></a>
								</p>
							</td>
						</tr>
					</table>
				</div><!-- /content -->
				
		</td>
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
	</tr>



</table><!-- /FOOTER -->


