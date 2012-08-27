package utils

class Newsletter {
    String title
    Date date    
    String newsitem

    static constraints = {
        newsitem size: 1..5000
    }
}
