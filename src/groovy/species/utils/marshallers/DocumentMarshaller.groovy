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

                placeName : document.placeName,
                reverseGeocodedName : document.reverseGeocodedName,
                geoPrivacy : document.geoPrivacy,
                locationAccuracy : document.locationAccuracy,
                topology : document.topology,
                group : document.group,
                habitat : document.habitat,
                
                fromDate : document.fromDate,
                toDate : document.toDate,
                createdOn : document.createdOn,
                lastRevised : document.lastRevised,
               
                contributors: document.contributors,
                attribution: document.attribution,
                license : document.license,
                doi: document.doi,
                thumbnail : document.mainImage()?.thumbnailUrl(null, null),
                notes : document.notes,
                summary : document.summary(null),

                language: document.language,
                userGroups : document.userGroups,
                
                deleted : document.deleted,

//                visitCount : document.visitCount,
                flagCount : document.flagCount,
                featureCount : document.featureCount,
//                rating : document.rating
            ]

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
