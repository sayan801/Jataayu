package `in`.projecteka.jataayu.consent.viewmodel

import `in`.projecteka.jataayu.consent.R
import `in`.projecteka.jataayu.consent.model.ConsentFlow
import `in`.projecteka.jataayu.consent.model.ConsentsListResponse
import `in`.projecteka.jataayu.consent.repository.ConsentRepository
import `in`.projecteka.jataayu.core.model.*
import `in`.projecteka.jataayu.core.model.RequestStatus.GRANTED
import `in`.projecteka.jataayu.core.model.RequestStatus.REQUESTED
import `in`.projecteka.jataayu.core.model.approveconsent.CareReference
import `in`.projecteka.jataayu.core.model.approveconsent.ConsentArtifact
import `in`.projecteka.jataayu.core.model.approveconsent.ConsentArtifactRequest
import `in`.projecteka.jataayu.core.model.approveconsent.ConsentArtifactResponse
import `in`.projecteka.jataayu.network.utils.PayloadLiveData
import `in`.projecteka.jataayu.network.utils.fetch
import `in`.projecteka.jataayu.presentation.callback.IDataBindingModel
import `in`.projecteka.jataayu.presentation.callback.ItemClickCallback
import `in`.projecteka.jataayu.util.extension.EMPTY
import `in`.projecteka.jataayu.util.livedata.SingleLiveEvent
import `in`.projecteka.jataayu.util.ui.DateTimeUtils
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConsentViewModel(private val repository: ConsentRepository) : ViewModel(), ItemClickCallback {

    val consentListResponse = PayloadLiveData<ConsentsListResponse>()
    val linkedAccountsResponse = PayloadLiveData<LinkedAccountsResponse>()
    val consentArtifactResponse = PayloadLiveData<ConsentArtifactResponse>()

    val requestedConsentsList = MutableLiveData<List<Consent>>()
    val grantedConsentsList = MutableLiveData<List<Consent>>()


    val onClickConsentEvent = SingleLiveEvent<Consent>()

    private val grantedConsentStatusList = listOf(
        R.string.status_active_granted_consents,
        R.string.status_expired_granted_consents,
        R.string.status_all_granted_consents
    )
    private val requestedConsentStatusList = listOf(
        R.string.status_active_requested_consents,
        R.string.status_expired_requested_consents,
        R.string.status_all_requested_consents
    )

    internal var selectedProviderName = String.EMPTY

    fun getConsents() =
        consentListResponse.fetch(repository.getConsents())


    fun getLinkedAccounts() =
        linkedAccountsResponse.fetch(repository.getLinkedAccounts())


    fun grantConsent(
        requestId: String,
        consentArtifacts: List<ConsentArtifact>
    ) =
        consentArtifactResponse.fetch(repository.grantConsent(requestId, ConsentArtifactRequest((consentArtifacts))))


    fun getConsentArtifact(
        links: List<Links>,
        hiTypeObjects: ArrayList<HiType>,
        permission: Permission
    ): List<ConsentArtifact> {

        val consentArtifactList = ArrayList<ConsentArtifact>()
        val hiTypes = hiTypeObjects.map { it.type }


        links.forEach { link ->
            val careReferences = ArrayList<CareReference>()
            link.careContexts.forEach { careContext ->
                if (careContext.contextChecked) careReferences.add(newCareReference(link, careContext))
            }

            if (careReferences.isNotEmpty()) {
                consentArtifactList.add(
                    ConsentArtifact(
                        hiTypes,
                        link.hip,
                        careReferences,
                        permission
                    )
                )
            }
        }
        return consentArtifactList
    }

    fun populateFilterItems(flow: ConsentFlow?): List<Pair<Int, Int>> =
        if (flow == ConsentFlow.GRANTED_CONSENTS) {
            grantedConsentStatusList.map { getFormattedItem(it, GRANTED) }
        } else {
            requestedConsentStatusList.map {
                getFormattedItem(it, REQUESTED)
            }
        }


    private fun getFormattedItem(filterItem: Int, requestStatus: RequestStatus): Pair<Int, Int> {
        val list = if (requestStatus == GRANTED) {
            grantedConsentsList.value
        } else {
            requestedConsentsList.value
        }

        val count = list?.count { consent ->
            val dataExpired = DateTimeUtils.isDateExpired(consent.permission.dataExpiryAt)
            when (filterItem) {
                R.string.status_active_granted_consents,
                R.string.status_active_requested_consents -> {
                    !dataExpired
                }
                R.string.status_expired_granted_consents,
                R.string.status_expired_requested_consents -> {
                    dataExpired
                }
                else -> {
                    true
                }
            }
        }
        return Pair(filterItem, count ?: 0)
    }

    fun getItems(links: List<Links?>): List<IDataBindingModel> {
        val items = arrayListOf<IDataBindingModel>()
        links.filterNotNull().forEach { link ->
            items.add(link.hip)
            items.addAll(link.careContexts)
        }
        return items
    }

    private fun newCareReference(link: Links, it: CareContext) = CareReference(link.referenceNumber, it.referenceNumber)

    fun checkSelectionInBackground(listOfBindingModels: List<IDataBindingModel>?): Pair<Boolean, Boolean> {
        var selectableItemsCount = 0
        var selectionCount = 0

        listOfBindingModels?.filterIsInstance<CareContext>()?.run {
            selectableItemsCount = count()
            selectionCount = count { it.contextChecked }
        }
        return Pair(selectableItemsCount == selectionCount, selectionCount > 0)
    }

    fun filterConsents(consentList: List<Consent>?) {
        requestedConsentsList.value = consentList?.filter {
            it.status == REQUESTED
        }
        grantedConsentsList.value = consentList?.filter {
            it.status == GRANTED
        }
    }

    fun revokeConsent(consent: Consent) {
        repository.revokeConsent(consent)
    }

    override fun onItemClick(
        iDataBindingModel: IDataBindingModel,
        itemViewBinding: ViewDataBinding
    ) {
        if (iDataBindingModel is Consent)
            onClickConsentEvent.value = iDataBindingModel
    }
}

