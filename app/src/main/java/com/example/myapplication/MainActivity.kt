package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
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
    private lateinit var placeholderText: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private var lastLeftViewId: Int = ConstraintSet.PARENT_ID
    private var lastRightViewId: Int = ConstraintSet.PARENT_ID
    private var noteCounter = 0

    private val addNoteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val title = it.data?.getStringExtra("note_title")
            val content = it.data?.getStringExtra("note_content")
            if (!title.isNullOrEmpty() || !content.isNullOrEmpty()) {
                if (noteCounter == 0) {
                    placeholderText.visibility = View.GONE
                }
                addNoteToContainer(title, content)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.mainlayout)

        drawerLayout = findViewById(R.id.main_drawer_layout)
        navView = findViewById(R.id.main_navigation_view)
        toolbar = findViewById(R.id.main_toolbar)
        fab = findViewById(R.id.main_fab_add)
        contentContainer = findViewById(R.id.content_container)
        placeholderText = findViewById(R.id.placeholder_text)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        fab.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }

        val menu = navView.menu
        val themeSwitchItem = menu.findItem(R.id.theme_switch_item)
        val themeSwitch = themeSwitchItem.actionView as SwitchCompat

        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPreferences.edit().putBoolean("is_dark_mode", true).apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences.edit().putBoolean("is_dark_mode", false).apply()
            }
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
        val deleteButton = noteView.findViewById<ImageButton>(R.id.note_item_delete)

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

        deleteButton.setOnClickListener {
            contentContainer.removeView(noteView)
            // The guideline and placeholder are children, so when only they are left, count is 2
            if (contentContainer.childCount <= 2) { 
                placeholderText.visibility = View.VISIBLE
                noteCounter = 0
                lastLeftViewId = ConstraintSet.PARENT_ID
                lastRightViewId = ConstraintSet.PARENT_ID
            }
        }

        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_small)
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(spacing, spacing, spacing, spacing)
        }
        noteView.layoutParams = params

        contentContainer.addView(noteView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentContainer)

        if (noteCounter % 2 == 1) { // Left column
            constraintSet.connect(noteView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(noteView.id, ConstraintSet.END, R.id.guideline_vertical_center, ConstraintSet.START)
            constraintSet.connect(noteView.id, ConstraintSet.TOP, lastLeftViewId, if (lastLeftViewId == ConstraintSet.PARENT_ID) ConstraintSet.TOP else ConstraintSet.BOTTOM, spacing)
            constraintSet.constrainWidth(noteView.id, ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.constrainHeight(noteView.id, ConstraintSet.WRAP_CONTENT)
            lastLeftViewId = noteView.id
        } else { // Right column
            constraintSet.connect(noteView.id, ConstraintSet.START, R.id.guideline_vertical_center, ConstraintSet.END)
            constraintSet.connect(noteView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.connect(noteView.id, ConstraintSet.TOP, lastRightViewId, if (lastRightViewId == ConstraintSet.PARENT_ID) ConstraintSet.TOP else ConstraintSet.BOTTOM, spacing)
            constraintSet.constrainWidth(noteView.id, ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.constrainHeight(noteView.id, ConstraintSet.WRAP_CONTENT)
            lastRightViewId = noteView.id
        }

        constraintSet.applyTo(contentContainer)
    }
}
