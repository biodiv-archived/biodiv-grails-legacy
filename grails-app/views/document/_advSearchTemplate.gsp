

		<label class="control-label" for="title">Title</label> <input
			id="aq.title" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.title"
			placeholder="Search by Document title" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
		
		<label
			class="control-label" for="grantee">Type</label> <input
			id="aq.type" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.type"
			placeholder="Search by Description" value="${(queryParams?.get('aq.type'))?.encodeAsHTML()}" />

