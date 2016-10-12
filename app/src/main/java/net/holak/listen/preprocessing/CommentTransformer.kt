package net.holak.listen.preprocessing

interface CommentTransformer {
    fun transform(comment: String): String
}