package `in`.projecteka.jataayu.provider.ui.fragment

import `in`.projecteka.featuresprovider.R
import `in`.projecteka.jataayu.core.databinding.VerityOtpFragmentBinding
import `in`.projecteka.jataayu.core.handler.OtpChangeHandler
import `in`.projecteka.jataayu.core.handler.OtpChangeWatcher
import `in`.projecteka.jataayu.core.handler.OtpSubmissionClickHandler
import `in`.projecteka.jataayu.network.model.ErrorResponse
import `in`.projecteka.jataayu.network.utils.ResponseCallback
import `in`.projecteka.jataayu.presentation.showErrorDialog
import `in`.projecteka.jataayu.presentation.ui.fragment.BaseFragment
import `in`.projecteka.jataayu.provider.model.Otp
import `in`.projecteka.jataayu.provider.model.SuccessfulLinkingResponse
import `in`.projecteka.jataayu.provider.viewmodel.ProviderSearchViewModel
import `in`.projecteka.jataayu.util.extension.setTitle
import `in`.projecteka.jataayu.util.sharedPref.setProviderAdded
import `in`.projecteka.jataayu.util.startDashboard
import `in`.projecteka.jataayu.util.startLauncher
import `in`.projecteka.jataayu.util.ui.UiUtils
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class VerifyOtpFragment : BaseFragment(),
    OtpSubmissionClickHandler, OtpChangeHandler, ResponseCallback {
    private lateinit var binding: VerityOtpFragmentBinding

    override fun setButtonEnabled(isOtpEntered: Boolean) {
        binding.isOtpEntered = isOtpEntered
    }

    companion object {
        fun newInstance() = VerifyOtpFragment()
        const val ERROR_CODE_INVALID_OTP = 1003
    }

    private val viewModel : ProviderSearchViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = VerityOtpFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBindings()
    }

    private fun initBindings() {
        binding.selectedProviderName = viewModel.selectedProviderName
        binding.mobile = viewModel.linkAccountsResponse.value?.link?.meta?.communicationHint
        binding.clickHandler = this
        binding.isOtpEntered = false
        binding.otpChangeWatcher = OtpChangeWatcher(6, this)
    }

    override fun onVisible() {
        super.onVisible()
        setTitle(R.string.verification)
    }

    private val observer = Observer<SuccessfulLinkingResponse> {
        activity?.run{
            finish()
            setProviderAdded(true)
            startDashboard(this){
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    override fun onSubmitOtp(view: View) {
        UiUtils.hideKeyboard(activity!!)
        viewModel.showProgress(true)
        viewModel.successfulLinkingResponse.observe(this, observer)
        val referenceNumber = viewModel.linkAccountsResponse.value?.link?.referenceNumber!!
        val otp = Otp(binding.etOtp.text.toString())
        viewModel.verifyOtp(referenceNumber, otp, this)
    }

    override fun <T> onSuccess(body: T?) {
        viewModel.showProgress(false)
    }

    override fun onFailure(errorBody: ErrorResponse) {
        viewModel.showProgress(false)
       binding.errorMessage = if (errorBody.error.code == ERROR_CODE_INVALID_OTP) {
            getString(R.string.invalid_otp)
        } else {
           errorBody.error.message
        }

    }

    override fun onFailure(t: Throwable) {
        viewModel.showProgress(false)
        context?.showErrorDialog(t.localizedMessage)
    }
}