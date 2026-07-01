package jetbrains.buildServer.sbt.test.agent.commandBuilder.mock

import jetbrains.buildServer.util.PasswordReplacer
import java.util.function.Function

class NoOpPasswordReplacer : PasswordReplacer {
    override fun replacePasswords(text: String): String = text

    override fun hasPasswords(): Boolean = false

    override fun getPasswords(): Set<String> = emptySet()

    override fun addPassword(password: String): Boolean = false

    override fun addPasswordsFilter(name: String, filter: Function<String, String>) {}

    override fun removePasswordsFilter(name: String): Boolean = false
}
