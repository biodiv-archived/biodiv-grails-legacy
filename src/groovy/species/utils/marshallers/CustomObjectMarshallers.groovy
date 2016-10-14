package species.utils.marshallers;

import org.apache.commons.logging.LogFactory;

import species.Field;
import species.SpeciesField;
import species.UserGroupTagLib;
import species.Synonyms;
import species.CommonNames;
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
import species.participation.ActivityFeed;
import species.utils.ImageType;
import species.Language;
import species.Contributor;
import species.NamesMetadata.NamePosition;
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
            return ['name': it.name.value(), 'url':it.url]
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
            def commonname = ['id':it.id, 'name':it.name, 'taxonConcept':['id':it.taxonConcept.id], 'isContributor':it.isSpeciesContributor() ];
            if(it.language) {
                commonname ['language'] =  it.language
            }
            return commonname;
        }
        
        JSON.registerObjectMarshaller(TaxonomyDefinition) {
            println 'TaxonomyDefinition Marshaller'
            List<TaxonomyDefinition> defaultHierarchy = it.fetchDefaultHierarchy();
            String parentName = defaultHierarchy ? (defaultHierarchy.size()==1?null:defaultHierarchy[-2].canonicalForm) : null
            String group = defaultHierarchy ?  defaultHierarchy[0].canonicalForm : null
            List<Map> defaultHierarchyMap = defaultHierarchy//.collect { ['id':it.id, 'name':it.name, 'canonicalForm':it.canonicalForm, 'rank':it.rank, 'speciesId':it.speciesId]};
            return ['id':it.id, 'name':it.name, 'canonicalForm': it.canonicalForm, 'italicisedForm':it.italicisedForm, 'rank':TaxonomyRank.list()[it.rank].value(), 'nameStatus' : it.status.value().toLowerCase(), 'sourceDatabase': it.viaDatasource?it.viaDatasource:'', 'defaultHierarchy':defaultHierarchyMap, 'group':it.group, 'parentName':parentName, 'position':it.position.value(), speciesId:it.speciesId]
        }

        JSON.registerObjectMarshaller(Classification) {
            println 'Classification Marshaller'
            return ['id':it.id, 'name':it.name, 'citation':it.citation]
        }

        JSON.registerObjectMarshaller(TaxonomyRegistry) {
            println 'TaxonomyRegistry Marshaller'
            return ['id':it.id, 'classification': ['id':it.classification.id, name : it.classification.name + it.contributors], 'parentTaxon':it.parentTaxon, 'taxonConcept':it.taxonDefinition]
        }
        
        JSON.registerObjectMarshaller(SUser) {
            return ['id':it.id, 'name':it.name, 'icon':it.profilePicture()]
        }
 
        JSON.registerObjectMarshaller(Recommendation) {
            println 'Recommendation Marshaller'
            def r = ['id':it.id, 'name':it.name];
            if(it.taxonConcept) {
                r['taxonomyDefinition'] = it.taxonConcept;
            }
            println 'taxonConcept'
/*            Long speciesId = it.taxonConcept?.findSpeciesId();
            if(speciesId) {
                r['speciesId'] = speciesId;
            }
            println 'speciesId'
*/
            if(it.languageId) {
                r['language'] = Language.read(it.languageId);
            }
            println 'language'
            return r;
        }

        JSON.registerObjectMarshaller(RecommendationVote) {
            println 'RecommendationVote Marshaller'
            def r = [id:it.id, observation:it.observation.id, recommendation:it.recommendation, author:it.author, confidence: it.confidence?.value(), votedOn: it.votedOn];
            if(it.commonNameReco) {
                r['commonNameReco'] = it.commonNameReco
            };
            if(it.comment) {
                r['comment'] = it.comment;
            }
            return r;
        }

        JSON.registerObjectMarshaller(UserGroup) {
            println 'UserGroup Marshaller'
            return ['id':it.id, 'name':it.name, 'description' : it.description, 'domainName':it.domainName, 'webaddress':it.webaddress, 'foundedOn':it.foundedOn, 'icon':it.icon ];
        }

        JSON.registerObjectMarshaller(Resource) {
            println 'Resource Marshaller'
            def basePath = '';
            String originalUrl = it.thumbnailUrl(basePath, null, ImageType.ORIGINAL);
            def imagePath = it.thumbnailUrl(basePath);
            def result = ['id':it.id, 'description':it.description, 'uploader':it.uploader, 'type':it.type.value(), 'uploadTime':it.uploadTime, 'rating':it.rating, 'totalRatings':it.totalRatings?:0, 'averageRating':it.averageRating?:0,'license':it.license, 'contributors':it.contributors, 'attributors': it.attributors, 'annotations':it.fetchAnnotations()];
            if(originalUrl) result['url'] = originalUrl;
            if(imagePath) result['icon'] = imagePath;
            return result;
        }
	
		JSON.registerObjectMarshaller(Comment) {
            println "comment marshaller"
			return ['id':it.id, 'text':it.body, 'author':it.author, 'lastUpdated' : it.lastUpdated, 'commentHolderType':it.commentHolderType];
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
        
        JSON.registerObjectMarshaller(ActivityFeed) {
            def map = [:];
            if(it.activityRootType) map['activityRootType'] = it.activityRootType;
            if(it.rootHolderId) { map['rootHolderId'] = it.rootHolderId; map['rootHolderType'] = it.rootHolderType; }
            map['activityType'] = it.activityType;
            if(it.activityDescrption) map['activityDescription'] = it.activityDescrption;
        
            if(it.activityHolderId) { map['activityHolderId'] = it.activityHolderId; map['activityHolderType'] = it.activityHolderType; }
            if(it.subRootHolderId) { map['subRootHolderId'] = it.subRootHolderId; map['subRootHolderType'] = it.subRootHolderType; }
            map['author'] = it.author;
            
            map['dateCreated'] = it.dateCreated;
            map['lastUpdated'] = it.lastUpdated;
            return map;
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

        JSON.registerObjectMarshaller(NamePosition) {
            return it.value();
        }
        
        JSON.registerObjectMarshaller(Contributor) {
            return ['name':it.name];
        }
    }
    }
}
