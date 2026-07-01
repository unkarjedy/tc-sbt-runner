package jetbrains.buildServer.sbt.test.agent.commandBuilder.mock

import jetbrains.buildServer.agent.BuildParametersMap

class MockBuildParametersMap(
    private val systemProperties: Map<String, String> = emptyMap(),
    private val environmentVariables: Map<String, String> = emptyMap(),
) : BuildParametersMap {
    private val allParameters: Map<String, String> =
        systemProperties.mapKeys { "system.${it.key}" } +
            environmentVariables.mapKeys { "env.${it.key}" }

    override fun getEnvironmentVariables(): Map<String, String> = environmentVariables

    override fun getSystemProperties(): Map<String, String> = systemProperties

    override fun getAllParameters(): Map<String, String> = allParameters
}
