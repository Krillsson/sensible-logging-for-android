import Foundation
import SensibleLogging

enum SampleError: Error {
    case timedOut
}

func run() {
    let configuration = LoggerSetupConfiguration()
    configuration.addStandardOutChannel(filter: AllowAllFilter.shared, formatter: SimpleFormatter.shared, default: true)
    LoggerSetup.shared.addChannels(channels: configuration.create())

    Logger.d("hello from swift", category: "Network", parameters: ["host": "nas.local"])
    Logger.w("could not connect", error: SampleError.timedOut, category: "Network")
    Logger.e("could not connect", error: NSError(domain: "SampleError", code: 7,
                                                userInfo: [NSLocalizedDescriptionKey: "sample failure"]),
             category: "Network")
}

run()
