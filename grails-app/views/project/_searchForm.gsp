<div  class="block-tagadelic ">

	<form id="search-projects" method="get"  title="Search"
		action="${uGroup.createLink(controller:'project', action:'list') }"
		class="searchbox">
		<label class="control-label" for="name">Title</label> <input id="title"
			data-provide="typeahead" type="text" class="input-block-level"
			name="title" 
			placeholder="Search by Project title" value="${params.title}" />
			
		<label class="control-label" for="grantee">Grantee</label> <input id="grantee"
			data-provide="typeahead" type="text" class="input-block-level"
			name="grantee"
			placeholder="Search by Grantee" value="${params.grantee}"/>

		<label class="control-label" for="sitename">Site Name</label> <input id="sitename"
			data-provide="typeahead" type="text" class="input-block-level"
			name="sitename"
			placeholder="Search by Site Name" value="${params.sitename}"/>
			
					<label class="control-label" for="corridor">Corridor</label> <input id="corridor"
			data-provide="typeahead" type="text" class="input-block-level"
			name="corridor"
			placeholder="Search by Corridior" value="${params.corridor}" />
			
					<label class="control-label" for="keywords">Keywords</label> <input id="keywords"
			data-provide="typeahead" type="text" class="input-block-level"
			name="keywords"
			placeholder="Search by Keywords" value="${params.keywords}" />

		<g:hiddenField name="offset" value="0" />
		<g:hiddenField name="max" value="12" />
		<g:hiddenField name="fl" value="id" />


	<div class="form-action">
		<button type="submit" id="search-btn"
			class="btn btn-primary pull-right" style="margin-top:10px;">Search</button>
	</div>
	</form>
	<div class="clearfix"></div>

</div>