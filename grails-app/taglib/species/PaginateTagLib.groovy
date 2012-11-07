package species

import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.springframework.web.servlet.support.RequestContextUtils;

import species.groups.SpeciesGroup;

class PaginateTagLib {
	
	def grailsApplication;
	static namespace = "p"

	def paginateOnSpeciesGroup = { attrs ->
		def writer = out
		
		def messageSource = grailsAttributes.messageSource
		def locale = RequestContextUtils.getLocale(request)

		def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))

		def linkParams = [:]
		if (attrs.params) linkParams.putAll(attrs.params)
		if (params.sort) linkParams.sort = params.sort
		if (params.order) linkParams.order = params.order

		def linkTagAttrs = [action:action]
		if (attrs.controller) {
			linkTagAttrs.controller = attrs.controller
		}
		if (attrs.id != null) {
			linkTagAttrs.id = attrs.id
		}
		if (attrs.fragment != null) {
			linkTagAttrs.fragment = attrs.fragment
		}
		linkTagAttrs.params = linkParams

		def speciesGroupId = params.startsWith ?: SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id;
		
		// display paginate steps
		SpeciesGroup.list().each { sGroup ->
			if (sGroup.id == speciesGroupId) {
				writer << "<span class=\"currentStep\">${sGroup.name}</span>"
			}
			else {
				//linkParams.offset = (i - 1) * max
				linkParams.sGroup = sGroup.id;
				writer << link(linkTagAttrs.clone()) {sGroup.name}
			}
		}
	}
	
	def paginateOnAlphabet = { attrs ->
		def writer = out
		if (attrs.total == null) {
			throwTagError("Tag [paginate] is missing required attribute [total]")
		}
		def messageSource = grailsAttributes.messageSource
		def locale = RequestContextUtils.getLocale(request)

		def total = attrs.int('total') ?: 0
		def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))

		def linkParams = attrs
		
		if (attrs.params) linkParams.putAll(attrs.params)
		if (params.sort) linkParams.sort = params.sort
		if (params.order) linkParams.order = params.order

		
		def linkTagAttrs = linkParams
		linkTagAttrs['action'] = action;
		linkTagAttrs['total'] = total;
		if (attrs.controller) {
			linkTagAttrs.controller = attrs.controller
		}
		if (attrs.id != null) {
			linkTagAttrs.id = attrs.id
		}
		if (attrs.fragment != null) {
			linkTagAttrs.fragment = attrs.fragment
		}
		

		String startsWith = params.startsWith ?: "A-Z";
		int currentstep = startsWith.charAt(0) - 65;
		if(startsWith == "A-Z") {
			currentstep = -1;
		}
		// display paginate steps
		(-1..25).each { i ->
			if(i == -1 && currentstep == i) {
				writer << "<span class=\"currentStep\">A-Z</span>"
			}
			else if (currentstep == i) {
				writer << "<span class=\"currentStep\">${Character.toChars(i+65)[0]}</span>"
			}
			else {
				if(i==-1) {
					linkParams.startsWith = 'A-Z';
					writer << link([url:uGroup.createLink(linkTagAttrs.clone())]) {'A-Z'}
				} else {
					//linkParams.offset = (i - 1) * max
					linkParams.startsWith = Character.toChars(i+65)[0];
					writer << link([url:uGroup.createLink(linkTagAttrs.clone())]) {Character.toChars(i+65)[0]}
				}
			}
		}
	}
	
	/**
	* Creates next/previous links to support pagination for the current controller.<br/>
	*
	* &lt;g:paginate total="${Account.count()}" /&gt;<br/>
	*
	* @attr total REQUIRED The total number of results to paginate
	* @attr action the name of the action to use in the link, if not specified the default action will be linked
	* @attr controller the name of the controller to use in the link, if not specified the current controller will be linked
	* @attr id The id to use in the link
	* @attr params A map containing request parameters
	* @attr prev The text to display for the previous link (defaults to "Previous" as defined by default.paginate.prev property in I18n messages.properties)
	* @attr next The text to display for the next link (defaults to "Next" as defined by default.paginate.next property in I18n messages.properties)
	* @attr max The number of records displayed per page (defaults to 10). Used ONLY if params.max is empty
	* @attr maxsteps The number of steps displayed for pagination (defaults to 10). Used ONLY if params.maxsteps is empty
	* @attr offset Used only if params.offset is empty
	* @attr fragment The link fragment (often called anchor tag) to use
	*/
   def paginateOnSearchResult = { attrs ->
	   def writer = out
	   if (attrs.total == null) {
		   throwTagError("Tag [paginate] is missing required attribute [total]")
	   }

	   def messageSource = grailsAttributes.messageSource
	   def locale = RequestContextUtils.getLocale(request)

	   def total = attrs.int('total') ?: 0
	   def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))
	   def start = params.int('start') ?: 0
	   def rows = params.int('rows')
	   def maxsteps = (attrs.int('maxsteps') ?: 10)

	   if (!start) start = (attrs.int('start') ?: 0)
	   if (!rows) rows = (attrs.int('rows') ?: 10)

	   def linkParams = [:]
	   if (attrs.params) linkParams.putAll(attrs.params)
	   linkParams.start = start - rows
	   linkParams.rows = rows
	   if (params.sort) linkParams.sort = params.sort
	   if (params.order) linkParams.order = params.order

	   def linkTagAttrs = [action:action]
	   if (attrs.controller) {
		   linkTagAttrs.controller = attrs.controller
	   }
	   if (attrs.id != null) {
		   linkTagAttrs.id = attrs.id
	   }
	   if (attrs.fragment != null) {
		   linkTagAttrs.fragment = attrs.fragment
	   }
	   linkTagAttrs.params = linkParams

	   // determine paging variables
	   def steps = maxsteps > 0
	   int currentstep = (start / rows) + 1
	   int firststep = 1
	   int laststep = Math.round(Math.ceil(total / rows))

	   // display previous link when not on firststep
	   if (currentstep > firststep) {
		   linkTagAttrs.class = 'prevLink'
		   linkParams.start = start - rows
		   writer << link(linkTagAttrs.clone()) {
			   (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
		   }
	   }

	   // display steps when steps are enabled and laststep is not firststep
	   if (steps && laststep > firststep) {
		   linkTagAttrs.class = 'step'

		   // determine begin and endstep paging variables
		   int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
		   int endstep = currentstep + Math.round(maxsteps / 2) - 1

		   if (beginstep < firststep) {
			   beginstep = firststep
			   endstep = maxsteps
		   }
		   if (endstep > laststep) {
			   beginstep = laststep - maxsteps + 1
			   if (beginstep < firststep) {
				   beginstep = firststep
			   }
			   endstep = laststep
		   }

		   // display firststep link when beginstep is not firststep
		   if (beginstep > firststep) {
			   linkParams.start = 0
			   writer << link(linkTagAttrs.clone()) {firststep.toString()}
			   writer << '<span class="step">..</span>'
		   }

		   // display paginate steps
		   (beginstep..endstep).each { i ->
			   if (currentstep == i) {
				   writer << "<span class=\"currentStep\">${i}</span>"
			   }
			   else {
				   linkParams.start = (i - 1) * rows
				   writer << link(linkTagAttrs.clone()) {i.toString()}
			   }
		   }

		   // display laststep link when endstep is not laststep
		   if (endstep < laststep) {
			   writer << '<span class="step">..</span>'
			   linkParams.start = (laststep -1) * rows
			   writer << link(linkTagAttrs.clone()) { laststep.toString() }
		   }
	   }

	   // display next link when not on laststep
	   if (currentstep < laststep) {
		   linkTagAttrs.class = 'nextLink'
		   linkParams.start = start + rows
		   writer << link(linkTagAttrs.clone()) {
			   (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
		   }
	   }
   }
   
   def paginate = { attrs ->
	   println attrs;
	   def writer = out
	   if (attrs.total == null) {
		   throwTagError("Tag [paginate] is missing required attribute [total]")
	   }

	   def messageSource = grailsAttributes.messageSource
	   def locale = RCU.getLocale(request)

	   def total = attrs.int('total') ?: 0
	   def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))
	   def offset = params.int('offset') ?: 0
	   def max = params.int('max')
	   def maxsteps = (attrs.int('maxsteps') ?: 10)

	   if (!offset) offset = (attrs.int('offset') ?: 0)
	   if (!max) max = (attrs.int('max') ?: 10)

	   def linkParams = [:]
	   if (attrs.params) linkParams.putAll(attrs.params)
	   linkParams.offset = offset - max
	   linkParams.max = max
	   if (params.sort) linkParams.sort = params.sort
	   if (params.order) linkParams.order = params.order

	   def linkTagAttrs = [action:action]
	   linkTagAttrs.putAll(linkParams)
	   linkTagAttrs.remove('offset')
	   if (attrs.controller) {
		   linkTagAttrs.controller = attrs.controller
	   }
	   if (attrs.id != null) {
		   linkTagAttrs.id = attrs.id
	   }
	   if (attrs.fragment != null) {
		   linkTagAttrs.fragment = attrs.fragment
	   }
	   if (attrs.userGroup) {
		   linkTagAttrs.userGroup = attrs.userGroup
	   }
	   if (attrs.userGroupWebaddress) {
		   linkTagAttrs.userGroupWebaddress = attrs.userGroupWebaddress
	   }
	   

	   // determine paging variables
	   def steps = maxsteps > 0
	   int currentstep = (offset / max) + 1
	   int firststep = 1
	   int laststep = Math.round(Math.ceil(total / max))
	   println "==="
	   println linkTagAttrs;
	   println uGroup.createLink(linkTagAttrs.clone())
	   
	   // display previous link when not on firststep
	   if (currentstep > firststep) {
		   linkTagAttrs.class = 'prevLink'
		   linkTagAttrs.offset = offset - max
		   def temp = linkTagAttrs.clone();
		   temp['url'] = uGroup.createLink(temp)
		   writer << link(temp) {
			   (attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
		   }
	   }

	   // display steps when steps are enabled and laststep is not firststep
	   if (steps && laststep > firststep) {
		   linkTagAttrs.class = 'step'

		   // determine begin and endstep paging variables
		   int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
		   int endstep = currentstep + Math.round(maxsteps / 2) - 1

		   if (beginstep < firststep) {
			   beginstep = firststep
			   endstep = maxsteps
		   }
		   if (endstep > laststep) {
			   beginstep = laststep - maxsteps + 1
			   if (beginstep < firststep) {
				   beginstep = firststep
			   }
			   endstep = laststep
		   }

		   // display firststep link when beginstep is not firststep
		   if (beginstep > firststep) {
			   linkParams.offset = 0
			   def temp = linkTagAttrs.clone();
			   temp['url'] = uGroup.createLink(temp)
			   writer << link(temp) {firststep.toString()}
			   writer << '<span class="step">..</span>'
		   }

		   // display paginate steps
		   (beginstep..endstep).each { i ->
			   if (currentstep == i) {
				   writer << "<span class=\"currentStep\">${i}</span>"
			   }
			   else {
				   linkTagAttrs.offset = (i - 1) * max
				   def temp = linkTagAttrs.clone();
				   temp['url'] = uGroup.createLink(temp)
				   writer << link(temp) {i.toString()}
			   }
		   }

		   // display laststep link when endstep is not laststep
		   if (endstep < laststep) {
			   writer << '<span class="step">..</span>'
			   linkTagAttrs.offset = (laststep -1) * max
			   def temp = linkTagAttrs.clone();
			   temp['url'] = uGroup.createLink(temp)
			   writer << link(temp) { laststep.toString() }
		   }
	   }

	   // display next link when not on laststep
	   if (currentstep < laststep) {
		   linkTagAttrs.class = 'nextLink'
		   linkTagAttrs.offset = offset + max
		   def temp = linkTagAttrs.clone();
		   temp['url'] = uGroup.createLink(temp)
		   writer << link(temp) {
			   (attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
		   }
	   }
   }

}
