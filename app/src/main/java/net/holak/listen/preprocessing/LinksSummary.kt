package net.holak.listen.preprocessing

/**
 * Transforms http:// and https:// links to a form that's pleasant to listen to.
 * Tries to extract some actual words from the link if possible, otherwise just reads
 * the domain, or a short word for some popular domains such as imgur or youtube.
 */
class LinksSummary : CommentTransformer {
    override fun transform(comment: String): String {
        return comment.replace(Regex("https?://([^/]+).*?(\\s|$|\\))"), {
            when (it.groupValues[1]) {
                "imgur.com", "m.imgur.com", "i.imgur.com" -> "(imgur link)"
                "youtube.com", "www.youtube.com", "m.youtube.com" -> "(youtube link)"
                else -> "(link to ${it.groupValues[1].split(".").takeLast(2).joinToString(".")})"
            }
        })
    }
}
