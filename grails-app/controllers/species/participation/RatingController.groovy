package species.participation

import grails.util.*
import org.grails.rateable.*
import grails.converters.JSON;

import grails.plugin.springsecurity.annotation.Secured

class RatingController extends RateableController {
    
	@Secured(['ROLE_USER'])
    def rate() {
        log.debug params;
        def result =  rateIt(params.id.toLong(), params.type, params.rating);
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def unrate() {
        log.debug params
        def rater = evaluateRater()

        def id = params.id.toLong();
        String type = params.type

        if(id && type && rater) {

            Rating.withTransaction {
                def ratingLinks = RatingLink.withCriteria {
                       createAlias("rating", "r")
                        eq "ratingRef", id
                        eq "type", type
                        if(rater) eq "r.raterId", rater.id.toLong()
                    }

                if(ratingLinks){
                    ratingLinks.each { rl ->
                        def rating = rl.rating
                        rl.delete();
                        rating.delete();
                    }
                }
            } 
        }
        def result = formatRatings(getRatings(id, type));
        render result as JSON
    }

    def fetchRate = {
        log.debug params;
        def result = formatRatings(getRatings(params.id.toLong(), params.type));
        render result as JSON
    }

    private def rateIt(long id, String type, String rate) {
        def rater = evaluateRater()
        Rating.withTransaction {
            // for an existing rating, update it
            def rating = getRatings(id, type, rater);
            if (rating && rate) {
                rating[0].stars = rate.toDouble()
                assert rating[0].save()
            }
            // create a new one otherwise
            else if(rate) {
                // create Rating
                rating = new Rating(stars: rate, raterId: rater.id, raterClass: rater.class.name)
                assert rating.save()
                def link = new RatingLink(rating: rating, ratingRef: id, type: type)
                assert link.save()
            }
        }
        return formatRatings(getRatings(id, type));
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
