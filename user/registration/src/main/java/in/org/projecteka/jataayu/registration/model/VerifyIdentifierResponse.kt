package `in`.org.projecteka.jataayu.registration.model

import com.google.gson.annotations.SerializedName

data class VerifyIdentifierResponse(@SerializedName("token") val token: String)