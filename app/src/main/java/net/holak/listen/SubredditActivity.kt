package net.holak.listen

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class SubredditActivity : AppCompatActivity() {

    val confirmSubredditButton by lazy{ findViewById(R.id.btnConfirmSubreddit) as Button }
    val editSubredditName by lazy{ findViewById(R.id.editAddSubreddit) as EditText }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subreddit)

        confirmSubredditButton.setOnClickListener{ onConfirm() }
    }

    fun onConfirm() {
        val result = Intent();
        result.putExtra(SubredditName, editSubredditName.text.toString())
        setResult(0, result)
        finish()
    }

    companion object {
        val SubredditName = "SubredditName"
    }

}