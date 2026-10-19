![header-image](header.png)
Sensible logging for Android aim to provide no-nonsense logging frontend API that is easily extended. 
The goal of this library is *not* to be rich in features, but to provide a stable baseline for you to build on in your own projects.

## Core concepts
The library consists of a few fundamental elements:

### Logger class
The `Logger` class is the main interaction point of this library. 
Inside it you will find the familiar log statement methods such as `Logger.d()`
 ```kotlin
 Logger.d("Initialising the flux capacitor", Categories.UI, Channels.CrashReporting)
```


### Channels
`Logger` directs the log statements to `Channel` implementations. One log statement can end up in multiple Channels.
Currently, the library includes `LogCatChannel` and `StandardOutChannel` (for unit tests).

Depending on your use-case, either implement a subtype of `ReleaseChannel` or `DebugChannel`.

 - `DebugChannel`s are meant to be used during development. Additional to the log-line, it includes the following information:
 ```kotlin
data class Meta(
    val className: String?,
    val simpleClassName: String?,
    val functionName: String?,
    val lineNumber: Int?,
    val threadName: String?,
    val fileName: String?
)
```
_It retrieves this data using `Throwable().stackTrace`. Be aware that it can have negative impact to performance, so default to only use it in debug builds._
_A field is `null` when the platform can't tell, and the built-in formatters leave it out._
 
- `ReleaseChannel`s only include the log-line and are meant to be used in release-builds.

#### Channel ids
A channel has a integer identifier. You can optionally specify a channel ID in your log statement to also print to that channel.

As an example, you can log non-fatal exceptions and messages to your crash reporting service via a `CrashReportingChannel`.
Using that you can easily log to your crash reporting service from wherever in your code.
```kotlin
    Logger.e("Something fatal occurred", exception, 4 /*CrashReportingChannel*/)
```

While the channel parameter is an integer. We recommend organising your channels in one file, for auto-completeness. Like so:
```kotlin
typealias Channel = Int

object Channels {
    const val LogCat: Channel = LogCatChannel.ID
    const val CrashReporting: Channel = CrashReportingChannel.ID
}

// then you can autocomplete your way to the channel
Logger.e("Something fatal occurred", exception, Channels.CrashReporting)
```
#### Default channels
During setup, you can mark a Channel as default. Log statements are always forwarded to default channels, meaning you don't have to specify them explicitly in your log statements.

An example where this is useful; you can mark the LogCat channel as default in a debug build, but not in a release build.

### Filters
```kotlin
interface Filter {
    fun matches(line: Line): Boolean
}
```
To control what a `Channel` should output you pass an instance of `Filter`. You can combine different filters by using the infix functions
`and` & `or`:
```kotlin
    SimpleLogLevelFilter(Level.ERROR) and SimpleCategoryFilter(Categories.UI)
```
would only print messages with level *error* and above, and of the category *UI*.
If you don't care about filters, you can pass the `AllowAllFilter` to your `Channel`.

#### Platform-specific filters
A filter that reads its settings from the platform, for example which categories are switched on in your debug menu,
can be declared once in `commonMain` and implemented per platform with `expect`/`actual`:

```kotlin
// commonMain
internal const val ENABLED_CATEGORIES_KEY = "enabled_log_categories"

expect class PersistedCategoryFilter : CategoryFilter {
    fun setEnabled(category: Category, enabled: Boolean)
}
```

```kotlin
// androidMain
actual class PersistedCategoryFilter(context: Context) : CategoryFilter() {

    private val preferences = context.getSharedPreferences("logging", Context.MODE_PRIVATE)

    override val categories: List<Category>
        get() = preferences.getStringSet(ENABLED_CATEGORIES_KEY, null)?.toList() ?: listOf(DEFAULT_CATEGORY)

    actual fun setEnabled(category: Category, enabled: Boolean) {
        val updated = if (enabled) categories + category else categories - category
        preferences.edit().putStringSet(ENABLED_CATEGORIES_KEY, updated.toSet()).apply()
    }
}
```

```kotlin
// appleMain
actual class PersistedCategoryFilter(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : CategoryFilter() {

    override val categories: List<Category>
        get() = defaults.stringArrayForKey(ENABLED_CATEGORIES_KEY)?.filterIsInstance<String>()
            ?: listOf(DEFAULT_CATEGORY)

    actual fun setEnabled(category: Category, enabled: Boolean) {
        val updated = if (enabled) categories + category else categories - category
        defaults.setObject(updated.distinct(), forKey = ENABLED_CATEGORIES_KEY)
    }
}
```

The constructors can differ per platform. Common code can't create the filter, but it can take one and call
`setEnabled`. `expect`/`actual` classes are still in Beta, so the compiler warns about them unless you pass
`-Xexpect-actual-classes`.

An `actual` has to be written in Kotlin, because Kotlin can't see Swift code. If you would rather write the Apple
side in Swift, skip `expect`/`actual`: let your shared code take a `Filter`, and implement the `Filter` protocol in
Swift:

```swift
final class UserDefaultsCategoryFilter: NSObject, Filter {
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func matches(line: Line) -> Bool {
        let enabled = defaults.stringArray(forKey: "enabled_log_categories") ?? [Categories.default]
        return enabled.contains(line.category)
    }
}

let configuration = Logger.SetupConfiguration()
configuration.addNSLogChannel(filter: UserDefaultsCategoryFilter(), formatter: LogCatFormatterExtended.shared,
                              default: true)
Logger.Setup.shared.addChannels(channels: configuration.create())
```

### Formatters
```kotlin
interface Formatter {
    fun format(line: Line, meta: Meta?): String
}
```
A `Channel` uses a `Formatter` to control the format of the output. If you are directing your output to a file, we recommend using `SimpleFormatter`

### Categories
All log statement methods inside `Log` allow the passing of a log category. This can be used to order your statements into high level areas of interest.
Want to know what is going on with your backend? Direct your network client log statements to the `Network` category, and enable only that category.

`Category` is a `typealias` for `String`, so any string works as a category, from Kotlin, Java and Swift alike.
Similar to channels, we recommend organising your categories in one file. Like so:

```kotlin
object Categories {
    const val Default: Category = sh.vcm.sensiblelogging.util.Constants.DEFAULT_CATEGORY
    const val Analytics: Category = "Analytics"
    const val Network: Category = "Network"
    const val Process: Category = "Process"
    const val Activity: Category = "Activity"
    const val Service: Category = "Service"
    const val Fragment: Category = "Fragment"
    const val RxJava: Category = "RxJava"
    const val FluxCapacitorFeature: Category = "FluxCapacitorFeature"
    const val Push: Category = "Push"
    const val UI: Category = "UI"
}
```

_`Category` used to be a value class. `Category("UI")` still compiles, but is deprecated and just returns the string.
`category.name` is deprecated too and now needs `import sh.vcm.sensiblelogging.name`, since it is an extension rather
than a member. Neither is exported to Swift._

## Usage

### Step 1

```kotlin
    if (BuildConfig.DEBUG) {
    // Sane defaults filter
    val categoriesFilter = Filter.categories(listOf(
        Categories.Default,
        Categories.Network,
        Categories.FluxCapacitorFeature,
        Categories.Process,
        Categories.Activity,
        Categories.Fragment
    ))
    val filterCombination = Filter.level(Level.WARN) or categoriesFilter
    val channels = Logger.Setup.Configuration()
        .addLogCatChannel(filter = filterCombination, default = true)
        .create()
    Logger.Setup.addChannels(channels)

    // optionally opt-in to logging out Process, Activity and Fragment lifecycle methods from the :lifecycle dependency
    registerLifecycleLoggers(
        processCategory = Categories.Process,
        activityCategory = Categories.Activity,
        fragmentCategory = Categories.Fragment
    )
}
```

### Step 2
```kotlin
 // Log from your code
 // Passing "Default" as category is optional, if no category is passed, default will be used 
 // Passing "LogCat" as channel is optional, if no channel is passed, default will be used 
 Logger.d("Initialising the flux capacitor", Categories.Default, Channels.LogCat)
```

### Step 3
Build your own Channels, Filters & Formatters to solve your project needs.

For example: you can log non-fatal exceptions to your crash reporting service via a `CrashReportingChannel`.
The `CrashReportingChannel` can be configured with a `Filter` that only pass a subset of your categories and all logged errors.
Using that you can easily log to this channel from wherever in your code.

Want persisted logs? Implement a `SQLiteChannel` using your favourite ORM library. You can then display those statements from
your debug UI. Or provide a shortcut from your app settings to dump the database to a text file that your users can email to you.

Want to control the log categories in runtime? Use the `SharedPreferencesCategoryFilter` with your `LogCatChannel` and enable updating of it from your debug UI.

### Kotlin Multiplatform
`sensible-logging-core` supports `jvm`, `iosArm64`, `iosSimulatorArm64` and `macosArm64`, so you can log from `commonMain`.
On Apple targets, use `addNSLogChannel` instead of `addLogCatChannel`:

```kotlin
// in iosMain / appleMain
val channels = Logger.Setup.Configuration()
    .addNSLogChannel(filter = Filter.level(Level.DEBUG))
    .create()
Logger.Setup.addChannels(channels)
```

`Meta` is read from the stack trace on every target. On Apple targets the stack trace only has symbol names and source
locations in debug builds, so release builds only get the thread name. Swift doesn't need the stack
trace: the Swift wrapper below passes the file, function and line of the call site. You can also pass `Meta` yourself
with `Logger.log(level, message, preFormattedMessage, meta, ...)`.

Swift errors are not Kotlin `Throwable`s. Use `NSError.asThrowable()` to pass one as `throwable`. It wraps the error in
`NSErrorException`, which keeps the original `NSError` for channels that want it, for example to report it to a crash
reporter with its domain and code. An `NSError` that Kotlin created from an exception is turned back into that exception.

#### Logging from Swift
Kotlin default arguments don't survive the Objective-C export, so `Logger` itself is awkward to call from Swift and
can't see the Swift call site. `LoggerBridge` exists to be wrapped by a small Swift file, which passes `#fileID`,
`#function` and `#line` so they end up in `Meta`. The wrapper calls
`LoggerBridge.shared.log(level:message:category:error:parameters:fileId:function:line:)`, which has the same name
whether or not you use [SKIE](https://skie.touchlab.co).

[`swift/Logger.swift`](swift/Logger.swift) is that wrapper. A Kotlin library can't ship Swift code, so it has to be
compiled in a Swift module that can see your framework. Either:

- add it to your iOS app target and change its `import` to the name of your framework, or
- with [SKIE's Swift code bundling](https://skie.touchlab.co/features/swift-code-bundling), put it in your framework
  module's `src/iosMain/swift` (or another Apple source set's `swift` folder) and remove the `import`, since the file
  becomes part of the framework itself. Its functions are `public` so your app can call them from outside the
  framework.

It adds static `v`, `d`, `i`, `w` and `e` functions to the exported Kotlin `Logger` in an extension, so Swift call sites
read the same way Kotlin ones do, and `Logger.Setup` and `Logger.SetupConfiguration` are right next to them. It also
declares `typealias Category = String`, since the Kotlin typealias doesn't make it into the header.

```swift
Logger.d("Loaded \(servers.count) servers", category: Categories.network)
Logger.e("Could not connect", error: error, category: Categories.network)
```

Organise your Swift categories in one place too. A case-less `enum` is the Swift way to make a namespace that can't be
instantiated:

```swift
enum Categories {
    static let `default` = Constants.shared.DEFAULT_CATEGORY
    static let network: Category = "Network"
    static let ui: Category = "UI"
}
```

Categories are plain strings on both sides, so a category logged from Swift is filtered the same way as one logged from
Kotlin.

Export the library from your framework so `LoggerBridge` and `Level` end up in its header:

```kotlin
iosTarget.binaries.framework {
    export("sh.vcm.sensiblelogging:sensible-logging-core:<version>")
}
```

[`swift/sample/main.swift`](swift/sample/main.swift) compiles the wrapper together with the `Categories` and
`UserDefaultsCategoryFilter` examples above and runs them against the macOS framework in CI, which is what keeps the
Swift side honest.

Download
--------

```groovy
// in your root build.gradle
repositories {
  mavenCentral()
}

// in your app build.gradle
dependencies {
  implementation 'sh.vcm.sensiblelogging:sensible-logging-core:2.1.1'      // Kotlin Multiplatform core, e.g. Logger, Channel, Filter, Formatter
  implementation 'sh.vcm.sensiblelogging:sensible-logging-android:2.1.1'   // Android integrations, e.g. LogCatChannel, SharedPreferencesCategoryFilter
  implementation 'sh.vcm.sensiblelogging:sensible-logging-lifecycle:2.1.1' // AndroidX Lifecycle-based logging helpers
}
```

## Requirements

 - `minSdk` is currently set to `16`
 - `sensible-logging-core` is Kotlin Multiplatform (JVM, iOS and macOS) with no Android SDK dependency, so it can
   be used from non-Android JVM modules and `commonMain` too
 - `sensible-logging-android` (Android-specific channels/filters, e.g. `LogCatChannel`) is only dependent on
   the Android SDK and kotlin stdlib
 - `sensible-logging-lifecycle` is dependent on `androidx.appcompat` and `androidx.lifecycle` libraries

## License

    Copyright 2022 Volvo Cars Corporation

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
