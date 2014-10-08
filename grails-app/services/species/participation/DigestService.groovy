package species.participation

import species.*;
import content.eml.Document;
import species.groups.UserGroup;
import species.auth.SUser;
import java.lang.*;
import java.text.SimpleDateFormat;
import org.codehaus.groovy.runtime.DateGroovyMethods;
import groovy.sql.Sql;
import grails.util.Environment;
import species.groups.UserGroupMemberRole;

class DigestService {

    def utilsService;
    def activityFeedService;
    def chartService;
    def dataSource;

    public static final MAX_DIGEST_OBJECTS = 5
    static transactional = false

    def sendDigestAction() {
        log.debug "Send digest action called"
        def digestList = Digest.list()
        def setTime = true
        digestList.each{ dig ->
            log.debug "Sending digest for ${dig}"
            sendDigestWrapper(dig, setTime)
        }

    }

    def sendDigestWrapper(Digest digest, setTime=true){
        int max = 50
        long offset = 0
        Date lastSent;
        if(setTime){
            lastSent = new Date()
        }

        def digestContent;
        Digest.withTransaction {
            digestContent = fetchDigestContent(digest)
        }

        log.debug  "Fetched digestContent ${digestContent}"

        def emailFlag = true

        while(emailFlag){
            List<SUser> usersEmailList = [];
            Digest.withTransaction { status ->
                usersEmailList = getParticipantsForDigest(digest.userGroup, max, offset)

                if(usersEmailList.size() != 0){
                    sendDigest(digest, usersEmailList, false, digestContent)
                    offset = offset + max
                }
                else{
                    emailFlag = false
                }
            }
            if(emailFlag) 
                Thread.sleep(600000L);
        }
        if(setTime) {
            digest.lastSent = lastSent;
            log.debug "Saving digest lastSent ${digest}"
            if(!digest.save(flush:true))
                digest.errors.allErrors.each { log.error it }
        }
        log.debug " MAIL SENT and Digest Last sent time updated "

    }

    def sendDigest(Digest digest, usersEmailList, setTime, digestContent){
        //def digestContent = fetchDigestContent(digest)
        if(digestContent){
            log.debug "SENDING A DIGEST MAIL FOR GROUP : " + digest.userGroup
            def otherParams = [:]
            otherParams['digestContent'] = digestContent
            otherParams['userGroup'] = digest.userGroup
            log.debug "DIGEST CONTENT " + otherParams['digestContent']
            def sp = new Species() 
            if(setTime){
                digest.lastSent = new Date()
            }

            otherParams['usersEmailList'] = usersEmailList  
            utilsService.sendNotificationMail(utilsService.DIGEST_MAIL,sp,null,null,null,otherParams)
            
            if(setTime) {
                if(!digest.save(flush:true))
                    digest.errors.allErrors.each { log.error it }
            }
            
        }else{
            log.warn "NO DIGEST CONTENT FOR GROUP " + digest.userGroup
        }
    }

    def fetchDigestContent(Digest digest){
        log.debug "fetchDigestContent"
        def params = [:]
        params.rootHolderId = digest.userGroup.id
        params.rootHolderType = UserGroup.class.getCanonicalName()
        params.refTime = ""+digest.lastSent.getTime()
        params.timeLine = ActivityFeedService.NEWER
        params.feedOrder = ActivityFeedService.LATEST_FIRST
        params.feedType = ActivityFeedService.GROUP_SPECIFIC

        def res = [:]
        res = latestContentsByGroup(digest)
        def obvList = [], unidObvList = [], spList = [], docList = [], userList = [];
        boolean obvFlag = false, unidObvFlag = false, spFlag = false, docFlag = false, userFlag = false;
        //HashSet obvIds = new HashSet(), unidObvIds = new HashSet(), spIds = new HashSet(), docIds = new HashSet(), userIds = new HashSet();
        log.debug "Fetching activity after refTime satisfying params ${params}"
        def feedsList = activityFeedService.getActivityFeeds(params)
        log.debug "Feeds List : ${feedsList}"
        def feedCount = 0
        feedsList.each{
            switch(it.rootHolderType){
                case [Observation.class.getCanonicalName(),Checklists.class.getCanonicalName(), Species.class.getCanonicalName(), Document.class.getCanonicalName(),SUser.class.getCanonicalName() ]:   
                feedCount++
                break
            } 
        }

        if(digest.threshold >= feedCount){
            res = null
        }
        else{
            for (def feed : feedsList){
                switch(feed.rootHolderType){
                    /*
                    case Observation.class.getCanonicalName():
                    if(obvList.size() < MAX_DIGEST_OBJECTS) { 
                        def obv = Observation.read(feed.rootHolderId)
                        if(!obvList.contains(obv)){
                            obvList.add(obv)
                        }
                    } else {
                        obvFlag = true
                    }

                    //UNIDENTIFIED OBV LIST
                    if (unidObvList.size() < MAX_DIGEST_OBJECTS) {
                        def obv = Observation.read(feed.rootHolderId)
                        if(!obv.maxVotedReco){
                            if(!unidObvList.contains(obv)){
                                unidObvList.add(obv)
                            }
                        }
                    } else {
                        unidObvFlag= true;
                    }

                    //obvIds.add(it.rootHolderId);
                    break

                    case Checklists.class.getCanonicalName():
                    if(obvList.size() < MAX_DIGEST_OBJECTS){
                        def chk = Checklists.read(feed.rootHolderId)
                        if(!obvList.contains(chk)){
                            obvList.add(chk)
                        }
                    } else {
                        obvFlag = true;
                    }
                    //obvIds.add(it.rootHolderId);
                    break
                    */

                    case Species.class.getCanonicalName():
                    if(spList.size() < MAX_DIGEST_OBJECTS){
                        def sp = Species.read(feed.rootHolderId)
                        if(!spList.contains(sp)){
                            spList.add(sp)
                        }
                    } else {
                        spFlag = true;
                    }
                    //spIds.add(it.rootHolderId);
                    break

                    case Document.class.getCanonicalName():
                    if(docList.size() < MAX_DIGEST_OBJECTS){
                        def doc = Document.read(feed.rootHolderId)
                        if(!docList.contains(doc)){
                            docList.add(doc)
                        }
                    } else {
                        docFlag = true;
                    }
                    //docIds.add(it.rootHolderId);
                    break

                    case UserGroup.class.getCanonicalName():
                    if(feed.activityHolderType == SUser.class.getCanonicalName()){
                        if(userList.size() < MAX_DIGEST_OBJECTS){
                            def user = SUser.read(feed.activityHolderId)
                            if(!userList.contains(user)){
                                userList.add(user)
                            }
                        } else {
                            userFlag = true;
                        }
                        //userIds.add(it.activityHolderId);
                    }
                    
                    break
                } 
                
                if(spFlag && docFlag && userFlag){
                    break;
                }

            }

            /*def obvListCount = obvIds.size(), unidObvListCount = 0;
            if(obvIds.size() > 0) {
                unidObvListCount = Observation.withCriteria() {
                    projections {
                        count('id')
                    }
                    'in'('id', obvIds.toList())
                    isNull('maxVotedReco')
                } 
                idObvListCount = Observation.withCriteria() {
                    projections {
                        count('id')
                    }
                    'in'('id', obvIds.toList())
                    isNotNull('maxVotedReco')
                }
            }*/
            /*
            res['observations'] = obvList
            res['unidObvs'] = unidObvList
            res['species'] = spList
            */
            res['documents'] = docList
            res['users'] = userList


            def p = [webaddress:digest.userGroup.webaddress];

            def recentTopContributors = [];
            def topIDProviders = [];
            def newDate = new Date()
            int days = (newDate - digest.startDateStats); 
            int max = 5
            UserGroup userGroupInstance = digest.userGroup

            def startDate = newDate.minus(days)
            DateGroovyMethods.clearTime(startDate)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            def startDateInFormat = "'"+dateFormat.format(startDate) +"'";
            def currentDateInFormat = "'"+dateFormat.format(newDate) + "'";

            if(digest.sendTopContributors){
                log.debug "activeUserStatsAuthorAndCount in ${userGroupInstance}"
                recentTopContributors = chartService.activeUserStatsAuthorAndCount(max, userGroupInstance, startDate );
                log.debug "recentTopContributors in ${userGroupInstance} ${recentTopContributors}"
                res['recentTopContributors'] = recentTopContributors
            }

            if(digest.sendTopIDProviders){
                log.debug "topIDProviders in ${userGroupInstance}"
                def sql =  Sql.newInstance(dataSource);
                def resultSet = sql.rows("select u.id as userid, u.username, u.date_created as registered, u.last_login_date, recoCount from ( select rv.author_id uid, count(*) recoCount from recommendation_vote rv, observation o, user_group_observations ugo where rv.observation_id = o.id and o.id = ugo.observation_id and o.is_deleted = false and o.is_showable = true and o.is_checklist = false and ugo.user_group_id="+digest.userGroup.id+" and  rv.voted_on >= "+startDateInFormat+" and rv.voted_on <= "+currentDateInFormat+" group by rv.author_id) group_user_reco, suser u where u.id = group_user_reco.uid order by recoCount desc limit 5")                
log.debug resultSet
                for (row in resultSet){
                    topIDProviders.add(["user":SUser.findById(row.getProperty("userid")), "recoCount":row.getProperty("recocount")])
                }
                log.debug "topIDProviders in ${userGroupInstance} ${topIDProviders}"
                res['topIDProviders'] = topIDProviders
            }


            def stats = [observationCount:chartService.getObservationCount(p), speciesCount:chartService.getSpeciesCount(p), checklistsCount:chartService.getChecklistCount(p), documentCount:chartService.getDocumentCount(p), userCount:chartService.getUserCount(p)];

            res['obvListCount'] = stats.observationCount;
            //res['idObvListCount'] = idObvListCount;
            //res['unidObvListCount'] = unidObvListCount;
            res['spListCount'] = stats.speciesCount;
            res['docListCount'] = stats.documentCount;
            res['userListCount'] = stats.userCount;
        }
        return res
    }

    def sendDigestPrizeEmail(){
        def max = 50
        def offset = 0
        def emailFlag = true
        def userGroup = UserGroup.read(18L)
        while(emailFlag){
            def usersEmailList = getParticipantsForDigest(userGroup, max, offset)
            if(usersEmailList.size() != 0){
                def otherParams = [:]
                otherParams['userGroup'] = userGroup
                otherParams['usersEmailList'] = usersEmailList
                def sp = new Species() 
                println "============================== Sending DIGEST PRIZE Email" 
                println usersEmailList
                utilsService.sendNotificationMail(utilsService.DIGEST_PRIZE_MAIL,sp,null,null,null,otherParams)
                offset = offset + max
                Thread.sleep(300000L);
            }
            else{
                emailFlag = false
            }
        }
        log.debug " DIGEST PRIZE EMAIL SENT "
    }

    def sendSampleDigestPrizeEmail(usersEmailList){
        def max = 50
        def offset = 0
        def emailFlag = true
        def userGroup = UserGroup.read(18L)
        //while(emailFlag){
        //def usersEmailList = getParticipantsForDigest(userGroup, max, offset)
        //if(usersEmailList.size() != 0){
        def otherParams = [:]
        otherParams['userGroup'] = userGroup
        otherParams['usersEmailList'] = usersEmailList
        def sp = new Species() 
        println "============================== Sending DIGEST PRIZE Email" 
        println usersEmailList
        utilsService.sendNotificationMail(utilsService.DIGEST_PRIZE_MAIL,sp,null,null,null,otherParams)
        offset = offset + max
        //Thread.sleep(300000L);
        //}
        //else{
        //  emailFlag = false
        //}
        //}
        log.debug " DIGEST PRIZE EMAIL SENT "
    }

    def List getParticipantsForDigest(userGroup, max, offset) {
        List participants = [];
        if (Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
            def result = UserGroupMemberRole.findAllByUserGroup(userGroup, [max: max, sort: "sUser", order: "asc", offset: offset]).collect {it.sUser};

            result.each { user ->
                if(user.sendDigest && !(user.accountLocked) && !participants.contains(user)){
                    participants << user
                }
            }
        } else {
            participants << springSecurityService.currentUser;
        }
        return participants;
    }

    def latestContentsByGroup(Digest digest) {
        log.debug "latestContentsByGroup ${digest}"
		def res = [:]
        int max = 5
        def obvList = Observation.withCriteria(){
            and{
                // taking undeleted observation
                eq('isDeleted', false)
                eq('isShowable', true)
                
                //filter by usergroup
                if(digest.userGroup){
                    userGroups {
                        eq('id', digest.userGroup.id)
                    }
                }
            }
            maxResults max
            order 'lastRevised', 'desc'
        }

        log.debug "latest ${max} observations ${obvList}"

        def unidObvList = Observation.withCriteria(){
            and{
                // taking undeleted observation
                eq('isDeleted', false)
                eq('isShowable', true)
                eq('isChecklist', false)
                isNull('maxVotedReco')
                //filter by usergroup
                if(digest.userGroup){
                    userGroups{
                        eq('id', digest.userGroup.id)
                    }
                }
            }
            maxResults max
            order 'lastRevised', 'desc'
        }
        log.debug "latest ${max} unidentified observations ${unidObvList}"
        /*
        def spList = Species.withCriteria(){
            and{
                //filter by usergroup
                if(digest.userGroup){
                    userGroups{
                        eq('id', digest.userGroup.id)
                    }
                }
            }
            maxResults max
            order 'lastUpdated', 'desc'
        }

        def docList = Document.withCriteria(){
            and{
                //filter by usergroup
                if(digest.userGroup){
                    userGroups{
                        eq('id', digest.userGroup.id)
                    }
                }
            }
            maxResults max
            order 'lastRevised', 'desc'
        }
        //wrong suser desc
        //def userList = UserGroupMemberRole.findAllByUserGroup(digest.userGroup, [max: max, sort: "sUser", order: "desc"]).collect {it.sUser};
        */
        res['observations'] = obvList
        res['unidObvs'] = unidObvList
        //res['species'] = spList
        //res['documents'] = docList
        //res['users'] = userList
        
        return res
    }
}
