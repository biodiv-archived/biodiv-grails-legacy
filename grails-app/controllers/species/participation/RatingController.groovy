package species.participation

import grails.util.*
import org.grails.rateable.*

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
            if (rating) {
                rating.stars = params.rating.toDouble()
                assert rating.save()
            }
            // create a new one otherwise
            else {
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
        println "----------------))))))))))))))))))";
println allRatings
println avg
        render "${avg},${allRatings.size()}"
    }

}
