package jetbrains.buildServer.sbt.test.agent

object TestSystemProperties {
    fun <T> withOsName(osName: String, action: () -> T): T =
        withSystemProperty("os.name", osName, action)

    private fun <T> withSystemProperty(name: String, value: String, action: () -> T): T {
        val properties = System.getProperties()
        val hadOriginalValue = properties.containsKey(name)
        val originalValue = System.getProperty(name)

        System.setProperty(name, value)
        return try {
            action()
        } finally {
            if (hadOriginalValue) {
                System.setProperty(name, originalValue)
            } else {
                System.clearProperty(name)
            }
        }
    }
}
