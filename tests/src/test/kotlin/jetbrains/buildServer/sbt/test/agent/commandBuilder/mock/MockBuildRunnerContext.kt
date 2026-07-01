package jetbrains.buildServer.sbt.test.agent.commandBuilder.mock

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.parameters.ValueResolver
import java.io.File

class MockBuildRunnerContext(
    private val build: AgentRunningBuild,
    private val workingDirectory: File,
    private val buildParameters: BuildParametersMap,
    private val runnerParameters: Map<String, String>,
    private val configParameters: Map<String, String>,
    private val runType: String,
    private val name: String,
) : BuildRunnerContext {
    private val systemProperties = mutableMapOf<String, String>()
    private val environmentVariables = mutableMapOf<String, String>()
    private val mutableConfigParameters = configParameters.toMutableMap()
    private val mutableRunnerParameters = runnerParameters.toMutableMap()

    override fun getId(): String = "mock-runner-context"

    override fun getBuild(): AgentRunningBuild = build

    override fun getWorkingDirectory(): File = workingDirectory

    override fun getRunType(): String = runType

    override fun getName(): String = name

    override fun getBuildParameters(): BuildParametersMap = buildParameters

    override fun getConfigParameters(): Map<String, String> = mutableConfigParameters

    override fun getRunnerParameters(): Map<String, String> = mutableRunnerParameters

    override fun addSystemProperty(key: String, value: String) {
        systemProperties[key] = value
    }

    override fun addEnvironmentVariable(key: String, value: String) {
        environmentVariables[key] = value
    }

    override fun addConfigParameter(key: String, value: String) {
        mutableConfigParameters[key] = value
    }

    override fun addRunnerParameter(key: String, value: String) {
        mutableRunnerParameters[key] = value
    }

    override fun getParametersResolver(): ValueResolver = unexpected("getParametersResolver")

    @Throws(ToolCannotBeFoundException::class)
    override fun getToolPath(toolName: String): String = unexpected("getToolPath")

    override fun parametersHaveReferencesTo(names: Collection<String>): Boolean = false

    override fun isVirtualContext(): Boolean = false

    override fun getVirtualContext(): VirtualContext = unexpected("getVirtualContext")

    private fun unexpected(methodName: String): Nothing =
        error("Unexpected BuildRunnerContext.$methodName call in test fixture")
}
