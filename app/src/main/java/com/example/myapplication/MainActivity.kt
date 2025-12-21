package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fab: FloatingActionButton
    private lateinit var contentContainer: ConstraintLayout

    private var lastLeftViewId: Int = R.id.left_column_item2_text
    private var lastRightViewId: Int = R.id.right_column_item2_text
    private var noteCounter = 0

    private val addNoteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val title = it.data?.getStringExtra("note_title")
            val content = it.data?.getStringExtra("note_content")
            if (!title.isNullOrEmpty() || !content.isNullOrEmpty()) {
                addNoteToContainer(title, content)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainlayout)

        drawerLayout = findViewById(R.id.main_drawer_layout)
        navView = findViewById(R.id.main_navigation_view)
        toolbar = findViewById(R.id.main_toolbar)
        fab = findViewById(R.id.main_fab_add)
        contentContainer = findViewById(R.id.content_container)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        fab.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    private fun addNoteToContainer(title: String?, content: String?) {
        noteCounter++

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val noteView = inflater.inflate(R.layout.note_item, null)
        noteView.id = View.generateViewId()

        val titleView = noteView.findViewById<TextView>(R.id.note_item_title)
        val contentView = noteView.findViewById<TextView>(R.id.note_item_content)

        if (!title.isNullOrEmpty()) {
            titleView.text = title
            titleView.visibility = View.VISIBLE
        } else {
            titleView.visibility = View.GONE
        }

        if (!content.isNullOrEmpty()) {
            contentView.text = content
            contentView.visibility = View.VISIBLE
        } else {
            contentView.visibility = View.GONE
        }

        contentContainer.addView(noteView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentContainer)

        if (noteCounter % 2 == 1) { // Left column
            constraintSet.connect(noteView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(noteView.id, ConstraintSet.END, R.id.guideline_vertical_center, ConstraintSet.START)
            constraintSet.connect(noteView.id, ConstraintSet.TOP, lastLeftViewId, if (lastLeftViewId == ConstraintSet.PARENT_ID) ConstraintSet.TOP else ConstraintSet.BOTTOM, resources.getDimensionPixelSize(R.dimen.spacing_medium))
            constraintSet.constrainWidth(noteView.id, ConstraintSet.MATCH_CONSTRAINT)
            lastLeftViewId = noteView.id
        } else { // Right column
            constraintSet.connect(noteView.id, ConstraintSet.START, R.id.guideline_vertical_center, ConstraintSet.END)
            constraintSet.connect(noteView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.connect(noteView.id, ConstraintSet.TOP, lastRightViewId, if (lastRightViewId == ConstraintSet.PARENT_ID) ConstraintSet.TOP else ConstraintSet.BOTTOM, resources.getDimensionPixelSize(R.dimen.spacing_medium))
            constraintSet.constrainWidth(noteView.id, ConstraintSet.MATCH_CONSTRAINT)
            lastRightViewId = noteView.id
        }

        constraintSet.applyTo(contentContainer)
    }
}
