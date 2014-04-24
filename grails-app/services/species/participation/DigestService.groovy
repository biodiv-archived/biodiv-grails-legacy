package species.participation
import species.*;
import content.eml.Document;
import species.groups.UserGroup;
import species.auth.SUser;
import java.lang.*;

class DigestService {

    def activityFeedService;
    def observationService;
    def chartService;

    public static final MAX_DIGEST_OBJECTS = 5
    static transactional = true

    def sendDigestAction() {
        def digestList = Digest.list()
        def setTime = true
        digestList.each{ dig ->
            sendDigestWrapper(dig, setTime)
        }

    }

    def sendDigestWrapper(Digest digest, setTime=true){
        def max = 50
        def offset = 0
        Date lastSent;
        if(setTime){
            lastSent = new Date()
        }
        def emailFlag = true
        while(emailFlag){
            def usersEmailList = observationService.getParticipantsForDigest(digest.userGroup, max, offset)
            if(usersEmailList.size() != 0){
                sendDigest(digest, usersEmailList, false)
                offset = offset + max
                Thread.sleep(600000L);
            }
            else{
                emailFlag = false
            }
        }
        if(setTime) {
            digest.lastSent = lastSent;
            if(!digest.save(flush:true))
                digest.errors.allErrors.each { log.error it }
        }
        log.debug " MAIL SENT and Digest Last sent time updated "

    }

    def sendDigest(Digest digest, usersEmailList, setTime){
        def digestContent = fetchDigestContent(digest)
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
            println "============================== Sending email" 
            println usersEmailList
            observationService.sendNotificationMail(observationService.DIGEST_MAIL,sp,null,null,null,otherParams)
            
            if(setTime) {
                if(!digest.save(flush:true))
                    digest.errors.allErrors.each { log.error it }
            }
            
        }else{
            log.error "NO DIGEST CONTENT FOR GROUP " + digest.userGroup
        }
    }

    private def fetchDigestContent(Digest digest){
        def params = [:]
        params.rootHolderId = digest.userGroup.id
        params.rootHolderType = UserGroup.class.getCanonicalName()
        params.refTime = ""+digest.lastSent.getTime()
        params.timeLine = ActivityFeedService.NEWER
        params.feedOrder = ActivityFeedService.LATEST_FIRST
        params.feedType = ActivityFeedService.GROUP_SPECIFIC

        def res = [:]
        def obvList = [], unidObvList = [], spList = [], docList = [], userList = [];
        boolean obvFlag = false, unidObvFlag = false, spFlag = false, docFlag = false, userFlag = false;
        //HashSet obvIds = new HashSet(), unidObvIds = new HashSet(), spIds = new HashSet(), docIds = new HashSet(), userIds = new HashSet();
        def feedsList = activityFeedService.getActivityFeeds(params)
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
                
                if(obvFlag && unidObvFlag && spFlag && docFlag && userFlag){
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
            res['observations'] = obvList
            res['unidObvs'] = unidObvList
            res['species'] = spList
            res['documents'] = docList
            res['users'] = userList
       
            def p = [webaddress:digest.userGroup.webaddress];
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
}
