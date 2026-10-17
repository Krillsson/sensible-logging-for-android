import Foundation
import SensibleLogging

enum Categories {
    static let `default` = Constants.shared.DEFAULT_CATEGORY
    static let network: Category = "Network"
}

enum SampleError: Error {
    case timedOut
}

func run() {
    let configuration = Logger.SetupConfiguration()
    configuration.addStandardOutChannel(filter: AllowAllFilter.shared, formatter: SimpleFormatter.shared, default: true)
    Logger.Setup.shared.addChannels(channels: configuration.create())

    Logger.d("hello from swift", category: Categories.network, parameters: ["host": "nas.local"])
    Logger.w("could not connect", error: SampleError.timedOut, category: Categories.network)
    Logger.e("could not connect", error: NSError(domain: "SampleError", code: 7,
                                                userInfo: [NSLocalizedDescriptionKey: "sample failure"]),
             category: Categories.network)
}

run()
