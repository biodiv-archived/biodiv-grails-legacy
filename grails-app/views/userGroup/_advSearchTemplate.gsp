

		<label class="control-label" for="title">Title</label> <input
			id="aq.title" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.title"
			placeholder="Search by UserGroup title" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
		
		<label
			class="control-label" for="grantee">Pages</label> <input
			id="aq.pages" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.pages"
			placeholder="Search by pages" value="${(queryParams?.get('aq.pages'))?.encodeAsHTML()}" />

