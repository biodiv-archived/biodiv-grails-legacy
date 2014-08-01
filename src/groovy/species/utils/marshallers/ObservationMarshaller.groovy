package species.utils.marshallers;

import species.participation.Observation;
import grails.converters.JSON

class ObservationMarshaller {
    
    void register() {
       JSON.registerObjectMarshaller( Observation) { Observation obv ->
            Map result = [
                id : obv.id,
                placeName : obv.placeName,
                reverseGeocodedName : obv.reverseGeocodedName,
                geoPrivacy : obv.geoPrivacy,
                locationAccuracy : obv.locationAccuracy,
                topology : obv.topology,
                group : obv.group,
                habitat : obv.habitat,
                
                fromDate : obv.fromDate,
                toDate : obv.toDate,
                createdOn : obv.createdOn,
                lastRevised : obv.lastRevised,
                
                author : obv.author,

                thumbnail : obv.mainImage()?.thumbnailUrl(null, !obv.resource ? '.png':null),
                notes : obv.notes,
                summary : obv.summary(),

                rating : obv.rating,


                resource : obv.listResourcesByRating(),
                recommendationVote : obv.recommendationVote,
                userGroups : obv.userGroups,
                annotations : obv.annotations,

                isDeleted : obv.isDeleted,
                isLocked : obv.isLocked,
                isChecklist : obv.isChecklist,

                visitCount : obv.visitCount,
                flagCount : obv.flagCount,
                featureCount : obv.featureCount
            ]

            if(obv.isChecklist && obv.isShowable) {
                result['checklistAnnotations'] = obv.checklistAnnotations;
            }

            Map maxVotedReco = new HashMap();
            if(obv.maxVotedReco) {
                if(obv.maxVotedReco.isScientificName) {
                    maxVotedReco['sciNameReco'] = obv.maxVotedReco
                }
                
                def commonNamesRecoList = obv.suggestedCommonNames(obv.maxVotedReco.id);
                if(commonNamesRecoList) {
                    def cNRList = [];
                    commonNamesRecoList.values().each {
                        cNRList += it
                    }
                    maxVotedReco['commonNamesRecoList'] = cNRList
                }
 
            }
            result['maxVotedReco'] = maxVotedReco;
 
            return result;
        }
    }
}
