/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("java-library")
    kotlin("jvm")
}

dependencies {
//    api(platform(project(":depconstraints")))
//
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
//    // ThreeTenBP for the shared module only. Date and time API for Java.
//    implementation(Libs.KOTLIN_STDLIB)
    implementation(libs.moshi)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
