package jetbrains.buildServer.sbt.test.agent.commandBuilder

import jetbrains.buildServer.sbt.SbtRunnerBuildService
import jetbrains.buildServer.sbt.SbtRunnerBuildService.Companion.SBTVersion
import jetbrains.buildServer.sbt.SbtRunnerCommandFileLayout
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Fast regression coverage for logger patch paths and redirected command-file arguments produced by the sbt runner.
 *
 * These tests cover platform-shaped strings directly,
 * so Windows quoting and path normalization are checked without requiring the test JVM to run on Windows.
 */
class SbtRunnerCommandFileLayoutTest {

    @Test
    fun logger_install_operation_for_sbt_1_x_uses_1_0_resource_and_destination() {
        val patch = SbtRunnerBuildService.getSbtLoggerPatch(SBTVersion.SBT_1_x)
        val operation = SbtRunnerCommandFileLayout.loggerInstallOperation(AUTO_INSTALL_SBT_FOLDER_WITH_SPACE, patch)

        Assert.assertEquals(operation.sourcePathInJar, "/sbt-distrib/1.0/")
        Assert.assertEquals(operation.sourceName, "sbt-teamcity-logger_2.12_1.0.jar")
        Assert.assertEquals(operation.destinationDir, "$AUTO_INSTALL_SBT_FOLDER_WITH_SPACE/tc_plugin/1.0")
        assertBundledResourceExists(operation)
    }


    @Test
    fun logger_install_operation_for_sbt_0_13_uses_0_13_resource_and_destination() {
        val patch = SbtRunnerBuildService.getSbtLoggerPatch(SBTVersion.SBT_0_13_x)
        val operation = SbtRunnerCommandFileLayout.loggerInstallOperation(AUTO_INSTALL_SBT_FOLDER_WITH_SPACE, patch)

        Assert.assertEquals(operation.sourcePathInJar, "/sbt-distrib/0.13/")
        Assert.assertEquals(operation.sourceName, "sbt-teamcity-logger_2.10_0.13.jar")
        Assert.assertEquals(operation.destinationDir, "$AUTO_INSTALL_SBT_FOLDER_WITH_SPACE/tc_plugin/0.13")
        assertBundledResourceExists(operation)
    }

    @Test
    fun logger_destination_path_is_used_as_apply_command_classpath() {
        val patch = SbtRunnerBuildService.getSbtLoggerPatch(SBTVersion.SBT_1_x)
        val loggerPath = SbtRunnerCommandFileLayout.loggerDestinationPath(AUTO_INSTALL_SBT_FOLDER_WITH_SPACE, patch)

        Assert.assertEquals(
            loggerPath,
            "$AUTO_INSTALL_SBT_FOLDER_WITH_SPACE/tc_plugin/1.0/sbt-teamcity-logger_2.12_1.0.jar",
        )

        val applyCommandText = SbtRunnerCommandFileLayout.buildSbtApplyCommandForLoggerPath(loggerPath)
        Assert.assertEquals(
            applyCommandText,
            """apply -cp "$AUTO_INSTALL_SBT_FOLDER_WITH_SPACE/tc_plugin/1.0/sbt-teamcity-logger_2.12_1.0.jar" $LOGGER_CLASS""",
        )
    }

    @Test
    fun apply_command_for_logger_path_uses_forward_slashes_for_windows_shaped_path() {
        val applyCommandText = SbtRunnerCommandFileLayout.buildSbtApplyCommandForLoggerPath(
            """C:\agent temp\agent-sbt\tc_plugin\1.0\sbt-teamcity-logger_2.12_1.0.jar""",
        )
        Assert.assertEquals(
            applyCommandText,
            // sbt and JVM APIs accept forward slashes on Windows, avoiding escaped backslashes inside sbt command text.
            """apply -cp "C:/agent temp/agent-sbt/tc_plugin/1.0/sbt-teamcity-logger_2.12_1.0.jar" $LOGGER_CLASS""",
        )
    }

    @Test
    fun redirected_command_file_argument_uses_single_quotes_on_unix() {
        val redirectCommandText =
            SbtRunnerCommandFileLayout.buildSbtInputRedirectionArgument("/agent temp/commands123.file", isWindows = false)
        Assert.assertEquals(
            redirectCommandText,
            // Unix-like shells do not need the doubled quote workaround used by sbt.bat.
            """< "/agent temp/commands123.file"""",
        )
    }

    /**
     * Verifies that Windows command-file redirection keeps the historical doubled-quote workaround.
     */
    @Test
    fun redirected_command_file_argument_uses_doubled_quotes_and_forward_slashes_on_windows() {
        Assert.assertEquals(
            SbtRunnerCommandFileLayout.buildSbtInputRedirectionArgument("""C:\agent temp\commands123.file""", isWindows = true),
            // sbt.bat historically needs doubled quotes around a redirected file path with spaces.
            "< \"\"C:/agent temp/commands123.file\"\"",
        )
    }

    private fun assertBundledResourceExists(operation: SbtRunnerCommandFileLayout.LoggerInstallOperation) {
        val resourcePath = operation.sourcePathInJar + operation.sourceName
        Assert.assertNotNull(
            javaClass.getResource(resourcePath),
            // This catches Maven/resource-layout regressions where the operation points at a jar not packaged with the agent.
            "Expected logger resource to be available on the test classpath: $resourcePath",
        )
    }

    companion object {
        // The space in this fake temp root guards the apply-command quoting regression.
        private const val AUTO_INSTALL_SBT_FOLDER_WITH_SPACE = "/agent temp/agent-sbt"
        private const val LOGGER_CLASS = "jetbrains.buildServer.sbtlogger.SbtTeamCityLogger"
    }
}
