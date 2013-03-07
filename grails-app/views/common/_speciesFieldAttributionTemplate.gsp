<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
	<g:if test="${speciesFieldInstance?.description}">
			<!--  content attribution -->
			<div class="attributionContent" style="display:none;">
							
						<!-- attributions -->
						<g:if test="${speciesFieldInstance.attributors.size() > 0}">
							<div class="prop span11">
								<div class="name span2">Attributions</div>
								<div class="span8">
									<ul style="list-style:none;margin-left:0px;">
										<g:each in="${speciesFieldInstance.attributors}" var="r">
											<li>
												<a href="#" class="editField" data-type="text" data-pk="${speciesFieldInstance.id}" data-params="{cid:${r.id}}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="attributor" data-original-title="Edit attributor name">${r.name}
												</a>
											</li>
										</g:each>
									</ul>
								</div>
							</div>
						</g:if>
					
						<g:if test="${speciesFieldInstance?.contributors}">
							<div class="prop span11">
								<div class="span2 name">Contributors</div>
								<div class="span8 value">
								<ul style="list-style:none;margin-left:0px;"><g:each
										in="${ speciesFieldInstance?.contributors}" var="contributor">
										<li>
										<a href="#" class="editField" data-type="text" data-pk="${speciesFieldInstance.id}" data-params="{cid:${contributor.id}}"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="contributor" data-original-title="Edit contributor name">${contributor.name}</a>
										</li> 
									</g:each>
								</ul>
								</div>
							</div>
						</g:if>
						<g:if test="${speciesFieldInstance?.status}">
							<div class="prop span11">
								<div class="span2 name">Status</div>
								<div class="span8 value">${speciesFieldInstance?.status?.value()}
								
								</div>
							</div>
						</g:if>
						<g:if test="${speciesFieldInstance?.audienceTypes}">
							<div class="prop span11">
								<div class="span2 name">Audiences</div>
								<div class="span8 value"><g:each
										in="${ speciesFieldInstance?.audienceTypes}"
										var="audienceType">
										${audienceType.value}
									</g:each>
								</div>
							</div>
						</g:if>
						<g:if test="${speciesFieldInstance?.licenses.size() > 0}">
							<div class="prop span11">
								<div class="span2 name">Licenses</div>
								<div class="span8 value"><g:each
										in="${speciesFieldInstance?.licenses}" var="license">
										<a class="license" href="${license?.url}" target="_blank"><img
											class="icon" style="float: left;"
											src="${createLinkTo(dir:'images/license', file: license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
											alt="${license?.name.value()}" /> </a>
									</g:each>
								</div>
							</div>
						</g:if>

					
						<!-- references -->
						<g:if test="${speciesFieldInstance.references.size() > 0}">
				<div class="prop span11">
					<div class="name span2">References</div>
					<div class="span8">
						<ol>
							<g:each in="${speciesFieldInstance.references}" var="r">
								<li style="margin-left: 20px;" title="${r.title?:r.url}"><g:if
										test="${r.url}">
										<a href="${r.url}" target="_blank"> ${r.title?r.title:r.url}
										</a>
									</g:if> <g:else>
										${r.title }
									</g:else></li>
							</g:each>
						</ol>
					</div>
				</div>
			</g:if>

				</div>
		</g:if>
</g:if>