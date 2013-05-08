package species.participation

import grails.util.*
import org.grails.rateable.*
import grails.converters.JSON;

import grails.plugins.springsecurity.Secured

class RatingController extends RateableController {
    
	@Secured(['ROLE_USER'])
    def rate = {
        def result =  rateIt(params.id.toLong(), params.type, params.rating);
        render result as JSON
    }

    def fetchRate = {
        log.debug params;
        def result = getRatings(params.id.toLong(), params.type);
        render result as JSON
    }

    private def rateIt(long id, String type, String rate) {
        def rater = evaluateRater()
        Rating.withTransaction {
            // for an existing rating, update it
            def rating = RatingLink.createCriteria().get {
                createAlias("rating", "r")
                projections {
                    property "rating"
                }
                eq "ratingRef", id
                eq "type", type
                eq "r.raterId", rater.id.toLong()
                cache true
            }
            if (rating && rate) {
                rating.stars = rate.toDouble()
                assert rating.save()
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
        return getRatings(id, type);
    }

    private def getRatings(long id, String type) {
        def allRatings = RatingLink.withCriteria {
            projections {
                property 'rating'
            }
            eq "ratingRef", id
            eq "type", type
            cache true
        }

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
