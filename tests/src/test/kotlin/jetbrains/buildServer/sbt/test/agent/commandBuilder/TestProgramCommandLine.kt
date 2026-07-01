package jetbrains.buildServer.sbt.test.agent.commandBuilder

import jetbrains.buildServer.agent.runner.ProgramCommandLine

/**
 * Test-focused [ProgramCommandLine] implementation whose string values may contain expectation macros.
 */
class TestProgramCommandLine private constructor(
    private val executablePath: String,
    private val arguments: List<String>,
    private val workingDirectory: String,
    private val environment: Map<String, String>,
) : ProgramCommandLine {
    override fun getExecutablePath(): String = executablePath

    override fun getWorkingDirectory(): String = workingDirectory

    override fun getArguments(): List<String> = arguments

    override fun getEnvironment(): Map<String, String> = environment

    companion object {
        fun fromArguments(
            executablePath: String,
            arguments: List<String>,
            workingDirectory: String = "",
            environment: Map<String, String> = emptyMap(),
        ): TestProgramCommandLine =
            TestProgramCommandLine(
                executablePath = executablePath,
                arguments = arguments,
                workingDirectory = workingDirectory,
                environment = environment,
            )
    }
}
