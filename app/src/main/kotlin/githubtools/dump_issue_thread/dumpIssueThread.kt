package githubtools.dump_issue_thread

import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.net.URL

// https://github.com/Expensify/App/issues/29886
data class IssueUrl(
    val repositoryName: String, // Expensify/App
    val issueId: Int, // 29886
)

/**
 * Parses an issue URL. Throws an exception if the URL is not valid.
 */
fun parseIssueUrl(
    urlString: String,
): IssueUrl {
    val url = URL(urlString)

    if (url.host != "github.com") {
        throw Exception("Invalid issue URL: $urlString")
    }

    val path = url.path

    val pathParts = path.split("/")

    if (pathParts.size != 5) {
        throw Exception("Invalid issue URL: $urlString")
    }

    val repositoryName = pathParts[1] + "/" + pathParts[2]

    val issueId = pathParts[4].toIntOrNull() ?: throw Exception("Invalid issue URL: $urlString")

    return IssueUrl(
        repositoryName = repositoryName,
        issueId = issueId,
    )
}

/**
 * Dump the issue thread to a human-readable string.
 */
fun dumpIssueThread(
    github: GitHub,
    issueUrlString: String,
): String {
    val issueUrl = parseIssueUrl(urlString = issueUrlString)

    val repository = github.getRepository(issueUrl.repositoryName)

    val issue = repository.getIssue(issueUrl.issueId)

    return issue.comments.joinToString(separator = "\n\n") { comment ->
        comment.user.login + ":\n\n" + comment.body
    }
}

private const val issueUrlString = "https://github.com/Expensify/App/issues/29886"

fun main() {
    val githubPersonalToken =
        System.getenv("GITHUB_PERSONAL_TOKEN") ?: throw Exception("GITHUB_PERSONAL_TOKEN environment variable not set")

    val github: GitHub = GitHubBuilder().withOAuthToken(githubPersonalToken).build();

    val dumpedThread = dumpIssueThread(
        github = github,
        issueUrlString = issueUrlString,
    )

    println(dumpedThread)
}
