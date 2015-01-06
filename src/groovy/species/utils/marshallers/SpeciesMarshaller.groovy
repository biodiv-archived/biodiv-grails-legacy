package species.utils.marshallers;

import species.Species;
import species.Synonyms;
import species.CommonNames;
import species.Classification;
import species.TaxonomyDefinition;
import species.TaxonomyRegistry;
import grails.converters.JSON

class SpeciesMarshaller {
    
    void register() {
        JSON.registerObjectMarshaller(Species) { Species species ->

            Map<Classification, List<TaxonomyDefinition>> taxonRegistryMap = species.fetchTaxonomyRegistry();
        
            def taxonRegistryFormattedMap = [];
            
            taxonRegistryMap.each {classification, taxonRegistries ->
                taxonRegistryFormattedMap << ['clasification':classification, 'hierarchies':taxonRegistries]
            }

            def synonyms =  Synonyms.findAllByTaxonConcept(species.taxonConcept);
            def common_names = CommonNames.findAllByTaxonConcept(species.taxonConcept);

            Map result = [
                id : species.id,
                title: species.title,

                taxonConcept : species.taxonConcept,
                taxonRegistry : taxonRegistryFormattedMap,
                
                synonyms : synonyms,
                common_names : common_names,

                group : species.fetchSpeciesGroup(),
                habitat : species.habitat,
                
                createdOn : species.createdOn,
                lastRevised : species.lastUpdated,
                
                thumbnail : species.mainImage()?.thumbnailUrl(null, !species.resources ? '.png':null),
                notes : species.notes(),
                summary : species.summary(),

                fields:species.fields,
                resource : species.listResourcesByRating(),
                userGroups : species.userGroups,
                
//                flagCount : species.flagCount,
                featureCount : species.featureCount,
                featuredNotes : species.featuredNotes()
            ]
            
            /*
            def mainImage = species.mainImage();
            def imagePath = '';
            def speciesGroupIcon =  species.fetchSpeciesGroup().icon(ImageType.ORIGINAL)
            if(mainImage?.fileName == speciesGroupIcon.fileName) 
                imagePath = mainImage.thumbnailUrl(null, '.png');
            else
                imagePath = mainImage?mainImage.thumbnailUrl():null;

            if(imagePath) {
                result['thumbnail'] = imagePath;
            }
            */
            return result;
        }
    }
}
