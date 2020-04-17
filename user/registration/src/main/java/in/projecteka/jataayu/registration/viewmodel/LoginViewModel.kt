package `in`.projecteka.jataayu.registration.viewmodel

import `in`.projecteka.jataayu.core.model.CreateAccountResponse
import `in`.projecteka.jataayu.network.utils.PayloadLiveData
import `in`.projecteka.jataayu.network.utils.fetch
import `in`.projecteka.jataayu.presentation.BaseViewModel
import `in`.projecteka.jataayu.registration.model.LoginRequest
import `in`.projecteka.jataayu.registration.repository.AuthenticationRepository
import `in`.projecteka.jataayu.registration.ui.activity.R
import `in`.projecteka.jataayu.util.livedata.SingleLiveEvent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

class LoginViewModel(private val repository: AuthenticationRepository) : BaseViewModel(), TextWatcher {

    companion object {
        private const val GRANT_TYPE_PASSWORD = "password"
    }

    val passwordInputType = ObservableInt(hiddenPasswordInputType())
    val inputUsernameLbl = ObservableField<String>()
    val inputPasswordLbl = ObservableField<String>()
    val inputPasswordVisibilityToggleLbl = ObservableField<Int>(R.string.show)
    val usernameProviderLbl = ObservableField<String>()
    val usernameProviderLblId = ObservableField<Int>(R.string.ncg)
    val loginEnabled = ObservableBoolean(false)

    val onClickRegisterEvent = SingleLiveEvent<Void>()
    val onPasswordVisibilityToggleEvent = SingleLiveEvent<Int>()
    val loginResponse = PayloadLiveData<CreateAccountResponse>()

    private fun visiblePasswordInputType(): Int {
        return InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    }

    private fun hiddenPasswordInputType(): Int {
        return InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
    }

    fun togglePasswordVisible() {
        val newInputType = when (passwordInputType.get()) {
            visiblePasswordInputType() -> {
                inputPasswordVisibilityToggleLbl.set(R.string.show)
                hiddenPasswordInputType()
            }
            hiddenPasswordInputType() -> {
                inputPasswordVisibilityToggleLbl.set(R.string.hide)
                visiblePasswordInputType()
            }
            else -> hiddenPasswordInputType()
        }
        passwordInputType.set(newInputType)
        onPasswordVisibilityToggleEvent.value = inputPasswordLbl.get()?.length ?: 0
    }

    fun onClickRegister() {
        onClickRegisterEvent.call()
    }

    fun onLoginClicked() {
        loginResponse.fetch(
            repository.login(
                LoginRequest(
                    "${inputUsernameLbl.get()}${usernameProviderLbl.get()}" ?: "", inputPasswordLbl.get() ?: "",
                    GRANT_TYPE_PASSWORD
                )
            )
        )
    }

    fun getAuthTokenWithTokenType(authToken: String?, tokenType: String?): String =
        "${tokenType?.capitalize()} $authToken"


    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        loginEnabled.set(inputUsernameLbl.get()?.isNotEmpty() == true && inputPasswordLbl.get()?.isNotEmpty() == true)
    }
}