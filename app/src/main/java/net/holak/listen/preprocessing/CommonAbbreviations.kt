package net.holak.listen.preprocessing

class CommonAbbreviations : CommentTransformer {
    override fun transform(comment: String): String {
        // This could be done more efficiently (maybe replaceEach from Apache Commons?), but does it even matter?
        return comment.replace("OP", "oh pee")
    }
}