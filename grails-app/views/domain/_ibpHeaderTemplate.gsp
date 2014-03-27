<%@page import="species.utils.Utils"%>
<div id="ibp-header" class="gradient-bg">
	<div id="ibp-header-bar" class="navbar navbar-static-top" style="margin-bottom: 0px;border-bottom:0px;">
		<div class="navbar-inner"
			style="box-shadow: none; background-color: #2d2d2d; background-image: none; padding: 0px 5px;  filter: progid :DXImageTransform.Microsoft.gradient (startColorstr = '#fafafa', endColorstr = '#fafafa' );">
			<div class="container" style="width: 100%">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> </a> <a
					class="brand"
					href="${createLink(url:Utils.getIBPServerDomain()) }">
                                        ${grailsApplication.config.speciesPortal.app.siteName}</a>
				
				<div class="nav-collapse">
					<ul class="nav pull-right">
						<li><uGroup:showSidebar /></li>
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
} else{ // if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>


        <div id="contributeMenu" class="collapse" style="border-bottom:0px;">
                        <div class="container">
    <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="Species pages intend to provide detailed information on every species in India." data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'species', action:'contribute', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Contribute to Species pages" title="Contribute to Species pages"> <i class="icon-plus"></i>Contribute to Species pages</a>

                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="An observation is an individual sighting of a species, submitted as an image or video." data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add an Observation" title="Add an Observation"> <i class="icon-plus"></i>Add an Observation</a>
                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="A list is a collection of sightings recorded from an area over a time period submitted with or without supporting media." data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'checklist', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add a List" title="Add a List"> <i class="icon-plus"></i>Add a List</a>
 
                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="Upload biodiversity related reports, presentations, posters etc." data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                    data-original-title="Add Document" title="Add Document">
                                    <i class="icon-plus"></i> Add Document
                                </a>
                                <!--a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="
                                    Suggest feedback on using and improving the portal." data-trigger="hover"
                                    href="/feedback_form"
                                    data-original-title="Provide Feedback" title="Provide Feedback">
                                    <i class="icon-plus"></i> Provide Feedback
                                </a-->
                        </div>

                    </div>

	<g:if test="${flash.error}">
		<div class="alertMsg alert alert-error" style="clear: both;margin-bottom:0px">
			${flash.error}
		</div>
	</g:if>

<%--	<div class="alertMsg alert alert-info"--%>
<%--		style="clear: both; margin: 0px; text-align: center;">--%>
<%--		Due to unavoidable infrastructure maintenance, disruption of the portal services is likely on Sunday (8th December 2013).--%>
<%--	</div>--%>
										
	<div class="alertMsg alert alert-info"
		style="clear: both; margin: 0px; text-align: center;">
		The TreesIndia@IBP is organizing a "Neighbourhood Trees Campaign" from Earth day 22nd-27th April. Participate in running the campaign by filling <a href="https://docs.google.com/forms/d/1qRWjfUyXKcmDeSsW7fXm78BLJPFEseV1JfL_MYxqUDc/viewform">this form</a>.
	</div>

	<div class="alertMsg ${(flash.message)?'alert':'' }"
		style="clear: both; margin: 0px">
		${flash.message}
	</div>

	<auth:ajaxLogin />
	<div id="fb-root"></div>
    </div>
</div>
