package net.holak.listen.preprocessing

class RemoveExtraSlashForSubreddits : CommentTransformer {

    override fun transform(comment: String) = comment.replace("/r/", "r/").replace("/u/", "u/")

}