<%@page import="species.utils.Utils"%>
<div id="ibp-footer" class="gradient-bg-reverse navbar">
	<div class="container outer-wrapper" style="width:940px">
		<div class="links_box_column">
			<ul>
				<li
					class=" nav-header bold${(params.controller == 'species')?' active':''}"><a
					href="${uGroup.createLink(controller:'species', action:'list')}"
					title="Species">All Species</a>
				</li>
				<li
					class="nav-header bold ${(request.getHeader('referer')?.contains('/map'))?' active':''}"><a
					href="${ '/map'}" title="Maps">All Maps</a></li>
				<li
					class=" nav-header bold${(params.controller == 'checklist')?' active':''}"><a
					href="${uGroup.createLink(controller:'checklist', action:'list')}" title="Checklists">All Checklists</a></li>
				<!-- li
					class="nav-header bold ${(params.controller == 'userGroup' && params.action== 'list')?' active':''}"><a
					href="${ uGroup.createLink(controller:"userGroup", "action":"list")}"
					title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality.">All
						Groups<sup>Beta</sup> </a>
				</li-->
			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"><a href='/theportal'>The Portal</a></li>
				<li><a href="${ '/biodiversity_in_india'}">Biodiversity in India</a>
				</li>
				<li><a href="${ '/about/whats-new'}">What's new?</a></li>
				<li><a href="${ '/about/technology'}">Technology</a></li>
				<li><a href="${ '/help/faqs'}">FAQ</a></li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"><a href='/people'>People</a></li>
				<li><a href="${ '/people/partners'}">Partners</a></li>
				<li><a href="${ '/people/donors'}">Donors</a></li>
				<li><a href="${ '/people/fraternity'}">Fraternity</a></li>
				<li><a href="${ '/people/team'}">Team</a></li>
			</ul>
		</div>

		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"><a href='/policy'>Policy</a>
				</li>
				<li><a href="${ '/policy/data_sharing'}">Data Sharing</a>
				</li>
				<li><a href="${ '/licenses'}">Licenses</a>
				</li>
				<li><a href="${ '/terms'}">Terms & Conditions</a>
				</li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold" style="color:#5E5E5E">Others</li>
				<li><a href="${ '/sitemap'}">Sitemap</a>
				</li>
				<li><a href="${ '/feedback_form'}">Feedback</a>
				</li>
				<li><a href="${ '/contact'}">Contact Us</a>
				</li>

			</ul>
		</div>



	</div>
</div>
<r:script>
$(document).ready(function(){
	 last_actions();
});
</r:script>