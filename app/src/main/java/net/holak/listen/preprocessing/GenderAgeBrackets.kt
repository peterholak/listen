package net.holak.listen.preprocessing

/**
 * Converts age/sex markers like [M24], or {35F} to human-readable text.
 */
class GenderAgeBrackets : CommentTransformer {

    override fun transform(comment: String): String {
        return comment.replace(Regex("[\\[\\(\\{](\\d+)[\\s\\/]*([MmFf])[\\]\\)\\}]", RegexOption.IGNORE_CASE), {
            result ->
            val age = result.groupValues[1]
            val gender = when(result.groupValues[2]) {
                "m", "M" -> "male"
                "f", "F" -> "female"
                else -> return@replace result.value
            }
            ", $gender $age,"
        })
    }

}
