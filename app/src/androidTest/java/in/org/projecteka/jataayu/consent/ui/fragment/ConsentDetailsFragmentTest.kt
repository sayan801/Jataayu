package `in`.org.projecteka.jataayu.consent.ui.fragment

import `in`.org.projecteka.jataayu.R
import `in`.org.projecteka.jataayu.core.model.Consent
import `in`.org.projecteka.jataayu.testUtil.AssetReaderUtil
import `in`.org.projecteka.jataayu.ui.activity.TestsOnlyActivity
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.notDisplayed
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



@LargeTest
@RunWith(AndroidJUnit4::class)
class ConsentDetailsFragmentTest{

    @get:Rule
    var activityRule: IntentsTestRule<TestsOnlyActivity> =
        IntentsTestRule(TestsOnlyActivity::class.java, true, true)

    private lateinit var consent: Consent

    private lateinit var eventBus: EventBus

    @Before
    @Throws(Exception::class)
    fun setup() {
        readConsentAndLaunchFragment("consent_requested.json")
        activityRule.activity.addFragment(ConsentDetailsFragment())
    }

    private fun readConsentAndLaunchFragment(fileName: String) {
        consent =  Gson().fromJson<Consent>(AssetReaderUtil.asset(activityRule.activity.applicationContext, fileName), Consent::class.java)!!

        EventBus.getDefault().postSticky(consent)
    }

    @Test
    fun shouldShowButtonsButtonIfStatusIsRequested(){
        displayed{
            id(R.id.btn_edit)
            text("Edit")
        }

        displayed { id(R.id.btn_grant)
            text("Grant consent")
        }

        displayed {
            id(R.id.btn_deny)
            text("deny")
        }
    }

    @Test
    fun shouldRenderConsentDetails(){
        displayed {
            id(R.id.tv_requester_name)
            text("Dr. Lakshmi")
        }

        displayed {
            id(R.id.tv_requester_organization)
            text("Max Health Care")
        }

        displayed {
            id(R.id.tv_purpose_of_request)
            text("REMOTE_CONSULTING")
        }

        displayed {
            id(R.id.tv_requests_info_from)
            text("01 Jan, 2020")
        }

        displayed {
            id(R.id.tv_requests_info_to)
            text("08 Jan, 2020")
        }

        displayed {
            id(R.id.tv_expiry)
            text("05 PM, 30 Jan, 2020")
        }

        displayed {
            id(R.id.cg_request_info_types)
            text("Condition")
            text("DiagnosticReport")
            text("Observation")
            }

        displayed { id(R.id.disclaimer)
        text("By granting this consent, you also agree to let Dr. Lakshmi view your linked accounts.")}
    }

    @Test
    fun shouldNotShowButtonsForExpiredConsent(){

        readConsentAndLaunchFragment("consent_expired.json")
        activityRule.activity.replaceFragment(ConsentDetailsFragment())

        notDisplayed { id(R.id.btn_edit)
        text("Edit")}

        notDisplayed { id(R.id.btn_grant)
            text("Grant consent")
        }

        notDisplayed {
            id(R.id.btn_deny)
            text("deny")
        }
    }


}