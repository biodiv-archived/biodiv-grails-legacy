package species.participation

import grails.util.*
import org.grails.rateable.*
import grails.converters.JSON;

class RatingController extends RateableController {
    
    def rate = {
        def rater = evaluateRater()
        Rating.withTransaction {
            // for an existing rating, update it
            def rating = RatingLink.createCriteria().get {
                createAlias("rating", "r")
                projections {
                    property "rating"
                }
                eq "ratingRef", params.id.toLong()
                eq "type", params.type
                eq "r.raterId", rater.id.toLong()
                cache true
            }
            if (rating && params.rating) {
                rating.stars = params.rating.toDouble()
                assert rating.save()
            }
            // create a new one otherwise
            else if(params.rating) {
                // create Rating
                rating = new Rating(stars: params.rating, raterId: rater.id, raterClass: rater.class.name)
                assert rating.save()
                def link = new RatingLink(rating: rating, ratingRef: params.id, type: params.type)
                assert link.save()
            }
        }

        def allRatings = RatingLink.withCriteria {
            projections {
                property 'rating'
            }
            eq "ratingRef", params.id.toLong()
            eq "type", params.type
            cache true
        }
        def avg = allRatings.size() ? allRatings*.stars.sum() / allRatings.size() : 0
        render (['status':'success', 'avg':avg, 'noOfRatings':allRatings.size()] as JSON)
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
