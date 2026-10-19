import Foundation
import SensibleLogging

enum Categories {
    static let `default` = Constants.shared.DEFAULT_CATEGORY
    static let network: Category = "Network"
    static let ui: Category = "UI"
}

/// Lets through the categories stored in user defaults, like the Kotlin filters do on the JVM.
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

enum SampleError: Error {
    case timedOut
}

func run() {
    // Registered defaults are only kept in memory, so the sample doesn't write any preferences
    UserDefaults.standard.register(defaults: ["enabled_log_categories": [Categories.network]])
    let configuration = Logger.SetupConfiguration()
    configuration.addStandardOutChannel(filter: UserDefaultsCategoryFilter(), formatter: SimpleFormatter.shared,
                                        default: true)
    Logger.Setup.shared.addChannels(channels: configuration.create())

    Logger.d("hello from swift", category: Categories.network, parameters: ["host": "nas.local"])
    Logger.w("could not connect", error: SampleError.timedOut, category: Categories.network)
    Logger.e("could not connect", error: NSError(domain: "SampleError", code: 7,
                                                userInfo: [NSLocalizedDescriptionKey: "sample failure"]),
             category: Categories.network)
    Logger.i("filtered out by category", category: Categories.ui)
}

run()
