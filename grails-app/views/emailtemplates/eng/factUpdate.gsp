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
				Hi ${username},<br /><br />

				<a href="${actorProfileUrl?: obvOwnUrl}"><img src="${currentUser?.profilePicture(ImageType.SMALL)}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif; max-width: 30px; max-height:30px; display:inline-block; vertical-align: middle;"></a>

				<g:set var="currentAction" value="${action}"></g:set>

				<g:if test="${currentUser?.username == tousername}">
					You 
				</g:if>
					<g:else>
							<a href="${actorProfileUrl}"> ${currentUser?.name} </a>
					</g:else>
	${raw(message)}
			</div>
			<!-- COLUMN WRAP -->
			<div class="column-wrap" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px; background-color: #D4ECE3;">
			
				<div class="column" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 450px;float: left; background-color: #D4ECE3;">
					<table align="right" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
						<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<td style="margin: 0;padding: 2px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">				
															
								<p style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 0px;font-weight: normal;font-size: 14px;line-height: 1.4;">

									<b>Trait:</b> ${trait} <br />
									<b>Trait Value:</b> ${traitValue.value} <br />
								</p>
							</td>
						</tr>
					</table>
				</div>
			</div><!-- /COLUMN WRAP -->				
		</td>
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
	</tr>
</table>

			
<!-- FOOTER -->
<table class="footer-wrap" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;clear: both;border-left: 1px solid #A1A376; border-right: 1px solid #A1A376; border-bottom: 1px solid #A1A376; width:610px;">
<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
		<td class="container" style="margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;display: block;max-width: 600px;clear: both;">
			
				<!-- content -->
				<div class="content" style="margin: 0 auto;padding: 0px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px;display: block;">
					<table style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;">
						<tr style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
							<td align="left" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;">
								<p style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 10px;font-weight: normal;font-size: 12px;line-height: 1;">
									If you don't want to recieve notifications from our portal, please unsubscribe by logging into <a href="${userProfileUrl?:obvOwnUrl}" style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;color: #2BA6CB;"><unsubscribe style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"> your profile</unsubscribe></a>
								</p>
							</td>
						</tr>
					</table>
				</div><!-- /content -->
				
		</td>
		<td style="margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;"></td>
	</tr>
</table><!-- /FOOTER -->


