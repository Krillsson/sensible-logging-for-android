import Foundation
import SensibleLogging

enum Logger {

    static func v(_ message: String, category: String = "Default", parameters: [String: String] = [:],
                  file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.verbose, message, category, nil, parameters, file, function, line)
    }

    static func d(_ message: String, category: String = "Default", parameters: [String: String] = [:],
                  file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.debug, message, category, nil, parameters, file, function, line)
    }

    static func i(_ message: String, category: String = "Default", parameters: [String: String] = [:],
                  file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.info, message, category, nil, parameters, file, function, line)
    }

    static func w(_ message: String, error: Error? = nil, category: String = "Default",
                  parameters: [String: String] = [:],
                  file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.warn, message, category, error, parameters, file, function, line)
    }

    static func e(_ message: String, error: Error? = nil, category: String = "Default",
                  parameters: [String: String] = [:],
                  file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.error, message, category, error, parameters, file, function, line)
    }

    private static func write(_ level: Level, _ message: String, _ category: String, _ error: Error?,
                              _ parameters: [String: String],
                              _ file: String, _ function: String, _ line: Int) {
        LoggerBridge.shared.__logLevel(level, message: message, category: category,
                                       error: error as NSError?, parameters: parameters,
                                       fileId: file, function: function, line: Int32(line))
    }
}
