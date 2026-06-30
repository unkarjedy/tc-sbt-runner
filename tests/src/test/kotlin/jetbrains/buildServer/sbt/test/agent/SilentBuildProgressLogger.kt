package jetbrains.buildServer.sbt.test.agent

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.FlowLogger
import jetbrains.buildServer.messages.BuildMessage1
import jetbrains.buildServer.messages.Status
import java.util.Date

class SilentBuildProgressLogger : BuildProgressLogger {
    override fun activityStarted(activityName: String?, activityType: String?) {}

    override fun activityStarted(
        activityName: String?,
        activityDescription: String?,
        activityType: String?
    ) {}

    override fun activityFinished(activityName: String?, activityType: String?) {}

    override fun targetStarted(targetName: String?) {}

    override fun targetFinished(targetName: String?) {}

    override fun buildFailureDescription(message: String?) {}

    override fun internalError(type: String?, message: String?, throwable: Throwable?) {}

    override fun progressStarted(message: String?) {}

    override fun progressFinished() {}

    override fun logMessage(message: BuildMessage1?) {}

    override fun flush() {}

    override fun ignoreServiceMessages(runnable: Runnable?) {}

    override fun getFlowLogger(flowId: String?): FlowLogger? = null

    override fun getThreadLogger(): FlowLogger? = null

    override fun getFlowId(): String? = null

    override fun logBuildProblem(buildProblem: BuildProblemData?) {}

    override fun message(message: String?) {}

    override fun message(message: String?, status: Status?) {}

    override fun debug(message: String?) {}

    override fun error(message: String?) {}

    override fun warning(message: String?) {}

    override fun exception(th: Throwable?) {}

    override fun progressMessage(message: String?) {}

    override fun logTestStarted(name: String?) {}

    override fun logTestStarted(name: String?, timestamp: Date?) {}

    override fun logTestFinished(name: String?) {}

    override fun logTestFinished(name: String?, timestamp: Date?) {}

    override fun logTestIgnored(name: String?, reason: String?) {}

    override fun logSuiteStarted(name: String?) {}

    override fun logSuiteStarted(name: String?, timestamp: Date?) {}

    override fun logSuiteFinished(name: String?) {}

    override fun logSuiteFinished(name: String?, timestamp: Date?) {}

    override fun logTestStdOut(testName: String?, out: String?) {}

    override fun logTestStdErr(testName: String?, out: String?) {}

    override fun logTestFailed(testName: String?, e: Throwable?) {}

    override fun logComparisonFailure(
        testName: String?,
        e: Throwable?,
        expected: String?,
        actual: String?
    ) {}

    override fun logTestFailed(testName: String?, message: String?, stackTrace: String?) {}

}