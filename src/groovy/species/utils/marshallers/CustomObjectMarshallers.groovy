package species.utils.marshallers;

import org.apache.commons.logging.LogFactory;

import species.Field;
import species.UserGroupTagLib;
import species.Synonyms;
import species.CommonNames;
import species.TaxonomyDefinition;
import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole
import species.groups.SpeciesGroup;
import species.Habitat;
import species.License;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.UserToken;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter;
import grails.converters.JSON;
import species.participation.Featured;
import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;
import species.TaxonomyRegistry;
import species.Classification;
import species.Resource;
import species.participation.Comment;

import grails.converters.JSON
import species.participation.Observation

class CustomObjectMarshallers {
    
    def grailsApplication;
    def userGroupService;

    List marshallers = []

    def register() {
        marshallers.each{ it.register() }

        JSON.registerObjectMarshaller(Geometry) {
            String geomStr = "error"
            WKTWriter wkt = new WKTWriter();
            try {
                geomStr = wkt.write(it);
            } catch(Exception e) {
                log.error "Error writing polygon wkt : ${it}"
            }
            return geomStr;
        }

        JSON.registerObjectMarshaller(SpeciesGroup) {
            return ['id':it.id, 'name': it.name, 'groupOrder':it.groupOrder]
        }

        JSON.registerObjectMarshaller(Habitat) {
            return ['id':it.id, 'name': it.name, 'habitatOrder':it.habitatOrder]
        }

        JSON.registerObjectMarshaller(License) {
            return ['name': it.name.value(), 'url':it.name]
        }

        JSON.registerObjectMarshaller(Featured) {
            if(it.userGroup) 
                return ['createdOn':it.createdOn, 'notes': it.notes, 'userGroupId':it.userGroup.id, 'userGroupName':it.userGroup.name, 'userGroupUrl':userGroupService.userGroupBasedLink(['mapping':'userGroup', 'controller':'userGroup', 'action':'show', 'userGroup':it.userGroup])]
            else
                return ['createdOn':it.createdOn, 'notes': it.notes]
        }

        JSON.registerObjectMarshaller(Synonyms) {
            def syn =  ['id':it.id, 'name':it.name,  'canonicalForm': it.canonicalForm, 'italicisedForm':it.italicisedForm, 'taxonConcept':['id':it.taxonConcept.id], 'isContributor':it.isContributor()]
            if(it.relationship) {
                syn['relationship'] = ['name':it.relationship.value()] 
            }
            return syn;
        }

        JSON.registerObjectMarshaller(CommonNames) {
            def commonname = ['id':it.id, 'name':it.name, 'taxonConcept':['id':it.taxonConcept.id], 'isContributor':it.isContributor() ];
            if(it.language) {
                commonname ['language'] =  ['id':it.language.id, 'name':it.language.name]
            }
            return commonname;
        }
        
        JSON.registerObjectMarshaller(TaxonomyDefinition) {
            return ['id':it.id, 'name':it.name, 'canonicalForm': it.canonicalForm, 'italicisedForm':it.italicisedForm, 'rank':TaxonomyRank.list()[it.rank].value()]
        }

        JSON.registerObjectMarshaller(Classification) {
            return ['id':it.classification.id, name : it.classification.name]
        }

        JSON.registerObjectMarshaller(TaxonomyRegistry) {
            return ['id':it.id, 'classification': ['id':it.classification.id, name : it.classification.name + it.contributors], 'parentTaxon':it.parentTaxon, 'taxonConcept':it.taxonDefinition]
        }
        
        JSON.registerObjectMarshaller(SUser) {
            return ['id':it.id, 'name':it.name, 'email': it.email, 'icon':it.profilePicture()]
        }
 
        JSON.registerObjectMarshaller(Recommendation) {
            return ['name':it.name, 'taxonomyDefinition' : it.taxonConcept];
        }

        JSON.registerObjectMarshaller(RecommendationVote) {
            return [recommendation:it.recommendation, commonNameReco:it.commonNameReco, author:it.author, confidence: it.confidence.value(), votedOn: it.votedOn, comment:it.comment] 
        }

        JSON.registerObjectMarshaller(UserGroup) {
            return ['id':it.id, 'name':it.name, 'description' : it.description, 'domainName':it.domainName, 'webaddress':it.webaddress, 'foundedOn':it.foundedOn, 'icon':it.icon ];
        }

        JSON.registerObjectMarshaller(Resource) {
            def basePath = '';
            
            if(it.context?.value() == Resource.ResourceContext.OBSERVATION.toString()){
                basePath = grailsApplication.config.speciesPortal.observations.serverURL
            }
            else if(it.context?.value() == Resource.ResourceContext.SPECIES.toString() || it?.context?.value() == Resource.ResourceContext.SPECIES_FIELD.toString()){
                basePath = grailsApplication.config.speciesPortal.resources.serverURL
            }

            def imagePath = it.thumbnailUrl(basePath);

            return ['id':it.id, url:it.url, 'icon' : imagePath, 'uploader':it.uploader, 'type':it.type.value(), 'uploadTime':it.uploadTime, 'rating':it.rating];
        }
	
		JSON.registerObjectMarshaller(Comment) {
			return ['id':it.id, 'text':it.body, 'authorId':it.author.id, 'lastUpdated' : it.lastUpdated, 'commentHolderType':it.commentHolderType];
		}

    }
}
