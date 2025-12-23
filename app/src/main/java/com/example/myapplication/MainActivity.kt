package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fab: FloatingActionButton
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var placeholderText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var noteClassifier: NoteClassifier
    private lateinit var noteDao: NoteDao
    private lateinit var noteAdapter: NoteAdapter

    private val noteResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val noteId = it.data?.getIntExtra("note_id", -1) ?: -1
            val title = it.data?.getStringExtra("note_title")
            val content = it.data?.getStringExtra("note_content")

            val noteText = "$title\n$content"
            val category = noteClassifier.classify(noteText)

            lifecycleScope.launch {
                if (noteId != -1) {
                    val existingNote = noteDao.getNoteById(noteId)
                    if (existingNote != null) {
                        // Update existing note
                        existingNote.title = title
                        existingNote.content = content
                        existingNote.category = category
                        noteDao.updateNote(existingNote)
                        loadNotes()
                    }
                } else if (!title.isNullOrEmpty() || !content.isNullOrEmpty()) {
                    val newNote = Note(title = title, content = content, category = category)
                    noteDao.insertNote(newNote)
                    loadNotes()
                }
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

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.mainlayout)

        noteClassifier = NoteClassifier(this)
        noteDao = AppDatabase.getDatabase(this).noteDao()

        drawerLayout = findViewById(R.id.main_drawer_layout)
        navView = findViewById(R.id.main_navigation_view)
        toolbar = findViewById(R.id.main_toolbar)
        fab = findViewById(R.id.main_fab_add)
        notesRecyclerView = findViewById(R.id.notes_recycler_view)
        placeholderText = findViewById(R.id.placeholder_text)

        setupRecyclerView()

        val coordinatorLayout = findViewById<View>(R.id.main_coordinator_layout)
        val appBarLayout = findViewById<View>(R.id.main_appbar)
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply top padding to the AppBarLayout to push the toolbar down
            appBarLayout.updatePadding(top = systemBars.top)

            // Apply other insets as padding to the main content area (CoordinatorLayout)
            view.updatePadding(left = systemBars.left, right = systemBars.right, bottom = systemBars.bottom)

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(navView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        fab.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            noteResultLauncher.launch(intent)
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

        loadNotes()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(mutableListOf(),
            onNoteClicked = {
                val intent = Intent(this, AddNoteActivity::class.java).apply {
                    putExtra("note_id", it.id)
                    putExtra("note_title", it.title)
                    putExtra("note_content", it.content)
                }
                noteResultLauncher.launch(intent)
            },
            onNoteDeleteClicked = {
                lifecycleScope.launch {
                    noteDao.deleteNote(it)
                    loadNotes()
                }
            }
        )
        notesRecyclerView.adapter = noteAdapter
        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_medium)
        notesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        notesRecyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            val allNotes = noteDao.getAllNotes()
            if (allNotes.isNotEmpty()) {
                placeholderText.visibility = View.GONE
                notesRecyclerView.visibility = View.VISIBLE
                noteAdapter.updateNotes(allNotes)
            } else {
                placeholderText.visibility = View.VISIBLE
                notesRecyclerView.visibility = View.GONE
            }
        }
    }
}
