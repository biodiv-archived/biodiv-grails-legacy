<div  class="block-tagadelic ">

	<form id="search-projects" method="get"  title="Search"
		action="${uGroup.createLink(controller:(params.controller!='userGroup')?params.controller:'project', action:'search') }"
		class="searchbox">
		<label class="control-label" for="name">Title</label> <input id="title"
			data-provide="typeahead" type="text" class="input-block-level"
			name="title" value="${queryParams['title']?.encodeAsHTML() }"
			placeholder="Search by Project title" />
		<label class="control-label" for="grantee">Grantee</label> <input id="grantee"
			data-provide="typeahead" type="text" class="input-block-level"
			name="grantee" value="${queryParams['grantee']?.encodeAsHTML() }"
			placeholder="Search by Grantee" />


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