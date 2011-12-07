<g:if test="${speciesFieldInstance?.description}">

	<!-- sub-category heading -->
	<g:if test="${speciesFieldInstance?.field?.subCategory}">
		<h5 style="margin-bottom: 0px">
			${speciesFieldInstance?.field?.subCategory}
		</h5>
	</g:if>

	<!-- message -->
	<g:showSpeciesFieldToolbar
		model="['speciesFieldInstance':speciesFieldInstance]" />

	<br />
	<!-- icons -->
	<div class="">
		<g:each in="${speciesFieldInstance.resources}" var="r">
			<g:if test="${r.type == species.Resource.ResourceType.ICON}">
				<img class="icon"
					src="${createLinkTo(dir: 'images/icons', file: r.fileName.trim(), absolute:true)}"
					title="${r?.description}" />
			</g:if>
		</g:each>
	</div>

	<!-- species field body -->
	<div>

		<!-- images -->

		<g:if test="${speciesFieldInstance.resources.size()>0}">
			<g:if test="${speciesFieldInstance.resources.size() > 1 }">
			<ul class="thumbwrap" style="width:50%;float:right;">
			</g:if>
			<g:else>
			<ul class="thumbwrap" style="float:right;">
			</g:else>
				<g:each in="${speciesFieldInstance.resources}" var="r">
					<g:if test="${r.type == species.Resource.ResourceType.IMAGE}">
						<li class="figure" style="list-style:none;float: right; max-height: 220px;"><div class="attributionBlock">
							<span class="ui-icon-info ui-icon-control " title="Show details"
								style="float: right;"></span>
							<div class="grid_3 ui-corner-all toolbarIconContent attribution"
								style="display: none;">
								<a class="ui-icon ui-icon-close" style="float: right;"></a>
								<g:imageAttribution model="['resource':r]"/>
							</div>
						</div><div>
								<%def imagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>					
								<a target="_blank"
									href="${createLinkTo(dir: 'images/', file: r.fileName.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}">
									<span class="wrimg"> <span></span> <img
										class="galleryImage"
										src="${createLinkTo(dir: 'images/', file: imagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
										title="${r?.description}" /> </span> 
								</a>
								<span class="caption">
										${r?.description}
								</span>
							</div>
						</li>

					</g:if>
				</g:each>
			</ul>
		</g:if>
		<g:each in="${speciesFieldInstance?.description.split('\n')}"
			var="para">
			<g:if test="${para}">
				<p>
					${para.trim()}
				</p>
			</g:if>

		</g:each>

	</div>
</g:if>
<!-- description -->
<g:if
	test="${speciesFieldInstance?.field.subCategory?.equalsIgnoreCase('Global Distribution Geographic Entity') && speciesInstance.globalDistributionEntities.size()>0}">
	<div>
		<h5>
			${speciesFieldInstance?.field?.subCategory}
		</h5>
	</div>
	<g:showSpeciesFieldToolbar
		model="['speciesFieldInstance':speciesFieldInstance]" />
	<g:each in="${speciesInstance.globalDistributionEntities}">
		<p>
			<span class=""> ${it?.country.countryName} (${it?.country.twoLetterCode})
			</span>
		</p>
	</g:each>
</g:if>
<g:elseif
	test="${speciesFieldInstance?.field.subCategory?.equalsIgnoreCase('Global Endemicity Geographic Entity') && speciesInstance.globalEndemicityEntities.size() > 0}">
	<div>
		<h5>
			${speciesFieldInstance?.field?.subCategory}
		</h5>
	</div>
	<g:showSpeciesFieldToolbar
		model="['speciesFieldInstance':speciesFieldInstance]" />
	<g:each in="${speciesInstance.globalEndemicityEntities}">
		<p>
			<span class=""> ${it?.country.countryName} (${it?.country.twoLetterCode})
			</span>
		</p>
	</g:each>
</g:elseif>
<g:elseif
	test="${!speciesFieldInstance?.description && !speciesFieldInstance?.field?.subCategory}">
	<g:showSpeciesFieldToolbar
		model="['speciesFieldInstance':speciesFieldInstance]" />
</g:elseif>






