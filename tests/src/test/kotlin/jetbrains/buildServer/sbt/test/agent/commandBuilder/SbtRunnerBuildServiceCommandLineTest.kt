package jetbrains.buildServer.sbt.test.agent.commandBuilder

import jetbrains.buildServer.sbt.SbtRunnerBuildService
import jetbrains.buildServer.sbt.test.agent.TestSystemProperties.withOsName
import jetbrains.buildServer.sbt.test.agent.commandBuilder.fixture.SbtRunnerBuildServiceFixture
import org.testng.annotations.Test

/**
 * Test coverage for the final TeamCity command line assembled by [SbtRunnerBuildService.makeProgramCommandLine].
 *
 * These tests intentionally inspect [jetbrains.buildServer.agent.runner.ProgramCommandLine.getExecutablePath] and
 * [jetbrains.buildServer.agent.runner.ProgramCommandLine.getArguments] so each command-line element is checked
 * independently.
 */
class SbtRunnerBuildServiceCommandLineTest {
    @Test
    fun make_program_command_line_builds_unix_command_file_argument_with_paths_containing_spaces() {
        SbtRunnerBuildServiceFixture.create("unix agent root").use { fixture ->
            val commandLine = withOsName("Linux") {
                fixture.service.makeProgramCommandLine()
            }

            CommandLineExpectation.forSbtRunnerFixture(fixture).assertMatches(
                commandLine,
                TestProgramCommandLine.fromArguments(
                    executablePath = "%JAVA_EXECUTABLE%",
                    arguments = listOf(
                        "-Djava.io.tmpdir=%AGENT_TEMP%/build temp with spaces",
                        "-Dsbt.ivy.home=%AGENT_TEMP%/system cache with spaces/sbt_ivy",
                        "-classpath",
                        "%AGENT_TEMP%/agent-sbt/bin/sbt-launch.jar:%AGENT_TEMP%/agent-sbt/bin/classes:",
                        "xsbt.boot.Boot",
                        """< "%AGENT_TEMP%/commands%RANDOM_NUMBER%.file"""",
                    ),
                ),
            )

            fixture.assertCommandsFileIsCreated()
        }
    }

    @Test
    fun make_program_command_line_builds_windows_command_file_argument_with_paths_containing_spaces() {
        SbtRunnerBuildServiceFixture.create("windows agent root").use { fixture ->
            val commandLine = withOsName("Windows 11") {
                fixture.service.makeProgramCommandLine()
            }
            CommandLineExpectation.forSbtRunnerFixture(fixture).assertMatches(
                commandLine,
                TestProgramCommandLine.fromArguments(
                    executablePath = "%JAVA_EXECUTABLE%",
                    arguments = listOf(
                        "-Djava.io.tmpdir=%AGENT_TEMP%/build temp with spaces",
                        "-Dsbt.ivy.home=%AGENT_TEMP%/system cache with spaces/sbt_ivy",
                        "-classpath",
                        "%AGENT_TEMP%/agent-sbt/bin/sbt-launch.jar:%AGENT_TEMP%/agent-sbt/bin/classes:",
                        "xsbt.boot.Boot",
                        "< \"\"%AGENT_TEMP%/commands%RANDOM_NUMBER%.file\"\"",
                    ),
                ),
            )

            fixture.assertCommandsFileIsCreated()
        }
    }
}
