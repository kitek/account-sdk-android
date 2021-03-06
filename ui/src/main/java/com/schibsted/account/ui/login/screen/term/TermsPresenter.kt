/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.term

import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.ui.component.CheckBoxView

/**
 * Following the MVP design pattern this interface represent the implementation of the [TermsContract.Presenter].
 * this class executes the terms and condition business logic and ask for UI updates depending on results.
 */
class TermsPresenter(private val termsView: TermsContract.View, private val provider: InputProvider<Agreements>) : TermsContract.Presenter {

    init {
        termsView.setPresenter(this)
    }

    /**
     * Verify if given checkbox is checked.
     * Calls [acceptAgreements]  if `true`
     * Shows errors if `false`
     *
     * @param termsBox [CheckBoxView]  the terms checkbox
     */
    override fun acceptTerms(termsBox: CheckBoxView) {
        if (termsView.isActive) {
            if (termsBox.isChecked) {
                acceptAgreements()
            } else {
                val tracker = BaseLoginActivity.tracker
                tracker?.eventError(TrackingData.UIError.AgreementsNotAccepted, TrackingData.Screen.AGREEMENTS)
                termsView.showError(termsBox)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    termsBox.errorView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
                }
            }
        }
    }

    /**
     * Accepts TC on backend side.
     * Order a navigation to an other screen if request succeeded, show an error otherwise
     */
    override fun acceptAgreements() {
        termsView.showProgress()
        provider.provide(Agreements(true), object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {}

            override fun onError(error: ClientError) {
                if (termsView.isActive) {
                    termsView.hideProgress()

                    val tracker = BaseLoginActivity.tracker
                    if (tracker != null && error.errorType === ClientError.ErrorType.NETWORK_ERROR) {
                        tracker.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.AGREEMENTS)
                    }
                }
            }
        })
    }
}
