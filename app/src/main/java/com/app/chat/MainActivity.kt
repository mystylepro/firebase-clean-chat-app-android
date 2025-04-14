package com.app.chat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.app.chat.databinding.ActivityMainBinding
import com.app.chat.domain.usecase.ChatUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var chatUseCase: ChatUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.customToolbar)

        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        setupToolbarInsets()
        setupAutoLogin()
        setupDestinationChangeListener()
    }

    private fun setupToolbarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.customToolbar) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = statusBarHeight)
            insets
        }
    }

    private fun setupAutoLogin() {
        if (firebaseAuth.currentUser != null) {
            navController.navigate(
                R.id.chatListFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    private fun setupDestinationChangeListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.signupFragment -> {
                    binding.customToolbar.visibility = View.GONE
                }
                R.id.chatListFragment -> {
                    binding.customToolbar.visibility = View.VISIBLE
                    setToolbarTitle("Chats")
                    setTypingSubtitle(false)
                }
                else -> {
                    binding.customToolbar.visibility = View.VISIBLE
                    setToolbarTitle(destination.label?.toString() ?: "")
                    setTypingSubtitle(false)
                }
            }
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val currentDestId = navController.currentDestination?.id
        val showMenuItems = currentDestId == R.id.chatListFragment || currentDestId == R.id.userListFragment

        menu?.findItem(R.id.actionLogout)?.isVisible = showMenuItems
        menu?.findItem(R.id.actionNewChat)?.isVisible = showMenuItems

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionLogout -> {
                handleLogout()
                true
            }

            R.id.actionNewChat -> {
                if (navController.currentDestination?.id != R.id.userListFragment) {
                    navController.navigate(
                        R.id.userListFragment,
                        null,
                        NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setRestoreState(true)
                            .build()
                    )
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            chatUseCase.logout()
            firebaseAuth.signOut()

            navController.navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun setTypingSubtitle(isTyping: Boolean) {
        val subtitleText = if (isTyping) "typing..." else ""
        binding.subtitleText.apply {
            visibility = if (subtitleText.isNotEmpty()) View.VISIBLE else View.GONE
            text = subtitleText
        }
    }

    fun setToolbarTitle(title: String?) {
        binding.titleText.text = title ?: ""
    }
}
