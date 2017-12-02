package species.dataset

import java.util.Date;

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
import species.utils.Utils;
import species.Language;
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

	//EML-Coverage fields
	GeographicalCoverage geographicalCoverage;
	TemporalCoverage temporalCoverage;
	TaxonomicCoverage taxonomicCoverage;

	//EML-Project
	String project;
	
	//EML-Methods;
	String methods;
	
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

	static constraints = {
		title nullable:false, blank:false;
		description nullable:false, blank:false, type:'text';
		language nullable:false
		externalId nullable:true
		externalUrl nullable:true
		viaId nullable:true
		viaCode nullable:true
		
		access nullable:true;
		party nullable:true;
	
		geographicalCoverage nullable:true;
		temporalCoverage nullable:true;
		taxonomicCoverage nullable:true;
		
		project nullable:true;
		methods nullable:true;
		
		uFile nullable:true;
	}

	static mapping = {
		description type:'text'
		tablePerHierarchy false
		//        tablePerSubClass true
	}

    static embedded = ['access', 'party', 'geographicalCoverage', 'temporalCoverage', 'taxonomicCoverage'];

	def beforeInsert(){
	}

	def beforeUpdate(){
	}

    def initParams(params) {

        XMLConverter xmlConverter = new XMLConverter();

        //Party
        if(params.author)  {
            log.debug "Setting access to ${params.author}"
            this.party = new Party(uploaderId:params.author.id);
        } else {
            log.debug "Setting access to ${springSecurityService.currentUser}"
            this.party = new Party(uploaderId:springSecurityService.currentUser.id);
        }

        if(params.contributorUserIds)  {
            /*this.party.contributorIds?.clear();
            params.contributorIds.each {
                SUser user = SUser.read(it);
                if(user) 
                    this.party.contributorIds << user.id;
            }*/
            this.party.contributorId = SUser.read(params.long('contributorUserIds')).id;
        }
        
        if(params.attributions)  {
            /*this.party.attributions?.clear();
            params.attributions.split('\n').each {
                Contributor contributor = xmlConverter.getContributorByName(it, true);
                this.party.attributions << contributor;
            }
            */
            this.party.attributions = params.attributions;
        }

        //Access
        String licensesStr = params.license_0?:params.license
        if(licensesStr) {
            log.debug "Setting access to ${licenseStr}"
            this.access = new Access(licenseId : xmlConverter.getLicenseByType(licenseStr, false).id)
        } else {
            log.debug "Setting access to ${LicenseType.CC_BY}"
            this.access = new Access(licenseId : xmlConverter.getLicenseByType(LicenseType.CC_BY, false).id);
        }

        //geographicalCoverage
        this.geographicalCoverage = new GeographicalCoverage([placeName:params.placeName, latitude:params.double('latitude'), longitude:params.double('longitude')]);
        def locScale =  Metadata.LocationScale.getEnum(params.locationScale)
        this.geographicalCoverage.locationScale = locScale?:Metadata.LocationScale.APPROXIMATE
        //this.geographicalCoverage.geoPrivacy = params.geoPrivacy ? (params.geoPrivacy.trim().toLowerCase().toBoolean()):false;

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
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

        //temporalCoverage
        if( params.fromDate != ""){
            log.debug "Parsing date ${params.fromDate}"
            Date fromDate = utilsService.parseDate(params.fromDate);
            log.debug "got ${fromDate}"
            Date toDate = params.toDate ? utilsService.parseDate(params.toDate) : fromDate

            this.temporalCoverage = new TemporalCoverage([fromDate:fromDate, toDate:toDate]);
        }

        //taxonomicCoverage
        //SpeciesGroup sG = params.group_id ? SpeciesGroup.findByName(params.group_id) : null
        log.debug "Setting group ${params.group}"
		this.taxonomicCoverage = params.group ? new TaxonomicCoverage(groupId:params.long('group')):null;

        this.language = params.locale_language;
        
        this.externalId = params.externalId;
        this.externalUrl = params.externalUrl;
        this.viaId = params.viaId;
        this.viaCode = params.viaCode;


    }

    SUser getUploader() {
        return SUser.read(this.party.uploaderId);
    }

    void setUploader(SUser user) {
         this.party.uploaderId = user.id;
    }

    SUser getAuthor() {
        return SUser.read(this.party.contributorId);
    }
/*
    void setAuthor(SUser uploader) {
        this.party.uploaderId = uploader.id;
    }
*/
}

