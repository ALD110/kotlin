/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import org.jetbrains.kotlin.ide.konan.gradle.KotlinGradleNativeMultiplatformModuleBuilder
import org.jetbrains.kotlin.idea.configuration.KotlinGradleMobileMultiplatformModuleBuilder
import org.jetbrains.kotlin.idea.configuration.KotlinGradleMobileSharedMultiplatformModuleBuilder
import org.jetbrains.kotlin.idea.configuration.KotlinGradleSharedMultiplatformModuleBuilder
import org.jetbrains.kotlin.idea.configuration.KotlinGradleWebMultiplatformModuleBuilder
import org.jetbrains.kotlin.idea.test.KotlinSdkCreationChecker
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.test.JUnit3WithIdeaConfigurationRunner
import org.jetbrains.kotlin.test.runTest
import org.junit.runner.RunWith
import java.nio.file.Paths

@RunWith(JUnit3WithIdeaConfigurationRunner::class)
class GradleMultiplatformWizardTest : AbstractGradleMultiplatformWizardTest() {

    // Checks that template has only the necessary folders (see KT-34127)
    private val dirsForCheckMobile = mapOf(
        Paths.get("app", "src").toString() to listOf("commonMain", "commonTest", "iosMain", "iosTest", "main", "test"),
        "iosApp" to listOf("iosApp", "iosApp.xcodeproj", "iosAppTests")
    )
    private val dirsForCheckMobileShared = mapOf("src" to listOf("commonMain", "commonTest", "iosMain", "iosTest", "jvmMain", "jvmTest"))
    private val dirsForCheckNative = mapOf("src" to listOf("macosMain", "macosTest"))
    private val dirsForCheckShared =
        mapOf("src" to listOf("commonMain", "commonTest", "jsMain", "jsTest", "jvmMain", "jvmTest", "macosMain", "macosTest"))
    private val dirsForCheckWeb =
        mapOf("src" to listOf("commonMain", "commonTest", "jsMain", "jsTest", "jvmMain", "jvmTest"))


    lateinit var sdkCreationChecker: KotlinSdkCreationChecker

    override fun setUp() {
        super.setUp()
        sdkCreationChecker = KotlinSdkCreationChecker()
    }

    override fun tearDown() {
        sdkCreationChecker.removeNewKotlinSdk()
        super.tearDown()
    }

    fun testMobile() {
        val builder = KotlinGradleMobileMultiplatformModuleBuilder()

        // TODO: add import & tests here when we will be able to locate Android SDK automatically (see KT-27635)
        val project = testImportFromBuilder(builder, performImport = false, dirsForCheck = dirsForCheckMobile)
        doImportProject(project, expectedError = "PleaseSpecifyAndroidSdkPathHere") // KT-34129

        if (!System.getenv("ANDROID_SDK").isNullOrEmpty()) {
            setAndroidSdk(project, System.getenv("ANDROID_SDK"), "SampleTests", "SampleTestsJVM")
            changeGradleVersions(project, listOf("4.9", "5.6.4")) // Error with 6.3 version of gradle
        }
    }

    fun testMobileShared() {
        val builder = KotlinGradleMobileSharedMultiplatformModuleBuilder()
        val project =
            testImportFromBuilder(builder, "SampleTests", "SampleTestsJVM", metadataInside = true, dirsForCheck = dirsForCheckMobileShared)

        // Native tests can be run on Mac only
        if (HostManager.hostIsMac) {
            runTaskInProject(project, builder.nativeTestName)
        }

        changeGradleVersions(project, listOf("4.9", "5.6.4", "6.3"))
    }

    fun testNative() {
        val builder = KotlinGradleNativeMultiplatformModuleBuilder()
        val project = testImportFromBuilder(builder, dirsForCheck = dirsForCheckNative)
        runTaskInProject(project, builder.nativeTestName)
        changeGradleVersions(project, listOf("4.9", "5.6.4", "6.3"))
    }

    fun testShared() {
        val builder = KotlinGradleSharedMultiplatformModuleBuilder()
        val project =
            testImportFromBuilder(
                builder,
                "SampleTests",
                "SampleTestsJVM",
                "SampleTestsNative",
                metadataInside = true,
                dirsForCheck = dirsForCheckShared
            )
        runTaskInProject(project, builder.nativeTestName)

        val moduleBuilder = KotlinGradleNativeMultiplatformModuleBuilder()
        testAddModule(moduleBuilder, project = project)
    }

    fun testSharedWithQualifiedName() {
        val builder = KotlinGradleSharedMultiplatformModuleBuilder()
        val project = testImportFromBuilder(
            builder,
            "SampleTests",
            "SampleTestsJVM",
            "SampleTestsNative",
            metadataInside = true,
            useQualifiedModuleNames = true
        )
        runTaskInProject(project, builder.nativeTestName)
        changeGradleVersions(project, listOf("4.9", "5.6.4", "6.3"))
    }

    fun testWeb() {
        runTest {
            testImportFromBuilder(
                KotlinGradleWebMultiplatformModuleBuilder(),
                "SampleTests",
                "SampleTestsJVM",
                dirsForCheck = dirsForCheckWeb
            )
        }
    }
}