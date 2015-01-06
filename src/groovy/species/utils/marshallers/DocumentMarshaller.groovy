package species.utils.marshallers;

import content.eml.*;
import grails.converters.JSON

class DocumentMarshaller {
   
    def grailsApplication;

    void register() {

        JSON.registerObjectMarshaller(Document) { Document document ->

            Map result = [
                id : document.id,
                title: document.title,
                type: document.type,
                author: document.author,
                createdOn : document.createdOn,
                lastRevised : document.lastRevised,
                license : document.license
            ]
            if(document.placeName)
                result['placeName'] = document.placeName
            if(document.reverseGeocodedName)
                result['reverseGeocodedName'] = document.reverseGeocodedName
            if(document.geoPrivacy)
                result['geoPrivacy'] = document.geoPrivacy
            if(document.locationAccuracy)
                result['locationAccuracy'] = document.locationAccuracy
            if(document.topology)
                result['topology'] = document.topology
            if(document.group)
                result['group'] = document.group
            if(document.habitat)
                result['habitat'] = document.habitat
                
              
            if(document.contributors)
                result['contributors'] = document.contributors
            if(document.attribution)
                result['attribution'] = document.attribution
            if(document.doi)
                result['doi'] = document.doi
                result['thumbnail'] = document.mainImage()?.thumbnailUrl(null, null)
            if(document.notes)
                result['notes'] = document.notes
                result['summary'] = document.summary(null)

                result['language'] = document.language
            if(document.userGroups)
                result['userGroups'] = document.userGroups
                
//                visitCount : document.visitCount,
                result['flagCount'] = document.flagCount
                result['featureCount'] = document.featureCount
//                rating : document.rating

            if(document.uFile && !document.uFile.deleted) {
                result['uFile'] = document.uFile
            }

            if(document.uri) {
                result['uri'] = document.uri;
            }

            return result;
        }

    }
}
