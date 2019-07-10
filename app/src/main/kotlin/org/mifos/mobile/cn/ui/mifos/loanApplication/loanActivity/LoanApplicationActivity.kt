package org.mifos.mobile.cn.ui.mifos.loanApplication.loanActivity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError
import kotlinx.android.synthetic.main.activity_loan_application.*
import org.mifos.mobile.cn.R
import org.mifos.mobile.cn.data.models.accounts.loan.*
import org.mifos.mobile.cn.data.models.product.Product
import org.mifos.mobile.cn.ui.adapter.LoanApplicationStepAdapter
import org.mifos.mobile.cn.ui.base.MifosBaseActivity
import org.mifos.mobile.cn.ui.mifos.loanApplication.OnNavigationBarListener

import org.mifos.mobile.cn.ui.utils.RxBus
import org.mifos.mobile.cn.ui.utils.ConstantKeys
import org.mifos.mobile.cn.ui.utils.ConstantKeys.CURRENT_STEP_POSITION
import org.mifos.mobile.cn.ui.utils.RxEvent
import java.util.ArrayList
import javax.inject.Inject

class LoanApplicationActivity : MifosBaseActivity(), StepperLayout.StepperListener, LoanApplicationContract.View ,OnNavigationBarListener.LoanDetailsData,
OnNavigationBarListener.ReviewLoan,OnNavigationBarListener.LoanDebtIncomeData,OnNavigationBarListener.LoanCoSignerData{


    var currentPosition = 0

    override lateinit var creditWorthinessSnapshot: MutableList<CreditWorthinessSnapshot>
    private lateinit var customerIdentifier: String
    private lateinit var loanParameters: LoanParameters
    override lateinit var selectedProduct: String
    override lateinit var loanAccount: LoanAccount

    @Inject
    lateinit var loanApplicationPresenter: LoanApplicationPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.apply_for_loan))
        activityComponent.inject(this)
        loanApplicationPresenter.attachView(this)
        setContentView(R.layout.activity_loan_application)

        creditWorthinessSnapshot = ArrayList()
        loanAccount =  LoanAccount()
        loanParameters = LoanParameters()
        customerIdentifier = intent.extras.getString(ConstantKeys.CUSTOMER_IDENTIFIER)


        val stepAdapter = LoanApplicationStepAdapter(
                supportFragmentManager, this)
        stepperLayout.setAdapter(stepAdapter, currentPosition)
        stepperLayout.setListener(this)
        stepperLayout.setOffscreenPageLimit(stepAdapter.count)
        showBackButton()
        initialiseListener()

    }

    fun initialiseListener() {
        RxBus.listen(RxEvent.SetLoanDetails::class.java).subscribe({
            setLoanDetails(it.currentState, it.identifier, it.productIdentifier, it.maximumBalance,
                    it.paymentCycle, it.termRange)
        })

        RxBus.listen(RxEvent.SetCoSignerDebtIncome::class.java).subscribe({
            setCoSignerDebtIncome(it.coSignerDebtIncome)
        })

        RxBus.listen(RxEvent.SetDebtIncome::class.java).subscribe({
            setDebtIncome(it.debtIncome)
        })
    }

    override fun onStepSelected(newStepPosition: Int) {

    }


    override fun onError(verificationError: VerificationError?) {

    }

    override fun onReturn() {
        finish()
    }

    override fun onCompleted(completeButton: View?) {
        loanParameters.creditWorthinessSnapshots = creditWorthinessSnapshot
        loanAccount.parameters = Gson().toJson(loanParameters)
        //TODO: add presenter to make api call
        loanApplicationPresenter.createLoan(loanAccount)


    }

    override fun applicationCreatedSuccessfully() {
        finish()
    }

    private fun setLoanDetails(currentState: LoanAccount.State, identifier: String,
                               productIdentifier: String, maximumBalance: Double?, paymentCycle: PaymentCycle,
                               termRange: TermRange) {
        loanAccount.currentState = currentState
        loanAccount.identifier = identifier
        loanAccount.productIdentifier = productIdentifier

        loanParameters.customerIdentifier = customerIdentifier
        loanParameters.maximumBalance = maximumBalance
        loanParameters.paymentCycle = paymentCycle
        loanParameters.termRange = termRange
    }

    override fun setDebtIncome(debtIncome: CreditWorthinessSnapshot) {
        debtIncome.forCustomer = customerIdentifier
        creditWorthinessSnapshot.add(debtIncome)
    }

    override fun setCoSignerDebtIncome(coSignerDebtIncome: CreditWorthinessSnapshot) {
        if (TextUtils.isEmpty(coSignerDebtIncome.forCustomer)) {
            coSignerDebtIncome.forCustomer = customerIdentifier
        }
        creditWorthinessSnapshot.add(coSignerDebtIncome)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_STEP_POSITION, stepperLayout.currentStepPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentPosition = savedInstanceState.getInt(CURRENT_STEP_POSITION)
    }

    //TODO: modify this
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun setLoanDetails(currentState: LoanAccount.State, identifier: String, productIdentifier: String, maximumBalance: Double?, paymentCycle: PaymentCycle, termRange: TermRange, selectedProduct: String) {
        this.selectedProduct = selectedProduct
        loanAccount.currentState = currentState
        loanAccount.identifier = identifier
        loanAccount.productIdentifier = productIdentifier

        loanParameters.customerIdentifier = customerIdentifier
        loanParameters.maximumBalance = maximumBalance
        loanParameters.paymentCycle = paymentCycle
        loanParameters.termRange = termRange
        loanAccount.parameters = Gson().toJson(loanParameters)
    }
    override fun showProgressbar(message: String) {
        stepperLayout.showProgress(message)
    }

}