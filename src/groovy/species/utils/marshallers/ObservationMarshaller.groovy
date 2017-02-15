package species.utils.marshallers;

import species.participation.Observation;
import grails.converters.JSON

class ObservationMarshaller {
    
    void register() {
       JSON.registerObjectMarshaller( Observation) { Observation obv ->
           println "Observation Marshaller"
            Map result = [
                id : obv.id,
                title: obv.fetchFormattedSpeciesCall(),

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

                thumbnail : obv.mainImage()?.thumbnailUrl(null, !obv.resource || obv.dataset ? '.png':null),
                notes : obv.notes,
                summary : obv.summary(),

                rating : obv.rating,


                resource : obv.listResourcesByRating(),
                userGroups : obv.userGroups,
                traits : obv.getTraits(),
                language : obv.language,

                isDeleted : obv.isDeleted,
                isLocked : obv.isLocked,
                isChecklist : obv.isChecklist,

                visitCount : obv.visitCount,
                flagCount : obv.flagCount,
                featureCount : obv.featureCount,
                noOfIdentifications : obv.noOfIdentifications,
                noOfImages : obv.noOfImages,
                noOfVideos : obv.noOfVideos,
                noOfAudio : obv.noOfAudio
            ]
            println result.fromDate
                //recommendationVote : obv.recommendationVote,
println "main obv json"
            if(obv.dataset) {
                result['dataset_id'] = obv.dataset.id;
            }

            if(obv.isChecklist && obv.isShowable) {
                result['checklist_id'] = obv.sourceId;
                result['checklistAnnotations'] = obv.checklistAnnotations;
            }

println "bfr maxReco"
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
