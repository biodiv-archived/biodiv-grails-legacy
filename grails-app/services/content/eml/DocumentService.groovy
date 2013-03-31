package content.eml

class DocumentService {

    static transactional = true


	Document createDocument(params) {
		
		def document = new Document(params)
		
		document.coverage.location = 'POINT(' + params.coverage.longitude + ' ' + params.coverage.latitude + ')'
		document.coverage.reverseGeocodedName = params.coverage.reverse_geocoded_name
		document.coverage.locationAccuracy = params.coverage.location_accuracy
		
		return document
	}
}
