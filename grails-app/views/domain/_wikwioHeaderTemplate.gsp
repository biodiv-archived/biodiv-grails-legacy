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
} else if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>


        <div id="contributeMenu" class="collapse" style="border-bottom:0px;">
                        <div class="container">
    <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.species.detailed.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'species', action:'contribute', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Contribute to Species pages" title="${g.message(code:'link.contribute.to')}"> <i class="icon-plus"></i>${g.message(code:'link.contribute.to')}</a>

                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.observation.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add an Observation" title="${g.message(code:'link.add.observation')}"> <i class="icon-plus"></i>${g.message(code:'link.add.observation')}</a>

								<a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.observation.multiple.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'observation', action:'bulkCreate', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add Multiple Observations" title="${g.message(code:'title.add.multiple')}"> <i class="icon-plus"></i>${g.message(code:'title.add.multiple')}</a>


                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.list.description')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'checklist', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add a List" title="${g.message(code:'link.add.list')}"> <i class="icon-plus"></i>${g.message(code:'link.add.list')}</a>
 
                                <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.document.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                    data-original-title="Add Document" title="${g.message(code:'link.add.document')}">
                                    <i class="icon-plus"></i> ${g.message(code:'link.add.document')}
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

	<div class="alertMsg ${(flash.message)?'alert':'' }"
		style="clear: both; margin: 0px">
		${flash.message}
	</div>

	<auth:ajaxLogin />
	<div id="fb-root"></div>
    </div>
</div>
