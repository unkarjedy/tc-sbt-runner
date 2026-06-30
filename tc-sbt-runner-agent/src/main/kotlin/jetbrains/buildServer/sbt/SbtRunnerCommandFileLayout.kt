package jetbrains.buildServer.sbt

import jetbrains.buildServer.sbt.SbtRunnerBuildService.Companion.SbtLoggerPatch

/**
 * Builds the deterministic paths and command fragments used to install and load the TeamCity sbt logger.
 * (https://github.com/JetBrains/sbt-tc-logger)
 */
object SbtRunnerCommandFileLayout {
    private const val SBT_PATCH_FOLDER_NAME = "tc_plugin"
    private const val SBT_DISTRIB = "sbt-distrib"
    private const val SBT_PATCH_CLASS_NAME = "jetbrains.buildServer.sbtlogger.SbtTeamCityLogger"

    /**
     * Describes one logger copy operation from the TeamCity agent plugin classpath into the temporary sbt home.
     */
    data class LoggerInstallOperation(
        val sourcePathInJar: String,
        val sourceName: String,
        val destinationDir: String,
    )

    /**
     * Returns the runtime jar path used by sbt's `apply -cp` command.
     */
    @JvmStatic
    fun loggerDestinationPath(autoInstallSbtFolder: String, patch: SbtLoggerPatch): String =
        "${loggerDestinationDirectory(autoInstallSbtFolder, patch)}/${patch.jarName}"

    /**
     * Returns the runtime directory where the selected logger jar is copied.
     */
    private fun loggerDestinationDirectory(autoInstallSbtFolder: String, patch: SbtLoggerPatch): String =
        "$autoInstallSbtFolder/${patchFolder(patch)}"

    /**
     * Returns the relative folder where a logger patch is installed under the temporary sbt home.
     */
    private fun patchFolder(patch: SbtLoggerPatch): String =
        "$SBT_PATCH_FOLDER_NAME/${patch.folderName}"

    /**
     * Returns the source and destination arguments needed to copy the selected logger jar.
     */
    @JvmStatic
    fun loggerInstallOperation(autoInstallSbtFolder: String, patch: SbtLoggerPatch): LoggerInstallOperation =
        LoggerInstallOperation(
            sourcePathInJar = loggerResourceDirectory(patch),
            sourceName = patch.jarName,
            destinationDir = loggerDestinationDirectory(autoInstallSbtFolder, patch),
        )

    /**
     * Returns the classpath resource folder that contains the selected logger jar.
     */
    private fun loggerResourceDirectory(patch: SbtLoggerPatch): String =
        "/$SBT_DISTRIB/${patch.folderName}/"

    /**
     * Builds the sbt command that loads the TeamCity logger from an already resolved patch jar path.
     *
     * Historical context:
     * - https://youtrack.jetbrains.com/issue/TW-50057
     * - https://youtrack.jetbrains.com/issue/TW-51551
     *
     * Those reports showed that agent paths with spaces made sbt split an unquoted `apply -cp` logger path.
     * The quoted classpath was introduced in commit 14200993af9529f20ead4c2bff7fd44c17a9a031.
     * Windows forward-slash normalization was added in commit 46f49c78ab94016e085e62be2265ff683d792a59.
     */
    @JvmStatic
    fun buildSbtApplyCommandForLoggerPath(loggerPath: String): String =
        """apply -cp "${normalizePathForSbt(loggerPath)}" $SBT_PATCH_CLASS_NAME"""

    /**
     * Builds the sbt argument that makes sbt read commands from the generated command file.
     *
     * sbt accepts a shell-like `< file` argument for command-file input, but the TeamCity runner passes this text
     * as a process argument rather than executing it through a Unix shell.
     *
     * Official sbt docs:
     * - https://www.scala-sbt.org/1.x/docs/Howto-Running-Commands.html#Read+commands+from+a+file
     * - https://www.scala-sbt.org/1.x/docs/Command-Line-Reference.html#General+commands
     *
     * The Windows doubled-quote fix was introduced in commit
     * 6697e8576d1c6d87ca1e69e3dd57fdeca0e2b178 after the initial single-quote command-file path fix
     * in commit 46f49c78ab94016e085e62be2265ff683d792a59.
     *
     * Historical context:
     * - https://youtrack.jetbrains.com/issue/TW-50057
     * - https://youtrack.jetbrains.com/issue/TW-51551
     */
    @JvmStatic
    fun buildSbtInputRedirectionArgument(commandFilePath: String, isWindows: Boolean): String {
        val fileNameQuotes = if (isWindows) "\"\"" else "\""
        val pathNormalised = normalizePathForSbt(commandFilePath)
        val pathQuoted = fileNameQuotes + pathNormalised + fileNameQuotes
        return String.format("< %s", pathQuoted)
    }

    /**
     * Converts backslashes to forward slashes because JVM file APIs and sbt accept them on Windows.
     */
    @JvmStatic
    private fun normalizePathForSbt(path: String): String =
        path.replace('\\', '/')
}
