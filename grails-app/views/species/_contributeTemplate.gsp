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
                    
                    Species pages are descriptions of species, with one page for every species. Each species can be described with over 60 standard set of fields. Species pages are contributed and curated by experts. All information published on the Portal will be on public access and under the Creative Commons License of your choice.
                    </p>
				<ol>
	
                    <li  style="margin-bottom:15px;"><b>Online creation of species pages</b> : To create a new species you should have write permission on the page. Permissions can be requested for a taxa. Please request permissions using the buttons below: <br/>
                    <a href="${uGroup.createLink(controller:'species', action:'taxonBrowser')}" class="btn btn-primary">Request Permission</a> <a href="${uGroup.createLink(controller:'species', 'action':'create')}" class="btn btn-primary">Create new species</a>
					</li>

					<li><b>Offline creation of species pages</b> : You may use our spreadsheet template to work offline and create species information. The template consists of one row per species. Please download the xlsx spreadsheet <a
						href="${createLinkTo(dir: '/../static/templates/spreadsheet/', file:'speciesTemplateSimple_v2.xlsx' , base:grailsApplication.config.speciesPortal.resources.serverURL)}">here</a>,
						fill in multiple species descriptions in the spreadsheet, have a
						directory of images, zip the directory and send it to us <span
                            class="mailme">${supportEmail}</span>. We will then upload the content on your behalf.
					</li>
	
				</ol>

                If you have any question please provide feedback and email us
				at <span class="mailme">${supportEmail}</span>
				</div>
	
			</div>
	</div>

