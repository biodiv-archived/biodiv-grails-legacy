package species.utils.marshallers;

import org.apache.commons.logging.LogFactory;

import species.Field;
import species.SpeciesField;
import species.UserGroupTagLib;
import species.Synonyms;
import species.CommonNames;
import species.TaxonomyDefinition;
import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole
import species.groups.SpeciesGroup;
import species.Habitat;
import species.Language;
import species.License;
import species.Reference;
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
import species.ScientificName.TaxonomyRank;
import species.TaxonomyRegistry;
import species.Classification;
import species.Resource;
import species.participation.Comment;
import species.utils.ImageType;
import species.Language;
import content.eml.UFile;

import grails.converters.JSON
import species.participation.Observation
import org.codehaus.groovy.grails.web.converters.marshaller.xml.MapMarshaller;
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException;
import java.util.Map;
import grails.converters.XML;

class CustomObjectMarshallers {
    
    def grailsApplication;
    def userGroupService;

    List marshallers = []

    def register() {

        JSON.createNamedConfig('v1') {
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
                commonname ['language'] =  it.language
            }
            return commonname;
        }
        
        JSON.registerObjectMarshaller(TaxonomyDefinition) {
            return ['id':it.id, 'name':it.name, 'canonicalForm': it.canonicalForm, 'italicisedForm':it.italicisedForm, 'rank':TaxonomyRank.list()[it.rank].value()]
        }

        JSON.registerObjectMarshaller(Classification) {
            return ['id':it.id, 'name':it.name, 'citation':it.citation]
        }

        JSON.registerObjectMarshaller(TaxonomyRegistry) {
            return ['id':it.id, 'classification': ['id':it.classification.id, name : it.classification.name + it.contributors], 'parentTaxon':it.parentTaxon, 'taxonConcept':it.taxonDefinition]
        }
        
        JSON.registerObjectMarshaller(SUser) {
            return ['id':it.id, 'name':it.name, 'icon':it.profilePicture()]
        }
 
        JSON.registerObjectMarshaller(Recommendation) {
            def r = ['id':it.id, 'name':it.name];
            if(it.taxonConcept) {
                r['taxonomyDefinition'] = it.taxonConcept;
            }
            Long speciesId = it.taxonConcept?.findSpeciesId();
            if(speciesId) {
                r['speciesId'] = speciesId;
            }
            if(it.languageId) {
                r['language'] = Language.read(it.languageId);
            }

            return r;
        }

        JSON.registerObjectMarshaller(RecommendationVote) {
            def r = [id:it.id, observation:it.observation.id, recommendation:it.recommendation, author:it.author, confidence: it.confidence.value(), votedOn: it.votedOn];
            if(it.commonNameReco) {
                r['commonNameReco'] = it.commonNameReco
            };
            if(it.comment) {
                r['comment'] = it.comment;
            }
            return r;
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

            return ['id':it.id, url:it.thumbnailUrl(basePath, null, ImageType.ORIGINAL), 'icon' : imagePath, 'uploader':it.uploader, 'type':it.type.value(), 'uploadTime':it.uploadTime, 'rating':it.rating, 'licenses':it.licenses];
        }
	
		JSON.registerObjectMarshaller(Comment) {
            println "comment marshaller"
			return ['id':it.id, 'text':it.body, 'authorId':it.author.id, 'lastUpdated' : it.lastUpdated, 'commentHolderType':it.commentHolderType];
		}

        JSON.registerObjectMarshaller(SpeciesField) {
			return ['id':it.id, 'field':it.field, 'text':it.description, 'dateCreated' : it.dateCreated, 'lastUpdated' : it.lastUpdated, 'licenses':it.licenses, 'audienceTypes':it.audienceTypes, 'resources':it.resources, 'references':it.references, 'contributors':it.contributors, 'attributors':it.attributors, 'uploader':it.uploader, 'uploadTime':it.uploadTime.getTime(), 'language':it.language, 'rating':it.averageRating?:0];
		}

        JSON.registerObjectMarshaller(Field) {
            Map result = [:];
            result['concept'] = it.concept
            if(it.category) result['category'] = it.category;
            if(it.subCategory) result['subCategory'] = it.subCategory;
            result['description'] = it.description
            result['language'] = it.language
            return result;
        }
        
        JSON.registerObjectMarshaller(Reference) {
            Map result = [:];
            if(it.title) result['title'] = it.title;
            if(it.url) result['url'] = it.url;
            return result;
        }

        JSON.registerObjectMarshaller(Language) {
            return ['id':it.id, 'name':it.name, 'threeLetterCode':it.threeLetterCode, 'twoLetterCode':it.twoLetterCode]
        }
 
        JSON.registerObjectMarshaller(UFile) {
            return ['path': grailsApplication.config.speciesPortal.content.serverURL + it.path, 'size':it.size, 'mimetype':it.mimetype]
        }

        XML.registerObjectMarshaller(new MapMarshaller() {
            public String getElementName(Object o) {
                return "response";
            }
            public void marshalObject(Object o, XML xml) throws ConverterException {
                Map<Object,Object> map = (Map<Object,Object>) o;
                for (Map.Entry<Object,Object> entry : map.entrySet()) {
                    xml.startNode(entry.getKey().toString());
                    xml.convertAnother(entry.getValue());
                    xml.end();
                }
            }
        })
    }
    }
}
