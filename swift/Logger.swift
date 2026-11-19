import Foundation
import SensibleLogging

extension Logger {

    public static func v(_ message: String, category: Category = Constants.shared.DEFAULT_CATEGORY,
                         parameters: [String: String] = [:], channels: Int32...,
                         file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.verbose, message, category, nil, parameters, channels, file, function, line)
    }

    public static func d(_ message: String, category: Category = Constants.shared.DEFAULT_CATEGORY,
                         parameters: [String: String] = [:], channels: Int32...,
                         file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.debug, message, category, nil, parameters, channels, file, function, line)
    }

    public static func i(_ message: String, category: Category = Constants.shared.DEFAULT_CATEGORY,
                         parameters: [String: String] = [:], channels: Int32...,
                         file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.info, message, category, nil, parameters, channels, file, function, line)
    }

    public static func w(_ message: String, error: Error? = nil, category: Category = Constants.shared.DEFAULT_CATEGORY,
                         parameters: [String: String] = [:], channels: Int32...,
                         file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.warn, message, category, error, parameters, channels, file, function, line)
    }

    public static func e(_ message: String, error: Error? = nil, category: Category = Constants.shared.DEFAULT_CATEGORY,
                         parameters: [String: String] = [:], channels: Int32...,
                         file: String = #fileID, function: String = #function, line: Int = #line) {
        write(.error, message, category, error, parameters, channels, file, function, line)
    }

    private static func write(_ level: Level, _ message: String, _ category: Category, _ error: Error?,
                              _ parameters: [String: String], _ channels: [Int32],
                              _ file: String, _ function: String, _ line: Int) {
        LoggerBridge.shared.log(level: level, message: message, category: category,
                                 error: error as NSError?, parameters: parameters,
                                 channels: channels.map { KotlinInt(int: $0) },
                                 fileId: file, function: function, line: Int32(line))
    }
}

/// Kotlin's `Category` is a typealias for `String`, which the Objective-C export doesn't carry over.
public typealias Category = String
