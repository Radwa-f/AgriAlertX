//
//  AgriAlertApp.swift
//  AgriAlert
//

import SwiftUI
import BackgroundTasks

@main
struct AgriAlertApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    @State private var isLoggedIn: Bool = false

        init() {
            // Checking if token exists
            if let token = KeychainHelper.shared.retrieve(key: "JWT_TOKEN") {
                print("Token found: \(token)")
                isLoggedIn = true
            } else {
                print("No token found.")
            }
        }

        var body: some Scene {
            WindowGroup {
                if isLoggedIn {
                    HomeView()
                } else {
                    ContentView()
                }
            }
        }
    
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Configuring background task scheduling
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "ma.ensaj.agri_alert.cropAnalysis", using: nil) { task in
            self.handleCropAnalysisTask(task: task as! BGProcessingTask)
        }
        
        // Requesting notification permissions
        requestNotificationPermissions()
        
        // Scheduling initial task
        scheduleBackgroundTask()
        
        return true
    }
    
    private func requestNotificationPermissions() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                print("Notification permissions granted")
            } else if let error = error {
                print("Error requesting notifications: \(error.localizedDescription)")
            }
        }
    }
    
    private func handleCropAnalysisTask(task: BGProcessingTask) {
        // Set up task expiration handler
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
        
        // Ensure we have enough time for the task
        let taskAssertionID = UIApplication.shared.beginBackgroundTask {
            // Handle task expiration
            task.setTaskCompleted(success: false)
        }
        
        let cropAnalyzer = CropAnalysisTask()
        cropAnalyzer.performAnalysis { success in
            // Schedule next task before completing current one
            self.scheduleBackgroundTask()
            
            // Complete the task
            task.setTaskCompleted(success: success)
            
            // End background task assertion
            UIApplication.shared.endBackgroundTask(taskAssertionID)
        }
    }
    
    private func scheduleBackgroundTask() {
        let request = BGProcessingTaskRequest(identifier: "ma.ensaj.agri_alert.cropAnalysis")
        request.requiresNetworkConnectivity = true  // Since we're making API calls
        request.requiresExternalPower = false       // Allow on battery
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60) // 1 minute from now
        
        do {
            try BGTaskScheduler.shared.submit(request)
            print("Successfully scheduled background task")
        } catch {
            print("Could not schedule background task: \(error.localizedDescription)")
        }
    }
}
