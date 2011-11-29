<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
	<g:if test="${speciesFieldInstance?.description}">
			<!--  content attribution -->
			<div class="attributionBlock">
				<span class="ui-icon-info ui-icon-control " title="Show details"
					style="float: right;"></span>
				<div class="grid_10 ui-corner-all toolbarIconContent attribution"
					style="display: none;">
					<a class="ui-icon ui-icon-close" style="float: right;"></a>
					<table>
						<g:if test="${speciesFieldInstance?.contributors}">
							<tr class="prop">
								<td valign="top" class="grid_1 name">Contributors</td>
								<td valign="top" class="grid_8 value"><g:each
										in="${ speciesFieldInstance?.contributors}" var="contributor">
										<a> ${contributor.name} </a>
									</g:each>
								</td>
							</tr>
						</g:if>
						<g:if test="${speciesFieldInstance?.status}">
							<tr class="prop">
								<td valign="top" class="grid_1 name">Status</td>
								<td valign="top" class="grid_8 value"><a> ${speciesFieldInstance?.status?.value()}
								</a>
								</td>
							</tr>
						</g:if>
						<g:if test="${speciesFieldInstance?.audienceTypes}">
							<tr class="prop">
								<td valign="top" class="grid_1 name">Audiences</td>
								<td valign="top" class="grid_8 value"><g:each
										in="${ speciesFieldInstance?.audienceTypes}"
										var="audienceType">
										<a> ${audienceType.value} </a>
									</g:each>
								</td>
							</tr>
						</g:if>
						<g:if test="${speciesFieldInstance?.licenses.size() > 0}">
							<tr class="prop">
								<td valign="top" class="grid_1 name">Licenses</td>
								<td valign="top" class="grid_8 value"><g:each
										in="${speciesFieldInstance?.licenses}" var="license">
										<a class="license" href="${license?.url}" target="_blank"><img
											class="icon" style="float: left;"
											src="${createLinkTo(dir:'images/license', file: license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
											alt="${license?.name.value()}" /> </a>
									</g:each>
								</td>
							</tr>
						</g:if>

						<!-- attributions -->
						<g:if test="${speciesFieldInstance.attributors.size() > 0}">
							<tr class="prop">
								<td valign="top" class="name grid_1">Attributions</td>
								<td valign="top" class="grid_8">
									<ol>
										<g:each in="${speciesFieldInstance.attributors}" var="r">
											<li style="margin-left: 20px;">
												${r.name}
											</li>
										</g:each>
									</ol>
								</td>
							</tr>
						</g:if>
						
						<!-- references -->
						<g:if test="${speciesFieldInstance.references.size() > 0}">
							<tr class="prop">
								<td valign="top" class="name grid_1">References</td>
								<td valign="top" class="grid_8">
									<ol>
										<g:each in="${speciesFieldInstance.references}" var="r">
											<li style="margin-left: 20px;"><g:if test="${r.url}">
													<a href="${r.url}" target="_blank"> ${r.title?r.title:r.url}
													</a>
												</g:if> <g:else>
													${r.title }
												</g:else>
											</li>
										</g:each>
									</ol>
								</td>
							</tr>
						</g:if>

					</table>
				</div>
			</div>
		</g:if>
</g:if>