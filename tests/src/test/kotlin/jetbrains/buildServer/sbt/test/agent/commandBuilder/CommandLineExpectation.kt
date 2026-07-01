package jetbrains.buildServer.sbt.test.agent.commandBuilder

import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.sbt.test.agent.commandBuilder.fixture.SbtRunnerBuildServiceFixture
import org.testng.Assert
import java.io.File

/**
 * Asserts a TeamCity command line against an expected command-line object.
 *
 * Expected [ProgramCommandLine] string values may contain macros such as `%AGENT_TEMP%`.
 *
 * Example:
 * ```
 * CommandLineExpectation.forSbtRunnerFixture(fixture).assertMatches(
 *     commandLine,
 *     TestProgramCommandLine.fromArguments(
 *         executablePath = "%JAVA_EXECUTABLE%",
 *         arguments = listOf(
 *             "-Djava.io.tmpdir=%AGENT_TEMP%/build temp with spaces",
 *             "xsbt.boot.Boot",
 *             "< \"%AGENT_TEMP%/commands%RANDOM_NUMBER%.file\"",
 *         ),
 *     ),
 * )
 * ```
 */
class CommandLineExpectation private constructor(
    private val macroPatterns: Map<String, String>,
) {
    fun assertMatches(actual: ProgramCommandLine, expected: ProgramCommandLine) {
        assertElementMatches(
            actual = actual.executablePath,
            expected = expected.executablePath,
            elementName = "executable path",
        )

        val expectedArguments = expected.arguments
        val actualArguments = actual.arguments
        Assert.assertEquals(
            actualArguments.size,
            expectedArguments.size,
            "Command-line arguments count did not match.\nExpected:\n${expectedArguments.joinToString("\n")}\nActual:\n${actualArguments.joinToString("\n")}",
        )

        expectedArguments.zip(actualArguments).forEachIndexed { index, (expectedPattern, actual) ->
            assertElementMatches(
                actual = actual,
                expected = expectedPattern,
                elementName = "argument #$index",
            )
        }
    }

    private fun assertElementMatches(actual: String, expected: String, elementName: String) {
        val expectedPattern = convertTemplateTextToPattern(expected)
        Assert.assertTrue(
            Regex(expectedPattern).matches(actual),
            "Command-line $elementName did not match expected template.\nExpanded expected pattern:\n$expectedPattern\nActual:\n$actual",
        )
    }

    private fun convertTemplateTextToPattern(text: String): String {
        val result = StringBuilder()
        var index = 0

        for (match in MACRO.findAll(text)) {
            result.append(Regex.escape(text.substring(index, match.range.first)))
            result.append(
                macroPatterns[match.value]
                    ?: error("Unknown command-line test macro: ${match.value}"),
            )
            index = match.range.last + 1
        }

        result.append(Regex.escape(text.substring(index)))
        return result.toString()
    }

    companion object {
        private val MACRO = Regex("%[A-Z_]+%")

        fun forSbtRunnerFixture(fixture: SbtRunnerBuildServiceFixture): CommandLineExpectation {
            val macroPatterns = mapOf(
                "%AGENT_TEMP%" to pathPattern(fixture.agentTempPathWithNormalisedSeparator()),
                "%JAVA_EXECUTABLE%" to Regex.escape(File(System.getProperty("java.home"), "bin/java").canonicalPath),
                "%RANDOM_NUMBER%" to "\\d+",
            )
            return CommandLineExpectation(macroPatterns)
        }

        private fun pathPattern(path: String): String {
            return if (path.startsWith("/var/")) {
                "(?:/private)?${Regex.escape(path)}"
            } else {
                Regex.escape(path)
            }
        }
    }
}
