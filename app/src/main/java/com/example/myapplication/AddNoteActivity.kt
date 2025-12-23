package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar

class AddNoteActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private var noteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This line enables edge-to-edge display.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_add_note)

        val rootView = findViewById<View>(R.id.add_note_root)
        val appBarLayout = findViewById<AppBarLayout>(R.id.add_note_appbar)

        // Set an OnApplyWindowInsetsListener to handle system bar insets correctly.
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply the top inset as padding to the AppBarLayout.
            // This pushes the toolbar down below the status bar.
            appBarLayout.updatePadding(top = insets.top)

            // Apply the other insets as padding to the root view.
            // This prevents the content from being obscured by the navigation bar.
            view.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)

            // Return CONSUMED to signal that we've handled the insets.
            WindowInsetsCompat.CONSUMED
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.add_note_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleInput = findViewById(R.id.add_note_title_input)
        contentInput = findViewById(R.id.add_note_content_input)

        noteId = intent.getIntExtra("note_id", -1)
        if (noteId != -1) {
            titleInput.setText(intent.getStringExtra("note_title"))
            contentInput.setText(intent.getStringExtra("note_content"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        val title = titleInput.text.toString()
        val content = contentInput.text.toString()

        val resultIntent = Intent().apply {
            putExtra("note_id", noteId)
            putExtra("note_title", title)
            putExtra("note_content", content)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
