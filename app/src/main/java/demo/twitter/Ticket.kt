package demo.twitter

class Ticket {
    var tweetID:String?=null
    var tweetText:String?=null
    var tweetImageURL:String?=null
    var tweetPersonID:String?=null
    constructor(tweetID:String,tweetText:String,tweetImageURL:String,tweetPersonID:String){
        this.tweetID=tweetID
        this.tweetImageURL=tweetImageURL
        this.tweetPersonID=tweetPersonID
        this.tweetText=tweetText
    }

}