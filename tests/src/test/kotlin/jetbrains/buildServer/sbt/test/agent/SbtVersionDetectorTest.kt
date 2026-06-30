package jetbrains.buildServer.sbt.test.agent

import jetbrains.buildServer.sbt.SbtRunnerBuildService.Companion.SBTVersion
import jetbrains.buildServer.sbt.SbtVersionDetector
import jetbrains.buildServer.sbt.test.agent.SbtVersionDetectorTest.FileUtils.div
import jetbrains.buildServer.util.FileUtil
import org.testng.Assert
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files

/**
 * Covers low-level sbt version readers and the discovery priority used by the agent build service.
 */
class SbtVersionDetectorTest {

    @Test
    fun readVersionFromSbtBootProperties() {
        var stream: InputStream? = null
        try {
            stream = FileInputStream(File("testdata/sbtVersionDiscovery/fromApp/sbt.boot.properties"))
            val map = SbtVersionDetector.readPropertiesFromStream(stream, "app")
            Assert.assertEquals(map.size, 5)
            Assert.assertEquals(map["version"], "\${sbt.version-read(sbt.version)[1.3.8]}")
            val version = SbtVersionDetector.getSbtVersionFromProperties(map)
            Assert.assertEquals(version, "1.3.8")
        } catch (e: Exception) {
            Assert.fail("Failed to read SBT boot properties", e)
        } finally {
            FileUtil.close(stream)
        }
    }

    @Test
    fun readVersionFromLauncher() {
        val file = File("testdata/sbtVersionDiscovery/fromLauncher/sbt-launch.jar")
        val map = SbtVersionDetector.readSectionFromBootPropertiesOf(file, "app")
        val version = SbtVersionDetector.getSbtVersionFromProperties(map)
        Assert.assertEquals(version, "1.10.10")
    }

    @Test
    fun foundInProjectProperties() {
        val file = File("testdata/sbtVersionDiscovery/fromProjectProperties")
        val version = SbtVersionDetector.readFromProjectProperties(file, null)
        Assert.assertEquals(version, "0.13.16")
    }

    @Test
    fun notFoundInProjectProperties() {
        val file = File("testdata/sbtVersionDiscovery/fromApp")
        val version = SbtVersionDetector.readFromProjectProperties(file, null)
        Assert.assertNull(version)
    }

    @Test
    fun foundInJavaArg() {
        val version = SbtVersionDetector.readFromJavaArguments(listOf("-Dsbt.version = 0.13.16 "))
        Assert.assertEquals(version, "0.13.16")
    }

    @Test
    fun notFoundInJavaArg() {
        val version = SbtVersionDetector.readFromJavaArguments(listOf("-XX:ReservedCodeCacheSize=128m"))
        Assert.assertNull(version)
    }

    @Test
    fun discoverSbtVersion_prefers_project_properties_over_jvm_args_and_launcher() {
        val workingDir = createTempWorkingDir_WithProjectVersionInBuildProperties("0.13.16")
        try {
            val version = SbtVersionDetector.discoverSbtVersion(
                workingDir,
                launcherWithSbt1(),
                // This intentionally conflicts with project/build.properties to prove project settings win.
                listOf("-Dsbt.version=1.10.10"),
                silentLogger(),
            )

            Assert.assertEquals(version, SBTVersion.SBT_0_13_x)
        } finally {
            workingDir.deleteRecursively()
        }
    }

    @Test
    fun discoverSbtVersion_prefers_jvm_args_over_launcher_when_project_properties_are_missing() {
        val workingDir = createTempWorkingDir()
        try {
            val version = SbtVersionDetector.discoverSbtVersion(
                workingDir,
                launcherWithSbt1(),
                // The bundled launcher reports sbt 1.x, so this 0.13 JVM property proves the second priority branch wins.
                listOf("-Dsbt.version=0.13.16"),
                silentLogger(),
            )

            Assert.assertEquals(version, SBTVersion.SBT_0_13_x)
        } finally {
            workingDir.deleteRecursively()
        }
    }

    @Test
    fun discoverSbtVersion_uses_launcher_when_project_properties_and_jvm_args_are_missing() {
        val workingDir = createTempWorkingDir()
        try {
            val version = SbtVersionDetector.discoverSbtVersion(
                workingDir,
                // This fixture embeds sbt/sbt.boot.properties with version 1.10.10.
                launcherWithSbt1(),
                emptyList(),
                silentLogger(),
            )

            Assert.assertEquals(version, SBTVersion.SBT_1_x)
        } finally {
            workingDir.deleteRecursively()
        }
    }

    //Q: make this default to SBT 1.x instead of 0.13? It's 2026 already.
    @Test
    fun discoverSbtVersion_defaults_to_0_13_when_no_source_reports_a_version() {
        val workingDir = createTempWorkingDir()
        try {
            val version = SbtVersionDetector.discoverSbtVersion(
                workingDir,
                // A missing launcher makes the launcher reader return an empty map, exercising the final default branch.
                File(workingDir, "missing-sbt-launch.jar"),
                emptyList(),
                silentLogger(),
            )

            Assert.assertEquals(version, SBTVersion.SBT_0_13_x)
        } finally {
            workingDir.deleteRecursively()
        }
    }

    private fun createTempWorkingDir_WithProjectVersionInBuildProperties(sbtVersion: String): File {
        val workingDir = createTempWorkingDir()
        FileUtils.writeTo(
            workingDir / "project/build.properties",
            "sbt.version=$sbtVersion\n"
        )
        return workingDir
    }

    private object FileUtils {
         fun writeTo(file: File, fileContent: String) {
            file.parentFile.mkdirs()
            file.writeText(fileContent, Charsets.UTF_8)
        }

         operator fun File.div(string: String): File = File(this, string)
    }

    private fun createTempWorkingDir(): File =
        Files.createTempDirectory("sbt-version-detector-test").toFile()

    private fun launcherWithSbt1(): File {
        val result = File("testdata/sbtVersionDiscovery/fromLauncher/sbt-launch.jar")
        assertTrue(result.exists())
        return result
    }

    private fun silentLogger(): SilentBuildProgressLogger = SilentBuildProgressLogger()
}


