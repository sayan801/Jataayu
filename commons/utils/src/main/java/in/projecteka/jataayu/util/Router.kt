package `in`.projecteka.jataayu.util

import android.content.Context
import android.content.Intent

typealias IntentDefinition = Intent.() -> Unit

private const val ACTIVITY_REGISTRATION = "in.projecteka.jataayu.registration.ui.activity.RegistrationActivity"
private const val ACTIVITY_PROVIDER = "In.projecteka.jataayu.provider.ui.ProviderActivity"
private const val ACTIVITY_ACCOUNT_CREATION = "in.projecteka.jataayu.account.AccountCreationActivity"
private const val ACTIVITY_LOGIN = "in.projecteka.jataayu.registration.ui.activity.LoginActivity"

fun startRegistration(context: Context, intentDefinition: IntentDefinition? = null) {
    context.startActivity(Intent(context, context.classLoader.loadClass(ACTIVITY_REGISTRATION)).apply {
        intentDefinition?.let { it(this) }
    })
}

fun startProvider(context: Context, intentDefinition: IntentDefinition? = null) {
    context.startActivity(Intent(context, context.classLoader.loadClass(ACTIVITY_PROVIDER)).apply {
        intentDefinition?.let { it(this) }
    })
}

fun startAccountCreation(context: Context, intentDefinition: IntentDefinition? = null) {
    context.startActivity(Intent(context, context.classLoader.loadClass(ACTIVITY_ACCOUNT_CREATION)).apply {
        intentDefinition?.let { it(this) }
    })
}
