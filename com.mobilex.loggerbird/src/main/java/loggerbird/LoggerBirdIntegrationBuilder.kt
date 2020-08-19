package loggerbird

/**
 * LoggerbirdIntegration class is a builder class for calling third party integrations in this pattern.
 */
class LoggerBirdIntegrationBuilder(
    val clubhouseApiToken: String?,
    val slackApiToken: String?,
    val gitlabApiToken: String?,
    val githubUserName: String?,
    val githubPassword: String?,
    val asanaApiToken: String?,
    val basecampApiToken: String?,
    val pivotalApiToken: String?,
    val trelloUserName: String?,
    val trelloPassword: String?,
    val trelloKey: String?,
    val trelloToken: String?,
    val jiraDomainName: String?,
    val jiraUserName: String?,
    val jiraApiToken: String?,
    val bitbucketUserName: String?,
    val bitbucketPassword: String?
) {
    data class Builder(
        private var clubhouseApiToken: String? = null,
        private var slackApiToken: String? = null,
        private var gitlabApiToken: String? = null,
        private var githubUserName: String? = null,
        private var githubPassword: String? = null,
        private var asanaApiToken: String? = null,
        private var basecampApiToken: String? = null,
        private var pivotalApiToken: String? = null,
        private var trelloUserName: String? = null,
        private var trelloPassword: String? = null,
        private var trelloKey: String? = null,
        private var trelloToken: String? = null,
        private var jiraDomainName: String? = null,
        private var jiraUserName: String? = null,
        private var jiraApiToken: String? = null,
        private var bitbucketUserName: String? = null,
        private var bitbucketPassword: String? = null
    ) {
        fun setClubhouseIntegration() = apply {
            val clubhouseToken = LoggerBird.decryptTokenKey("5ef8dbb1-aad1-4d9d-8ea0-1bfd13826aff")
            LoggerBird.clubhouseApiToken = clubhouseToken }

        fun setSlackIntegration() = apply {
            val slackApiToken =
                LoggerBird.decryptTokenKey("523949707746.1306258412789.1357cff390bf1fa5e560c150fffc15c1f517388d552e84a300aa86b330e4e228")
            LoggerBird.slackApiToken = slackApiToken }

        fun setGitlabIntegration() = apply {
            val gitlabApiToken = LoggerBird.decryptTokenKey("6W7xso6qN8mqrDfwZZrd")
            LoggerBird.gitlabApiToken = gitlabApiToken }

        fun setGithubIntegration() = apply {
            val githubUserName = LoggerBird.decryptTokenKey("berkavc")
            val githubPassword = LoggerBird.decryptTokenKey("umbasta1")
            LoggerBird.githubUserName = githubUserName;LoggerBird.githubPassword = githubPassword
        }

        fun setAsanaIntegration() = apply {
            val asanaApiToken =
                LoggerBird.decryptTokenKey("1/1182746606250186:defab27f657ce740cf05ba7f5180cc6e")
            LoggerBird.asanaApiToken = asanaApiToken }

        fun setBasecampIntegration() = apply {
            val basecampApiToken =
                LoggerBird.decryptTokenKey("BAhbB0kiAbl7ImNsaWVudF9pZCI6IjAyMDM2OGMwNDM4YzQ1ZjIyNjQzYmY5MTM2MDE5MjEzZTQzNGFiY2UiLCJleHBpcmVzX2F0IjoiMjAyMC0wNy0yN1QxMTo0NTo1MloiLCJ1c2VyX2lkcyI6WzQyMzI4NzkyLDQyNDI2NDU3XSwidmVyc2lvbiI6MSwiYXBpX2RlYWRib2x0IjoiODFkYTljZjlkYzljNDA0OThkZjhkOTRhODBmNzg4ZjYifQY6BkVUSXU6CVRpbWUNaxsewMIiQbcJOg1uYW5vX251bWkC3QI6DW5hbm9fZGVuaQY6DXN1Ym1pY3JvIgdzMDoJem9uZUkiCFVUQwY7AEY=--ed8edece558992d6423ab6e85838ce04800eabe5")
            LoggerBird.basecampApiToken = basecampApiToken }

        fun setPivotalIntegraton() = apply {
                val pivotalApiToken = "070e64617f8042311841cf6616d3af8b"
                LoggerBird.pivotalApiToken = pivotalApiToken }

        fun setTrelloIntegration() = apply {
            val trelloUserName = LoggerBird.decryptTokenKey("appcaesars@gmail.com")
            val trelloPassword = LoggerBird.decryptTokenKey("umbasta1")
            val trelloKey = LoggerBird.decryptTokenKey("23c4a97f599e7db20e36b32d38853210cf2120bbb955a7cc4fe9550a24f8b32c")
            val trelloToken = LoggerBird.decryptTokenKey("4b4185b55f32b9e76fef2effcc9147c8")
            LoggerBird.trelloUserName = trelloUserName;LoggerBird.trelloPassword =
            trelloPassword; LoggerBird.trelloKey = trelloKey; LoggerBird.trelloToken =
            trelloToken
        }

        fun setJiraIntegration() = apply {
            val jiraDomainName = LoggerBird.decryptTokenKey("https://appcaesars.atlassian.net")
            val jiraUserName = LoggerBird.decryptTokenKey("appcaesars@gmail.com")
            val jiraApiToken = LoggerBird.decryptTokenKey("uPPXsUw0FabxeOa5CkDm0BAE")
            LoggerBird.jiraDomainName = jiraDomainName;LoggerBird.jiraUserName =
            jiraUserName; LoggerBird.jiraApiToken = jiraApiToken
        }

        fun setBitbucketIntegration() = apply {
            val bitbucketUserName = LoggerBird.decryptTokenKey("appcaesars")
            val bitbucketPassword = LoggerBird.decryptTokenKey("umbasta1")
            LoggerBird.bitbucketUserName = bitbucketUserName
            LoggerBird.bitbucketPassword = bitbucketPassword
        }

        fun build() = LoggerBirdIntegrationBuilder(
            clubhouseApiToken,
            slackApiToken,
            gitlabApiToken,
            githubUserName,
            githubPassword,
            asanaApiToken,
            basecampApiToken,
            pivotalApiToken,
            trelloUserName,
            trelloPassword,
            trelloKey,
            trelloToken,
            jiraDomainName,
            jiraUserName,
            jiraApiToken,
            bitbucketUserName,
            bitbucketPassword
        )
    }
}