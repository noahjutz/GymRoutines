/*
 * Splitfit
 * Copyright (C) 2020  Noah Jutz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.noahjutz.gymroutines

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.resetAppSettings
import com.noahjutz.gymroutines.di.koinModule
import com.noahjutz.gymroutines.ui.MainActivity
import com.noahjutz.gymroutines.util.isFirstRun
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

const val PERSISTENT_WORKOUT_CHANNEL_ID = "persistent-workout-notification"

class GymRoutinesApplication : Application() {
    private val preferences: DataStore<Preferences> by inject()
    private val scope = CoroutineScope(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@GymRoutinesApplication)
            modules(koinModule)
        }

        scope.launch {
            launch {
                val isForeground = callbackFlow {
                    val callback = object : DefaultLifecycleObserver {
                        override fun onStart(owner: LifecycleOwner) {
                            super.onStart(owner)
                            trySend(true)
                        }

                        override fun onStop(owner: LifecycleOwner) {
                            super.onStop(owner)
                            trySend(false)
                        }
                    }

                    scope.launch(Dispatchers.Main) {
                        ProcessLifecycleOwner.get().lifecycle.addObserver(callback)
                    }

                    awaitClose {
                        scope.launch(Dispatchers.Main) {
                            ProcessLifecycleOwner.get().lifecycle.removeObserver(callback)
                        }
                    }
                }

                val currentWorkoutId = preferences.data.map { it[AppPrefs.CurrentWorkout.key] }

                val showNotification =
                    combine(isForeground, currentWorkoutId) { isForeground, currentWorkoutId ->
                        !isForeground && currentWorkoutId != null && currentWorkoutId >= 0
                    }

                combine(showNotification, currentWorkoutId) { p1, p2 -> Pair(p1, p2) }
                    .collect { (showNotification, currentWorkoutId) ->
                        val workoutInProgressIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://gymroutines.com/workoutInProgress/$currentWorkoutId".toUri(),
                            applicationContext,
                            MainActivity::class.java
                        )

                        val flag =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
                            else PendingIntent.FLAG_UPDATE_CURRENT

                        val pending: PendingIntent =
                            TaskStackBuilder.create(applicationContext).run {
                                addNextIntentWithParentStack(workoutInProgressIntent)
                                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or flag)!!
                            }
                        if (showNotification) {
                            val builder =
                                NotificationCompat.Builder(applicationContext, PERSISTENT_WORKOUT_CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_gymroutines)
                                    .setContentTitle("Workout in progress")
                                    .setContentText("Tap to return to your workout")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pending)
                                    .setOngoing(true)
                            with(NotificationManagerCompat.from(applicationContext)) {
                                notify(0, builder.build())
                            }
                        } else {
                            with(NotificationManagerCompat.from(applicationContext)) {
                                cancel(0)
                            }
                        }
                    }
            }
            if (isFirstRun()) {
                preferences.resetAppSettings()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.persistent_workout_channel_name)
            val descriptionText = getString(R.string.persistent_workout_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(PERSISTENT_WORKOUT_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
