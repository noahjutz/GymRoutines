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

object App {
    const val compileSdk = 33
    const val minSdk = 21
    const val targetSdk = 33
}

object Versions {
    const val ktlintPlugin = "10.3.0"
}

object Libs {
    object Core {
        const val core = "androidx.core:core-ktx:1.9.0"
        const val splashScreen = "androidx.core:core-splashscreen:1.0.0"
    }

    object Coroutines {
        private const val version = "1.6.4"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object Material {
        private const val version = "1.6.1"
        const val material = "com.google.android.material:material:$version"
    }

    object Serialization {
        private const val version = "1.5.0-RC"
        const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
    }

    object Room {
        private const val version = "2.5.0"
        const val room = "androidx.room:room-ktx:$version"
        const val compiler = "androidx.room:room-compiler:$version"
        const val runtime = "androidx.room:room-runtime:$version"
        const val testing = "androidx.room:room-testing:$version"
    }

    object Lifecycle {
        private const val version = "2.5.1"
        const val lifecycle = "androidx.lifecycle:lifecycle-compiler:$version"
        const val livedata = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        const val process = "androidx.lifecycle:lifecycle-process:$version"
    }

    object Navigation {
        private const val version = "2.5.3"
        const val compose = "androidx.navigation:navigation-compose:$version"
    }

    object Activity {
        private const val version = "1.6.1"
        const val compose = "androidx.activity:activity-compose:$version"
    }

    object Compose {
        const val ui = "androidx.compose.ui:ui:1.3.3"
        const val tooling = "androidx.compose.ui:ui-tooling:1.3.3"
        const val foundation = "androidx.compose.foundation:foundation:1.3.1"
        const val material = "androidx.compose.material:material:1.3.1"
        const val iconsCore = "androidx.compose.material:material-icons-core:1.3.1"
        const val iconsExtended = "androidx.compose.material:material-icons-extended:1.3.1"
        const val runtimeLivedata = "androidx.compose.runtime:runtime-livedata:1.3.3"
        const val test = "androidx.compose.ui:ui-test:1.3.3"
        const val testJunit4 = "androidx.compose.ui:ui-test-junit4:1.3.3"
    }

    object Accompanist {
        private const val version = "0.28.0"
        const val navigationMaterial =
            "com.google.accompanist:accompanist-navigation-material:$version"
        const val navigationAnimation =
            "com.google.accompanist:accompanist-navigation-animation:$version"
        const val placeholder = "com.google.accompanist:accompanist-placeholder-material:$version"
    }

    object DataStore {
        private const val version = "1.0.0"
        const val preferences = "androidx.datastore:datastore-preferences:$version"
    }

    object Koin {
        private const val version = "3.3.3"
        const val android = "io.insert-koin:koin-android:$version"
        const val compose = "io.insert-koin:koin-androidx-compose:3.4.1"
        const val test = "io.insert-koin:koin-test:$version"
    }

    object ProcessPhoenix {
        private const val version = "2.1.2"
        const val processPhoenix = "com.jakewharton:process-phoenix:$version"
    }
}

object TestLibs {
    object Junit4 {
        private const val version = "4.13.1"
        const val junit4 = "junit:junit:$version"
    }

    object AssertJ {
        private const val version = "3.18.1"
        const val assertJ = "org.assertj:assertj-core:$version"
    }

    object Mockk {
        private const val version = "1.10.2"
        const val unit = "io.mockk:mockk:$version"
        const val instrumented = "io.mockk:mockk-android:$version"
    }

    object Test {
        private const val version = "1.4.0"
        const val core = "androidx.test:core:$version"
        const val coreKtx = "androidx.test:core-ktx:$version"
    }
}

object GradlePlugins {
    object Android {
        private const val version = "7.4.1"
        const val classpath = "com.android.tools.build:gradle:$version"
    }

    object Kotlin {
        const val version = "1.8.10"
        const val classpath = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
    }
}
