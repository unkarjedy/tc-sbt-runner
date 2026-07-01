@file:Suppress("OVERRIDE_DEPRECATION")

package jetbrains.buildServer.sbt.test.agent.commandBuilder.mock

import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentCheckoutMode
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildInterruptReason
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.NullBuildProgressLogger
import jetbrains.buildServer.agentServer.AgentBuild
import jetbrains.buildServer.artifacts.ArtifactDependencyInfo
import jetbrains.buildServer.parameters.ValueResolver
import jetbrains.buildServer.util.Option
import jetbrains.buildServer.util.PasswordReplacer
import jetbrains.buildServer.vcs.VcsChangeInfo
import jetbrains.buildServer.vcs.VcsRoot
import jetbrains.buildServer.vcs.VcsRootEntry
import jetbrains.buildServer.xmlrpc.NodeIdHolder
import java.io.File

class MockAgentRunningBuild(
    private val agentConfiguration: BuildAgentConfiguration,
    private val buildParameters: BuildParametersMap,
    private val runnerParameters: Map<String, String>,
    private val checkoutDirectory: File,
    private val workingDirectory: File,
    private val buildTempDirectory: File,
    private val agentTempDirectory: File,
    private val passwordReplacer: PasswordReplacer = NoOpPasswordReplacer(),
    private val buildLogger: BuildProgressLogger = NullBuildProgressLogger(),
) : AgentRunningBuild {
    private val sharedConfigParameters = mutableMapOf<String, String>()
    private val sharedSystemProperties = mutableMapOf<String, String>()
    private val sharedEnvironmentVariables = mutableMapOf<String, String>()

    override fun getCheckoutDirectory(): File = checkoutDirectory

    override fun getEffectiveCheckoutMode(): AgentCheckoutMode = AgentCheckoutMode.ON_AGENT

    override fun getWorkingDirectory(): File = workingDirectory

    override fun getArtifactsPaths(): String = ""

    override fun getFailBuildOnExitCode(): Boolean = true

    override fun getBuildParameters(): BuildParametersMap = buildParameters

    override fun getRunnerParameters(): Map<String, String> = runnerParameters

    override fun getBuildNumber(): String = "1"

    override fun getSharedConfigParameters(): Map<String, String> = sharedConfigParameters

    override fun addSharedConfigParameter(key: String, value: String) {
        sharedConfigParameters[key] = value
    }

    override fun addSharedSystemProperty(key: String, value: String) {
        sharedSystemProperties[key] = value
    }

    override fun addSharedEnvironmentVariable(key: String, value: String) {
        sharedEnvironmentVariables[key] = value
    }

    override fun getSharedBuildParameters(): BuildParametersMap = buildParameters

    override fun getSharedParametersResolver(): ValueResolver = unexpected("getSharedParametersResolver")

    override fun getBuildFeatures(): Collection<AgentBuildFeature> = emptyList()

    override fun getBuildFeaturesOfType(type: String): Collection<AgentBuildFeature> = emptyList()

    override fun stopBuild(reason: String) {}

    override fun getInterruptReason(): BuildInterruptReason = BuildInterruptReason.UNKNOWN_REASON

    override fun interruptBuild(comment: String, reAddToQueue: Boolean) {}

    override fun isBuildFailingOnServer(): Boolean = false

    override fun isInAlwaysExecutingStage(): Boolean = false

    override fun getPasswordReplacer(): PasswordReplacer = passwordReplacer

    override fun getArtifactStorageSettings(): Map<String, String> = emptyMap()

    override fun getNodeIdHolder(): NodeIdHolder = unexpected("getNodeIdHolder")

    override fun getProjectName(): String = "mock project"

    override fun getBuildTypeId(): String = "MockBuildTypeId"

    override fun getBuildTypeExternalId(): String = "MockBuildTypeExternalId"

    override fun getBuildTypeName(): String = "mock build type"

    override fun getBuildId(): Long = 1L

    override fun isCleanBuild(): Boolean = false

    override fun isPersonal(): Boolean = false

    override fun isPersonalPatchAvailable(): Boolean = false

    override fun isCheckoutOnAgent(): Boolean = true

    override fun isCheckoutOnServer(): Boolean = false

    override fun getCheckoutType(): AgentBuild.CheckoutType = AgentBuild.CheckoutType.ON_AGENT

    override fun getExecutionTimeoutMinutes(): Long = 0L

    override fun getArtifactDependencies(): List<ArtifactDependencyInfo> = emptyList()

    override fun getAccessUser(): String = ""

    override fun getAccessCode(): String = ""

    override fun getVcsRootEntries(): List<VcsRootEntry> = emptyList()

    override fun getBuildCurrentVersion(root: VcsRoot): String = ""

    override fun getBuildPreviousVersion(root: VcsRoot): String = ""

    override fun isCustomCheckoutDirectory(): Boolean = false

    override fun getVcsChanges(): List<VcsChangeInfo> = emptyList()

    override fun getPersonalVcsChanges(): List<VcsChangeInfo> = emptyList()

    override fun getBuildTempDirectory(): File = buildTempDirectory

    override fun getAgentTempDirectory(): File = agentTempDirectory

    override fun getBuildLogger(): BuildProgressLogger = buildLogger

    override fun getAgentConfiguration(): BuildAgentConfiguration = agentConfiguration

    override fun <T : Any?> getBuildTypeOptionValue(option: Option<T>): T = unexpected("getBuildTypeOptionValue")

    override fun getDefaultCheckoutDirectory(): File = checkoutDirectory

    override fun getVcsSettingsHashForCheckoutMode(checkoutMode: AgentCheckoutMode): String = ""

    override fun getBuildRunners(): List<BuildRunnerSettings> = emptyList()

    override fun describe(verbose: Boolean): String = "sbt command line health test build"

    private fun unexpected(methodName: String): Nothing =
        error("Unexpected AgentRunningBuild.$methodName call in test fixture")
}
