package com.punedelights.user.model


import java.io.Serializable

class Section : Serializable {
    lateinit var id: String
    lateinit var title: String
    lateinit var short_description: String
    lateinit var style: String
    lateinit var offer_images: ArrayList<OfferImages?>
    lateinit var products: ArrayList<ProductList?>
}