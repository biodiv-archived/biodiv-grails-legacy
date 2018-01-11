package species.trait

import grails.converters.JSON;
import grails.converters.XML;

import species.Language;
import species.AbstractObjectController;
import grails.plugin.springsecurity.annotation.Secured
import species.participation.UploadLog;
import species.participation.ActivityFeed;
import grails.util.Holders;
import static org.springframework.http.HttpStatus.*;
import species.participation.Observation;
import species.TaxonomyDefinition;
import species.groups.CustomField;
import species.auth.SUser;
import species.License;
import species.Species;
import species.trait.Trait.DataTypes;

class FactController extends AbstractObjectController {

    def factService;
    def customFieldService
    def springSecurityService;
    def activityFeedService;

    static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

    def index = {
        redirect(action: "list", params: params)
    }

    def list() {
        render(controller:'trait', view:"list");
    }

    def show() {
        render(view:'show', model:factService.show(params.id))
    }

    @Secured(['ROLE_USER'])
    def save() {
        if(request.method == 'POST') {
            //TODO:edit also calls here...handle that wrt other domain objects
            saveAndRender(params, false)
        } else {
            msg = "Method Not Allowed"
            def model = utilsService.getErrorModel(msg, null, METHOD_NOT_ALLOWED.value());
            withFormat {
                html {
                    flash.message = msg;
                    redirect (url:uGroup.createLink(action:'create', controller:"fact", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
    }

    @Secured(['ROLE_USER'])
    def flagDeleted() {
        def result = factService.delete(params)
        result.remove('url')
        String url = result.url;
        withFormat {
            html {
                flash.message = result.message
                redirect (url:url)
            }
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    @Secured(['ROLE_USER'])
    def update() {
        boolean success=false;
        Map model = [:], result=[:];
        Trait trait;
        try {
        if(params.traitId) {
            trait = Trait.read(params.long('traitId'));
        }
        if(trait) {
            if(params.objectId && params.objectType) {
                def object;
                switch(params.objectType) {
                    case 'species.Species': 
                    object = Species.read(Long.parseLong(params.objectId));
                    break;
                    case 'species.participation.Observation': 
                    object = Observation.read(Long.parseLong(params.objectId));
                    break;
                }
                if(object) {
                    println params;
                    params['contributor'] = springSecurityService.currentUser.email;
                    params['attribution'] = springSecurityService.currentUser.email;
                    params['license'] = License.LicenseType.CC_BY.value();
                    if(params.traits) {
                        params.putAll(factService.getTraits(params.remove('traits')));
                        params['replaceFacts'] = 'true';
                        Map r = factService.updateFacts(params, object, null, true);
                        //TODO: to update this from approprite result from factService.update
                        success = r.success;
                        result = [success:success, msg:success?'Successfully updated fact':'Error updating fact'];
                        def activityFeed;
                        if(success) {
                            r.facts_updated.each { fact ->
                                activityFeed = activityFeedService.addActivityFeed(object, fact, fact.contributor, activityFeedService.FACT_UPDATED, fact.getActivityDescription());
                            }
                            r.facts_created.each { fact ->
                                activityFeed = activityFeedService.addActivityFeed(object, fact, fact.contributor, activityFeedService.FACT_CREATED, fact.getActivityDescription());
                            }

                            List<Fact> facts = Fact.findAllByTraitAndObjectIdAndObjectType(trait, object.id, object.class.getCanonicalName());
                            Map queryParams = ['trait':[:]], factInstance = [:], otherParams = [:];
                            queryParams.trait[trait.id] = '';
                            facts.each { fact ->
                                String tvStr = '';
                                if(fact.traitValue) {
                                    tvStr = fact.traitValue.id;
                                } else if (trait.dataTypes == DataTypes.DATE) {
                                    tvStr = fact.fromDate + (fact.toDate ? ":" + fact.toDate:'')
                                }else {
                                    tvStr = fact.value + (fact.toValue ? ":" + fact.toValue:'')
                                }

                                queryParams.trait[trait.id] += tvStr+',';
                                if(!factInstance[trait.id]) {
                                    factInstance[trait.id] = [];
                                }
                                factInstance[trait.id] << (fact.traitValue ?: tvStr);

                                otherParams["trait"] = trait.name
                                otherParams["traitValueStr"] = fact.getActivityDescription()
                                utilsService.sendNotificationMail(utilsService.FACT_UPDATE,object,null,null,null,otherParams)
                            }
                            if(queryParams.trait) {
                                queryParams.trait.each {k,v->
                                    println k
                                    println queryParams.trait[k]
                                    if(queryParams.trait[k]) {
                                        println v[0..-2]
                                        queryParams.trait[k] = v[0..-2];
                                    }
                                }
                            }

                            println "======================"
                            println queryParams

                            model['traitHtml'] = g.render(template:"/trait/showTraitTemplate", model:['trait':trait, 'factInstance':factInstance, 'object':object, 'queryParams':queryParams, displayAny:false, editable:true]);
                        } else {

                        }
                    }else{
                        // if no traitValue selected 
                        List<Fact> facts = Fact.findAllByTraitAndObjectIdAndObjectType(trait, object.id, object.class.getCanonicalName());

                        facts.each { fact ->
                            fact.isDeleted = true;
                            fact.save();
                            result = [success:true, msg:'Successfully deleted fact']
                            model['traitHtml'] = g.render(template:"/trait/showTraitTemplate", model:['trait':trait, 'queryParams':'', displayAny:false, editable:true]);
                        }

                    }

                } else {
                    result['msg'] = 'Not a valid object'; 
                }               
            } else {
                result['msg'] = 'Not a valid object';
            }
        } else {
            result['msg'] = 'Not a valid trait';
        }
        } catch(Exception e) {
            e.printStackTrace();
            result['msg'] = e.getMessage();
        }
        if(result.success) {
            model = utilsService.getSuccessModel(result.msg, null, OK.value(), model);
        } else
            model = utilsService.getErrorModel(result.msg, null, OK.value(), model);

        withFormat {
            json { render model as JSON }
            xml { render model as XML }
        }

    }
    
    private saveAndRender(params, sendMail=true) {
        println "saveAndRender==============="+params
        params.locale_language = utilsService.getCurrentLanguage(request);
        def result = factService.save(params, sendMail)
        withFormat {
            html {
                //flash.message = "${message(code: 'error')}";
            }
            json {
                result.remove('instance');
                render result as JSON 
            }
            xml {
                result.remove('instance');
                render result as XML
            }
        }
    }


    @Secured(['ROLE_ADMIN'])
    def upload() {

        if(!params.fFile?.path) {
            flash.message = "Facts file is required";
            render (view:'upload', model:[fFile:['path':params.fFile?.path,'size':params.fFile?.size]]);
            return;
        }

        File contentRootDir = new File(Holders.config.speciesPortal.content.rootDir);
        if(!contentRootDir.exists()) {
            contentRootDir.mkdir();
        }

        params.fFile = contentRootDir.getAbsolutePath() + File.separator + params.fFile.path;
        params.file = params.fFile;

        def fFileValidation = factService.validateFactsFile(params.fFile, new UploadLog());
        
        if(fFileValidation.success) {
            log.debug "Validation of fact file is done. Proceeding with upload"
            def r = factService.upload(params);
            if(r.success) {
                flash.message = r.msg;
                redirect(controller:'trait', action: "list")
            } else {
                flash.message = r.msg;
                render (view:'upload', model:[fFile:['path':params.fFile], errors:r.errors]);
            }
        } else {
            String msg = g.message(code: 'newsletter.create.fix.error', default:'Please fix the errors before proceeding');
            flash.message = msg;
            render (view:'upload', model:[fFile:['path':params.fFile, errors:fFileValidation.errors]]);
        }
    }

    def migrateCustomFields() {
        File contentRootDir = new File(Holders.config.speciesPortal.content.rootDir+File.separator+params.controller);          
        println "Loading 7";
        int noOfUpdatedFacts_7 = migrateCustomFieldsToFacts(contentRootDir.getAbsolutePath()+'/customfields_group/gp 7.tsv','7');
        println noOfUpdatedFacts_7;
        println "Loading 38";
        int noOfUpdatedFacts_38 = migrateCustomFieldsToFacts(contentRootDir.getAbsolutePath()+'/customfields_group/gp 38.tsv','38');
        println noOfUpdatedFacts_38;
        println "Loading 33";
        int noOfUpdatedFacts_33 = migrateCustomFieldsToFacts(contentRootDir.getAbsolutePath()+'/customfields_group/gp 33.tsv','33');
        println noOfUpdatedFacts_33;
        println "Loading 30";
        int noOfUpdatedFacts_30 = migrateCustomFieldsToFacts(contentRootDir.getAbsolutePath()+'/customfields_group/gp 30.tsv','30');
        println noOfUpdatedFacts_30;
        println "Loading 18";
        int noOfUpdatedFacts_18 = migrateCustomFieldsToFacts(contentRootDir.getAbsolutePath()+'/customfields_group/gp 18.tsv','18');
        println noOfUpdatedFacts_18;
        println "Loading 13";
        int noOfUpdatedFacts_13 = migrateCustomFieldsToFacts(contentRootDir.getAbsolutePath()+'/customfields_group/gp13.tsv','13');
        println noOfUpdatedFacts_13;

        render noOfUpdatedFacts_7+" "+noOfUpdatedFacts_38+" "+noOfUpdatedFacts_33+" "+noOfUpdatedFacts_30+" "+noOfUpdatedFacts_18+" "+noOfUpdatedFacts_13;
    } 

    private int migrateCustomFieldsToFacts(tsvFile,groupNo) {
        int i=0;
        String[] headers;
        String l;
        int noOfUpdatedFacts = 0;
        new File(tsvFile).withReader { l = it.readLine() }  
        headers = l.split('\t');
        i=0;
        Observation obv;
        (new File(tsvFile)).splitEachLine('\t') { line ->
            if(i==0) {
            i++;
            return;
        } else {
            List cfs = [];
            Map cf_traits = [:];
            Map cf_taxons = [:]
            obv = null;
            line.eachWithIndex { key, j ->
                if(headers[j] == 'observation_id') {
                    try{
                        if(key) {
                            obv = Observation.read(Long.parseLong(key));
                        }
                    } catch(Exception e) {
                        println e.getMessage();
                    }
                } else if(headers[j] =~ /cf_[0-9]+$/) {
                    cfs << headers[j];
                } else if(headers[j] =~ /cf_[0-9]+ trait|value$/) {
                    cf_traits[headers[j]] = key;
                } else if(headers[j] =~ /cf_[0-9]+ taxonID$/) {
                    cf_taxons[headers[j]] = key;
                }
            }
            if(obv) {
                cfs.each { cf ->
                    String tv_str = cf_traits[cf+' trait|value'];
                    if(tv_str) {
                        tv_str.split(',').each {v->
                        def tv = v.trim().split("\\|");
                        def taxon,trait,traitValue;
                        if(cf_taxons[cf+' taxonID']) {
                            taxon = TaxonomyDefinition.read(Long.parseLong(cf_taxons[cf+' taxonID'])); 
                            def traits = Trait.executeQuery("select t from Trait t join t.taxon taxon where t.name=? and taxon = ?", [tv[0], taxon]);
                            if(traits) trait = traits[0];
                        } else {
                            trait = Trait.findByName(tv[0]);
                        }
                        traitValue = TraitValue.findByTraitAndValue(trait, tv[1]);
                        if(traitValue && trait) {
                            //TODO:do get contri and attr from activity feed
                            def contributor;
                            def authors = ActivityFeed.executeQuery("select author from ActivityFeed where activity_holder_id=:oid and activity_type='Custom field edited' order by last_updated desc limit 1",[oid:obv.id]);
                            if(authors) contributor = authors[0];
                            if(!contributor) contributor = SUser.findByEmail('admin@strandls.com');
                            Map m = ['attribution':contributor.name, 'contributor':contributor.email, 'license':'BY'];
                            if(!m[trait.id+''])  {
                                m[trait.id+''] = traitValue.value
                            } else {
                                m[trait.id+''] += ','+traitValue.value
                            }
                            println '=================='
                            println m;
                            println '=================='
                            if(factService.updateFacts(m, obv)) {
                                //TODO delete customfield
                                customFieldService.delCf("update custom_fields_group_${groupNo} set ${cf}='' where observation_id=:oid",[oid:obv.id]);
                                noOfUpdatedFacts++;
                            }
                        }
                        }
                    }
                }
            }
        }
    }
    return noOfUpdatedFacts;
    }
}
