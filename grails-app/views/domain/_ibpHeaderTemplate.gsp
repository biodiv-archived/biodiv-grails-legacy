<%@page import="species.utils.Utils"%>
<div id="ibp-header" class="gradient-bg">
	<div id="ibp-header-bar" class="navbar navbar-static-top" style="margin-bottom: 0px;">
		<div class="navbar-inner"
			style="box-shadow: none; background-color: #dddbbb; background-image: none; padding: 0px 5px;  filter: progid :DXImageTransform.Microsoft.gradient (startColorstr = '#dddbbb', endColorstr = '#dddbbb' );">
			<div class="container" style="width: 100%">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> </a> <a
					class="brand"
					href="${createLink(url:Utils.getIBPServerDomain()) }">
					India Biodiversity Portal</a>
				
				<div class="nav-collapse">
				
				
					<ul class="nav pull-right">
						
						
						<li><uGroup:showSidebar /></li>
<%--						<li><sUser:userLoginBox--%>
<%--								model="['userGroup':userGroupInstance]" /></li>--%>

					</ul>
				</div>
			</div>

		</div>
	</div>
	<domain:showHeader model="['userGroupInstance':userGroupInstance]" />
         <div class="">
<%
String supportEmail = "";
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	supportEmail = grailsApplication.config.speciesPortal.wgp.supportEmail;
} else if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>


        <div id="contributeMenu" class="collapse">
                        <div class="container">
    <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="Species pages intend to provide detailed information on every species in India." data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'species', action:'contribute', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Contribute to Species pages" title="Contribute to Species pages"> <i class="icon-plus"></i>Contribute to Species pages</a>

                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="An observation is an individual sighting of a species, submitted as an image or video." data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add an Observation" title="Add an Observation"> <i class="icon-plus"></i>Add an Observation</a>
                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="Upload biodiversity related reports, presentations, posters etc." data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                    data-original-title="Add Document" title="Add Document">
                                    <i class="icon-plus"></i> Add Document
                                </a>
                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="
                                    Suggest feedback on using and improving the portal." data-trigger="hover"
                                    href="/feedback_form"
                                    data-original-title="Provide Feedback" title="Provide Feedback">
                                    <i class="icon-plus"></i> Provide Feedback
                                </a>
                            </ul>
                        </div>

                    </div>

	<g:if test="${flash.error}">
		<div class="alertMsg alert alert-error" style="clear: both;margin-bottom:0px">
			${flash.error}
		</div>
	</g:if>

	<div class="alertMsg ${(flash.message)?'alert':'' }"
		style="clear: both; margin: 0px">
		${flash.message}
	</div>

	<auth:ajaxLogin />
	<div id="fb-root"></div>
    </div>
</div>
