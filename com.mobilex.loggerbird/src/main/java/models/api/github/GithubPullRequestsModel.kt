package models.api.github

/**
 * This class is a model for Github Api request
 */
internal data class GithubPullRequestsModel(
    var title:String? = null,
    var html_url:String? = null
)

