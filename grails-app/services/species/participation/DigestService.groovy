package species.participation
import species.*;
import content.eml.Document;
import species.groups.UserGroup;
import species.auth.SUser;
import java.lang.*;

class DigestService {

    def activityFeedService;
    def observationService;

    public static final MAX_DIGEST_OBJECTS = 5
    static transactional = true

    def sendDigestWrapper(Digest digest, setTime=true){
        def max = 50
        def offset = 0
        def emailFlag = true
        while(emailFlag){
            def usersEmailList = observationService.getParticipantsForDigest(digest.userGroup, max, offset)
            if(usersEmailList.size() != 0){
                sendDigest(digest, usersEmailList, setTime)
                offset = offset + max
                Thread.sleep(900000L);
            }
            else{
                emailFlag = false
            }
        }
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
            observationService.sendNotificationMail(observationService.DIGEST_MAIL,sp,null,null,null,otherParams)
            if(digest.save(flush:true)){
                digest.errors.allErrors.each { log.error it }
            }
            log.debug " MAIL SENT and Digest Last sent time updated "
        }else{
            println "NO DIGEST CONTENT FOR GROUP " + digest.userGroup
        }
    }

    private def fetchDigestContent(Digest digest){
        def params = [:]
        params.rootHolderId = digest.userGroup.id
        params.rootHolderType = UserGroup.class.getCanonicalName()
        params.refTime = ""+digest.lastSent.getTime()
        params.timeLine = "newer"
        params.feedOrder = "latestFirst"
        params.feedType = "GroupSpecific"

        def res = [:]
        def obvList = [], unidObvList = [], spList = [], docList = [], userList = [];
        HashSet obvIds = new HashSet(), unidObvIds = new HashSet(), spIds = new HashSet(), docIds = new HashSet(), userIds = new HashSet();

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
            feedsList.each{
                switch(it.rootHolderType){
                    case Observation.class.getCanonicalName():
                    if(obvList.size() < MAX_DIGEST_OBJECTS) { 
                        def obv = Observation.read(it.rootHolderId)
                        if(!obvList.contains(obv)){
                            obvList.add(obv)
                        }
                    }

                    //UNIDENTIFIED OBV LIST
                    if (unidObvList.size() < MAX_DIGEST_OBJECTS) {
                        def obv = Observation.read(it.rootHolderId)
                        if(!obv.maxVotedReco){
                            if(!unidObvList.contains(obv)){
                                unidObvList.add(obv)
                            }
                        }
                    }

                    obvIds.add(it.rootHolderId);
                    break

                    case Checklists.class.getCanonicalName():
                    if(obvList.size() < MAX_DIGEST_OBJECTS){
                        def chk = Checklists.read(it.rootHolderId)
                        if(!obvList.contains(chk)){
                            obvList.add(chk)
                        }
                    }
                    obvIds.add(it.rootHolderId);
                    break


                    case Species.class.getCanonicalName():
                    if(spList.size() < MAX_DIGEST_OBJECTS){
                        def sp = Species.read(it.rootHolderId)
                        if(!spList.contains(sp)){
                            spList.add(sp)
                        }
                    }
                    spIds.add(it.rootHolderId);
                    break

                    case Document.class.getCanonicalName():
                    if(docList.size() < MAX_DIGEST_OBJECTS){
                        def doc = Document.read(it.rootHolderId)
                        if(!docList.contains(doc)){
                            docList.add(doc)
                        }
                    }
                    docIds.add(it.rootHolderId);
                    break

                    case UserGroup.class.getCanonicalName():
                    if(it.activityHolderType == SUser.class.getCanonicalName()){
                        if(userList.size() < MAX_DIGEST_OBJECTS){
                            def user = SUser.read(it.activityHolderId)
                            if(!userList.contains(user)){
                                userList.add(user)
                            }
                        }
                        userIds.add(it.activityHolderId);
                    }
                    
                    break
                } 
            }

            def obvListCount = obvIds.size(), unidObvListCount = 0;
            if(obvIds.size() > 0) {
                unidObvListCount = Observation.withCriteria() {
                    projections {
                        count('id')
                    }
                    'in'('id', obvIds.toList())
                    isNull('maxVotedReco')
                } 
                /*idObvListCount = Observation.withCriteria() {
                    projections {
                        count('id')
                    }
                    'in'('id', obvIds.toList())
                    isNotNull('maxVotedReco')
                }*/
            }
            res['observations'] = obvList
            res['unidObvs'] = unidObvList
            res['species'] = spList
            res['documents'] = docList
            res['users'] = userList

            res['obvListCount'] = obvListCount;
            //res['idObvListCount'] = idObvListCount;
            res['unidObvListCount'] = unidObvListCount;
            res['spListCount'] = spIds.size();
            res['docListCount'] = docIds.size();
            res['userListCount'] = userIds.size();
        }
        return res
    }
}
