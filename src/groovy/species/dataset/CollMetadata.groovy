package species.dataset

import java.util.Date;
import grails.converters.JSON

import org.grails.rateable.Rateable;
import org.grails.taggable.Taggable;

import com.vividsolutions.jts.geom.Geometry;

import content.eml.UFile;

import species.Language;
import species.auth.SUser;
import speciespage.ObservationService;
import species.sourcehandler.XMLConverter;
import species.auth.SUser;
import species.License;
import species.License.LicenseType;

import species.Contributor;
import org.hibernatespatial.GeometryUserType
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.PrecisionModel;

import species.Metadata;
import species.Metadata.DateAccuracy;
import species.utils.Utils;
import species.Language;
import species.groups.UserGroup;
import species.groups.CustomField;
import species.participation.Featured;
import species.dataset.DataPackage.SupportingModules;

/**
 * @author sravanthi
 *
 */
abstract class CollMetadata implements Taggable, Rateable {

	String title;
	String description;

	//EML-Access fields
	Access access;

	//EML-Party fields
	Party party;
    SUser uploader;

	//EML-Coverage fields
	GeographicalCoverage geographicalCoverage;
	TemporalCoverage temporalCoverage;
	TaxonomicCoverage taxonomicCoverage;

	//EML-Project
	String project;
	
	//EML-Methods;
	String methods;

    //CustomFields
    String customFields;

	Date createdOn = new Date();
	Date lastRevised = new Date();

	Language language;

	String externalId;
	String externalUrl;
	String viaId;
	String viaCode;

	int rating;
	int flagCount = 0;
	int featureCount = 0;
	
	boolean isDeleted = false;

	//EML-physical
//	UFile uFile;
    def utilsService;
    def springSecurityService;
    def grailsApplication;
    def commentService;

    static embedded = ['access', 'party', 'geographicalCoverage', 'temporalCoverage', 'taxonomicCoverage'];

	static constraints = {
		title nullable:false, blank:false;
		description nullable:false, blank:false, type:'text';
		language nullable:false
		externalId nullable:true
		externalUrl nullable:true
		viaId nullable:true
		viaCode nullable:true
		
        project nullable:true;
		methods nullable:true;
		
		uFile nullable:true;
		customFields nullable:true;
	}

	static mapping = {
		description type:'text'
		customFields type:'text'
		tablePerHierarchy false
		//        tablePerSubClass true
	}

	def beforeInsert(){
	}

	def beforeUpdate(){
	}

    def initParams(params) {

        XMLConverter xmlConverter = new XMLConverter();

        //Party
        this.uploader = springSecurityService.currentUser;
        if(params.author)  {
            log.debug "Setting access to ${params.author}"
            this.party = new Party(uploaderId:params.author.id);
        } else {
            log.debug "Setting access to ${springSecurityService.currentUser}"
            this.party = new Party(uploaderId:springSecurityService.currentUser.id);
        }

        if(params.contributorUserIds)  {
           this.party.contributorId = SUser.read(Long.parseLong(params.contributorUserIds)).id;
        } else {
            this.party.contributorId = springSecurityService.currentUser.id; 
        }
        
        if(params.attributions)  {
           this.party.attributions = params.attributions;
        }

        //Access
        String licenseStr = params.licenseName?:params.licenseName
        if(licenseStr) {
            log.debug "Setting access to ${licenseStr}"
            this.access = new Access(licenseId : xmlConverter.getLicenseByType(licenseStr, false).id)
        } else {
            log.debug "Setting access to ${LicenseType.CC_BY}"
            this.access = new Access(licenseId : xmlConverter.getLicenseByType(LicenseType.CC_BY, false).id);
        }

        //geographicalCoverage
        if((params.latitude && params.longitude) || params.areas) {
            this.geographicalCoverage = new GeographicalCoverage([placeName:params.placeName]);
            if(params.latitude) 
                this.geographicalCoverage.latitude = Double.parseDouble(params.latitude);
            if(params.longitude)
                this.geographicalCoverage.longitude = Double.parseDouble(params.longitude);

            def locScale =  Metadata.LocationScale.getEnum(params.locationScale)
            this.geographicalCoverage.locationScale = locScale?:Metadata.LocationScale.APPROXIMATE
            this.geographicalCoverage.geoPrivacy = params.geoPrivacy ? (params.geoPrivacy.trim().toLowerCase().toBoolean()):false;
            this.geographicalCoverage.locationAccuracy = params.locationAccuracy;

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
            if(params.topology) {
                this.geographicalCoverage.topology = params.topology;
            }
            else {
                params.areas = params.areas?:params.topology;
                if(params.areas) {
                    log.debug "Setting topology ${params.areas}"
                    WKTReader wkt = new WKTReader(geometryFactory);
                    try {
                        Geometry geom = wkt.read(params.areas);
                        this.geographicalCoverage.topology = geom;
                    } catch(ParseException e) {
                        log.error "Error parsing polygon wkt : ${params.areas}"
                    }
                }
            }
        }

        //temporalCoverage
        DateAccuracy dateAccuracy =  Metadata.DateAccuracy.getEnum(params.dateAccuracy)
        println dateAccuracy
println "*************************************"
println params.fromDate
        if(dateAccuracy == Metadata.DateAccuracy.UNKNOWN) {
            this.temporalCoverage = new TemporalCoverage([fromDate:new Date(0), toDate:new Date(0), dateAccuracy:Metadata.DateAccuracy.UNKNOWN.value()]);
        } else {
            if(params.fromDate != "") {
                log.debug "Parsing date ${params.fromDate}"
                Date fromDate = params.fromDate instanceof Date ?params.fromDate:utilsService.parseDate(params.fromDate);
                log.debug "got ${fromDate}"

                Date toDate = params.toDate ?  (params.toDate instanceof Date ?params.toDate:utilsService.parseDate(params.toDate)) : fromDate
                if(fromDate > new Date()) {
                    this.errors.reject('temporalCoverage.fromDate', 'From date cannot be null')
                } else if(toDate < fromDate) {
                    this.errors.reject('temporalCoverage.toDate', 'To date cannot be null and should be > than from date')
                } else {
                    this.temporalCoverage = new TemporalCoverage([fromDate:fromDate, toDate:toDate]);
                }
                dateAccuracy = dateAccuracy?:Metadata.DateAccuracy.ACCURATE;
                this.temporalCoverage.dateAccuracy = dateAccuracy;
            } else {
                this.errors.reject('temporalCoverage.fromDate', 'From date and to date cannot be null')
            }
        }
        //taxonomicCoverage
        //SpeciesGroup sG = params.group_id ? SpeciesGroup.findByName(params.group_id) : null
        log.debug "Setting group ${params.group}"
        if(params.group) {
            Set groups = new HashSet();
            params.group.each { i,v->
                groups << Long.parseLong(v);
            }
		    this.taxonomicCoverage = new TaxonomicCoverage();
            this.taxonomicCoverage.updateGroups(groups);
        }

        //customFields
        def cf = [:];
        params.each { paramName, paramValue -> 
            if(paramName.startsWith(CustomField.PREFIX)) {
                String columnName = paramName.replaceAll(CustomField.PREFIX, "");
                cf[(columnName)] = paramValue;
            }
        }
        this.customFields = cf as JSON;

        this.language = params.locale_language;
        
        this.externalId = params.externalId;
        this.externalUrl = params.externalUrl;
        this.viaId = params.viaId;
        this.viaCode = params.viaCode;


    }

    SUser getAuthor() {
        return this.uploader;//SUser.read(this.party.contributorId);
    }

    def fetchCustomFields() {
        return customFields?JSON.parse(customFields):null;
    }

    def fetchCustomFields(SupportingModules supportingModule) {
        Map cfs = customFields?JSON.parse(customFields):[];
        return cfs[supportingModule.value()];
    }

    List featuredNotes() {
        return Featured.featuredNotes(this, null);
    }

	def fetchCommentCount(){
		return commentService.getCount(null, this, null, null)
	}

}

