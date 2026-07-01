package jetbrains.buildServer.sbt.test.agent.commandBuilder.fixture

import jetbrains.buildServer.runner.JavaRunnerConstants
import jetbrains.buildServer.sbt.IvyCacheProvider
import jetbrains.buildServer.sbt.SbtRunnerBuildService
import jetbrains.buildServer.sbt.SbtRunnerConstants
import jetbrains.buildServer.sbt.test.agent.commandBuilder.mock.*
import jetbrains.buildServer.util.FileUtil
import org.testng.Assert
import java.io.Closeable
import java.io.File
import java.nio.file.Files

/**
 * @param agentRoot temporary agent root directory deleted when the fixture is closed
 * @param service initialized SBT runner build service under test
 * @param agentTemp agent temporary directory
 */
class SbtRunnerBuildServiceFixture private constructor(
    private val agentRoot: File,
    val service: SbtRunnerBuildService,
    val agentTemp: File,
) : Closeable {
    fun agentTempPathWithNormalisedSeparator(): String =
        agentTemp.absolutePath.replace('\\', '/')

    fun assertCommandsFileIsCreated() {
        val commandFiles = agentTemp.listFiles(::isSbtCommandsFile).orEmpty()

        Assert.assertEquals(
            commandFiles.size,
            1,
            "Expected exactly one generated sbt command file in ${agentTemp.absolutePath}",
        )
    }

    private fun isSbtCommandsFile(file: File): Boolean =
        file.isFile && file.name.startsWith("commands") && file.name.endsWith(".file")

    override fun close() {
        FileUtil.delete(agentRoot)
    }

    companion object {
        fun create(agentRootPrefix: String): SbtRunnerBuildServiceFixture {
            val root = Files.createTempDirectory(agentRootPrefix).toFile()

            val agentTemp = File(root, "agent temp with spaces").apply(File::mkdirs)
            val buildTemp = File(agentTemp, "build temp with spaces").apply(File::mkdirs)
            val checkout = File(root, "checkout dir with spaces").apply(File::mkdirs)
            val working = File(root, "working dir with spaces").apply(File::mkdirs)
            val ivyCache = File(agentTemp, "system cache with spaces/sbt_ivy").apply(File::mkdirs)

            val buildParameters = MockBuildParametersMap(
                systemProperties = emptyMap(),
                environmentVariables = mapOf("PATH" to System.getenv("PATH").orEmpty()),
            )
            val runnerParameters = mapOf(
                SbtRunnerConstants.SBT_INSTALLATION_MODE_PARAM to SbtRunnerConstants.AUTO_INSTALL_FLAG,
                SbtRunnerConstants.SBT_ARGS_PARAM to "clean compile test",
                JavaRunnerConstants.TARGET_JDK_HOME to System.getProperty("java.home"),
            )
            val configParameters = mapOf(
                "teamcity.internal.sbt.setXdgRuntimeDir.enabled" to "false",
                "teamcity.internal.sbt.tempFilesCleanup.enabled" to "false",
            )

            val agentConfiguration = MockBuildAgentConfiguration(
                rootDirectory = root,
                agentTempDirectory = agentTemp,
                buildTempDirectory = buildTemp,
                buildParameters = buildParameters,
                cacheDirectories = mapOf("sbt_ivy" to ivyCache),
            )
            val build = MockAgentRunningBuild(
                agentConfiguration = agentConfiguration,
                buildParameters = buildParameters,
                runnerParameters = runnerParameters,
                checkoutDirectory = checkout,
                workingDirectory = working,
                buildTempDirectory = buildTemp,
                agentTempDirectory = agentTemp,
            )
            val runnerContext = MockBuildRunnerContext(
                build = build,
                workingDirectory = working,
                buildParameters = buildParameters,
                runnerParameters = runnerParameters,
                configParameters = configParameters,
                runType = SbtRunnerConstants.RUNNER_TYPE,
                name = "SBT health test",
            )

            val service = SbtRunnerBuildService(IvyCacheProvider(agentConfiguration))
            service.initialize(build, runnerContext)

            return SbtRunnerBuildServiceFixture(
                agentRoot = root,
                service = service,
                agentTemp = agentTemp,
            )
        }
    }
}
