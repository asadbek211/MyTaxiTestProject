package com.bizmiz.testproject.intent

sealed class MainIntent {

    data object FetchLocation : MainIntent()

}