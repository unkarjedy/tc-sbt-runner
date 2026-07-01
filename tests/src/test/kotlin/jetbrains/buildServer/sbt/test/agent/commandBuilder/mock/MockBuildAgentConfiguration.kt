@file:Suppress("OVERRIDE_DEPRECATION")

package jetbrains.buildServer.sbt.test.agent.commandBuilder.mock

import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildAgentSystemInfo
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.parameters.ValueResolver
import java.io.File

class MockBuildAgentConfiguration(
    private val rootDirectory: File,
    private val agentTempDirectory: File,
    private val buildTempDirectory: File,
    private val buildParameters: BuildParametersMap = MockBuildParametersMap(),
    private val cacheDirectories: Map<String, File> = emptyMap(),
) : BuildAgentConfiguration {
    private val customProperties = mutableMapOf<String, String>()
    private val configurationParameters = mutableMapOf<String, String>()

    override fun getName(): String = "mock agent"

    override fun getOwnPort(): Int = 0

    override fun getOwnAddress(): String = "127.0.0.1"

    override fun getServerUrl(): String = "http://teamcity.test"

    override fun getAuthorizationToken(): String = ""

    override fun getPingCode(): String = ""

    override fun getWorkDirectory(): File = File(rootDirectory, "work")

    override fun getBuildTempDirectory(): File = buildTempDirectory

    override fun getAgentTempDirectory(): File = agentTempDirectory

    override fun getAgentToolsDirectory(): File = File(rootDirectory, "tools")

    override fun getCacheDirectory(name: String): File =
        cacheDirectories[name] ?: File(rootDirectory, "cache/$name")

    override fun getSystemDirectory(): File = File(rootDirectory, "system")

    override fun getTempDirectory(): File = File(rootDirectory, "temp")

    override fun getAgentLibDirectory(): File = File(rootDirectory, "lib")

    override fun getAgentPluginsDirectory(): File = File(rootDirectory, "plugins")

    override fun getAgentUpdateDirectory(): File = File(rootDirectory, "update")

    override fun getAgentHomeDirectory(): File = rootDirectory

    override fun getAgentLogsDirectory(): File = File(rootDirectory, "logs")

    override fun getSystemInfo(): BuildAgentSystemInfo = unexpected("getSystemInfo")

    override fun getServerConnectionTimeout(): Int = 0

    override fun addAlternativeAgentAddress(address: String) {}

    override fun getCustomProperties(): Map<String, String> = customProperties

    override fun addCustomProperty(key: String, value: String) {
        customProperties[key] = value
    }

    override fun addSystemProperty(key: String, value: String) {}

    override fun addEnvironmentVariable(key: String, value: String) {}

    override fun addConfigurationParameter(key: String, value: String) {
        configurationParameters[key] = value
    }

    override fun removeConfigurationParameter(key: String): Boolean =
        configurationParameters.remove(key) != null

    override fun getBuildParameters(): BuildParametersMap = buildParameters

    override fun getConfigurationParameters(): Map<String, String> = configurationParameters

    override fun getParametersResolver(): ValueResolver = unexpected("getParametersResolver")

    private fun unexpected(methodName: String): Nothing =
        error("Unexpected BuildAgentConfiguration.$methodName call in test fixture")
}
