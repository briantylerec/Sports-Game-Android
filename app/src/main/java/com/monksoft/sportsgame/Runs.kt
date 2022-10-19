package com.monksoft.sportsgame

data class Runs(

    var date : String ?= null,
    var startTime : String ?= null,
    var user : String ?= null,
    var duration : String ?= null,

    var intervalMode : Boolean ?= null,
    var intervalDuration: Int ?= null,
    var runningTime : String ?= null,
    var walkingTime : String ?= null,

    var challengeDuration: String ?= null,
    var challengeDistance: String ?= null,

    var distance: Double ?= null,
    var maxSpeed: Double ?= null,
    var avgSpeed: Double ?= null,

    var minAltitude: Double ?= null,
    var maxAltitude: Double ?= null,
    var minLatitude: Double ?= null,
    var maxLatitude: Double ?= null,
    var minLongitude: Double ?= null,
    var maxLongitude: Double ?= null,

    var centerLatitude: Double ?= null,
    var centerLongitude: Double ?= null,

    var sport: String ?= null,

    var activatedGPS: Boolean ?= null
)
