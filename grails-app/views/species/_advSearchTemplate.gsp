<%@page import="species.utils.Utils"%>
        <label class="control-label" for="aq.taxon">Taxon Hierarchy</label> <input data-provide="typeahead" id="aq.taxon"
            type="text" class="input-block-level" name="aq.taxon" value="${(queryParams?.get('aq.taxon'))?.encodeAsHTML()}"
            placeholder="Search using taxon hierarchy" />

		<label class="control-label" for="sp_overview">Overview</label> <input id="aq.sp_overview"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.sp_overview" value="${(queryParams?.get('aq.sp_overview'))?.encodeAsHTML() }"
            placeholder="Search by species overview" />

	    <label class="control-label" for="aq.sp_nc">Nomenclature & Classification</label> <input data-provide="typeahead" id="aq.sp_nc"
			type="text" class="input-block-level" name="aq.sp_nc" value="${(queryParams?.get('aq.sp_nc'))?.encodeAsHTML()}"
			placeholder="Search using species nomenculature & classification" />
			
		<label class="control-label" for="aq.sp_nh">Natural History</label> <input data-provide="typeahead" id="aq.sp_nh"
			type="text" class="input-block-level" name="aq.sp_nh" value="${(queryParams?.get('aq.sp_nh'))?.encodeAsHTML()}" 
            placeholder="Field to search species natural history" /> 
        
        <label
			class="control-label" for="aq.sp_hd">Habitat and Distribution</label> <input data-provide="typeahead" id="aq.sp_hd"
			type="text" class="input-block-level" name="aq.sp_hd" value="${(queryParams?.get('aq.sp_hd'))?.encodeAsHTML() }"
			placeholder="Field to search species habitat and distribution" />
			
		<label class="control-label" for="aq.sp_dc">Demography and Conservation</label> <input data-provide="typeahead" id="aq.sp_dc"
			type="text" class="input-block-level" name="aq.sp_dc" value="${(queryParams?.get('aq.sp_dc'))?.encodeAsHTML() }"
            placeholder="Search species demography and conservation" />  

			
		<label class="control-label" for="aq.sp_um">Uses and Management</label> <input data-provide="typeahead" id="aq.sp_um"
			type="text" class="input-block-level" name="aq.sp_um" value="${(queryParams?.get('aq.sp_um'))?.encodeAsHTML() }"
            placeholder="Search species uses and management" />  

		<label class="control-label" for="aq.sp_il">Information Listing</label> <input data-provide="typeahead" id="aq.sp_il"
			type="text" class="input-block-level" name="aq.sp_il" value="${(queryParams?.get('aq.sp_il'))?.encodeAsHTML() }"
            placeholder="Search species information listing" />  


