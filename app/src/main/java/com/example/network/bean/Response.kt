package com.example.network.bean


data class HuangliResponse(
    var id:String?,//
    var yangli:String?,//
    var yinli:String?,//
    var wuxing:String?,//
    var chongsha:String?,//
    var baiji:String?,//
    var jishen:String?,//
    var yi:String?,//
    var xiongshen:String?,//
    var ji:String?,//
)

data class NewsResponse(
    var uniquekey:String?,//
    var detail:NewsContentResponse?,//
    var content:String?,//

)

data class NewsContentResponse(
    var title:String?,//
    var date:String?,//
    var category:String?,//
    var author_name:String?,//
    var url:String?,//
    var thumbnail_pic_s:String?,//
    var thumbnail_pic_s02:String?,//
    var thumbnail_pic_s03:String?,//
)
