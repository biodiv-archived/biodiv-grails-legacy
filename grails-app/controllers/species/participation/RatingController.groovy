package species.participation

import grails.util.*
import org.grails.rateable.*
import grails.converters.JSON;

import grails.plugin.springsecurity.annotation.Secured

class RatingController extends RateableController {

	def observationsSearchService;
	def utilsService;

	@Secured(['ROLE_USER'])
    def rate() {
        log.debug params;
        def result =  rateIt(params.id?.toLong(), params.type, params.rating, params.parent, params.parentId?.toLong());
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def unrate() {
        log.debug params
        def rater = evaluateRater()

        def id = params.id.toLong();
        String type = params.type
				def flag = false;

        if(id && type && rater) {
            Rating.withTransaction {
                def ratingLinks = RatingLink.withCriteria {
                       createAlias("rating", "r")
                        eq "ratingRef", id
                        eq "type", type
                        if(rater) eq "r.raterId", rater.id.toLong()
                    }

                if(ratingLinks){
									flag=true;
                    ratingLinks.each { rl ->
                        def rating = rl.rating
                        if(rl.delete() && rating.delete()) {
													println("insdie unrate");
                        }
                    }
                }
            }
        }
				utilsService.cleanUpGorm(true)
				if(flag==true){
					updateReprImage(params.parent, params.parentId?.toLong(),id,type);
				}

        def result = formatRatings(getRatings(id, type));
        render result as JSON
    }

    def fetchRate = {
        log.debug params;
        def result = formatRatings(getRatings(params.id.toLong(), params.type));
        render result as JSON
    }

    private def rateIt(long id, String type, String rate, String parent, Long parentId) {
        def rater = evaluateRater()
				def flag = false;
        Rating.withTransaction {
            // for an existing rating, update it
            def rating = getRatings(id, type, rater);
            if (rating && rate) {
                rating[0].stars = rate.toDouble()
                if(rating[0].save(flush:  true)) {
									flag = true;
										//utilsService.cleanUpGorm(true)
										//sleep(3000);

                }
            }
            // create a new one otherwise
            else if(rate) {
                // create Rating
                rating = new Rating(stars: rate, raterId: rater.id, raterClass: rater.class.name)
                def link = new RatingLink(rating: rating, ratingRef: id, type: type)
                if(rating.save(flush: true) && link.save(flush: true)) {
										flag = true;
                    //updateReprImage(parent, parentId,id,type);
                }
            }
        }
				utilsService.cleanUpGorm(true)
				if(flag==true){
					updateReprImage(parent, parentId,id,type);
				}
        return formatRatings(getRatings(id, type));
    }

    private def updateReprImage(String parent, Long parentId,Long id,String type) {
      if(parent && parentId){
        def obj = grailsApplication.domainClasses.find { it.clazz.simpleName == parent.capitalize() }.clazz.read(parentId);
				long objId = obj.id;
				obj.updateReprImage();
				obj.save(flush:true);
				utilsService.cleanUpGorm(true)
				if(type=="resource"){
				Observation obv = Observation.get(objId);
				List<Observation> obvs=new ArrayList<Observation>();
				obvs.add(obv);
				observationsSearchService.publishSearchIndex(obvs, true);
				}
      }else{
				if(type=="observation"){
					Observation obv = Observation.get(Long.parseLong(params.id));
					 List<Observation> obvs=new ArrayList<Observation>();
					 obvs.add(obv);
					 observationsSearchService.publishSearchIndex(obvs, true);
				}
			}


    }

    private def getRatings(long id, String type, rater=null) {
        return RatingLink.withCriteria {
            createAlias("rating", "r")
            projections {
                property 'rating'
            }
            eq "ratingRef", id
            eq "type", type
            if(rater) eq "r.raterId", rater.id.toLong()
            cache true
        }
    }

    private Map formatRatings(List allRatings) {
        def avg = allRatings.size() ? allRatings*.stars.sum() / allRatings.size() : 0
        return ['status':'success', 'avg':avg, 'noOfRatings':allRatings.size()]
    }

    def evaluateRater() {
		def evaluator = grailsApplication.config.grails.rateable.rater.evaluator
		def rater
		if(evaluator instanceof Closure) {
			evaluator.delegate = this
			evaluator.resolveStrategy = Closure.DELEGATE_ONLY
			rater = evaluator.call()
		}

		if(rater && rater.id) {
		    return rater
        } else {

        }
	}

}
