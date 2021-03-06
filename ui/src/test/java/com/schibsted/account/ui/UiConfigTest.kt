package com.schibsted.account.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.test.mock.MockContext
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.kotlintest.matchers.beOfType
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.WordSpec
import java.util.Locale

class UiConfigTest : WordSpec({
    "Creating a new builder should retain the original values" {
        val original = OptionalConfiguration(
                Locale.CANADA,
                OptionalConfiguration.SignUpMode.Disabled("Some message"),
                false,
                123)

        original shouldEqual original.newBuilder().build()
    }

    "The builder should correctly set values from the builder" {
        val conf = OptionalConfiguration.Builder().locale(Locale.CHINA).clientLogo(123).build()
        conf.locale shouldBe Locale.CHINA
        conf.clientLogo shouldBe 123
    }

    "fromManifest should resolve it's fields from the manifest" {
        val locale = Locale("fr", "FR")
        val mockBundle: Bundle = mock {
            on { getString(any()) } doReturn listOf(
                    "fr_FR",
                    "my disabled message"
            )

            on { get(any()) }.thenReturn(
                    false,
                    true,
                    555
            )
        }
        val mockPackageManager: PackageManager = mock { on { getApplicationInfo(any(), any()) } doReturn ApplicationInfo().apply { metaData = mockBundle } }
        val mockContext: MockContext = mock {
            on { packageManager } doReturn mockPackageManager
            on { packageName } doReturn "MYPACKAGENAME"
            on { getString(any()) } doReturn "AAA"
        }

        val conf = OptionalConfiguration.fromManifest(mockContext)

        conf.locale shouldBe locale
        conf.signUpEnabled should beOfType<OptionalConfiguration.SignUpMode.Disabled>()
        (conf.signUpEnabled as OptionalConfiguration.SignUpMode.Disabled).disabledMessage shouldBe "my disabled message"
        conf.isCancellable shouldBe true
        conf.clientLogo shouldBe 555
    }

    "fromUiProvider should get it's properties from the provider" {
        val provider = object : OptionalConfiguration.UiConfigProvider {
            override fun getUiConfig() = OptionalConfiguration.DEFAULT.copy(Locale.GERMAN)
        }

        val result = OptionalConfiguration.fromUiProvider(provider)
        result.locale shouldBe Locale.GERMAN
    }
})
