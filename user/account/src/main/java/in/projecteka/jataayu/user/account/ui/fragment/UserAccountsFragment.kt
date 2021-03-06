package `in`.projecteka.jataayu.user.account.ui.fragment

import `in`.projecteka.jataayu.core.model.HipHiuIdentifiable
import `in`.projecteka.jataayu.network.utils.Failure
import `in`.projecteka.jataayu.network.utils.PartialFailure
import `in`.projecteka.jataayu.presentation.adapter.ExpandableRecyclerViewAdapter
import `in`.projecteka.jataayu.presentation.callback.IDataBindingModel
import `in`.projecteka.jataayu.presentation.callback.ItemClickCallback
import `in`.projecteka.jataayu.presentation.showAlertDialog
import `in`.projecteka.jataayu.presentation.showErrorDialog
import `in`.projecteka.jataayu.presentation.ui.fragment.BaseFragment
import `in`.projecteka.jataayu.presentation.ui.viewmodel.ProviderAddedLiveData
import `in`.projecteka.jataayu.user.account.R
import `in`.projecteka.jataayu.user.account.databinding.FragmentUserAccountBinding
import `in`.projecteka.jataayu.user.account.ui.activity.ProfileActivity
import `in`.projecteka.jataayu.user.account.viewmodel.UserAccountsViewModel
import `in`.projecteka.jataayu.util.startProvider
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserAccountsFragment : BaseFragment(), ItemClickCallback {
    private lateinit var binding: FragmentUserAccountBinding
    private val viewModel: UserAccountsViewModel by viewModel()
    private var listItems: List<IDataBindingModel> = emptyList()

    companion object {
        fun newInstance(): UserAccountsFragment {
            return UserAccountsFragment()
        }
        private const val VERIFIED_IDENTIFIER_MOBILE = "MOBILE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserAccountBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            setHasOptionsMenu(true)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        initObservers()
        renderUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                context?.startActivity(Intent(context, ProfileActivity::class.java))
                return false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderUi() {
        viewModel.fetchAll()
    }

    private fun initObservers() {
        viewModel.updateLinks.observe(viewLifecycleOwner, Observer {
            listItems = it
            binding.rvUserAccounts.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ExpandableRecyclerViewAdapter(this@UserAccountsFragment, this@UserAccountsFragment, it
            )}
        })
        viewModel.userProfileResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Failure -> {
                    context?.showErrorDialog(it.error.localizedMessage)
                }
                is PartialFailure -> {
                    context?.showAlertDialog(
                        getString(R.string.failure), it.error?.message, getString(
                            android
                                .R.string.ok
                        )
                    )
                }
            }
        })
        viewModel.addProviderEvent.observe(viewLifecycleOwner, Observer {
            startProvider(context!!)
        })

        viewModel.linkedAccountsResponse.observe(viewLifecycleOwner, Observer { links ->
            val hipList = links.map { it.hip }
            getNamesOfHipList(hipList)
        })

        ProviderAddedLiveData.providerAddedEvent.observe(viewLifecycleOwner, Observer {
            if (it){
                ProviderAddedLiveData.providerAddedEvent.value = false
                viewModel.fetchAll()
            }
        })
    }

    override fun onItemClick(
        iDataBindingModel: IDataBindingModel,
        itemViewBinding: ViewDataBinding
    ) {}

    private fun getNamesOfHipList(idList: List<HipHiuIdentifiable>) {
        val hipHiuNameResponse = viewModel.getHipHiuNamesByIdList(idList)
        hipHiuNameResponse.observe(viewLifecycleOwner, Observer { hipHiuNameResponse ->
            if (hipHiuNameResponse.status) {
                val linkedAccountsResponse = viewModel.linkedAccountsResponse.value
                linkedAccountsResponse?.forEach { it.hip.name = hipHiuNameResponse.nameMap[it.hip.getId()] ?: "" }
                viewModel.updateDisplayAccounts(viewModel.linkedAccountsResponse.value)
            } else {
                context?.showErrorDialog(getString(R.string.something_went_wrong))
            }
        })
    }
}


