<div  class="block-tagadelic ">

	<form id="search-ufiles" method="get"  title="Search"
		action="${uGroup.createLink(controller:'UFile', action:'search') }"
		class="searchbox">
		<label class="control-label" for="name">Title</label> <input id="title"
			data-provide="typeahead" type="text" class="input-block-level"
			name="name" 
			placeholder="Search by File title" />
			
		<label class="control-label" for="description">Description</label> <input id="grantee"
			data-provide="typeahead" type="text" class="input-block-level"
			name="description"
			placeholder="Search in Description" />

		
					<label class="control-label" for="keywords">Keywords</label> <input id="keywords"
			data-provide="typeahead" type="text" class="input-block-level"
			name="keywords"
			placeholder="Search by Keywords" />

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