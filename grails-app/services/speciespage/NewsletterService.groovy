package speciespage

import org.apache.solr.common.util.NamedList;

class NewsletterService {

    static transactional = false
	def newsletterSearchService

   	def nameTerms(params) {
		List result = new ArrayList();
		
	   def queryResponse = newsletterSearchService.terms(params.term, params.max);
	   NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];

	   for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
		   Map.Entry tag = (Map.Entry) iterator.next();
		   result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Pages"]);
	   }
		return result;
	}
}
