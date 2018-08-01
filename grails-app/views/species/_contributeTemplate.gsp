<%@page import="species.utils.Utils"%>
<%
String supportEmail = "";
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	supportEmail = grailsApplication.config.speciesPortal.wgp.supportEmail;
} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>
		<div class="">
				<div class="section">
                    <p>
                    
                   <g:message code="species.contributetemp.public.access" /> 
                    </p>
				<ol>
	
                    <li  style="margin-bottom:15px;"><b><g:message code="species.contributetemp.online.species" /></b> <g:message code="species.contributetemp.request.permission" />  <br/>
                    <a href="${uGroup.createLink(controller:'species', action:'taxonBrowser')}" class="btn btn-primary"><g:message code="link.request" /></a> <a href="${uGroup.createLink(controller:'species', 'action':'create')}" class="btn btn-primary"><g:message code="button.create.species" /></a>
					</li>

					<li><b><g:message code="species.contributetemp.Offline.creation" /></b> :<g:message code="species.contributetemp.template" /><a
						href="${createLinkTo(dir: '/../static/templates/spreadsheet/', file:'speciesTemplateSimple_v2.xlsx' , base:grailsApplication.config.speciesPortal.resources.serverURL)}"><g:message code="msg.here" /></a>,
						<g:message code="species.contributetemp.spreadsheet.species" /> <span
                            class="mailme">${supportEmail}</span>.<g:message code="species.contributetemp.we.upload" /> 
					</li>
	
				</ol>

                <p>For any technical feedback, please contact Mr. Kezang Wangmo at kwangmo [at] moaf.gov.bt</p>

                <p>For any content feedback, please contact Ms. Sangay Dema at sangaydema [at] moaf.gov.bt</p>

				</div>
	
			</div>
	</div>

