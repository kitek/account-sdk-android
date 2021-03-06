/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.AccountStatusResponse
import com.schibsted.account.network.response.ApiContainer
import retrofit2.Call

internal class AccountStatusOperation(
    identifier: Identifier,
    failure: (error: NetworkError) -> Unit,
    success: (status: AccountStatusResponse) -> Unit
) {

    init {
        ClientTokenOperation(
                { failure(it) },
                { clientToken ->
                    val requestCall: Call<ApiContainer<AccountStatusResponse>> = if (identifier.identifierType == Identifier.IdentifierType.EMAIL) {
                        ServiceHolder.clientService.getEmailSignUpStatus(clientToken, identifier.identifier)
                    } else {
                        ServiceHolder.clientService.getPhoneSignUpStatus(clientToken, identifier.identifier)
                    }

                    requestCall.enqueue(object : NetworkCallback<ApiContainer<AccountStatusResponse>>("Verifying status of the user") {
                        override fun onSuccess(result: ApiContainer<AccountStatusResponse>) {
                            success(result.data)
                        }

                        override fun onError(error: NetworkError) {
                            failure(error)
                        }
                    })
                }
        )
    }
}
