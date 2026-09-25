package app.kumo.beta.player

data class PlaybackRequest(val contentId:String,val episodeId:String,val sourceUrl:String,val quality:Int?=null,val language:String?=null,val headers:Map<String,String> = emptyMap(),val referer:String?=null,val resumePositionMs:Long=0L)
data class PlaybackPreferences(val defaultQuality:Int?=null,val playbackSpeed:Float=1f,val autoplayNext:Boolean=true,val seekSeconds:Int=10,val preferredAudio:String?=null,val preferredSubtitle:String?=null)
